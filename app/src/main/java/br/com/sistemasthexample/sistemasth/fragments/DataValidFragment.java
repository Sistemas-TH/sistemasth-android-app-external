package br.com.sistemasthexample.sistemasth.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import br.com.sistemasthexample.R;
import br.com.sistemasthexample.sistemasth.Constants;
import br.com.sistemasthexample.sistemasth.Storage;


public class DataValidFragment extends BaseFragment {


    public DataValidFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_use_datavalid, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button yesButton = view.findViewById(R.id.yes);
        final Button noButton = view.findViewById(R.id.no);


        Decision(0);
        yesButton.setOnClickListener(v -> Decision(1));
        noButton.setOnClickListener(v -> Decision(0));


    }

    private void Decision(int value) {
        Storage.functions.put(Constants.DV, value);
        final NavDirections action = DataValidFragmentDirections.actionDataValidFragmentToGetCpfFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }
}