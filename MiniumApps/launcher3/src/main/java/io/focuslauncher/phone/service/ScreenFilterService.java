package io.focuslauncher.phone.service;


import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.WindowManager;

import io.focuslauncher.phone.utils.NotificationUtils;
import io.focuslauncher.R;
import io.focuslauncher.Receivers.OrientationChangeReceiver;
import io.focuslauncher.phone.screenfilter.ScreenFilterPresenter;
import io.focuslauncher.phone.screenfilter.ScreenManager;
import io.focuslauncher.phone.screenfilter.SettingsModel;
import io.focuslauncher.phone.screenfilter.*;

import static io.focuslauncher.phone.utils.NotificationUtils.ANDROID_CHANNEL_ID;

public class ScreenFilterService extends Service implements ServiceLifeCycleController {
    public static final int VALID_COMMAND_START = 0;
    public static final int COMMAND_ON = 0;
    public static final int COMMAND_OFF = 1;
    public static final int COMMAND_PAUSE = 2;
    public static final int VALID_COMMAND_END = 2;

    public static final String BUNDLE_KEY_COMMAND = "cngu.bundle.key.COMMAND";

    private static final String TAG = "ScreenFilterService";
    private static final boolean DEBUG = true;

    private ScreenFilterPresenter mPresenter;
    private SettingsModel mSettingsModel;
    private OrientationChangeReceiver mOrientationReceiver;
    private NotificationUtils notificationUtils;
    @Override
    public void onCreate() {
        super.onCreate();
        notificationUtils = new NotificationUtils(this);
        if (DEBUG) Log.i(TAG, "onCreate");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationUtils.createChannels();
            NotificationCompat.Builder newBuilder = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("")
                    .setPriority(NotificationManagerCompat.IMPORTANCE_LOW)
                    .setCategory(Context.NOTIFICATION_SERVICE)
                    .setAutoCancel(true);
            Notification newNotification = newBuilder.build();
            startForeground(ScreenFilterPresenter.NOTIFICATION_ID, newNotification);
        }
        // Initialize helpers and managers
        Context context = this;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        ScreenFilterView view = new ScreenFilterView(context);
        WindowViewManager wvm = new WindowViewManager(windowManager);
        ScreenManager sm = new ScreenManager(this, windowManager);
        Notification.Builder nb = new Notification.Builder(this);
        FilterCommandFactory fcf = new FilterCommandFactory(this);
        FilterCommandParser fcp = new FilterCommandParser();

        // Wire MVP classes
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSettingsModel = new SettingsModel(context.getResources(), sharedPreferences);

        mPresenter = new ScreenFilterPresenter(view, mSettingsModel, this, context, wvm, sm, nb, fcf, fcp);

        // Make Presenter listen to settings changes and orientation changes
        mSettingsModel.openSettingsChangeListener();
        mSettingsModel.setOnSettingsChangedListener(mPresenter);

        registerOrientationReceiver(mPresenter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.i(TAG, String.format("onStartCommand(%s, %d, %d", intent, flags, startId));

        mPresenter.onScreenFilterCommand(intent);

        // Do not attempt to restart if the hosting process is killed by Android
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Prevent binding.
        return null;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.i(TAG, "onDestroy");

        mSettingsModel.closeSettingsChangeListener();
        unregisterOrientationReceiver();

        super.onDestroy();
    }

    @Override
    public void stop() {
        if (DEBUG) Log.i(TAG, "Received stop request");

        stopSelf();
    }

    private void registerOrientationReceiver(OrientationChangeReceiver.OnOrientationChangeListener listener) {
        if (mOrientationReceiver != null) {
            return;
        }

        IntentFilter orientationIntentFilter = new IntentFilter();
        orientationIntentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

        mOrientationReceiver = new OrientationChangeReceiver(this, listener);
        registerReceiver(mOrientationReceiver, orientationIntentFilter);
    }

    private void unregisterOrientationReceiver() {
        unregisterReceiver(mOrientationReceiver);
        mOrientationReceiver = null;
    }
}
