package br.com.sistemasthexample.sistemasth.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Base64Utils;

import br.com.sistemasthexample.BuildConfig;
import br.com.sistemasthexample.EntryChoiceActivity;
import br.com.sistemasthexample.R;
import br.com.sistemasthexample.java.CameraXLivePreviewActivity;
import br.com.sistemasthexample.sistemasth.Constants;
import br.com.sistemasthexample.sistemasth.FaceDetectionResult;
import br.com.sistemasthexample.sistemasth.Helpers;
import br.com.sistemasthexample.sistemasth.Storage;
import br.com.sistemasthexample.sistemasth.adapters.InstructionsAdapter;
import br.com.sistemasthexample.sistemasth.adapters.InstructionsVerticalSpaceDecoration;
import br.com.sistemasthexample.sistemasth.models.ResponseBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import br.com.nxcd.facedetection.NxcdFaceDetection;

public class LivenessInstructionsFragment extends BaseFragment {
    private Uri photoUri;
    private static final String TAG = "FaceDetection";
    private final int DETECTION_REQUEST_CODE = 1;
    private final int CAMERA_REQUEST_CODE = 2;

    private int SELFIE_ID = 123;

    public LivenessInstructionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instructions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView) view.findViewById(R.id.instructions_text_view)).setText(R.string.instructions_face);

        view.findViewById(R.id.auto_capture).setOnClickListener(button -> {
            startFaceDetection();
        });
        view.findViewById(R.id.manual_capture).setOnClickListener(button -> {
            startFaceCaptureManual();
        });
        RecyclerView instructionsRecyclerView = view.findViewById(R.id.instructions_recycler_view);
        instructionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        InstructionsVerticalSpaceDecoration instructionsVerticalSpaceDecoration = new InstructionsVerticalSpaceDecoration((int) getResources().getDimension(R.dimen.margin_medium));
        instructionsRecyclerView.addItemDecoration(instructionsVerticalSpaceDecoration);

        List<String> instructions = Arrays.asList(getResources().getString(R.string.position_your_face_centered_in_the_enclosed_area),
                getResources().getString(R.string.keep_a_neutral_expression_app_instruction),
                getResources().getString(R.string.keep_your_eyes_open_app_instruction),
                getResources().getString(R.string.look_for_a_well_lit_place),
                getResources().getString(R.string.if_possible_be_in_a_white_background),

                getResources().getString(R.string.remove_any_type_of_accessory),
                getResources().getString(R.string.position_your_nose_on_the_point)
        );


        InstructionsAdapter instructionsAdapter = new InstructionsAdapter(getContext(), instructions);
        instructionsRecyclerView.setAdapter(instructionsAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == Activity.RESULT_OK) {
            treatResultMLKIT(resultCode, data);
        } else if (requestCode == DETECTION_REQUEST_CODE) {
            treatResultSDK(resultCode, data);
        }
        if (requestCode == CAMERA_REQUEST_CODE) {

            treatResultManual(resultCode, data);
        }
    }

    private void treatResultManual(final int resultCode, final Intent resultIntent) {


        if (Activity.RESULT_CANCELED == resultCode) {
//            Log.d(TAG, "Face capture canceled");

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


        ImageView img = requireView().findViewById(R.id.imageView2);
        img.setImageURI(photoUri);
        if (photoUri != null) {


            Storage.storage.put(Constants.PV_API_RESULT_STRING, "");
            Storage.storage.put(Constants.PV_API_RESULT_OBJECT, "");
            final Bitmap imageResult = Helpers.getBitMapFromUri(photoUri, getActivity().getContentResolver());
            Storage.storage.put(Constants.PV_IMAGE, imageResult);


            Continue(true);

        }


    }

    private void Continue(Boolean result) {
        // Save response from SDK


        if (result) {

            if (Storage.requested_OC() || Storage.requested_FM() || Storage.requested_DV()) {
                // TODO: Call documentInstructions
                final NavDirections action = LivenessInstructionsFragmentDirections.actionInstructionsFragmentToDocumentInstructionsFragment();
                Navigation.findNavController(requireView()).navigate(action);
                return;
            }
            // TODO: Call apis TH and redirect to finalResult view

            final NavDirections action = (NavDirections) LivenessInstructionsFragmentDirections.actionInstructionsLivenessFragmentToFinalResultFragment(new ResponseBody());
            Navigation.findNavController(requireView()).navigate(action);
            return;


        } else {
            final NavDirections actionNoSuccess = (NavDirections) LivenessInstructionsFragmentDirections.actionToFaceDetectionResultFragment(FaceDetectionResult.FAILURE);
            Navigation.findNavController(requireView()).navigate(actionNoSuccess);
        }
    }

    private void treatResultMLKIT(final int resultCode, final Intent resultIntent) {

        if (Activity.RESULT_CANCELED == resultCode) {
            Toast.makeText(requireContext(), "A captura da face foi cancelada.", Toast.LENGTH_LONG).show();
        } else {
            Boolean result = resultIntent.getBooleanExtra("success", false);
            String base64 = resultIntent.getStringExtra("imageFace");

            //base64 string to bitmap
            if (result) {
                byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Storage.storage.put(Constants.PV_IMAGE, decodedByte);
            }

                Continue(result);
            }
        }

        private void treatResultSDK ( final int resultCode, final Intent resultIntent){
            if (Activity.RESULT_CANCELED == resultCode) {
                Log.d(TAG, "Face detection canceled");

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

            if (resultIntent != null && resultIntent.hasExtra(NxcdFaceDetection.RESULT)) {


                final HashMap<String, Object> resultDataFromAPI = (HashMap<String, Object>) resultIntent.getExtras().getSerializable(NxcdFaceDetection.RESULT);
                final String resultDataFromApiJson = Storage.transformObjectToString(resultDataFromAPI);

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Result from API: " + resultDataFromApiJson);
                }


                try {
                    Storage.storage.put(Constants.PV_API_RESULT_STRING, resultDataFromApiJson);
                    Storage.storage.put(Constants.PV_API_RESULT_OBJECT, resultDataFromAPI);
                    final Bitmap imageResult = BitmapFactory.decodeStream(requireContext().openFileInput(NxcdFaceDetection.IMAGE_RESULT));
                    Storage.storage.put(Constants.PV_IMAGE, imageResult);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Error when trying open result image.", e);
                }

                boolean result = (boolean) ((HashMap<String, Object>) resultDataFromAPI.get("data")).get("isAlive");

                Continue(result);

            }
        }

        protected void startFaceDetection () {

            Intent cameraActivity = new Intent(getActivity(), CameraXLivePreviewActivity.class);

            cameraActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivityForResult(cameraActivity, Activity.RESULT_OK);


        }

        protected void startFaceCaptureManual () {
            showCamera();
        }


        protected void showCamera () {

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
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(photoFile)
                            photoUri = FileProvider.getUriForFile(Objects.requireNonNull(requireActivity().getApplicationContext()),
                                    BuildConfig.APPLICATION_ID + ".provider", photoFile)

                    );

//                photoUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getPackageName()+".fileprovider", photoFile);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                }
            }
        }

        private File createImageFile () throws IOException {
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


            // Save a file: path for use with ACTION_VIEW intents
            String mCurrentPhotoPath = "file:" + image.getAbsolutePath();
            return image;
        }


    }