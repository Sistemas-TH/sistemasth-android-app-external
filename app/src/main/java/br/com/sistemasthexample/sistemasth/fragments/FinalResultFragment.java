package br.com.sistemasthexample.sistemasth.fragments;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import br.com.sistemasthexample.R;
import br.com.sistemasthexample.sistemasth.Constants;
import br.com.sistemasthexample.sistemasth.Credentials;
import br.com.sistemasthexample.sistemasth.Storage;
import br.com.sistemasthexample.sistemasth.models.CheckBodyRequest;
import br.com.sistemasthexample.sistemasth.models.OCRData;
import br.com.sistemasthexample.sistemasth.models.ResponseBody;
import br.com.sistemasthexample.sistemasth.models.THSDKFaceMatchModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class FinalResultFragment extends BaseFragment {


    private static final String TAG = "FinalView";
    private ResponseBody<List<OCRData>> dataResponseBody;
    private TextView result;

    public FinalResultFragment() {
        // Required empty public constructor
    }

    private DocumentInstructionsFragment.ServiceLiveness serviceLiveness;
    private String base64Face = "";
    private String base64DocFront = "";
    private String base64DocBack = "";

    private View loading;
    private View content;
    private Retrofit retroTH;
    private Retrofit retroTHLiveness;
    private Service serviceTH;
    private Service serviceTHLiveness;
    private Integer FM = 0;
    private Integer PV = 0;
    private Integer OC = 0;
    private Integer DC = 0;
    private Integer DV = 0;
    private Integer maxCalls = 0;
    private Integer calls = 0;


    private View liveness_label_result;
    private View liveness_result_text;
    private ImageView selfie_image_view;
    private ImageView doc_image_view;
    private View facematch_label_result;
    private View facematch_scrollView;
    private View ocr_label_result;
    private View ocr_scrollview;


    private Button result_button;

    private TextView ocr_result_json;
    private TextView facematch_result_json;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        retroTH = provideRetrofit(getCredentials());
        serviceTH = retroTH.create(Service.class);

        retroTHLiveness = provideRetrofitLiveness();
        serviceLiveness = retroTHLiveness.create(DocumentInstructionsFragment.ServiceLiveness.class);

        View v = inflater.inflate(R.layout.fragment_final_result, container, false);


        SpinningLoading(v);

        return v;
    }

    private void fakeValues() {
        Storage.functions.put(Constants.FM, 1);
        Storage.functions.put(Constants.PV, 1);
        Storage.functions.put(Constants.OC, 1);
        Storage.functions.put(Constants.DC, 0);
        Storage.functions.put(Constants.DV, 0);

        Storage.storage.put(Constants.CPF, "");
        Storage.storage.put(Constants.PV_API_RESULT_STRING, "");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //fakeValues();
        facematch_scrollView = view.findViewById(R.id.facematch_scrollView);
        liveness_label_result = view.findViewById(R.id.liveness_label_result);
        liveness_result_text = view.findViewById(R.id.liveness_result_text);
        selfie_image_view = view.findViewById(R.id.selfie_image_view);
        facematch_label_result = view.findViewById(R.id.facematch_label_result);
        doc_image_view = view.findViewById(R.id.doc_image_view);
        ocr_label_result = view.findViewById(R.id.ocr_label_result);
        ocr_scrollview = view.findViewById(R.id.ocr_scrollview);
        ocr_result_json = view.findViewById(R.id.ocr_result_json);
        result_button = view.findViewById(R.id.result_button);
        facematch_result_json = view.findViewById(R.id.facematch_result_json);

        selfie_image_view.setVisibility(View.GONE);
        liveness_label_result.setVisibility(View.GONE);
        liveness_result_text.setVisibility(View.GONE);
        facematch_scrollView.setVisibility(View.GONE);
        facematch_label_result.setVisibility(View.GONE);
        doc_image_view.setVisibility(View.GONE);
        ocr_label_result.setVisibility(View.GONE);
        ocr_scrollview.setVisibility(View.GONE);

        result_button.setOnClickListener(v -> {

            Storage.functions.put(Constants.FM, 0);
            Storage.functions.put(Constants.PV, 0);
            Storage.functions.put(Constants.OC, 0);
            Storage.functions.put(Constants.DC, 0);
            Storage.functions.put(Constants.DV, 0);
            Storage.storage.put(Constants.CPF, "");
            Storage.storage.put(Constants.PV_API_RESULT_STRING, "");
            final NavDirections actionC = FinalResultFragmentDirections.actionFinalResultFragmentToWelcomeFragment();
            Navigation.findNavController(requireView()).navigate(actionC);
        });
        try {

            loading = view.findViewById(R.id.fullscreen_content_final_result_loading);
            content = view.findViewById(R.id.fullscreen_content_final_result_content);

//            loading.setVisibility(View.GONE);
//            content.setVisibility(View.VISIBLE);
//
        loading.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);



            FM = Storage.coalesceInteger(Storage.functions.get(Constants.FM), 0);
            PV = Storage.coalesceInteger(Storage.functions.get(Constants.PV), 0);
            OC = Storage.coalesceInteger(Storage.functions.get(Constants.OC), 0);
            DC = Storage.coalesceInteger(Storage.functions.get(Constants.DC), 0);
            DV = Storage.coalesceInteger(Storage.functions.get(Constants.DV), 0);


            if (OC == 1) {
                doc_image_view.setVisibility(View.VISIBLE);
                ocr_label_result.setVisibility(View.VISIBLE);
                ocr_scrollview.setVisibility(View.VISIBLE);
            }
            if (PV == 1) {
                selfie_image_view.setVisibility(View.VISIBLE);
                liveness_label_result.setVisibility(View.VISIBLE);
                liveness_result_text.setVisibility(View.VISIBLE);
            }
            if (FM == 1) {
                facematch_scrollView.setVisibility(View.VISIBLE);
                selfie_image_view.setVisibility(View.VISIBLE);
                facematch_label_result.setVisibility(View.VISIBLE);
                doc_image_view.setVisibility(View.VISIBLE);

            }

            String CPF = Storage.coalesceStorage(Storage.storage.get(Constants.CPF), "");
            String API_RESULT = Storage.coalesceStorage(Storage.storage.get(Constants.PV_API_RESULT_STRING), "");


            this.dataResponseBody = FinalResultFragmentArgs.fromBundle(getArguments()).getResult();


            Storage.storage.get(Constants.DOC_IMAGE);

            Object imageLivenessObject = Storage.storage.get(Constants.PV_IMAGE);
            Object imageDocObject = Storage.storage.get(Constants.DOC_IMAGE);

            if (imageLivenessObject != null) {
                Bitmap imageLiveness = (Bitmap) imageLivenessObject;
                selfie_image_view.setImageBitmap(imageLiveness);
                base64Face = getBase64String(imageLiveness);

            }
            if (imageDocObject != null) {
                Bitmap imageDoc = (Bitmap) imageDocObject;
                doc_image_view.setImageBitmap(imageDoc);
                base64DocFront = getBase64String(imageDoc);
            }

            Object json_result = Storage.storage.get(Constants.PV_API_RESULT_OBJECT);

            maxCalls = FM + OC;
            Log.d("final", "maxCalls:" + maxCalls);

            CallFaceMatchApiAndOCR();


            String result_full = "";


            if (json_result != null) {

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

                String originalJson = json_result.toString();
                JsonNode tree = null;
                try {
                    tree = objectMapper.readTree(originalJson);
                    String formattedJson = objectMapper.writeValueAsString(tree);
                    result_full = result_full + "\nJSON:" + formattedJson;
                } catch (Throwable e) {
                    e.printStackTrace();
                }


                facematch_result_json.setText(json_result.toString());
            } else {
                facematch_result_json.setText(result_full);
            }

        } catch (Exception ex) {

        }


    }

    private Credentials getCredentials() {

        return new Credentials("ADADA", "ADADA", "ADADADA"); 
    }


    private String getJsonFromObject(Object param) {
        String jsonSerialized;
        try {

            ObjectMapper mapper = new ObjectMapper();
            jsonSerialized = mapper.writeValueAsString(param);
        } catch (Throwable e) {
            jsonSerialized = e.toString();
        }
        return jsonSerialized;
    }

    private void CallFaceMatchApiAndOCR() {

        if (FM != 0) FacematchAPI();
        if (OC != 0) OcrAPI();
        if (FM + OC == 0) {
            loading.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
        }
    }

    private void OcrAPI() {
        Bitmap image = (Bitmap) Storage.storage.get(Constants.DOC_IMAGE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        RequestBody body2 = RequestBody.create(MediaType.parse("application/octet-stream"), byteArray);
        serviceLiveness.GetOCR(body2).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String ocrResponse = response.body();
                    try {
                        JSONObject json = new JSONObject(ocrResponse);


                        String nome = json.getString("nome");
                        String rg = json.getString("rg");
                        String orgEmissor = json.getString("orgEmissor");
                        String uf = json.getString("uf");
                        String cpf = json.getString("cpf");
                        String dataNascimento = json.getString("dataNascimento");
                        String pai = json.getString("pai");
                        String mae = json.getString("mae");
                        String numeroRegistro = json.getString("numeroRegistro");
                        String validade = json.getString("validade");
                        String primeiraHabilitacao = json.getString("primeiraHabilitacao");
                        String localNascimento = json.getString("localNascimento");
                        String dataEmissao = json.getString("dataEmissao");
                        String categoria = json.getString("categoria");
                        String nacionalidade = json.getString("nacionalidade");


                        String textResult = "Nome: " + "\r\n" + nome + "\r\n\r\n" +
                                "Rg: " + "\r\n" + rg + "\r\n\r\n" +
                                "Orgão Emissor: " + "\r\n" + orgEmissor + "\r\n\r\n" +
                                "UF: " + "\r\n" + uf + "\r\n\r\n" +
                                "CPF: " + "\r\n" + cpf + "\r\n\r\n" +
                                "Data de Nascimento: " + "\r\n" + dataNascimento + "\r\n\r\n" +
                                "Nome do Pai: " + "\r\n" + pai + "\r\n\r\n" +
                                "Nome da Mãe: " + "\r\n" + mae + "\r\n\r\n" +
                                "Número de Registro: " + "\r\n" + numeroRegistro + "\r\n\r\n" +
                                "Validade: " + "\r\n" + validade + "\r\n\r\n" +
                                "Primeira Habilitação: " + "\r\n" + primeiraHabilitacao + "\r\n\r\n" +
                                "Local de Nascimento: " + "\r\n" + localNascimento + "\r\n\r\n" +
                                "Data de Emissão: " + "\r\n" + dataEmissao + "\r\n\r\n" +
                                "Categoria: " + "\r\n" + categoria + "\r\n\r\n" +
                                "Nacionalidade: " + "\r\n" + nacionalidade;




                        ocr_result_json.setText(textResult);
                    } catch (Throwable e) {
                        Log.e(TAG, "ERROFDP");
                        Log.e(TAG, e.getMessage());
                        ocr_result_json.setText(ocrResponse);
                    }


                    Log.d(TAG, "OCR Response sucesso: " + ocrResponse.toString());
                }
                calls++;
                if (calls.equals(maxCalls)) {
                    loading.setVisibility(View.GONE);
                    content.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("TAG", "OCR Response erro2: " + t.getMessage());
                calls++;
                if (calls.equals(maxCalls)) {
                    loading.setVisibility(View.GONE);
                    content.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void FacematchAPI() {
        String cpf = Storage.storage.get(Constants.CPF).toString();
        final CheckBodyRequest body = new CheckBodyRequest();
        body.setsFrente(base64DocFront);
        body.setsVerso(base64DocBack);
        body.setsIdBiometria("SDK");
        body.setsValidarCPF(false);
        body.setsSelfie(base64Face);

        String jsonSerialized = getJsonFromObject(body);
        serviceTH.postCheck(cpf, jsonSerialized).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                final String message = "Response Success from API TH (Check): ";
                String result = response.body();

                try {

                    JSONArray json = new JSONArray(result);
                    JSONObject obj = json.getJSONObject(0);
                    JSONObject bio = obj.getJSONObject("BIO");
                    String similarity = bio.getString("Similaridade");
                    String biometry = bio.getString("Biometria");

                    if (biometry.equals(similarity)) {
                        facematch_result_json.setText("Similaridade (%): " + similarity );
                    }
                    else {
                        facematch_result_json.setText("Similaridade: " + similarity + "\r\n\r\nBiometria: " + biometry);
                    }

                } catch (Throwable e) {
                    Log.e(TAG, "ERROFDP");
                    Log.e(TAG, e.getMessage());

                    facematch_result_json.setText(result);
                }


                Log.d(TAG, "Biometria: " + result);
                try {
                    Log.d(TAG, "result: " + result);
                } catch (Throwable e) {
                    Log.d(TAG, "Biometria: " + e);
                }
                calls++;
                if (calls.equals(maxCalls)) {
                    loading.setVisibility(View.GONE);
                    content.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
//                progress.dismiss();
                final String message = "Response Error from API TH (Check): ";
                result.setText(t.toString());
                Log.e(TAG, message + t.toString());

                calls++;
                if (calls.equals(maxCalls)) {
                    loading.setVisibility(View.GONE);
                    content.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private Retrofit provideRetrofit(Credentials credentials) {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .callTimeout(3, TimeUnit.MINUTES);


        httpClient.addInterceptor(chain -> {
            final Request request = chain.request()
                    .newBuilder()
                    .addHeader("model", Build.MODEL)
                    .addHeader("OSVersion", Build.VERSION.RELEASE)
                    .addHeader("ID", Settings.Secure.ANDROID_ID)
                    .addHeader("Content-type", "application/json")
                    .addHeader("Authorization", "Basic " + credentials.getBase64()).build();


            return chain.proceed(request);
        });

        return new Retrofit.Builder()
                .baseUrl("https://api6.sistemasth.com.br/")
//                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient.build())
                .build();


    }


    private String getBase64String(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] imageBytes = baos.toByteArray();

        String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        return base64String;
    }

    private void SpinningLoading(View view) {
        ImageView loadingImageView = view.findViewById(R.id.fullscreen_content_loading);
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

    private interface Service {
        @Headers("Content-Type:application/json; charset=UTF-8")
        @POST("api/CHECK")
        Call<String> postCheck(@Query("pCPF") String cpf, @Body String bodyRequest);


    }

}
