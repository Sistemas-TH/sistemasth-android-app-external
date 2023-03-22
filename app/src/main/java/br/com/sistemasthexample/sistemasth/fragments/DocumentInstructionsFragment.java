package br.com.sistemasthexample.sistemasth.fragments;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import br.com.sistemasthexample.BuildConfig;
import br.com.sistemasthexample.R;
import br.com.sistemasthexample.sistemasth.Constants;
import br.com.sistemasthexample.sistemasth.Credentials;
import br.com.sistemasthexample.sistemasth.Helpers;
import br.com.sistemasthexample.sistemasth.Storage;
import br.com.sistemasthexample.sistemasth.adapters.InstructionsAdapter;
import br.com.sistemasthexample.sistemasth.adapters.InstructionsVerticalSpaceDecoration;
import br.com.sistemasthexample.sistemasth.models.OCRData;
import br.com.sistemasthexample.sistemasth.models.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import br.com.nxcd.facedetection.NxcdFaceDetection;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class DocumentInstructionsFragment extends BaseFragment {
    private Uri photoUri;
    private static final String TAG = "DocumentInstructions";
    private static final int REQUEST_CODE = 1000;
    private final int CAMERA_REQUEST_CODE = 2;
    private final List<Bitmap> images = new ArrayList<>();
    private Boolean isCNH = false;
    private Retrofit retroTH;
    private ServiceLiveness serviceLiveness;

    private  View instructions_loading;
    private  View instructions_texts;
    private TextView instructions_textview_loading;

    public DocumentInstructionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_instructions, container, false);

        SpinningLoading(v);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.instructions_text_view)).setText(R.string.instructions_doc);


        retroTH = provideRetrofitLiveness();
        serviceLiveness = retroTH.create(ServiceLiveness.class);
        instructions_texts = view.findViewById(R.id.instructions_texts);

        instructions_loading = view.findViewById(R.id.instructions_loading);
        instructions_textview_loading = view.findViewById(R.id.instructions_textview_loading);


        view.findViewById(R.id.auto_capture).setVisibility(View.GONE);
        view.findViewById(R.id.auto_capture).setOnClickListener(button -> startDocumentDetection());
        view.findViewById(R.id.manual_capture).setVisibility(View.VISIBLE);
        view.findViewById(R.id.manual_capture).setOnClickListener(button -> startDocCaptureManual());

        final RecyclerView instructionsRecyclerView = view.findViewById(R.id.instructions_recycler_view);
        instructionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        final InstructionsVerticalSpaceDecoration instructionsVerticalSpaceDecoration = new InstructionsVerticalSpaceDecoration((int) getResources().getDimension(R.dimen.margin_medium));
        instructionsRecyclerView.addItemDecoration(instructionsVerticalSpaceDecoration);

        final List<String> instructions = Arrays.asList(getResources().getString(R.string.place_the_document_without_a_cover_on_a_table),
                getResources().getString(R.string.look_for_a_well_lit_place),
//                getResources().getString(R.string.position_your_document_centered_in_the_enclosed_area),
                getResources().getString(R.string.keep_your_device_steady_and_avoid_tilting));

        final InstructionsAdapter instructionsAdapter = new InstructionsAdapter(requireContext(), instructions);
        instructionsRecyclerView.setAdapter(instructionsAdapter);


    }

    private void startDocumentDetection() {

        final NxcdFaceDetection nxcdFaceDetection = new NxcdFaceDetection(REQUEST_CODE, getResources().getString(R.string.homolog_token), R.style.SDKTheme_Ex);
        nxcdFaceDetection.setHomologation();
        nxcdFaceDetection.startDocumentDetection(this);
        treatResultSDK(-1, new Intent());
    }

    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> treatResultSDK(result.getResultCode(), result.getData()));

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode) {
            treatResultSDK(resultCode, data);
        }
        if (requestCode == CAMERA_REQUEST_CODE) {

            treatResultManual(resultCode, data);
        }
    }

    private void treatResultSDK(final int resultCode, final Intent resultIntent) {
        if (Activity.RESULT_CANCELED == resultCode) {
            Log.d(TAG, "Document detection canceled");

            if (resultIntent != null) {
                if (resultIntent.hasExtra(NxcdFaceDetection.RESULT)) {
                    final HashMap<String, Object> result = (HashMap<String, Object>) resultIntent.getSerializableExtra(NxcdFaceDetection.RESULT);
                    Log.d(TAG, "Classify image has failed: " + result.toString());
                    Toast.makeText(requireContext(), result.toString(), Toast.LENGTH_LONG).show();
                }

                if (resultIntent.hasExtra(NxcdFaceDetection.THROWABLE)) {
                    final Throwable throwable = (Throwable) resultIntent.getSerializableExtra(NxcdFaceDetection.THROWABLE);
                    Log.e(TAG, "Classify image has failed.", throwable);
                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            return;
        }
        if (resultIntent != null && resultIntent.hasExtra(NxcdFaceDetection.RESULT)) {
            final HashMap<String, Object> resultDataFromAPI = (HashMap<String, Object>) resultIntent.getExtras().getSerializable(NxcdFaceDetection.RESULT);
            final String resultDataFromApiJson = Storage.transformObjectToString(resultDataFromAPI);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Result from API: " + resultDataFromAPI.toString());
            }
            Storage.storage.put(Constants.CL, resultDataFromAPI);


            try {
                Storage.storage.put(Constants.DOC_API_RESULT_STRING, resultDataFromApiJson);
                Storage.storage.put(Constants.DOC_API_RESULT_OBJECT, resultDataFromAPI);
                final Bitmap imageResult = BitmapFactory.decodeStream(requireContext().openFileInput(NxcdFaceDetection.IMAGE_RESULT));
                Storage.storage.put(Constants.DOC_IMAGE, imageResult);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error when trying open result image.", e);
            }


            try {
                final Bitmap imageResult = BitmapFactory.decodeStream(requireContext().openFileInput(NxcdFaceDetection.IMAGE_RESULT));
                images.add(imageResult);
                Storage.storage.put(Constants.DOC_ARRAY_IMAGES, imageResult);
                askUserWantCaptureAnotherImage();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error when trying open result image.", e);
            }
        }

    }


    private void treatResultManual(final int resultCode, final Intent resultIntent) {


        if (Activity.RESULT_CANCELED == resultCode) {
            Log.d(TAG, "Doc capture canceled");

            if (resultIntent != null) {
                if (resultIntent.hasExtra(NxcdFaceDetection.RESULT)) {
                    final HashMap<String, Object> result = (HashMap<String, Object>) resultIntent.getSerializableExtra(NxcdFaceDetection.RESULT);
                    Log.d(TAG, "Analyze image has failed: " + result.toString());
                    Toast.makeText(requireContext(), result.toString(), Toast.LENGTH_LONG).show();
                }

                if (resultIntent.hasExtra(NxcdFaceDetection.THROWABLE)) {
                    final Throwable throwable = (Throwable) resultIntent.getSerializableExtra(NxcdFaceDetection.THROWABLE);
                    Log.e(TAG, "Analyze image has failed.", throwable);
                    Toast.makeText(requireContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            return;
        }


        if (photoUri != null) {


            Storage.storage.put(Constants.DOC_API_RESULT_STRING, "");
            Storage.storage.put(Constants.DOC_API_RESULT_OBJECT, "");
            final Bitmap imageResult = Helpers.getBitMapFromUri(photoUri, getActivity().getContentResolver());
            Storage.storage.put(Constants.DOC_IMAGE, imageResult);
            captureDone();


//            try {
//                final Bitmap imageResult = BitmapFactory.decodeStream(requireContext().openFileInput(NxcdFaceDetection.IMAGE_RESULT));
//                images.add(imageResult);
//                Storage.storage.put(Constants.DOC_ARRAY_IMAGES, imageResult);
//                askUserWantCaptureAnotherImage();
//            } catch (FileNotFoundException e) {
//                Log.e(TAG, "Error when trying open result image.", e);
//            }
        }


    }


    private void askUserWantCaptureAnotherImage() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.instructions)
                .setMessage(R.string.do_you_want_capture_another_document_image)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialog, which) -> startDocumentDetection())
                .setNegativeButton(R.string.no, (dialog, which) -> captureDone())
                .show();
    }


    private void captureDone() {
        classifyAndOCR();
    }

    private void classifyAndOCR() {
        Bitmap image = (Bitmap) Storage.storage.get(Constants.DOC_IMAGE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), byteArray);

        instructions_loading.setVisibility(View.VISIBLE);
        instructions_texts.setVisibility(View.GONE);
        instructions_textview_loading.setText("Estamos analisando a sua foto...");
        serviceLiveness.IsCNH(body).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                isCNH = false;
                if (response.isSuccessful()) {
                    String isCNHJson = response.body();


                    if (isCNHJson != null) {

                        Log.d(TAG, "OCR Response sucesso: " + isCNHJson.toString());

                        if (!isCNHJson.contains("true")) {
                            CNHNotIdentified();

                        } else {
                            isCNH = true;

                        }

                    }


                } else {
                    Log.d(TAG, "ISCNH Response erro1: " + response.errorBody());
                    Log.d(TAG, "ISCNH Response httpcode: " + response.code());
                    Log.d(TAG, "ISCNH Response erro1: " + response.message());
                    Log.d(TAG, "ISCNH Response erro1: " + response.body());
                    CNHNotIdentified();
                }
                finishFlow();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                CNHNotIdentified();
                Log.d(TAG, "ISCNH Response erro2: " + t.getMessage());
            }
        });


    }
    private void CNHNotIdentified() {
        instructions_loading.setVisibility(View.GONE);
        instructions_texts.setVisibility(View.VISIBLE);
        isCNH = false;
        Storage.storage.put(Constants.IS_CNH, true);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.instructions)
                .setMessage("Não identificamos uma CNH. Por favor, tente tirar a foto novamente.")
                .setCancelable(false)
                .setNeutralButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void finishFlow() {

        if (isCNH) {
            final NavDirections actionC = DocumentInstructionsFragmentDirections.actionDocumentInstructionsFragmentToDocumentDetectionResultFragment(new ResponseBody());
            Navigation.findNavController(requireView()).navigate(actionC);

        }

    }

    private void navigateToResult(final ResponseBody<List<OCRData>> responseBody) {
        final NavDirections action = DocumentInstructionsFragmentDirections
                .actionDocumentInstructionsFragmentToDocumentDetectionResultFragment(responseBody);
        Navigation.findNavController(requireView()).navigate(action);
    }

    private List<MultipartBody.Part> convertImageToRequest(@NonNull final List<Bitmap> images) {
        if (images.isEmpty()) return Collections.emptyList();
        final List<MultipartBody.Part> files = new ArrayList<>();
        final MediaType mediaType = MediaType.parse("image/jpeg");
        final String fileNameTemplate = "file_{count}";
        RequestBody body;
        int count = 1;
        String name;
        for (Bitmap image : images) {
            name = fileNameTemplate.replace("{count}", String.valueOf(count));
            body = RequestBody.create(mediaType, toJpeg(image));
            files.add(MultipartBody.Part.createFormData(name, name + ".jpeg", body));
            count++;
        }

        return files;
    }

    private byte[] toJpeg(final Bitmap bitmap) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        return out.toByteArray();
    }


    private Retrofit provideRetrofitTH(Credentials credentials) {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS);

        final String credentials_join = String.format("%s:%s:%s", credentials.Client, credentials.User, credentials.Password);
        String credentials64 = new String(android.util.Base64.encode(credentials_join.getBytes(), android.util.Base64.DEFAULT));
        credentials64 = credentials64.replace("\r", "");
        credentials64 = credentials64.replace("\n", "");
        final String credentials64_2 = credentials64;


        httpClient.addInterceptor(chain -> {
            final Request request = chain.request()
                    .newBuilder()
                    .addHeader("model", Build.MODEL)
                    .addHeader("OSVersion", Build.VERSION.RELEASE)
                    .addHeader("ID", Settings.Secure.ANDROID_ID)
                    .addHeader("Authorization", "Basic " + credentials64_2).build();

            return chain.proceed(request);
        });

        return new Retrofit.Builder()
                .baseUrl("https://api6.sistemasth.com.br/")
                //.addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient.build())
                .build();
    }

    private Retrofit provideRetrofitLiveness() {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.MINUTES)
                .callTimeout(30, TimeUnit.MINUTES);


        httpClient.addInterceptor(chain -> {
            final Request request = chain.request()
                    .newBuilder()
//                    .addHeader("Authorization", "Basic " + credentials64_2)
                    .addHeader("model", Build.MODEL)
                    .addHeader("OSVersion", Build.VERSION.RELEASE)
                    .addHeader("ID", Settings.Secure.ANDROID_ID)
                    .build();
            return chain.proceed(request);
        });

        return new Retrofit.Builder()

                .baseUrl(getContext().getString(R.string.url_server))
                //.addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient.build())
                .build();


    }

    protected void startDocCaptureManual() {
        showCamera();
    }


    protected void showCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("Error creating image fi", "CAM");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri = FileProvider.getUriForFile(Objects.requireNonNull(requireActivity().getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", photoFile));
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            } else {
                Log.d("Error creating image fi", "CAM");
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,                       /* prefix */
                ".jpg",             /* suffix */
                storageDir      /* directory */
        );

//        SistemasTHSDK.ProvaDeVida();


        // Save a file: path for use with ACTION_VIEW intents
        String mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    private interface Service {
        @Multipart
        @POST("full-ocr/v3")
        Call<ResponseBody<List<OCRData>>> ocr(@Part List<MultipartBody.Part> files/*, @Query("federalRevenueNumber") String cpf*/);


        @GET("api/BuscaCPF")
        Call<String> getCPF(@Query("sCpf") String cpf, @Query("sModulos") String modulos);


        @POST("api/ITH_BiodocumentoscopiaSDK")
        Call<String> getBio(@Query("sCpf") String cpf, @Query("sModulos") String modulos, @Body String body);


    }

    public interface ServiceLiveness {
        @POST("classify-document")
        Call<String> IsCNH(@Body RequestBody file/*, @Query("federalRevenueNumber") String cpf*/);

        @POST("get-cnh-ocr")
        Call<String> GetOCR(@Body RequestBody file/*, @Query("federalRevenueNumber") String cpf*/);


    }

    private void SpinningLoading(View view) {
        ImageView loadingImageView = view.findViewById(R.id.instructions_content_loading);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                loadingImageView.setRotation(value);
            }
        });

        animator.start();
    }

}
