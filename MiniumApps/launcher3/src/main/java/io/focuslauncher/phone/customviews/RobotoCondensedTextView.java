package io.focuslauncher.phone.customviews;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

public class RobotoCondensedTextView extends AppCompatTextView {
    public RobotoCondensedTextView(Context context) {
        super(context);
        init(null);
    }

    public RobotoCondensedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RobotoCondensedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            try {
                Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/robotocondensedregular.ttf");
                setTypeface(myTypeface);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
