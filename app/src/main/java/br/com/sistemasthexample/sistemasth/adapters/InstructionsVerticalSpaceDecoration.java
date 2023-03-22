package br.com.sistemasthexample.sistemasth.adapters;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class InstructionsVerticalSpaceDecoration extends RecyclerView.ItemDecoration {

    private final int verticalSpaceHeight;

    public InstructionsVerticalSpaceDecoration(int verticalSpaceHeight) {
        this.verticalSpaceHeight = verticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, @NotNull View view, @NotNull RecyclerView parent,
                               @NotNull RecyclerView.State state) {
        outRect.bottom = verticalSpaceHeight;
    }
}
