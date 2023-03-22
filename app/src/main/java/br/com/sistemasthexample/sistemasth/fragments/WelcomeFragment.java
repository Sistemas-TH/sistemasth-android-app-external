package br.com.sistemasthexample.sistemasth.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import br.com.sistemasthexample.R;
import br.com.sistemasthexample.sistemasth.Constants;
import br.com.sistemasthexample.sistemasth.Storage;
import br.com.sistemasthexample.sistemasth.models.CheckBodyRequest;
import br.com.sistemasthexample.sistemasth.models.Config;
import br.com.sistemasthexample.sistemasth.models.ResponseBody;
import br.com.sistemasthexample.sistemasth.models.THSDKFaceMatchModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class WelcomeFragment extends BaseFragment {


    public static boolean ApiTHAlreadyDone() {
        return Storage.storage.get(Constants.API_RESULT_TH) != null;
    }


    private Button startButton;
    private Button pvButton;
    private Button dcButton;
    private Button fmButton;
    private Button ocButton;


    private List<Constants> arrayList;

    public WelcomeFragment() {
        // Required empty public constructor
        int i = 0;
        arrayList = new ArrayList<Constants>();
        arrayList.add(Constants.PV);
        arrayList.add(Constants.DC);
        arrayList.add(Constants.FM);
        arrayList.add(Constants.OC);
        arrayList.add(Constants.DV);


        for (Constants function : arrayList) {
            Storage.functions.put(function, i);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        super.onViewCreated(view, savedInstanceState);

//        Spinner spinner = view.findViewById(R.id.spinner_ending);

        Storage.functions.put(Constants.DV, 0);
        Storage.functions.put(Constants.CPF, 0);
        Storage.storage.put(Constants.CPF, "");


        startButton = view.findViewById(R.id.btn_start);
        pvButton = view.findViewById(R.id.btn_liveness);
        dcButton = view.findViewById(R.id.btn_documentoscopy);
        fmButton = view.findViewById(R.id.btn_facematch);
        ocButton = view.findViewById(R.id.btn_ocr);


        pvButton.setOnClickListener(v -> ChangeColorButton(pvButton, Constants.PV));
        fmButton.setOnClickListener(v -> ChangeColorButton(fmButton, Constants.FM));
        ocButton.setOnClickListener(v -> ChangeColorButton(ocButton, Constants.OC));
//        dcButton.setOnClickListener(v -> ChangeColorButton(dcButton, Constants.DC));

//        ocButton.setOnClickListener(v -> SendDisabledMessage());
        dcButton.setOnClickListener(v -> SendDisabledMessage());


        startButton.setOnClickListener(v -> {
//            final NavDirections action = WelcomeFragmentDirections.actionWelcomeFragmentToDataValidFragment();
//            Navigation.findNavController(requireView()).navigate(action);
            Navigate(v);

        });

        HandleColorButton();
        HandleStartButton();


    }

    private void Navigate(View view) {
        // quais os fluxos exigem selfie?
        // FM, DC, PV

        if (Storage.requested_FM() || Storage.requested_DC() || Storage.requested_PV()) {
            // goto liveness instructions
            final NavDirections livenessInstructions = WelcomeFragmentDirections.actionWelcomeFragmentToInstructionsLivenessFragment();
            Navigation.findNavController(view).navigate(livenessInstructions);
            return;
        }

        if (Storage.requested_OC() || Storage.requested_DV()) {
            // goto Document Instructions

            final NavDirections documentInstructions = WelcomeFragmentDirections.actionWelcomeFragmentToDocumentInstructionsFragment();
            Navigation.findNavController(view).navigate(documentInstructions);
            return;
        }
    }

    public static void CallApisTH(View v) {

        new Timer().schedule(
                new TimerTask() {

                    @Override
                    public void run() {

                        //if you need some code to run when the delay expires
                    }

                }, 10000);
//        android.os.SystemClock.sleep(10000);

        final NavDirections actionC = DocumentInstructionsFragmentDirections.actionDocumentInstructionsFragmentToDocumentDetectionResultFragment(new ResponseBody());
        Navigation.findNavController(v).navigate(actionC);
    }

    private void HandleStartButton() {

        int somethingSelected = 0;
        for (Constants function : arrayList) {
            int i = Storage.functions.get(function);
            if (i % 2 == 1) somethingSelected++;
        }
        if (somethingSelected > 0)
            startButton.setEnabled(true);
        else
            startButton.setEnabled(false);


    }

    private void SendDisabledMessage() {


        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.instructions)
                .setMessage(R.string.coming_soon)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, (dialog, which) -> {


                })
                .setIcon(android.R.drawable.btn_star)
                .show();
    }

    private void HandleColorButton() {
        SetColorButton(pvButton, Storage.functions.get(Constants.PV));
        SetColorButton(fmButton, Storage.functions.get(Constants.FM));
        SetColorButton(ocButton, Storage.functions.get(Constants.OC));
//        SetColorButton(dcButton, Storage.functions.get(Constants.DC));

        DisableButton(dcButton);
//        DisableButton(ocButton);


    }

    private void DisableButton(Button button) {
        button.setBackgroundColor(getResources().getColor(R.color.light_grey_400));
        button.setTextColor(getResources().getColor(R.color.black));


    }

    private void SetColorButton(Button button, Integer i) {

        if (i % 2 == 1) {
//            button.setBackgroundColor(Color.parseColor(Config.boxColor));
            button.setBackgroundColor(getResources().getColor(R.color.red_color_th));
            button.setTextColor(getResources().getColor(R.color.white));
        } else {
            button.setBackgroundColor(getResources().getColor(R.color.white));
            button.setTextColor(getResources().getColor(R.color.red_color_th));
        }

    }

    private void ChangeColorButton(Button button, Constants funName) {

        int i = Storage.functions.get(funName);

        i++;
        Storage.functions.put(funName, i % 2);

        SetColorButton(button, i);

        HandleStartButton();
    }


}
