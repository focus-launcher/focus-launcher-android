package io.focuslauncher.phone.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import io.focuslauncher.BuildConfig;
import io.focuslauncher.R;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.app.Launcher3App;


public class UIUtils {
    public static final String PACKAGE_NAME = "co.siempo.phone";
    private static AlertDialog alertDialog;

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, context.getResources().getDisplayMetrics());
    }


    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void toast(Context context, @StringRes int msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void toastShort(Context context, @StringRes int msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void toastShort(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void alert(Context context, String msg) {
        alert(context, null, msg);
    }

    public static void alert(Context context, String title, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public static void feedbackAlert(Context context, String title, String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.feedbackDialogStyle)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public static void alert(Context context, int layoutRes) {
        new AlertDialog.Builder(context)
                .setView(layoutRes)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public static void confirm(Context context, String msg, DialogInterface.OnClickListener listener) {
        confirm(context, null, msg, listener);
    }

    public static void confirm(Context context, String title, String msg, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Global method to access dialog with ok button text.
     *
     * @param context
     * @param title
     * @param msg
     * @param listener
     */
    public static void confirmWithSingleButton(Context context, String title, String msg, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, listener)
                .show();
    }

    public static void confirmWithCancel(Context context, String title, String msg, DialogInterface.OnClickListener listener, DialogInterface.OnClickListener listenerNo) {


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listenerNo);

        alertDialog = alertDialogBuilder.create();
        if (alertDialog != null) {
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        } else {
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

    }


    public static void confirmWithCancel(Context context, String title, String msg, String okButton, String cancelButton, DialogInterface.OnClickListener listener, DialogInterface.OnClickListener listenerNo) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(okButton, listener)
                .setNegativeButton(cancelButton, listenerNo);

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();


    }


    public static void confirmWithCancel(Context context, String msg, String okButton, String cancelButton, DialogInterface.OnClickListener listener, DialogInterface.OnClickListener listenerNo) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
                .setMessage(msg)
                .setCancelable(false)

                .setPositiveButton(okButton, listener)
                .setNegativeButton(cancelButton, listenerNo);
        if (alertDialog != null) {
            if (!alertDialog.isShowing()) {
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        } else {
            alertDialog = alertDialogBuilder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }

    }

    public static void ask(Context context, String msg, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(null)
                .setMessage(msg)
                .setPositiveButton(R.string.label_yes, listener)
                .setNegativeButton(R.string.label_no, null)
                .show();
    }

    public static void notification(Context context, String title, String msg, @StringRes int resOk, @StringRes int resCancel, @DrawableRes int resIcon, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setIcon(resIcon)
                .setPositiveButton(resOk, listener)
                .setNegativeButton(resCancel, listener)
                .show();
    }

    public static float getScreenHeight(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return (float) displaymetrics.heightPixels;

    }

    public static float getScreenWidth(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return (float) displaymetrics.widthPixels;

    }

    public static void showKeyboard(EditText editText) {
        try {
            if (editText != null) {
                InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method return true if my application is default launcher.
     *
     * @param activity
     * @return
     */
    public static boolean isMyLauncherDefault(Activity activity) {
        return getLauncherPackageName(activity).equals(activity.getPackageName());
    }

    /**
     * This method returns default launcher package name.
     *
     * @param activity
     * @return
     */
    public static String getLauncherPackageName(Activity activity) {
        PackageManager localPackageManager = activity.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        return localPackageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
    }

    /**
     * Hides the soft keyboard
     */
    public static void hideSoftKeyboard(Context mContext, IBinder windowToken) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    public static int getDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) metrics.density;
    }

    /**
     * This method returns the device model name.
     *
     * @param context refrence of calling view.
     * @return return the Manufacture Name,Model Name,Android Version and Display Size in pixel.
     */
    public static String getDeviceInfo(Context context) {
        return "\n\n\nMy Device Information is as follows:"
                + "\nMANUFACTURER : " + Build.MANUFACTURER
                + "\nMODEL : " + Build.MODEL
                + "\nOS VERSION : " + Build.VERSION.RELEASE
                + "\nDISPLAY : " + getScreenDisplaySize(context);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * This method returns the screen resolution.
     *
     * @param context takem the current view context.
     * @return Display size in pixel
     */
    private static String getScreenDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = null;
        if (wm != null) {
            display = wm.getDefaultDisplay();
        }
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int width = size.x;
        return "" + width + "*" + height;
    }


    public static boolean isAppInstalledAndEnabled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            CoreApplication.getInstance().logException(e);
            //  e.printStackTrace();
        }
        return applicationInfo != null && applicationInfo.enabled;
// Installed
    }

    public static boolean isAppInstalledAndEnabled(String packageName) {
        PackageManager packageManager = Launcher3App.getInstance().getApplicationContext().getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            CoreApplication.getInstance().logException(e);
            //  e.printStackTrace();
        }
        return applicationInfo != null && applicationInfo.enabled;
// Installed
    }

    public static boolean isInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            CoreApplication.getInstance().logException(e);
            e.printStackTrace();
        }
        return applicationInfo != null;
// Installed
    }

    public static boolean isAppEnabled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            CoreApplication.getInstance().logException(e);
            e.printStackTrace();
        }
        return applicationInfo.enabled;
// Installed
    }


    public static byte[] convertBitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap convertBytetoBitmap(byte[] byteArray) {
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            if (bitmap != null && bitmap.isMutable()) {
                bitmap.setWidth(bitmap.getWidth() / 2);
                bitmap.setHeight(bitmap.getHeight() / 2);
                return bitmap;
            } else {
                return bitmap;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isDeviceHasSimCard(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
        //the phone has a sim card
//no sim card available
        return tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT;
    }

    public static int getCurrentVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            CoreApplication.getInstance().logException(e);
            e.printStackTrace();
        }
        return BuildConfig.VERSION_CODE;
    }

    /**
     * This method is used for dim the background when user open the pop up dialog.
     *
     * @param parent
     * @param dimAmount
     */
    public static void applyDim(@NonNull ViewGroup parent, float dimAmount) {
        //Drawable dim = new ColorDrawable(Color.BLACK);
        Drawable dim = new ColorDrawable(parent.getContext().getResources()
                .getColor(R.color.black));
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    /**
     * This method remove the dim background.
     *
     * @param parent
     */
    public static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

    public static void setDynamicHeight(Activity activity, final ListView listView) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListAdapter adapter = listView.getAdapter();
                //check adapter if null
                if (adapter == null) {
                    return;
                }
                int height = 0;
                int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
                for (int i = 0; i < adapter.getCount(); i++) {
                    View listItem = adapter.getView(i, null, listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                    height += listItem.getMeasuredHeight();
                }
                ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
                layoutParams.height = height + (listView.getDividerHeight() * (adapter.getCount() - 1));
                listView.setLayoutParams(layoutParams);
                listView.requestLayout();
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (appOps != null) {
            mode = appOps.checkOpNoThrow("android:get_usage_stats",
                    android.os.Process.myUid(), context.getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static class FadePageTransformer implements ViewPager
            .PageTransformer {
        public void transformPage(@NonNull View view, float position) {

            // Page is not an immediate sibling, just make transparent
            if (position < -1 || position > 1) {
                view.setAlpha(0);
            }
            // Page is sibling to left or right
            else if (position <= 0 || position <= 1) {

                // Calculate alpha.  Position is decimal in [-1,0] or [0,1]
                float alpha = (position <= 0) ? position + 1 : 1 - position;
                view.setAlpha(alpha);

            }
            // Page is active, make fully visible
            else if (position == 0) {
                view.setAlpha(1);
            }

        }

    }

}
