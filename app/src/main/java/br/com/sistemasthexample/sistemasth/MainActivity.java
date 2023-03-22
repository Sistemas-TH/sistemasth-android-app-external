package br.com.sistemasthexample.sistemasth;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.sistemasthexample.R;
import br.com.sistemasthexample.sistemasth.models.Config;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadConfigs();




        if (!allRuntimePermissionsGranted()) {
            getRuntimePermissions();
        }


    }

    private void loadConfigs() {

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        StringRequest send = new StringRequest(
                Request.Method.GET,
                "https://liveness.sistemasth.com.br/api/config",
                response -> {
                    // Handle the response
                    try {
                        JSONObject r = new JSONObject(response);
                        Log.d("api/config", response);
                        Config.nosePointSize = (float) r.getDouble("nosePointSize");
                        Config.nosePointRadius = (float) r.getDouble("nosePointRadius");
                        Config.nosePointColor = r.getString("nosePointHexColor");
                        Config.boxColor = r.getString("boxHexColor");
                        Config.urlApi = r.getString("urlApi");
                        Config.cnhProbabilityPercent = (float) r.getDouble("cnhProbabilityPercent");
                        Log.d("api/config", Config.urlApi);
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Erro ao algumas configurações personalizadas.", Toast.LENGTH_SHORT).show();
                        Log.e("api/config", e.getMessage());
                        e.printStackTrace();
                    } finally {
                        setContentView(R.layout.activity_main);
                    }


                },
                error -> {
                    // Handle errors
                    Toast.makeText(getApplicationContext(), "Erro ao carregar as configurações personalizadas. Iremos utilizar as configurações padrão.", Toast.LENGTH_SHORT).show();
                    Log.e("api/config", error.toString());
                    setContentView(R.layout.activity_main);
                });

        requestQueue.add(send);


    }

    private boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }


    private boolean allRuntimePermissionsGranted() {
        for (String permission : Companion.REQUIRED_RUNTIME_PERMISSIONS) {
            if (permission != null && !isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : Companion.REQUIRED_RUNTIME_PERMISSIONS) {
            if (permission != null && !isPermissionGranted(this, permission)) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissionsArray, Companion.PERMISSION_REQUESTS);
        }
    }

    public static final class Companion {
        private static final String TAG = "EntryChoiceActivity";
        private static final int PERMISSION_REQUESTS = 1;
        private static final String[] REQUIRED_RUNTIME_PERMISSIONS = {
                Manifest.permission.CAMERA,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        private Companion() {
        }
    }


}