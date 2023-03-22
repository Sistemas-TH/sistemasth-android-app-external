package br.com.sistemasthexample.sistemasth.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import br.com.sistemasthexample.R;
import br.com.sistemasthexample.sistemasth.FaceDetectionResult;
import br.com.sistemasthexample.sistemasth.Storage;
import br.com.sistemasthexample.sistemasth.models.ResponseBody;


public class FaceDetectionResultFragment extends BaseFragment {


    private FaceDetectionResult faceDetectionResult;

    public FaceDetectionResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_face_detection_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.faceDetectionResult = FaceDetectionResultFragmentArgs.fromBundle(getArguments()).getResult();



        TextView resultTitleTextView = view.findViewById(R.id.result_title_text_view);
        TextView resultDescriptionTextView = view.findViewById(R.id.result_description_text_view);
        Button resultButton = view.findViewById(R.id.result_button);
        ImageView resultImageView = view.findViewById(R.id.result_image_view);

        Resources resources = getResources();
        if (faceDetectionResult == FaceDetectionResult.SUCCESS) {
            resultImageView.setBackgroundResource(R.drawable.check_circle_outline);
            resultTitleTextView.setText(resources.getString(R.string.verification_completed_successfully));
            resultDescriptionTextView.setText(resources.getString(R.string.congratulations_your_facial_check_has_been_successfully_completed));
            resultButton.setText(resources.getString(R.string.text_continue));

            // TODO: Fazer aqui as calls para as Apis da TH ou a call para a captura de documento
            if (Storage.requested_OC() || Storage.requested_FM() || Storage.requested_DV()) {
                // TODO: Call captura de documento
                final NavDirections action = LivenessInstructionsFragmentDirections.actionInstructionsFragmentToDocumentInstructionsFragment();
                Navigation.findNavController(requireView()).navigate(action);
                return;
            }
            // TODO: Call result Fragment
            final NavDirections action = LivenessInstructionsFragmentDirections.actionInstructionsLivenessFragmentToFinalResultFragment(new ResponseBody());
            Navigation.findNavController(requireView()).navigate(action);
            return;
        } else {
            resultImageView.setBackgroundResource(R.drawable.error_outline);
            resultTitleTextView.setText(resources.getString(R.string.verification_could_not_be_completed));
            resultDescriptionTextView.setText(resources.getString(R.string.please_try_to_take_your_photo_again));
            resultButton.setText(resources.getString(R.string.try_again));
        }

        resultButton.setOnClickListener(v -> {
            // TODO: criar um botao para realizar nova verificacao
            // TODO: limpar as variaveis e enviar o usuario para a primeira pagina de Welcome
            // TODO: Criar pagina de login *init*
            Navigation.findNavController(requireView()).popBackStack();
        });
    }
}
