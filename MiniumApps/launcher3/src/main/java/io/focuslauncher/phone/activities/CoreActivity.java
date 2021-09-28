package io.focuslauncher.phone.activities;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import io.focuslauncher.R;
import io.focuslauncher.phone.app.Config;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.event.DownloadApkEvent;
import io.focuslauncher.phone.event.JunkAppOpenEvent;
import io.focuslauncher.phone.helper.Validate;
import io.focuslauncher.phone.interfaces.NFCInterface;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.receivers.ScreenOffAdminReceiver;
import io.focuslauncher.phone.util.AppUtils;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;
import org.androidannotations.annotations.EActivity;

import java.io.*;

/**
 * This activity will be the base activity
 * All activity of all the modules should extend this activity
 * <p>
 * Created by shahab on 3/17/16.
 */

@EActivity
public abstract class CoreActivity extends AppCompatActivity implements NFCInterface, GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    public static File localPath, backupPath;
    public int currentIndex = 0;
    public View mTestView = null;
    public WindowManager windowManager = null;
    public boolean isOnStopCalled = false;
    int onStartCount = 0;
    SharedPreferences launcherPrefs;
    UserPresentBroadcastReceiver userPresentBroadcastReceiver;
    private IntentFilter mFilter;
    private InnerRecevier mRecevier;
    private String state = "";
    private String TAG = "CoreActivity";
    private DownloadReceiver mDownloadReceiver;

    public GestureDetector gestureDetector;
    private NotificationManager mNotificationManager;

    // Static method to return File at localPath
    public static File getLocalPath() {
        return localPath;
    }

    // Static method to return File at backupPath
    public static File getBackupPath() {
        return backupPath;
    }

    public static boolean isSiempoLauncher(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (defaultLauncher != null && defaultLauncher.activityInfo != null && defaultLauncher.activityInfo.packageName != null) {
                String defaultLauncherStr = defaultLauncher.activityInfo.packageName;
                return defaultLauncherStr.equals(context.getPackageName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        boolean read = PrefSiempo.getInstance(this).read(PrefSiempo.IS_DARK_THEME, false);
        setTheme(read ? R.style.SiempoAppThemeDark : R.style.SiempoAppTheme);
        super.onCreate(savedInstanceState);
        this.setVolumeControlStream(AudioManager.STREAM_SYSTEM);
        windowManager = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);

        mRecevier = new InnerRecevier();
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        userPresentBroadcastReceiver = new UserPresentBroadcastReceiver();
        registerReceiver(userPresentBroadcastReceiver, intentFilter);

        if (PrefSiempo.getInstance(this).read(PrefSiempo.SELECTED_THEME_ID, 0) != 0) {
            setTheme(PrefSiempo.getInstance(this).read(PrefSiempo.SELECTED_THEME_ID, 0));
        }
        try {
            registerReceiver(mRecevier, mFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDownloadReceiver = new DownloadReceiver();
        IntentFilter downloadIntent = new IntentFilter();
        downloadIntent.addAction("android.intent.action.DOWNLOAD_COMPLETE");
        registerReceiver(mDownloadReceiver, downloadIntent);

        // TODO: consider to remove the dialog
//        startAlarm();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                params.bottomMargin = insets.getSystemWindowInsetBottom();
                return insets.consumeSystemWindowInsets();
            }
        });

        // set gesture detector
        gestureDetector = new GestureDetector(this, this);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        gestureDetector.setOnDoubleTapListener(this);
        AppUtils.notificationBarManaged(this, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnStopCalled = false;
        AppUtils.notificationBarManaged(this, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        boolean read = PrefSiempo.getInstance(this).read(PrefSiempo.IS_DARK_THEME, false);
        setTheme(read ? R.style.SiempoAppThemeDark : R.style.SiempoAppTheme);
        super.onNewIntent(intent);
    }

    public void loadDialog() {
        if (mTestView == null) {
            WindowManager.LayoutParams layoutParams;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                //noinspection deprecation
                layoutParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
            }
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.gravity = Gravity.TOP | Gravity.START;
            layoutParams.flags =
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            mTestView = View.inflate(CoreActivity.this, R.layout.tooltip_launcher, null);
            if (currentIndex == 0) {
                mTestView.findViewById(R.id.linSiempoApp).setVisibility(View.VISIBLE);
                mTestView.findViewById(R.id.linDefaultApp).setVisibility(View.GONE);
                mTestView.findViewById(R.id.txtTitle).setVisibility(View.VISIBLE);
            } else {
                mTestView.findViewById(R.id.linSiempoApp).setVisibility(View.GONE);
                mTestView.findViewById(R.id.linDefaultApp).setVisibility(View.VISIBLE);
                mTestView.findViewById(R.id.txtTitle).setVisibility(View.GONE);
            }
            //Must wire up back button, otherwise it's not sent to our activity
            mTestView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (mTestView != null)
                            windowManager.removeView(mTestView);
                        mTestView = null;
                        onBackPressed();
                    }
                    return true;
                }
            });
            mTestView.findViewById(R.id.linSecond).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTestView != null)
                        windowManager.removeView(mTestView);
                    mTestView = null;
                }
            });

            mTestView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTestView != null)
                        windowManager.removeView(mTestView);
                    mTestView = null;
                }
            });
            if (Build.VERSION.SDK_INT >= 23) {
                if (Settings.canDrawOverlays(this)) {
                    windowManager.addView(mTestView, layoutParams);
                }
            } else {
                windowManager.addView(mTestView, layoutParams);
            }

        }

    }

    private void onCreateAnimation(Bundle savedInstanceState) {
        onStartCount = 1;
        if (savedInstanceState == null) {
            this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        } else {
            onStartCount = 2;
        }
    }

    private void onStartAnimation() {
        if (onStartCount > 1) {
            this.overridePendingTransition(R.anim.anim_slide_in_right,
                    R.anim.anim_slide_out_right);
        } else if (onStartCount == 1) {
            onStartCount++;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        try {
            if (Config.isNotificationAlive) {
                getFragmentManager().beginTransaction().
                        remove(getFragmentManager().findFragmentById(R.id.mainView)).commit();
                Config.isNotificationAlive = false;
            }
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            e.printStackTrace();
        }
        isOnStopCalled = true;
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userPresentBroadcastReceiver != null) {
            unregisterReceiver(userPresentBroadcastReceiver);
        }
        if (mRecevier != null) {
            try {
                unregisterReceiver(mRecevier);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mDownloadReceiver != null) {
            try {
                unregisterReceiver(mDownloadReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load fragment by replacing all previous fragments
     *
     * @param fragment
     */
    public void loadFragment(Fragment fragment, int containerViewId, String tag) {
        try {
            FragmentManager fragmentManager = getFragmentManager();
            // clear back stack
            for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction t = fragmentManager.beginTransaction();
            t.replace(containerViewId, fragment, tag);
            fragmentManager.popBackStack();
            t.commitAllowingStateLoss();
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
        }
    }

    /**
     * @param fragment
     * @param containerViewId
     */
    public void loadChildFragment(Fragment fragment, int containerViewId) {
        Validate.notNull(fragment);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(containerViewId, fragment, "main");
        ft.addToBackStack(null);
        try {
            ft.commit();
        } catch (Exception e) {
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                handleBackPress();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void genericEvent(Object event) {
        // DO NOT code here, it is a generic catch event method
    }

    @Override
    public void onBackPressed() {
        handleBackPress();
    }

    private void handleBackPress() {
        try {
            if (getFragmentManager().getBackStackEntryCount() == 0) {
                this.finish();
            } else {
                getFragmentManager().popBackStack();
            }
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            e.printStackTrace();
        }
    }

    @Subscribe
    public void downloadApkEvent(DownloadApkEvent event) {
        try {
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(Uri.fromFile(new File(event.getPath())),
                    "application/vnd.android.package-archive");
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(installIntent);
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }
    }

    @Override
    public String nfcRead(Tag t) {
        return null;
    }

    @Override
    public String readText(NdefRecord record) {
        return null;
    }

    @Override
    public void nfcReader(Tag tag) {

    }

    @Subscribe()
    public void onEvent(JunkAppOpenEvent junkAppOpenEvent) {
        if (junkAppOpenEvent != null && junkAppOpenEvent.isNotify()) {
            try {
//                if (PackageUtil.isSiempoLauncher(this) && PrefSiempo
//                        .getInstance(this).read
//                                (PrefSiempo.JUNK_RESTRICTED,
//                                        false)) {
//                    Intent intent = new Intent(this, OverlayService.class);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings
//                            .canDrawOverlays(this)) {
//                        startService(intent);
//                    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//                        startService(intent);
//                    }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


    /**
     * This BroadcastReceiver is included for the when user press home button and lock the screen.
     * when it comes back we have to show launcher dialog,toottip window.
     */
    public class UserPresentBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            try {
                if (intent != null && intent.getAction() != null && null != arg0) {
                    if (PackageUtil.isSiempoLauncher(arg0) && (intent.getAction()
                            .equals
                                    (Intent.ACTION_USER_PRESENT) ||
                            intent.getAction().equals(Intent.ACTION_SCREEN_ON))) {
                        boolean lockcounterstatus = PrefSiempo.getInstance(CoreActivity.this).read
                                (PrefSiempo
                                        .LOCK_COUNTER_STATUS, false);
                        if (lockcounterstatus) {
                            DashboardActivity.currentIndexDashboard = 1;
                            DashboardActivity.currentIndexPaneFragment = 2;
                            try {
                                Intent startMain = new Intent(Intent.ACTION_MAIN);
                                startMain.addCategory(Intent.CATEGORY_HOME);
                                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(startMain);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                            PrefSiempo.getInstance(CoreActivity.this).write(PrefSiempo
                                    .LOCK_COUNTER_STATUS, false);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class InnerRecevier extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    Log.e(TAG, "action:" + action + ",reason:" + reason);
                    if (!state.equalsIgnoreCase(SYSTEM_DIALOG_REASON_RECENT_APPS) && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        DashboardActivity.currentIndexDashboard = 1;
                        DashboardActivity.currentIndexPaneFragment = 2;
                    }
                    state = reason;
                }
            }
        }
    }

    public class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long receivedID = intent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
            DownloadManager mgr = (DownloadManager)
                    context.getSystemService(Context.DOWNLOAD_SERVICE);

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(receivedID);
            Cursor cur = mgr.query(query);
            int index = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (cur.moveToFirst()) {
                if (cur.getInt(index) == DownloadManager.STATUS_SUCCESSFUL) {
                    // do something
                    Log.e("download sucessfull", String.valueOf(receivedID));
                    String title = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    CoreApplication.getInstance().getRunningDownloadigFileList().remove(title);
                    Log.e("downloaded file", String.valueOf(title));
                } else if (cur.getInt(index) == DownloadManager.ERROR_UNKNOWN) {
                    String title = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    if (CoreApplication.getInstance().getRunningDownloadigFileList().contains(title)) {
                        CoreApplication.getInstance().getRunningDownloadigFileList().remove(title);
                    }
                } else if (cur.getInt(index) == DownloadManager.PAUSED_WAITING_TO_RETRY) {
                    String title = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    if (CoreApplication.getInstance().getRunningDownloadigFileList().contains(title)) {
                        CoreApplication.getInstance().getRunningDownloadigFileList().remove(title);
                    }
                }
            }
            cur.close();
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
//        Toast.makeText(getApplicationContext(), "DOUBLE TAP Event",Toast.LENGTH_SHORT).show();
        if (motionEvent.getAction() == 1) {
            if (PrefSiempo.getInstance(CoreActivity.this).read(PrefSiempo.IS_DND_ENABLE, false)) {
                changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_NONE);
            }

            if (PrefSiempo.getInstance(CoreActivity.this).read(PrefSiempo.IS_SLEEP_ENABLE, false)) {
                sleep();
            }
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

        // SSA-1960 START
        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.shortcuts_bottom, null);
        mBottomSheetDialog.setContentView(sheetView);

        ImageView shortcutSettings = sheetView.findViewById(R.id.shortcut_settings);
        shortcutSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CoreActivity.this, SettingsActivity.class);
                startActivity(intent);
                mBottomSheetDialog.closeOptionsMenu();
                mBottomSheetDialog.hide();
            }
        });

        ImageView shortcutWallpaper = sheetView.findViewById(R.id.shortcut_wallpaper);
        shortcutWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                CoreApplication.getInstance().downloadSiempoImages();
//                startActivity(new Intent(CoreActivity.this, ChooseBackgroundActivity.class));
                showWallPaperSelection();
                mBottomSheetDialog.closeOptionsMenu();
                mBottomSheetDialog.hide();
            }
        });

        ImageView shortcutDistractApp = sheetView.findViewById(R.id.shortcut_distract_app);
        shortcutDistractApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent junkFoodFlagIntent = new Intent(CoreActivity.this, JunkfoodFlaggingActivity.class);
                startActivity(junkFoodFlagIntent);
                mBottomSheetDialog.closeOptionsMenu();
                mBottomSheetDialog.hide();
            }
        });

        mBottomSheetDialog.show();

        // SSA-1960 END
    }


    public void showWallPaperSelection() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 10);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Intention screen Wallpaper selection
        if (requestCode == 10 || requestCode == 7) {
            switch (requestCode) {
                case 10:
                    if (resultCode == Activity.RESULT_OK) {
                        Uri uri = data.getData();

                        if (uri != null && !TextUtils.isEmpty(uri.toString())) {

//                            if (uri.toString().contains("com.google.android.apps.photos.contentprovider")) {
//                                return;
//                            }

                            if (uri.toString().contains("/storage")) {
                                String[] storagepath = uri.toString().split("/storage");
                                if (storagepath.length > 1) {
                                    String filePath = "/storage" + storagepath[1];
                                    Intent mUpdateBackgroundIntent = new Intent(this, UpdateBackgroundActivity.class);
                                    mUpdateBackgroundIntent.putExtra("imageUri", filePath);
                                    startActivityForResult(mUpdateBackgroundIntent, 3);
                                }
                            } else {
                                String id = uri.getLastPathSegment();

                                if (!TextUtils.isEmpty(id) && uri != null) {

                                    try {
                                        InputStream inputStream = this.getContentResolver().openInputStream(uri);
                                        File file = new File(this.getCacheDir(), id);
                                        writeFile(inputStream, file);
                                        String filePath = file.getAbsolutePath();

                                        if (filePath.contains("raw:")) {
                                            String[] downloadPath = filePath.split("raw:");
                                            if (downloadPath.length > 1) {
                                                filePath = downloadPath[1];
                                            }
                                        }

                                        Intent mUpdateBackgroundIntent = new Intent(this, UpdateBackgroundActivity.class);
                                        mUpdateBackgroundIntent.putExtra("imageUri", filePath);
                                        startActivityForResult(mUpdateBackgroundIntent, 3);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                    }
                    break;
                case 7:
                    if (resultCode == Activity.RESULT_OK) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();
                        Intent mUpdateBackgroundIntent = new Intent(this,
                                UpdateBackgroundActivity
                                        .class);
                        mUpdateBackgroundIntent.putExtra("imageUri", picturePath);
                        startActivityForResult(mUpdateBackgroundIntent, 3);
                    }
                    break;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    void writeFile(InputStream in, File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }


    public boolean checkNotificationAccessGranted(boolean onlyAccessCheck) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // If api level minimum 23
            // If notification policy access granted for this package
            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                if (!onlyAccessCheck) {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivityForResult(intent, 111);
                }
                return false;
            }

            return true;

        }
        return false;
    }

    public void changeInterruptionFiler(int interruptionFilter) {
        if (checkNotificationAccessGranted(true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mNotificationManager.setInterruptionFilter(interruptionFilter);
            }
        }
    }

    private boolean checkDeviceAdminAccessGranted(boolean onlyAccessCheck) {
        DevicePolicyManager policyManager = (DevicePolicyManager) CoreActivity.this
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminReceiver = new ComponentName(CoreActivity.this,
                ScreenOffAdminReceiver.class);
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (onlyAccessCheck) {
            return admin;
        } else {
            if (!admin) {
                // ask for device administration rights
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                ComponentName mDeviceAdmin = new ComponentName(CoreActivity.this, ScreenOffAdminReceiver.class);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.device_admin_description);
                startActivityForResult(intent, 112);
            }
            return false;
        }

    }

    private void sleep() {
        DevicePolicyManager policyManager = (DevicePolicyManager) CoreActivity.this
                .getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (checkDeviceAdminAccessGranted(true)) {
            policyManager.lockNow();
        }
    }

}
