package io.focuslauncher.phone.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import io.focuslauncher.R;

/**
 * Created by hardik on 6/11/17.
 */

public class NotificationUtils extends ContextWrapper {

    public static final String ANDROID_CHANNEL_ID = "co.siempo.phone.ANDROID";
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";
    private NotificationManager mManager;

    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }

    public void createChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // create android channel
            NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                    ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            getManager().createNotificationChannel(androidChannel);
//
//            // create ios channel
//            NotificationChannel iosChannel = new NotificationChannel(IOS_CHANNEL_ID,
//                    IOS_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
//            iosChannel.enableLights(true);
//            iosChannel.enableVibration(true);
//            iosChannel.setLightColor(Color.GRAY);
//            iosChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
//            getManager().createNotificationChannel(iosChannel);
        }
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public Notification.Builder getAndroidChannelNotification(String title, String body) {
        Notification.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                    .setContentTitle(title)
                    .setSortKey(getResources().getString(R.string.lock_screen_label))
                    .setContentText(body)
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setAutoCancel(true);
        }
        return builder;

    }

    public Notification getForeGroundNotification(){
        NotificationCompat.Builder newBuilder = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("")
                .setPriority(NotificationManagerCompat.IMPORTANCE_LOW)
                .setCategory(Context.NOTIFICATION_SERVICE)
                .setAutoCancel(true);
        Notification newNotification = newBuilder.build();
        return newNotification;
    }

}
