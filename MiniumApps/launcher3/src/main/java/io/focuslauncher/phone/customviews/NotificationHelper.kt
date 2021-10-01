package io.focuslauncher.phone.customviews

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import io.focuslauncher.phone.app.CoreApplication.Companion.instance
import io.focuslauncher.phone.utils.PrefSiempo

@RequiresApi(api = Build.VERSION_CODES.O)
class NotificationHelper(ctx: Context?, packageName: String?) : ContextWrapper(ctx) {
    /**
     * Get the notification manager.
     *
     *
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private var manager: NotificationManager? = null
        private get() {
            if (field == null) {
                field = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }
            return field
        }

    /**
     * Send a notification.
     *
     * @param id           The ID of the notification
     * @param notification The notification object
     */
    fun notify(id: Int, notification: Notification?) {
        manager!!.notify(id, notification)
    }

    companion object {
        const val PRIMARY_CHANNEL = "default"
        const val SECONDARY_CHANNEL = "second"
    }

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param ctx The application context
     */
    init {
        val channelId = instance!!.listApplicationName[packageName]
        val channelName: CharSequence? = instance!!.listApplicationName[packageName]
        val importance: Int
        importance = if (!PrefSiempo.getInstance(ctx).read(PrefSiempo.ALLOW_PEAKING, true)) {
            NotificationManager.IMPORTANCE_DEFAULT
        } else {
            NotificationManager.IMPORTANCE_HIGH
        }
        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationChannel.vibrationPattern = longArrayOf(1000)
        manager!!.createNotificationChannel(notificationChannel)
        manager!!.createNotificationChannelGroup(NotificationChannelGroup("" + channelName, channelName))
    }
}