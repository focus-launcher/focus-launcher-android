package io.focuslauncher.phone.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class FitsSystemWindowConstraintLayout extends ConstraintLayout {


    private Drawable mStatusBarBackground;
    private boolean mDrawStatusBarBackground;

    private WindowInsetsCompat mLastInsets;

    private Map<View, int[]> childsMargins = new HashMap<>();

    public FitsSystemWindowConstraintLayout(Context context) {
        this(context, null);
    }

    public FitsSystemWindowConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitsSystemWindowConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (ViewCompat.getFitsSystemWindows(this)) {
            ViewCompat.setOnApplyWindowInsetsListener(this, new androidx.core.view.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                    FitsSystemWindowConstraintLayout layout = (FitsSystemWindowConstraintLayout) view;
                    layout.setChildInsets(insets, insets.getSystemWindowInsetTop() > 0);
                    return insets.consumeSystemWindowInsets();
                }
            });
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            TypedArray typedArray = context.obtainStyledAttributes(new int[]{android.R.attr.colorPrimaryDark});
            try {
                mStatusBarBackground = typedArray.getDrawable(0);
            } finally {
                typedArray.recycle();
            }
        } else {
            mStatusBarBackground = null;
        }
    }

    public void setChildInsets(WindowInsetsCompat insets, boolean draw) {
        mLastInsets = insets;
        mDrawStatusBarBackground = draw;
        setWillNotDraw(!draw && getBackground() == null);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if (ViewCompat.getFitsSystemWindows(this)) {
                    LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();

                    if (ViewCompat.getFitsSystemWindows(child)) {
                        ViewCompat.dispatchApplyWindowInsets(child, insets);
                    } else {
                        int[] childMargins = childsMargins.get(child);
                        if (childMargins == null) {
                            childMargins = new int[]{layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin};
                            childsMargins.put(child, childMargins);
                        }
                        if (layoutParams.leftToLeft == LayoutParams.PARENT_ID) {
                            layoutParams.leftMargin = childMargins[0] + insets.getSystemWindowInsetLeft();
                        }
                        if (layoutParams.topToTop == LayoutParams.PARENT_ID) {
                            layoutParams.topMargin = childMargins[1] + insets.getSystemWindowInsetTop();
                        }
                        if (layoutParams.rightToRight == LayoutParams.PARENT_ID) {
                            layoutParams.rightMargin = childMargins[2] + insets.getSystemWindowInsetRight();
                        }
                        if (layoutParams.bottomToBottom == LayoutParams.PARENT_ID) {
                            layoutParams.bottomMargin = childMargins[3] + insets.getSystemWindowInsetBottom();
                        }
                    }
                }
            }
        }

        requestLayout();
    }

    public void setStatusBarBackground(Drawable bg) {
        mStatusBarBackground = bg;
        invalidate();
    }

    public Drawable getStatusBarBackgroundDrawable() {
        return mStatusBarBackground;
    }

    public void setStatusBarBackground(int resId) {
        mStatusBarBackground = resId != 0 ? ContextCompat.getDrawable(getContext(), resId) : null;
        invalidate();
    }

    public void setStatusBarBackgroundColor(@ColorInt int color) {
        mStatusBarBackground = new ColorDrawable(color);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawStatusBarBackground && mStatusBarBackground != null) {
            int inset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            if (inset > 0) {
                mStatusBarBackground.setBounds(0, 0, getWidth(), inset);
                mStatusBarBackground.draw(canvas);
            }
        }
    }
}