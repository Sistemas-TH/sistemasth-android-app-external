package br.com.sistemasthexample.sistemasth.fragments;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import br.com.sistemasthexample.R;
import br.com.sistemasthexample.sistemasth.Constants;
import br.com.sistemasthexample.sistemasth.Storage;

import java.util.Arrays;
import java.util.List;

import br.com.sapereaude.maskedEditText.MaskedEditText;
import kotlin.text.Regex;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GetCpfFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class GetCpfFragment  extends BaseFragment {
    TextView tvTitle;
    TextView tvErrorCpf;
    MaskedEditText mskCpfInput;
    Button btnNext;
    public GetCpfFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_get_cpf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.title_get_cpf);
        mskCpfInput = view.findViewById(R.id.editTextCPF);
        tvErrorCpf = view.findViewById(R.id.error_message_cpf);
        btnNext = view.findViewById(R.id.btn_next);



        btnNext.setEnabled(true);
        mskCpfInput.setText("33032041791");


        TextWatcher fieldValidatorTextWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                btnNext.setEnabled(false);
                String cpf = mskCpfInput.getRawText();
                if (cpf.length() != 11) {
                    tvErrorCpf.setVisibility(View.INVISIBLE);
                    return;
                }
                if (!IsCpf(cpf)) {
                    tvErrorCpf.setVisibility(View.VISIBLE);
                    return;
                }
                btnNext.setEnabled(true);


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

        };
        mskCpfInput.addTextChangedListener(fieldValidatorTextWatcher);

        Decision("");

        btnNext.setOnClickListener(v -> {
            Decision(mskCpfInput.getRawText());


        });



    }

    private void Decision(String text) {
        Storage.storage.put(Constants.CPF, text);
        Navigate(requireView());
    }

    private void Navigate(View view) {
        // quais os fluxos exigem selfie?
        // FM, DC, PV

        if (Storage.requested_FM() || Storage.requested_DC() || Storage.requested_PV()) {
            // goto liveness instructions
            final NavDirections livenessInstructions = GetCpfFragmentDirections.actionGetCpfFragmentToLivenessInstructionsFragment();
            Navigation.findNavController(view).navigate(livenessInstructions);
            return;
        }

        if (Storage.requested_OC() || Storage.requested_DV()) {
            // goto Document Instructions

            final NavDirections documentInstructions = GetCpfFragmentDirections.actionGetCpfFragmentToDocumentInstructionsFragment2();
            Navigation.findNavController(view).navigate(documentInstructions);
            return;
        }
    }

    public static boolean IsCpf(String cpf)
    {
        List<String> cpfinvalidos = Arrays.asList("00000000000", "11111111111", "22222222222", "33333333333", "44444444444", "55555555555", "66666666666", "77777777777", "88888888888", "99999999999");


        List<Integer> multiplicador1 = Arrays.asList( 10, 9, 8, 7, 6, 5, 4, 3, 2 );
        List<Integer> multiplicador2 = Arrays.asList( 11, 10, 9, 8, 7, 6, 5, 4, 3, 2 );
        String tempCpf;
        String digito;
        int soma;
        int resto;
        cpf = OnlyNumbers(cpf);
        if (cpf == null) return false;
        if (cpf.length() == 0) return false;
        //cpf = Convert.ToInt64(cpf).ToString();
        if (cpf.length() > 11 || cpf.length() < 7)  return false;

        cpf =  Right( padLeft(11, "0") + cpf,  11); ;
        if (cpfinvalidos.contains(cpf)) return false;
        tempCpf = cpf.substring(0, 9);
        soma = 0;
        for (int i = 0; i < 9; i++)
            soma += Integer.parseInt(tempCpf.substring(i, i+1)) * multiplicador1.get(i);
        resto = soma % 11;
        if (resto < 2)
            resto = 0;
        else
            resto = 11 - resto;
        digito =  String.valueOf(resto);
        tempCpf = tempCpf + digito;
        soma = 0;
        for (int i = 0; i < 10; i++)
            soma += Integer.parseInt(tempCpf.substring(i, i+1)) * multiplicador2.get(i);
        resto = soma % 11;
        if (resto < 2)
            resto = 0;
        else
            resto = 11 - resto;
        digito = digito + String.valueOf(resto);

        return cpf.endsWith(digito);

    }

    private static String OnlyNumbers(String text) {
        Regex r = new Regex("[^0-9]");
        return text != null ? r.replace(text , "") : null;

    }

    private static String Right(String text, Integer num) {

        return text.substring(text.length() - num, text.length());

    }
    private static String padLeft(int padUpTo, String input){
        return String.format("%0" + padUpTo + "d", Integer.parseInt(input));
    }
}