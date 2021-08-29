package io.focuslauncher.phone.screenfilter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

public class ScreenFilterView extends View {
    public static final int MIN_DIM      = 0;
    private static final float MAX_DIM   = 100f;
    private static final float MIN_ALPHA = 0x00;
    private static final float MAX_ALPHA = (int) (0xFF * 0.75);

    private int mDimLevel = 60;
    private int mAlpha = dimLevelToAlpha(mDimLevel);
    private int mRgbColor = 0xFF91a7ff;

    public ScreenFilterView(Context context) {
        super(context);
    }

    public int getFilterDimLevel() {
        return mDimLevel;
    }

    public int getFilterRgbColor() {
        return mRgbColor;
    }

    /**
     * Sets the dim level of the screen filter.
     *
     * @param dimLevel value between 0 and 100, inclusive, where 0 is fully transparent, and 100 is
     *                 the maximum allowed dim level determined by the system, but is guaranteed to
     *                 never be fully opaque.
     */
    public void setFilterDimLevel(int dimLevel) {
        mDimLevel = dimLevel;
        mAlpha = dimLevelToAlpha(dimLevel);
        invalidate();
    }

    /**
     * Sets the color tint of the screen filter.
     *
     * @param color RGB color represented by a 32-bit int; the format is the same as the one defined
     *              in {@link android.graphics.Color}, but the alpha byte is ignored.
     */
    public void setFilterRgbColor(int color) {
        mRgbColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.argb(mAlpha, Color.red(mRgbColor), Color.green(mRgbColor), Color.blue(mRgbColor)));
    }

    private static int dimLevelToAlpha(int dimLevel) {
        return (int) mapToRange((float) dimLevel, MIN_DIM, MAX_DIM, MIN_ALPHA, MAX_ALPHA);
    }

    private static float mapToRange(float value, float minInput, float maxInput,
                                    float minOutput, float maxOutput) {
        return (value - minInput) * ((maxOutput - minOutput) / (maxInput - minInput)) + minOutput;
    }
}

