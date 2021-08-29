package io.focuslauncher.phone.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by rajeshjadi on 11/1/18.
 */

public class LockEditText extends android.support.v7.widget.AppCompatEditText {
    /* Must use this constructor in order for the layout files to instantiate the class properly */
    public LockEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            Log.e("onKeyPreIme ", "" + event);
            return true;
        } else {
            return false;
        }
        //Log.e("onKeyPreIme ",""+event);
    }

}
