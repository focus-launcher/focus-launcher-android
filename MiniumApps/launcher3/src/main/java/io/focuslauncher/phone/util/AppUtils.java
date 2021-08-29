package io.focuslauncher.phone.util;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import io.focuslauncher.R;
import io.focuslauncher.phone.utils.PrefSiempo;

public class AppUtils
{
    public static int backGroundColor = 0;
    public static int statusBarColorJunk = 0;
    public static int statusBarColorPane = 1;

    public static void statusBarManaged(Activity activity) {
        if (PrefSiempo.getInstance(activity).read(PrefSiempo
                .IS_DARK_THEME, false)) {
            cancelLightStatusBar(activity);
        } else {
            changeToLightStatusBar(activity);
        }
    }

    private static void changeToLightStatusBar(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        Window window = activity.getWindow();
        if (window == null) {
            return;
        }
        View decorView = window.getDecorView();
        if (decorView == null) {
            return;
        }
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private static void cancelLightStatusBar(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        Window window = activity.getWindow();
        if (window == null) {
            return;
        }
        View decorView = window.getDecorView();
        if (decorView == null) {
            return;
        }
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & (~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
    }

    public static void notificationBarManaged(Activity activity, RelativeLayout linMain) {
        if (PrefSiempo.getInstance(activity).read(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, false)) {
            hideNotification(activity);
            if (linMain != null) {
                linMain.setPadding(0, 0, 0, 0);
            }
        } else {
            showNotification(activity);
            if (linMain != null) {
                linMain.setPadding(0, getStatusBarHeight(activity), 0, 0);
            }

        }

        detectNotificationVisibility(activity);
    }

    private static void hideNotification(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //decorView.setFitsSystemWindows(false);
    }

    private static void showNotification(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
        //decorView.setFitsSystemWindows(true);
    }

    public static void statusbarColor0(Activity activity, int i)
    {
        if(backGroundColor != 0)
        {
            if(!PrefSiempo.getInstance(activity).read(PrefSiempo.DEFAULT_BAG_ENABLE, false))
            {
                if(!PrefSiempo.getInstance(activity).read(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, true))
                {
                    if(PrefSiempo.getInstance(activity).read(PrefSiempo.IS_DARK_THEME, false))
                    {
                        cancelLightStatusBar(activity);
                        activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity , R.color.dark_window));
                    }else
                    {
                        changeToLightStatusBar(activity);
                        activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity , backGroundColor));
                    }
                }
            }else
            {
                if(!PrefSiempo.getInstance(activity).read(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, false))
                {
                    activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity , R.color.transparent));
                }
            }
        }
    }

    private static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private static void detectNotificationVisibility(final Activity activity) {
        final boolean isEnable = PrefSiempo.getInstance(activity).read(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, false);
        if (isEnable) {
            activity.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                    (new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                if (PrefSiempo.getInstance(activity).read(PrefSiempo.DEFAULT_NOTIFICATION_ENABLE, false)) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            View decorView = activity.getWindow().getDecorView();
                                            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                                            decorView.setSystemUiVisibility(uiOptions);
                                            //decorView.setFitsSystemWindows(true);
                                        }
                                    }, 3000);
                                }
                            }
                        }
                    });

        } else {
            showNotification(activity);
            statusBarManaged(activity);
        }
    }
}
