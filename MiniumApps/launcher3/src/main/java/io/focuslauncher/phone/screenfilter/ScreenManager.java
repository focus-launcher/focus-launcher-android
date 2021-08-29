package io.focuslauncher.phone.screenfilter;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

public class ScreenManager {
    private static final String TAG = "ScreenManager";
    private static final boolean DEBUG = false;

    private static final int DEFAULT_NAV_BAR_HEIGHT_DP = 48;
    private static final int DEFAULT_STATUS_BAR_HEIGHT_DP = 25;

    private Resources mResources;
    private WindowManager mWindowManager;

    private int mStatusBarHeight = -1;
    private int mNavigationBarHeight = -1;

    public ScreenManager(Context context, WindowManager windowManager) {
        mResources = context.getResources();
        mWindowManager = windowManager;
    }

    public int getScreenHeight() {
        Display display = mWindowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);

        int screenHeight = dm.heightPixels + getStatusBarHeightPx();

        if (inPortrait()) {
            screenHeight += getNavigationBarHeightPx();
        }

        return screenHeight;
    }

    public int getStatusBarHeightPx() {
        if (mStatusBarHeight == -1) {
            int statusBarHeightId = mResources.getIdentifier("status_bar_height", "dimen", "android");

            if (statusBarHeightId > 0) {
                mStatusBarHeight = mResources.getDimensionPixelSize(statusBarHeightId);
                if (DEBUG) Log.i(TAG, "Found Status Bar Height: " + mStatusBarHeight);
            } else {
                mStatusBarHeight = (int) dpToPx(DEFAULT_STATUS_BAR_HEIGHT_DP);
                if (DEBUG) Log.i(TAG, "Using default Status Bar Height: " + mStatusBarHeight);
            }
        }

        return mStatusBarHeight;
    }

    public int getNavigationBarHeightPx() {
        if (mNavigationBarHeight == -1) {
            int navBarHeightId = mResources.getIdentifier("navigation_bar_height", "dimen", "android");

            if (navBarHeightId > 0) {
                mNavigationBarHeight = mResources.getDimensionPixelSize(navBarHeightId);
                if (DEBUG) Log.i(TAG, "Found Navigation Bar Height: " + mNavigationBarHeight);
            } else {
                mNavigationBarHeight = (int) dpToPx(DEFAULT_NAV_BAR_HEIGHT_DP);
                if (DEBUG) Log.i(TAG, "Using default Navigation Bar Height: " + mNavigationBarHeight);
            }
        }

        return mNavigationBarHeight;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mResources.getDisplayMetrics());
    }

    private boolean inPortrait() {
        return mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}

