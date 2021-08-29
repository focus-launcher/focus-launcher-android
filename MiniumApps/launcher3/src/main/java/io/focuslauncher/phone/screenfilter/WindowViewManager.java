package io.focuslauncher.phone.screenfilter;


import android.view.View;
import android.view.WindowManager;

public class WindowViewManager {

    private WindowManager mWindowManager;

    public WindowViewManager(WindowManager windowManager) {
        mWindowManager = windowManager;
    }

    /**
     * Creates and opens a new Window to display {@code view}.
     * @param view the view to render in the new Window.
     * @param wlp the {@link android.view.WindowManager.LayoutParams} to use when laying out the window.
     */
    public void openWindow(View view, WindowManager.LayoutParams wlp) {
        mWindowManager.addView(view, wlp);
    }

    /**
     * Triggers a Window undergo a screen measurement and layout pass with the provided
     * {@link android.view.WindowManager.LayoutParams}.
     *
     * @param view the Window containing this view will have its LayoutParams set to {@code wlp}.
     * @param wlp the new LayoutParams to set on the Window.
     */
    public void reLayoutWindow(View view, WindowManager.LayoutParams wlp) {
        mWindowManager.updateViewLayout(view, wlp);
    }

    /**
     * Closes the Window that is currently displaying {@code view}.
     * @param view the Window containing this view will be closed.
     */
    public void closeWindow(View view) {
        mWindowManager.removeView(view);
    }
}

