package io.focuslauncher.phone.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import io.focuslauncher.R;


/**
 * Creates a circular swatch of a specified color.  Adds a checkmark if marked as checked
 */
@SuppressLint("ViewConstructor")
public class ColorPickerSwatch extends FrameLayout implements View.OnClickListener {
    private int mColor;
    private ImageView mSwatchImage;
    private ImageView mCheckmarkImage;
    private OnColorSelectedListener mOnColorSelectedListener;

    public ColorPickerSwatch(Context context, int color, boolean checked,
                             OnColorSelectedListener listener) {

        super(context);
        mColor = color;
        mOnColorSelectedListener = listener;

        LayoutInflater.from(context).inflate(R.layout.color_picker_swatch, this);
        mSwatchImage = findViewById(R.id.color_picker_swatch);
        mCheckmarkImage = findViewById(R.id.color_picker_checkmark);
        setColor(color);
        setChecked(checked);
        setOnClickListener(this);
    }

    protected void setColor(int color) {
        Drawable[] colorDrawable = new Drawable[]
                {getContext().getResources().getDrawable(R.drawable.color_picker_swatch)};

        mSwatchImage.setImageDrawable(new ColorStateDrawable(colorDrawable, color));
    }

    private void setChecked(boolean checked) {
        if (checked) {
            mCheckmarkImage.setVisibility(View.VISIBLE);
        } else {
            mCheckmarkImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnColorSelectedListener != null)
            mOnColorSelectedListener.onColorSelected(mColor);
    }

    /**
     * Interface for a callback when a color square is selected
     */
    public interface OnColorSelectedListener {
        // Called when a specific color square has been selected
        void onColorSelected(int color);
    }
}
