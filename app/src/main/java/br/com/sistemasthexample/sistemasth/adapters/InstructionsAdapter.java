package br.com.sistemasthexample.sistemasth.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import br.com.sistemasthexample.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class InstructionsAdapter extends RecyclerView.Adapter<InstructionsAdapter.InstructionsViewHolder> {

    private final List<String> instructions;
    private final LayoutInflater inflater;

    public InstructionsAdapter(Context context, List<String> instructions) {
        this.inflater = LayoutInflater.from(context);
        this.instructions = instructions;
    }

    @NotNull
    @Override
    public InstructionsViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.instruction_item_list_view, parent, false);
        return new InstructionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InstructionsViewHolder holder, int position) {
        holder.instructionsTextView.setText(instructions.get(position));
        holder.instructionsNumberTextView.setText(String.valueOf(++position));
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    static class InstructionsViewHolder extends RecyclerView.ViewHolder {
        TextView instructionsNumberTextView;
        TextView instructionsTextView;

        InstructionsViewHolder(View itemView) {
            super(itemView);
            instructionsNumberTextView = itemView.findViewById(R.id.instructions_number_text_view);
            instructionsTextView = itemView.findViewById(R.id.instructions_text_view);
        }
    }
}