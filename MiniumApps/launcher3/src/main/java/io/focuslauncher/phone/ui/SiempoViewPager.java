package io.focuslauncher.phone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by roma on 11/4/18.
 * Added as part of SSA-1510, it is a touch crash occurring in google component
 * and resolution is to catch it in onIntercept touch
 */

public class SiempoViewPager extends android.support.v4.view.ViewPager {

    public SiempoViewPager(Context context) {
        super(context);
    }

    public SiempoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
