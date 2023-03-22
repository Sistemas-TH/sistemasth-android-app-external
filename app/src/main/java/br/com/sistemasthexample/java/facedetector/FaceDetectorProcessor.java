/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.sistemasthexample.java.facedetector;

import static java.lang.String.format;
import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;

import br.com.sistemasthexample.BitmapUtils;
import br.com.sistemasthexample.GraphicOverlay;
import br.com.sistemasthexample.R;
import br.com.sistemasthexample.java.VisionProcessorBase;
import br.com.sistemasthexample.preference.PreferenceUtils;
import br.com.sistemasthexample.sistemasth.models.Config;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Face Detector Demo.
 */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {

    private static final String TAG = "FaceDetectorProcessor";
    private static final String TAG1 = "ResettingValues";
    private final FaceDetector detector;
    private final Context _context;
    protected int xNose = 0;
    protected int yNose = 0;
    private int frameCounter = 0;
    private int frameModCounter = 1;
    private boolean captureFinalized = false;
    private boolean endVerifyHit = false;
    private String requestId = "";
    private String token = "";
    private boolean success = false;
    private final String baseUrl;
    RequestQueue queueFrames;
    Integer pendingRequests = 0;
    protected float heightBox = 0;
    protected float widthBox = 0;
    protected float xBox = 0;
    protected float yBox = 0;
    protected int startVerifyDone = 0; // 0 = not started, 1 = started, 2 = done
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected Activity _activity;
    protected AppCompatActivity _compactActivity;
    protected ViewGroup cameraView;
    protected TextView resultView;
    protected TextView instructionsView;
    protected String _base64;

    public FaceDetectorProcessor(Context context) {
        super(context);
//        baseUrl = context.getString(R.string.url_server);
        baseUrl = Config.urlApi;

        _context = context;
        queueFrames = Volley.newRequestQueue(context);
        FaceDetectorOptions faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(context);
        Log.v(MANUAL_TESTING_LOG, "Face detector options: " + faceDetectorOptions);
        detector = FaceDetection.getClient(faceDetectorOptions);


        _activity = ((Activity) _context);
        _compactActivity = (AppCompatActivity) _context;

        cameraView = _compactActivity.findViewById(R.id.constraintLayout_CameraX);
        resultView = _compactActivity.findViewById(R.id.fullscreen_content);
        instructionsView = _compactActivity.findViewById(R.id.textView_instructions);
        setTextInstructions("Aguarde...");
        FaceGraphic.isNoseDetected = false;


    }

    private void setTextInstructions(String text) {
        _compactActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                instructionsView.setText(text);
            }
        });
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void processImageProxy(ImageProxy imageProxy, GraphicOverlay graphicOverlay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.processImageProxy(imageProxy, graphicOverlay); // isso mostra a imagem na tela

            imageHeight = imageProxy.getHeight();
            imageWidth = imageProxy.getWidth();
//            Log.d(TAG1, "startVerifyDone: " + startVerifyDone);
            startVerify();
//            Log.d(TAG1, "startVerifyDone: " + startVerifyDone);
            if (startVerifyDone != 2) return;
//            Log.d(TAG1, "startVerifyDone: " + startVerifyDone);

            if (!captureFinalized) {
                if (Objects.equals(token, "")) {
                    return;
                }
                int frameMaxCaptures = 30 * frameModCounter;
                if (frameCounter >= frameMaxCaptures || FaceGraphic.isNoseDetected) {
                    cameraView.setVisibility(ViewGroup.GONE);
                    Log.d(TAG, "captureFinalized: " + (captureFinalized ? "true" : "false"));
                    captureFinalized = true;
                } else {
                    if (frameCounter % frameModCounter == 0) {
                        Log.d(TAG, "vai capturar um frame: " + frameCounter);
                        Bitmap b = BitmapUtils.getBitmap(imageProxy);
                        String base64 = getBase64String(b);


                        pendingRequests++;
                        captureFrame(base64);
                    }
                    frameCounter++;
                }
            } else if (!endVerifyHit && pendingRequests == 0) {
                endVerify();

            }
        }

    }

    private void EndActivity(String base64, Boolean status) {
        Intent intent = new Intent();
        intent.putExtra("success", status);
        intent.putExtra("imageFace", base64);
        ((Activity) _context).setResult(Activity.RESULT_OK, intent);
        ((Activity) _context).finish();
    }

    private void endVerify() {
        endVerifyHit = true;
        // Instantiate the RequestQueue.
        String url = format(format("%sus/challenge/%%s/verify", baseUrl), requestId);
        JSONObject body = new JSONObject();


        setTextInstructions(_context.getResources().getString(R.string.ending));
        // Perform processing here


        try {
            body.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String endVerify = "endVerify";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, body, response -> {
            try {
                success = response.getBoolean("success");
                resultView.setText((success ? "Verificação foi finalizada com sucesso" : "Houve uma falha, por gentileza, tente novamente."));

                Log.d(TAG, response.toString());

                // Wait 1.5 seconds
                new Handler().postDelayed(() -> {
                    EndActivity(_base64, success);
                }, 1500);

            } catch (JSONException e) {
                e.printStackTrace();
                EndActivity(_base64, false);
                Log.d(TAG, endVerify + "ERROJSON");
                Log.d(TAG, endVerify + e.getMessage());
            }
        }, error -> {
            Log.d(TAG, endVerify + "ERROREQUEST");
            Log.d(TAG, endVerify + url);
            Log.d(TAG, error.toString());
            EndActivity(_base64, false);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("model", Build.MODEL);
                headers.put("OSVersion", Build.VERSION.RELEASE);
                headers.put("ID", Settings.Secure.ANDROID_ID);

                return headers;
            }
        };

        // Add the request to the Volley request queue
        queueFrames.add(jsonObjectRequest);
    }

    private void startVerify() {

        if (startVerifyDone != 0) return;
        if (imageHeight == 0 || imageWidth == 0) return;
        setTextInstructions(_context.getResources().getString(R.string.wait_for_the_white_nose_point));

        startVerifyDone = 1;

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(_context);
        String url = baseUrl + "us/challenge/start";
        JSONObject body = new JSONObject();
        try {
            body.put("userId", "9286228c-5b45-4b73-9284-c44d877f0a81");
            body.put("imageWidth", imageWidth);
            body.put("imageHeight", imageHeight);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, body, response -> {

            try {

                Log.d(TAG1, "startVerify: " + response.toString());
                requestId = response.getString("id");
                String userId = response.getString("userId");
                token = response.getString("token");
                xNose = response.getInt("noseLeft");
                yNose = response.getInt("noseTop");

                heightBox = response.getInt("areaHeight");
                widthBox = response.getInt("areaWidth");
                xBox = response.getInt("areaLeft");
                yBox = response.getInt("areaTop");

                startVerifyDone = 2;
                setTextInstructions(_context.getResources().getString(R.string.wait_for_the_white_nose_point));

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "StartVerify: JSONException" + e.getMessage());
            }
        }, error -> {
            Log.d(TAG, "StartVerify: VolleyError" + url);
            Log.d(TAG, error.toString());
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("model", Build.MODEL);
                headers.put("OSVersion", Build.VERSION.RELEASE);
                headers.put("ID", Settings.Secure.ANDROID_ID);

                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void captureFrame(String base64) {
        // Instantiate the RequestQueue.

        setTextInstructions(_context.getResources().getString(R.string.position_your_nose_on_the_point));
        String url = format(String.format("%sus/challenge/%%s/frames", baseUrl), requestId);
        JSONObject body = new JSONObject();

        try {
            body.put("timestamp", new java.util.Date().getTime());
            body.put("frameBase64", base64);
            _base64 = base64;

//            EndActivity(_base64, true);
            body.put(_context.getString(R.string.tokenString), token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, body, response -> {
            try {
                pendingRequests--;
                String message = response.getString("message");
                Log.d(TAG, "Capturando Frame: " + message);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.d(TAG, "sendFrame: " + error.toString());
            Log.d(TAG, "sendFrame: " + error.getMessage());
//            Log.d(TAG, "sendFrame: " + error.networkResponse.statusCode);

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("model", Build.MODEL);
                headers.put("OSVersion", Build.VERSION.RELEASE);
                headers.put("ID", Settings.Secure.ANDROID_ID);

                return headers;
            }
        };

        queueFrames.add(jsonObjectRequest);
    }

    @Override
    public void stop() {
        super.stop();
        detector.close();
    }

    @Override
    protected Task<List<Face>> detectInImage(InputImage image) {
        return detector.process(image);
    }

    @Override
    protected void onSuccess(@NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay) {
        for (Face face : faces) {
            if (faces.size() == 1) {
                FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay, face, this);
                graphicOverlay.add(faceGraphic);


            }
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }

    private String getBase64String(Bitmap bitmap) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArray);
        byte[] imageBytes = byteArray.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);
    }
}
