package io.focuslauncher.phone.customviews;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.RequiresApi;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.utils.PrefSiempo;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotificationHelper extends ContextWrapper {
    public static final String PRIMARY_CHANNEL = "default";
    public static final String SECONDARY_CHANNEL = "second";
    private NotificationManager manager;

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param ctx The application context
     */

    public NotificationHelper(Context ctx, String packageName) {
        super(ctx);

        String channelId = CoreApplication.Companion.getInstance().getListApplicationName().get(packageName);
        CharSequence channelName = CoreApplication.Companion.getInstance().getListApplicationName().get(packageName);


        int importance;
        if (!PrefSiempo.getInstance(ctx).read(PrefSiempo.ALLOW_PEAKING, true)) {
            importance = NotificationManager.IMPORTANCE_DEFAULT;
        } else {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannel.setVibrationPattern(new long[]{1000});
        getManager().createNotificationChannel(notificationChannel);
        getManager().createNotificationChannelGroup(new NotificationChannelGroup("" + channelName, channelName));

    }


    /**
     * Send a notification.
     *
     * @param id           The ID of the notification
     * @param notification The notification object
     */
    public void notify(int id, Notification notification) {
        getManager().notify(id, notification);
    }


    /**
     * Get the notification manager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
}