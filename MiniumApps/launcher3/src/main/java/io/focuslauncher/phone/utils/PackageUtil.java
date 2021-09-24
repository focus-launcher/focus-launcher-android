package io.focuslauncher.phone.utils;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.focuslauncher.R;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.app.Launcher3App;
import io.focuslauncher.phone.app.Launcher3App_;
import io.focuslauncher.phone.customviews.NotificationHelper;
import io.focuslauncher.phone.db.DBUtility;
import io.focuslauncher.phone.db.TableNotificationSms;
import io.focuslauncher.phone.db.TableNotificationSmsDao;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.models.AlarmData;
import io.focuslauncher.phone.models.AppMenu;
import io.focuslauncher.phone.models.CustomNotification;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.models.MainListItemType;
import io.focuslauncher.phone.service.AlarmBroadcast;

/**
 * Created by Shahab on 5/17/2017.
 */

public class PackageUtil {


    public static boolean isCallPackage(String pkg) {
        return pkg != null && !pkg.equalsIgnoreCase("") && (pkg.contains("telecom") || pkg.contains("dialer"));
    }

    public static boolean isMsgPackage(String pkg) {
        return pkg != null && !pkg.equalsIgnoreCase("") && (pkg.contains("messaging") || pkg.contains("com.android.mms"));
    }

    public static boolean isSiempoLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (defaultLauncher != null && defaultLauncher.activityInfo != null && defaultLauncher.activityInfo.packageName != null) {
            String defaultLauncherStr = defaultLauncher.activityInfo.packageName;
            return defaultLauncherStr.equals(context.getPackageName());
        }
        return false;

    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (Build.VERSION.SDK_INT >= 26) {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth() / 2, drawable.getIntrinsicHeight() / 2, Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } else {
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap();
                }
            }

            if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth() / 2, drawable.getIntrinsicHeight() / 2, Bitmap.Config.ARGB_8888);
            }
        }
        return bitmap;
    }


    public static int getIdByPackage(String pkg) {
        int ret = 0;
        for (int i = 0; i < pkg.length(); i++) {
            ret += ((i + 1) * pkg.charAt(i));
        }
        return ret;
    }

    public static void checkPermission(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            if (!Settings.canDrawOverlays(context)) {
                Toast.makeText(context, R.string.msg_overlay_settings, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            }
        }


    }

    /**
     * Match signature of application to identify that if it is signed by system
     * or not.
     *
     * @param packageName package of application. Can not be blank.
     * @return <code>true</code> if application is signed by system certificate,
     * otherwise <code>false</code>
     */
    public static boolean isSystemApp(String packageName, Context context) {
        try {
            PackageManager mPackageManager = context.getPackageManager();
            // Get packageinfo for target application

            ApplicationInfo ai = mPackageManager.getApplicationInfo(packageName, 0);
            return ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0);


        } catch (PackageManager.NameNotFoundException e) {
            CoreApplication.Companion.getInstance().logException(e);
            return false;
        }
    }


    public static void createNotification(Context context, CustomNotification customNotification) {
        int notificationListSize = customNotification.getNotificationSms().size();
        if (notificationListSize != 0) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String packageName = customNotification.getNotificationSms().get(0).getPackageName();
            if (packageName != null && !packageName.equalsIgnoreCase("android")) {
                int icon = customNotification.getNotificationSms().get(0).getApp_icon();
                if (Build.VERSION.SDK_INT >= 26) {
                    NotificationHelper notificationHelper = new NotificationHelper(context, packageName);
                    String strChannelName = CoreApplication.Companion.getInstance().getListApplicationName().get(packageName);
                    Bitmap bitmap = CoreApplication.Companion.getInstance().getBitmapFromMemCache(packageName);
                    Notification newMessageNotification = new NotificationCompat.Builder(context, strChannelName)
                            .setSmallIcon(R.drawable.ic_focus_launcher_notification)
                            .setContentTitle(strChannelName)
                            .setContentText("")
                            .setLargeIcon(bitmap)
                            .setGroup(strChannelName)
                            .build();
                    notificationHelper.notify(customNotification.getNotificationSms().get(0).getApp_icon(), newMessageNotification);
                    for (TableNotificationSms tableNotificationSms : customNotification.getNotificationSms()) {
                        NotificationCompat.Builder builder = getNotification(context, tableNotificationSms);
                        notificationHelper.notify(tableNotificationSms.getId().intValue(), builder.build());
                        notificationHelper.notify(tableNotificationSms.getApp_icon(), newMessageNotification);
                    }
                } else {
                    if (notificationListSize > 1) {
                        if (Build.VERSION.SDK_INT >= 24) {
                            String strChannelName = CoreApplication.Companion.getInstance().getListApplicationName().get(packageName);
                            Bitmap bitmap = CoreApplication.Companion.getInstance().getBitmapFromMemCache(packageName);
                            Notification newMessageNotification = new NotificationCompat.Builder(context, strChannelName)
                                    .setSmallIcon(R.drawable.ic_focus_launcher_notification)
                                    .setContentTitle(strChannelName)
                                    .setContentText("")
                                    .setLargeIcon(bitmap)
                                    .setGroup(strChannelName)
                                    .build();
                            if (notificationManager != null) {
                                notificationManager.notify(customNotification.getNotificationSms().get(0).getApp_icon(), newMessageNotification);
                            }
                            for (TableNotificationSms tableNotificationSms : customNotification.getNotificationSms()) {
                                NotificationCompat.Builder builder = getNotification(context, tableNotificationSms);
                                notificationManager.notify(tableNotificationSms.getId().intValue(), builder.build());
                                notificationManager.notify(tableNotificationSms.getApp_icon(), newMessageNotification);

                            }
                        } else {
                            generateBelow24(context, notificationManager, customNotification.getNotificationSms(), packageName);
                        }
                    } else {
                        NotificationCompat.Builder builder = getNotification(context, customNotification.getNotificationSms().get(0));
                        if (notificationManager != null) {
                            notificationManager.notify(icon, builder.build());
                        }
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    static void createNotificationChannel(Context context, NotificationManager notificationManager, String packageName) {
        CharSequence channelName = CoreApplication.Companion.getInstance().getListApplicationName().get(packageName);
        int importance;
        if (!PrefSiempo.getInstance(context).read(PrefSiempo.ALLOW_PEAKING, true)) {
            importance = NotificationManager.IMPORTANCE_DEFAULT;
        } else {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }
        NotificationChannel notificationChannel = new NotificationChannel(packageName, channelName, importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannel.setVibrationPattern(new long[]{1000});
        notificationManager.createNotificationChannel(notificationChannel);
    }


    /**
     * Create Notification after parsing the notification from notification listener
     *
     * @param notification database object
     * @param context      user context
     * @param icon         application icon integer format
     */
    public synchronized static void recreateNotification(TableNotificationSms notification, Context context, int icon) {
        try {
            if (notification.getPackageName() != null && !notification.getPackageName().equalsIgnoreCase("android")) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                String applicationNameFromPackageName = CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(notification.getPackageName());
                if (Build.VERSION.SDK_INT >= 26) {
                    Tracer.i("Tracking notification greater");
                    NotificationChannel notificationChannel = createChannel(context,
                            applicationNameFromPackageName);
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(notificationChannel);
                        Tracer.i("Tracking createNotificationChannel");
                    }
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(applicationNameFromPackageName, applicationNameFromPackageName));
                        Tracer.i("Tracking createNotificationChannelGroup");
                    }
                }
                NotificationCompat.Builder groupBuilder = createGroupNotification(context, notification, applicationNameFromPackageName);


                NotificationCompat.Builder builder = getNotification(context, notification);

                if (notificationManager != null) {
                    notificationManager.notify(icon, groupBuilder.build());
                }
                if (notificationManager != null) {
                    notificationManager.notify(notification.getId().intValue(), builder.build());
                }
            }
        } catch (Exception e) {
            CoreApplication.Companion.getInstance().logException(e);
            e.printStackTrace();
        }

    }


    /**
     * Generate Notification object
     *
     * @param context      selected context
     * @param notification database object
     * @return notification builder object
     */
    private static NotificationCompat.Builder getNotification(Context context, TableNotificationSms notification) {

        Tracer.i("Tracking getNotification1");
        String applicationNameFromPackageName = CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(notification.getPackageName());
        Tracer.i("Tracking getNotification2");
        int priority = !PrefSiempo.getInstance(context).read(PrefSiempo.ALLOW_PEAKING, true) ? Notification.PRIORITY_DEFAULT : Notification.PRIORITY_HIGH;
        Tracer.i("Tracking getNotification3");
        NotificationCompat.Builder b
                = new NotificationCompat.Builder(context, applicationNameFromPackageName);
        b.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        b.setDefaults(Notification.DEFAULT_LIGHTS);
        PendingIntent contentIntent = getPendingIntent(context, notification);
        Tracer.i("Tracking getNotification4");
        Bitmap bitmap = notification.getUser_icon() != null ? UIUtils.convertBytetoBitmap(notification.getUser_icon()) : null;
        Tracer.i("Tracking getNotification5");
        DateFormat sdf = new SimpleDateFormat(getTimeFormat(context), Locale.getDefault());
        String time = sdf.format(notification.get_date());
        Tracer.i("Tracking getNotification6");
        String title = getNotificationTitle(notification.get_contact_title(), notification.getPackageName(), context);

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_card);
        try {
            Drawable drawable = context.getPackageManager().getApplicationIcon(notification.getPackageName());
            contentView.setImageViewBitmap(R.id.imgAppIcon, drawableToBitmap(drawable));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Tracer.i("Tracking getNotification load exception");
        }

        contentView.setImageViewBitmap(R.id.imgUserImage, bitmap);
        contentView.setTextViewText(R.id.txtUserName, title);
        contentView.setTextViewText(R.id.txtMessage, notification.get_message());
        contentView.setTextViewText(R.id.txtTime, time);
        contentView.setTextViewText(R.id.txtAppName, applicationNameFromPackageName);
        b.setAutoCancel(true)
                .setGroup(applicationNameFromPackageName)
                .setWhen(notification.get_date().getTime())
                .setSmallIcon(R.drawable.ic_focus_launcher_notification)
                .setPriority(priority)
                .setContentTitle(title)
                .setContentText(notification.get_message())
                .setContentIntent(contentIntent)
                .setCustomContentView(contentView)
                .setCustomBigContentView(contentView)
                .setLights(Color.MAGENTA, 500, 500)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentInfo("Info");
        return b;
    }

    /**
     * Navigation for user flow.
     *
     * @param context      current context
     * @param notification database object
     * @return
     */
    @Nullable
    public static PendingIntent getPendingIntent(Context context, TableNotificationSms notification) {
        Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(notification.getPackageName());
        PendingIntent contentIntent = null;
        if (launchIntentForPackage != null) {
            int requestID = (int) System.currentTimeMillis();
            contentIntent = PendingIntent.getActivity(context, requestID, launchIntentForPackage, PendingIntent.FLAG_UPDATE_CURRENT);
            launchIntentForPackage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        return contentIntent;
    }

    /**
     * Generate Notification Group object
     *
     * @param context        current context
     * @param notification   current database object
     * @param strChannelName selected channel name
     * @return notification builder object for group
     */
    private static NotificationCompat.Builder createGroupNotification(Context context, TableNotificationSms notification, String strChannelName) {

        Tracer.i("Tracking createGroupNotification1");
        Bitmap bitmap = notification.getUser_icon() != null ? UIUtils.convertBytetoBitmap(notification.getUser_icon()) : null;
        Tracer.i("Tracking createGroupNotification2");
        List<TableNotificationSms> notificationSms = DBUtility.getNotificationDao().queryBuilder()
                .where(TableNotificationSmsDao.Properties.PackageName.eq(notification.getPackageName())).list();
        Tracer.i("Tracking createGroupNotification3");
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        //In case of more than one notification, instead of showing contact
        // image bitmap the app icon will be used
        if (notificationSms.size() > 1) {
            if (notification.getPackageName() != null) {
                bitmap = CoreApplication.Companion.getInstance().getBitmapFromMemCache
                        (notification.getPackageName());
            }
        }


        for (int i = 0; i < notificationSms.size(); i++) {
            String title = getNotificationTitle
                    (notificationSms.get(i).get_contact_title(), notification
                            .getPackageName(), context);
            inboxStyle.addLine(title + ": " + notificationSms.get(i).get_message());
        }
        inboxStyle.setSummaryText("You have " + notificationSms.size() + " unread message");
        PendingIntent pendingIntent = getPendingIntent(context, notification);
        NotificationCompat.Builder groupBuilder =
                new NotificationCompat.Builder(context, strChannelName)
                        .setContentTitle(strChannelName)
                        .setContentText(notificationSms.size() + " New message")
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.ic_focus_launcher_notification)
                        .setGroupSummary(false)
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setGroup(strChannelName)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);
        groupBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            groupBuilder.setStyle(inboxStyle);
            Tracer.i("Tracking createGroupNotification5");
        } else {
//            Tracer.i("Tracking createGroupNotification4");
//            groupBuilder.setStyle(new NotificationCompat.BigTextStyle());
        }
        return groupBuilder;
    }


    private static NotificationCompat.Builder generateBelow24(Context context, NotificationManager notificationManager, ArrayList<TableNotificationSms> notificationSms, String packageName) {
        int SUMMARY_ID;
        Bitmap bitmap = null;
        //In case of more than one notification, instead of showing contact
        // image bitmap the app icon will be used
        if (notificationSms.size() > 1) {
            if (packageName != null) {
                bitmap = CoreApplication.Companion.getInstance().getBitmapFromMemCache(packageName);
            }
        }
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        SUMMARY_ID = notificationSms.get(0).getApp_icon();
        for (int i = 0; i < notificationSms.size(); i++) {
            String title = getNotificationTitle
                    (notificationSms.get(i).get_contact_title(), packageName, context);
            inboxStyle.addLine(title + ": " + notificationSms.get(i).get_message());
        }
        inboxStyle.setSummaryText("You have " + notificationSms.size() + " unread message");
        PendingIntent pendingIntent = getPendingIntent(context, notificationSms.get(0));
        String strChannelName = CoreApplication.Companion.getInstance().getListApplicationName().get(packageName);
        NotificationCompat.Builder groupBuilder =
                new NotificationCompat.Builder(context, strChannelName)
                        .setContentTitle(strChannelName)
                        .setContentText(notificationSms.size() + " New message")
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.ic_focus_launcher_notification)
                        .setGroupSummary(true)
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setGroup(strChannelName)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);
        groupBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        groupBuilder.setStyle(inboxStyle);

        for (int i = 0; i < notificationSms.size(); i++) {
            NotificationCompat.Builder b = getNotification(context, notificationSms.get(i));
            notificationManager.notify(notificationSms.get(i).getId().intValue(), b.build());
            notificationManager.notify(SUMMARY_ID, groupBuilder.build());
        }
        return groupBuilder;
    }

    public static String getTimeFormat(Context context) {
        String format;
        boolean is24hourformat = android.text.format.DateFormat.is24HourFormat(context);

        if (is24hourformat) {
            format = "HH:mm";
        } else {
            format = "hh:mm a";
        }
        return format;
    }


    /**
     * this method cancel the alarm.
     *
     * @param id
     */
    private static void cancelAlarm(int id) {
        try {
            if (isAlarmEnable(id)) {
                Log.d("Alarm", "Cancel Enabled Alarm :" + id);
                Intent intentToFire = new Intent(CoreApplication.Companion.getInstance(), AlarmBroadcast.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(CoreApplication.Companion.getInstance(), id, intentToFire, 0);
                AlarmManager alarmManager = (AlarmManager) CoreApplication.Companion.getInstance().getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.cancel(alarmIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isAlarmEnable(int id) {
        if (CoreApplication.Companion.getInstance() != null) {
            Intent intentToFire = new Intent(CoreApplication.Companion.getInstance(), AlarmBroadcast.class);
            return (PendingIntent.getBroadcast(CoreApplication.Companion.getInstance(), id, intentToFire, PendingIntent.FLAG_NO_CREATE) != null);
        }
        return false;
    }

    public static void enableDisableAlarm(Calendar calendar, int id) {
        try {
            if (id == -1) {
                PackageUtil.cancelAlarm(0);
            }
            if (id != -1 && CoreApplication.Companion.getInstance() != null) {

                Intent intentToFire = new Intent(CoreApplication.Companion.getInstance(), AlarmBroadcast.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(CoreApplication.Companion.getInstance(), id, intentToFire, 0);
                AlarmManager alarmManager = (AlarmManager) CoreApplication.Companion.getInstance().getSystemService(Context.ALARM_SERVICE);
                long time = calendar.getTimeInMillis();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Wakes up the device in Doze Mode
                    if (alarmManager != null) {
                        Log.d("Alarm", "Time:" + calendar.getTime());
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, alarmIntent);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Wakes up the device in Idle Mode
                    if (alarmManager != null) {
                        Log.d("Alarm", "Time:" + calendar.getTime());
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, alarmIntent);
                    }
                } else {
                    if (alarmManager != null) {
                        Log.d("Alarm", "Time:" + calendar.getTime());
                        alarmManager.set(AlarmManager.RTC_WAKEUP, time, alarmIntent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            CoreApplication.Companion.getInstance().logException(e);
        }
    }

    public static Calendar getOnlyAt(Context context) {
        String timeString;
        if (android.text.format.DateFormat.is24HourFormat(context)) {
            timeString = "HH:mm";
        } else {
            timeString = "hh:mm a";
        }
        SimpleDateFormat df = new SimpleDateFormat(timeString, Locale.getDefault());
        String strTimeData = PrefSiempo.getInstance(context).read(PrefSiempo
                .ONLY_AT, "12:01");
        String strTime[] = strTimeData.split(",");

        Calendar calendar1 = Calendar.getInstance();
        if (strTime.length == 1) {
            String str1 = strTime[0];
            int setMinute, setHours;

            setHours = Integer.parseInt(str1.split(":")[0]);
            setMinute = Integer.parseInt(str1.split(":")[1]);

            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
        } else if (strTime.length == 2) {
            Calendar currentTime = Calendar.getInstance();

            int systemMinute, setMinute, systemHours, setHours;

            ArrayList<AlarmData> hourList = new ArrayList<>();

            systemHours = currentTime.get(Calendar.HOUR_OF_DAY);
            systemMinute = currentTime.get(Calendar.MINUTE);

            String str1 = strTime[0];
            setHours = Integer.parseInt(str1.split(":")[0]);
            setMinute = Integer.parseInt(str1.split(":")[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            hourList.add(new AlarmData(setHours, setMinute, df.format(calendar1.getTime())));
            String str2 = strTime[1];
            setHours = Integer.parseInt(str2.split(":")[0]);
            setMinute = Integer.parseInt(str2.split(":")[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            hourList.add(new AlarmData(setHours, setMinute, df.format(calendar1.getTime())));
            try {
                Collections.sort(hourList, new PackageUtil.HoursComparator());
                for (int i = 0; i < hourList.size(); i++) {
                    if (hourList.get(i).getHours() == systemHours) {
                        if (hourList.get(i).getMinute() > systemMinute) {
                            String str4 = strTime[i];
                            setHours = Integer.parseInt(str4.split(":")[0]);
                            setMinute = Integer.parseInt(str4.split(":")[1]);
                            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
                            calendar1.set(Calendar.MINUTE, setMinute);
                            break;
                        } else {
                            calendar1.set(Calendar.HOUR_OF_DAY, hourList.get(0).getHours());
                            calendar1.set(Calendar.MINUTE, hourList.get(0).getMinute());
                        }
                    } else if (hourList.get(i).getHours() > systemHours) {
                        String str4 = strTime[i];
                        setHours = Integer.parseInt(str4.split(":")[0]);
                        setMinute = Integer.parseInt(str4.split(":")[1]);
                        calendar1.set(Calendar.HOUR_OF_DAY, setHours);
                        calendar1.set(Calendar.MINUTE, setMinute);
                        break;
                    } else {
                        calendar1.set(Calendar.HOUR_OF_DAY, hourList.get(0).getHours());
                        calendar1.set(Calendar.MINUTE, hourList.get(0).getMinute());
                    }
                }
            } catch (Exception e) {
                CoreApplication.Companion.getInstance().logException(e);
            }

        } else if (strTime.length == 3) {
            Calendar currentTime = Calendar.getInstance();

            ArrayList<AlarmData> hourList = new ArrayList<>();

            int systemMinute, setMinute, systemHours, setHours;
            systemHours = currentTime.get(Calendar.HOUR_OF_DAY);
            systemMinute = currentTime.get(Calendar.MINUTE);


            String str1 = strTime[0];
            setHours = Integer.parseInt(str1.split(":")[0]);
            setMinute = Integer.parseInt(str1.split(":")[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            hourList.add(new AlarmData(setHours, setMinute, df.format(calendar1.getTime())));

            String str2 = strTime[1];
            setHours = Integer.parseInt(str2.split(":")[0]);
            setMinute = Integer.parseInt(str2.split(":")[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            hourList.add(new AlarmData(setHours, setMinute, df.format(calendar1.getTime())));

            String str3 = strTime[2];
            setHours = Integer.parseInt(str3.split(":")[0]);
            setMinute = Integer.parseInt(str3.split(":")[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            hourList.add(new AlarmData(setHours, setMinute, df.format(calendar1.getTime())));
            try {
                Collections.sort(hourList, new PackageUtil.HoursComparator());
                for (int i = 0; i < hourList.size(); i++) {
                    if (hourList.get(i).getHours() == systemHours) {
                        if (hourList.get(i).getMinute() > systemMinute) {
                            String str4 = strTime[i];
                            setHours = Integer.parseInt(str4.split(":")[0]);
                            setMinute = Integer.parseInt(str4.split(":")[1]);
                            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
                            calendar1.set(Calendar.MINUTE, setMinute);
                            break;
                        } else {
                            calendar1.set(Calendar.HOUR_OF_DAY, hourList.get(0).getHours());
                            calendar1.set(Calendar.MINUTE, hourList.get(0).getMinute());
                        }
                    } else if (hourList.get(i).getHours() > systemHours) {
                        String str4 = strTime[i];
                        setHours = Integer.parseInt(str4.split(":")[0]);
                        setMinute = Integer.parseInt(str4.split(":")[1]);
                        calendar1.set(Calendar.HOUR_OF_DAY, setHours);
                        calendar1.set(Calendar.MINUTE, setMinute);
                        break;
                    } else {
                        calendar1.set(Calendar.HOUR_OF_DAY, hourList.get(0).getHours());
                        calendar1.set(Calendar.MINUTE, hourList.get(0).getMinute());
                    }
                }
            } catch (Exception e) {
                CoreApplication.Companion.getInstance().logException(e);
            }
        }
        calendar1.set(Calendar.SECOND, 0);
        // If the time being set is past time, android system will keep on creating alarms
        // Hence in order to prevent this, check with current system time, and if the time is past
        // then add 24 hours to it.
        if (calendar1.getTimeInMillis() < System.currentTimeMillis()) {
            calendar1.add(Calendar.DATE, 1);
        }
        return calendar1;
    }

    public static Calendar batchMode(Context context) {
        int batchTime = PrefSiempo.getInstance(context).read(PrefSiempo
                .BATCH_TIME, 15);
        Calendar calendar = Calendar.getInstance();
        int hour;
        int minute = calendar.get(Calendar.MINUTE);
        if (batchTime == 15) {
            if (minute >= 0 && minute < 15) {
                calendar.set(Calendar.MINUTE, 15);
            } else if (minute >= 15 && minute < 30) {
                calendar.set(Calendar.MINUTE, 30);
            } else if (minute >= 30 && minute < 45) {
                calendar.set(Calendar.MINUTE, 45);
            } else if (minute >= 45 && minute < 60) {
                calendar.set(Calendar.MINUTE, 60);
            }
        } else if (batchTime == 30) {
            if (minute >= 0 && minute < 30) {
                calendar.set(Calendar.MINUTE, 30);
            } else if (minute >= 30 && minute < 60) {
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                calendar.set(Calendar.MINUTE, 0);
            }
        } else if (batchTime == 1) {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            calendar.set(Calendar.MINUTE, 0);
        } else if (batchTime == 2) {
            calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            int intHour = forTwoHours(hour);
            calendar.set(Calendar.HOUR_OF_DAY, intHour);
            calendar.set(Calendar.MINUTE, 0);
        } else if (batchTime == 4) {
            calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            int intHour = forFourHours(hour);
            calendar.set(Calendar.HOUR_OF_DAY, intHour);
            calendar.set(Calendar.MINUTE, 0);
        }
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private static NotificationChannel createChannel(Context context, String channelName) {
        int priority = !PrefSiempo.getInstance(context).read(PrefSiempo.ALLOW_PEAKING, true) ? NotificationManager.IMPORTANCE_DEFAULT : NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel chan = new NotificationChannel(channelName,
                channelName, priority);
        chan.setLightColor(Color.BLUE);
        chan.setDescription("");
        chan.enableLights(true);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        chan.setVibrationPattern(new long[]{1000});
        return chan;
    }


    /**
     * Below function is used to get contact name from contact number store in contact list
     */
    private static String nameFromContactNumber(String number, Context context) {
        String contactName;
        try {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI}, null, null, null);


            if (cursor != null && cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                cursor.close();
            } else {
                contactName = number;
            }
        } catch (Exception e) {
            contactName = "";
            e.printStackTrace();
        }
        return contactName;
    }

    public static String getNotificationTitle(String notificationTitle, String notificationPackageName, Context context) {
        String title = "";
        if (!TextUtils.isEmpty(notificationTitle)) {
            title = notificationTitle;
            String smsPackage = Telephony.Sms.getDefaultSmsPackage(context);
            if (!TextUtils.isEmpty(notificationPackageName) && notificationPackageName.equalsIgnoreCase(smsPackage)) {
                title = nameFromContactNumber(notificationTitle, context);
            }
        }
        return title;
    }

    public static void appSettings(Context context, String packageName) {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            context.startActivity(intent);
        } catch (Exception e) {
            Tracer.e(e, e.getMessage());
            CoreApplication.Companion.getInstance().logException(e);
        }
    }


    public static ArrayList<MainListItem> getToolsMenuData(Context context, ArrayList<MainListItem> items) {


        //create an empty array to hold the list of sorted Customers
        ArrayList<MainListItem> sortedTools = new ArrayList<>();

        //get the JSON array of the ordered of sorted customers
        String jsonListOfSortedToolsId = PrefSiempo.getInstance(context).read(PrefSiempo.SORTED_MENU, "");
        Tracer.d("MenuItem",jsonListOfSortedToolsId);
        Log.d("MenuItem", jsonListOfSortedToolsId);

        //check for null
        if (!jsonListOfSortedToolsId.isEmpty()) {

            //convert onNoteListChangedJSON array into a List<Long>
            Gson gson = new GsonBuilder()
                    .setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
            List<Long> listOfSortedCustomersId = gson.fromJson(jsonListOfSortedToolsId, new TypeToken<List<Long>>() {
            }.getType());

            //build sorted list
            if (listOfSortedCustomersId != null && listOfSortedCustomersId.size() > 0) {
                for (Long id : listOfSortedCustomersId) {
                    for (MainListItem tools : items) {
                        if (tools.getId() == id) {
                            sortedTools.add(tools);
                            items.remove(tools);
                            break;
                        }
                    }
                }
            }

            //if there are still tools that were not in the sorted list
            //maybe they were added after the last drag and drop
            //add them to the sorted list
//            if (items.size() > 0) {
//                sortedTools.addAll(items);
//            }

            return sortedTools;
        } else {
            return items;
        }
    }


    public static ArrayList<MainListItem> getToolsMenuData(ArrayList<MainListItem> items) {


        //create an empty array to hold the list of sorted Customers
        ArrayList<MainListItem> sortedTools = new ArrayList<>();

        //get the JSON array of the ordered of sorted customers
        String jsonListOfSortedToolsId = PrefSiempo.getInstance(Launcher3App_.getInstance().getApplicationContext()).read(PrefSiempo.SORTED_MENU, "");
        Tracer.d("MenuItem",jsonListOfSortedToolsId);
        Log.d("MenuItem", jsonListOfSortedToolsId);

        //check for null
        if (!jsonListOfSortedToolsId.isEmpty()) {

            //convert onNoteListChangedJSON array into a List<Long>
            Gson gson = new GsonBuilder()
                    .setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
            List<Long> listOfSortedCustomersId = gson.fromJson(jsonListOfSortedToolsId, new TypeToken<List<Long>>() {
            }.getType());

            //build sorted list
            if (listOfSortedCustomersId != null && listOfSortedCustomersId.size() > 0) {
                for (Long id : listOfSortedCustomersId) {
                    for (MainListItem tools : items) {
                        if (tools.getId() == id) {
                            sortedTools.add(tools);
                            items.remove(tools);
                            break;
                        }
                    }
                }
            }

            //if there are still tools that were not in the sorted list
            //maybe they were added after the last drag and drop
            //add them to the sorted list
//            if (items.size() > 0) {
//                sortedTools.addAll(items);
//            }

            return sortedTools;
        } else {
            return items;
        }
    }



    /**
     * Changes for SSA-1770-Changes
     *
     * @param context
     * @param isFalse-check Whether list is fetched for empty FavItemsList or not.
     * @return
     */
    public static ArrayList<MainListItem> getFavoriteList(Context context, boolean isFalse) {
        ArrayList<MainListItem> sortedFavoriteList = new ArrayList<>();
        try {
            ArrayList<MainListItem> appList = getAppList(context);
            if (appList.size() > 0) {
                String jsonListOfSortedFavorites = PrefSiempo.getInstance(context).read(PrefSiempo.FAVORITE_SORTED_MENU, "");
                List<String> listOfSortFavoritesApps;
                if (!TextUtils.isEmpty(jsonListOfSortedFavorites)) {
                    listOfSortFavoritesApps = syncFavoriteList(jsonListOfSortedFavorites, context);
                    sortedFavoriteList = sortFavoriteAppsByPosition(listOfSortFavoritesApps, appList, context);
                } else {
                    if (isFalse) {
                        sortedFavoriteList = addDefaultFavoriteAppsSetting(context, appList);
                    } else {
                        sortedFavoriteList = addDefaultFavoriteApps(context, appList);
                    }

                }
            } else {
                sortedFavoriteList = addDefaultFavoriteApps(context, appList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sortedFavoriteList;
    }

    /**
     * Changes for SSA-1770-Changes
     *
     * @param context
     * @param isFalse-check Whether list is fetched for empty FavItemsList or not.
     * @return
     */
    public static ArrayList<MainListItem> getFavoriteList(boolean isFalse) {
        ArrayList<MainListItem> sortedFavoriteList = new ArrayList<>();
        try {
            Context context = Launcher3App_.getInstance().getApplicationContext();
            ArrayList<MainListItem> appList = getAppList(context);
            if (appList.size() > 0) {
                String jsonListOfSortedFavorites = PrefSiempo.getInstance(context).read(PrefSiempo.FAVORITE_SORTED_MENU, "");
                List<String> listOfSortFavoritesApps;
                if (!TextUtils.isEmpty(jsonListOfSortedFavorites)) {
                    listOfSortFavoritesApps = syncFavoriteList(jsonListOfSortedFavorites, context);
                    sortedFavoriteList = sortFavoriteAppsByPosition(listOfSortFavoritesApps, appList, context);
                } else {
                    if (isFalse) {
                        sortedFavoriteList = addDefaultFavoriteAppsSetting(context, appList);
                    } else {
                        sortedFavoriteList = addDefaultFavoriteApps(context, appList);
                    }

                }
            } else {
                sortedFavoriteList = addDefaultFavoriteApps(context, appList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sortedFavoriteList;
    }



    private static ArrayList<MainListItem> getAppList(Context context) {
        ArrayList<MainListItem> appList = new ArrayList<>();
        try {
            List<String> installedPackageList = CoreApplication.Companion.getInstance().getPackagesList();

            //Added as a part of SSA-1483, in case of installed package
            if (installedPackageList.isEmpty()) {
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);

                for (ResolveInfo resolveInfo : pkgAppsList) {
                    installedPackageList.add(resolveInfo.activityInfo
                            .packageName);
                }


            }
            for (String resolveInfo : installedPackageList) {
                if (!resolveInfo.equalsIgnoreCase(context.getPackageName())) {
                    if (!TextUtils.isEmpty(resolveInfo)) {
                        String strAppName = CoreApplication.Companion.getInstance().getListApplicationName().get(resolveInfo);
                        if (strAppName == null) {
                            strAppName = CoreApplication.Companion.getInstance().getApplicationNameFromPackageName(resolveInfo);
                        }
                        if (!TextUtils.isEmpty(strAppName)) {
                            appList.add(new MainListItem(-1, "" + strAppName, resolveInfo));
                        }
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appList;
    }


    private static List<String> syncFavoriteList(String jsonListOfSortedFavorites, Context context) {
        Set<String> favorite_List_App = PrefSiempo.getInstance(context).read(PrefSiempo.FAVORITE_APPS, new HashSet<String>());
        List<String> listOfSortFavoritesApps = new ArrayList<>();
        //Below logic is use to sync FAVORITE_SORTED_MENU Preference AND FAVORITE_APPS LIST
        if (!jsonListOfSortedFavorites.isEmpty()) {

            //convert onNoteListChangedJSON array into a List<Long>
            Gson gson1 = new Gson();
            listOfSortFavoritesApps = gson1.fromJson(jsonListOfSortedFavorites, new TypeToken<List<String>>() {
            }.getType());


            for (String packageName : favorite_List_App) {
                if (!listOfSortFavoritesApps.contains(packageName)) {
                    boolean isEnable = UIUtils.isAppInstalledAndEnabled(context, packageName);
                    if (isEnable) {
                        for (int j = 0; j < listOfSortFavoritesApps.size(); j++) {
                            if (TextUtils.isEmpty(listOfSortFavoritesApps.get(j).trim())) {
                                listOfSortFavoritesApps.set(j, packageName);
                                break;
                            }
                        }
                    }
                }
            }
            Gson gson2 = new Gson();
            String jsonListOfFavoriteApps = gson2.toJson(listOfSortFavoritesApps);
            PrefSiempo.getInstance(context).write(PrefSiempo.FAVORITE_SORTED_MENU, jsonListOfFavoriteApps);
        }
        return listOfSortFavoritesApps;

    }


    private static ArrayList<MainListItem> sortFavoriteAppsByPosition(List<String> listOfSortFavoritesApps, List<MainListItem> appList, Context context) {

        ArrayList<MainListItem> sortedFavoriteList = new ArrayList<>();
        //build sorted list
        if (listOfSortFavoritesApps != null && listOfSortFavoritesApps.size() > 0) {
            for (String packageName : listOfSortFavoritesApps) {
                if (TextUtils.isEmpty(packageName)) {
                    MainListItem m = new MainListItem(-10, "", "");
                    sortedFavoriteList.add(m);
                } else {
                    for (MainListItem items : appList) {
                        if (!TextUtils.isEmpty(items.getPackageName()) && items.getPackageName().toLowerCase().trim().equalsIgnoreCase(packageName.toLowerCase().trim())) {
                            sortedFavoriteList.add(items);
                            break;
                        }
                    }
                }
            }
            int remainingFavoriteList = 12 - sortedFavoriteList.size();
            for (int i = 0; i < remainingFavoriteList; i++) {
                MainListItem m = new MainListItem(-10, "", "");
                sortedFavoriteList.add(m);
            }
        }
        return sortedFavoriteList;
    }

    public static ArrayList<MainListItem> getListOfBlankAndFavoriteApps(Context context, List<MainListItem> appList) {
        ArrayList<MainListItem> sortedFavoriteList = new ArrayList<>();

        Set<String> favorite_List_App = PrefSiempo.getInstance(context).read(PrefSiempo.FAVORITE_APPS, new HashSet<String>());

        for (String packageName : favorite_List_App) {
            for (MainListItem items : appList) {
                if (!TextUtils.isEmpty(items.getPackageName()) && items.getPackageName().toLowerCase().trim().equalsIgnoreCase(packageName.trim())) {
                    sortedFavoriteList.add(items);
                }
            }
        }

        int remainingFavoriteList = 12 - sortedFavoriteList.size();
        for (int i = 0; i < remainingFavoriteList; i++) {
            MainListItem m = new MainListItem(-10, "", "");
            sortedFavoriteList.add(m);
        }
        return sortedFavoriteList;
    }

    private static ArrayList<MainListItem> addDefaultFavoriteApps(Context context,
                                                                  List<MainListItem> appList) {

        LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        Set<String> list;
        list = PrefSiempo.getInstance(context).read(PrefSiempo.FAVORITE_APPS, new HashSet<String>());

        ArrayList<MainListItem> items = new ArrayList<>();
        String CHROME_PACKAGE = "com.android.chrome", SYSTEM_SETTING = "com.android.settings";


        for (int i = 0; i < appList.size(); i++) {
            if (!TextUtils.isEmpty(appList.get(i).getPackageName())) {
                if (appList.get(i).getPackageName().equalsIgnoreCase(CHROME_PACKAGE) || appList.get(i).getPackageName().equalsIgnoreCase(SYSTEM_SETTING)) {
                    boolean isEnable = UIUtils.isAppInstalledAndEnabled(context, appList.get(i).getPackageName());
                    if (isEnable) {
                        items.add(appList.get(i));
                    }
                }
            }
        }


        int remainingFavoriteList = 12 - items.size();
        for (int i = 0; i < remainingFavoriteList; i++) {
            MainListItem m = new MainListItem(-10, "", "");
            items.add(m);
        }


        //get the JSON array of the ordered of sorted customers
        String jsonListOfSortedFavorites = PrefSiempo.getInstance(context).read(PrefSiempo.FAVORITE_SORTED_MENU, "");
        //convert onNoteListChangedJSON array into a List<Long>
        Gson gson1 = new Gson();
        List<String> listOfSortFavoritesApps = gson1.fromJson(jsonListOfSortedFavorites, new TypeToken<List<String>>() {
        }.getType());

        if (listOfSortFavoritesApps != null) {
            if (!listOfSortFavoritesApps.contains(CHROME_PACKAGE)) {
                for (int i = 0; i < listOfSortFavoritesApps.size(); i++) {
                    if (TextUtils.isEmpty(listOfSortFavoritesApps.get(i).trim())) {
                        boolean isEnable = UIUtils.isAppInstalledAndEnabled(context, CHROME_PACKAGE);
                        if (isEnable) {
                            listOfSortFavoritesApps.set(i, CHROME_PACKAGE);
                            if (list != null && !list.contains(CHROME_PACKAGE)) {
                                list.add(CHROME_PACKAGE);
                            }
                        }
                        break;
                    }
                }
            }

            if (!listOfSortFavoritesApps.contains(SYSTEM_SETTING)) {
                for (int i = 0; i < listOfSortFavoritesApps.size(); i++) {
                    if (TextUtils.isEmpty(listOfSortFavoritesApps.get(i).trim())) {
                        boolean isEnable = UIUtils.isAppInstalledAndEnabled(context, SYSTEM_SETTING);
                        if (isEnable) {
                            listOfSortFavoritesApps.set(i, SYSTEM_SETTING);
                            if (list != null && !list.contains(SYSTEM_SETTING)) {
                                list.add(SYSTEM_SETTING);
                            }
                        }
                        break;
                    }
                }
            }
        } else {
            listOfSortFavoritesApps = new ArrayList<>();
            boolean isChromeEnable = UIUtils.isAppInstalledAndEnabled(context, CHROME_PACKAGE);
            if (isChromeEnable) {
                listOfSortFavoritesApps.add(CHROME_PACKAGE);
            }
            boolean isSystemSettingEnable = UIUtils.isAppInstalledAndEnabled(context, SYSTEM_SETTING);
            if (isSystemSettingEnable) {
                listOfSortFavoritesApps.add(SYSTEM_SETTING);
            }
            int remainingCount = 12 - listOfSortFavoritesApps.size();
            for (int j = 0; j < remainingCount; j++) {
                listOfSortFavoritesApps.add("");
            }

            if (list != null) {
                if (isChromeEnable) {
                    list.add(CHROME_PACKAGE);
                }
                if (isSystemSettingEnable) {
                    list.add(SYSTEM_SETTING);
                }
            }
        }


        Gson gson2 = new Gson();
        String jsonListOfFavoriteApps = gson2.toJson(listOfSortFavoritesApps);
        PrefSiempo.getInstance(context).write(PrefSiempo.FAVORITE_SORTED_MENU, jsonListOfFavoriteApps);
        PrefSiempo.getInstance(context).write(PrefSiempo.FAVORITE_APPS, list);

        return items;
    }

    private static ArrayList<MainListItem> addDefaultFavoriteAppsSetting(Context context,
                                                                         List<MainListItem> appList) {

        LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        Set<String> list;
        list = PrefSiempo.getInstance(context).read(PrefSiempo.FAVORITE_APPS, new HashSet<String>());

        ArrayList<MainListItem> items = new ArrayList<>();
        String SYSTEM_SETTING = "com.android.settings";


        for (int i = 0; i < appList.size(); i++) {
            if (!TextUtils.isEmpty(appList.get(i).getPackageName())) {
                if (appList.get(i).getPackageName().equalsIgnoreCase(SYSTEM_SETTING)) {
                    boolean isEnable = UIUtils.isAppInstalledAndEnabled(context, appList.get(i).getPackageName());
                    if (isEnable) {
                        items.add(appList.get(i));
                    }
                }
            }
        }


        int remainingFavoriteList = 12 - items.size();
        for (int i = 0; i < remainingFavoriteList; i++) {
            MainListItem m = new MainListItem(-10, "", "");
            items.add(m);
        }


        //get the JSON array of the ordered of sorted customers
        String jsonListOfSortedFavorites = PrefSiempo.getInstance(context).read(PrefSiempo.FAVORITE_SORTED_MENU, "");
        //convert onNoteListChangedJSON array into a List<Long>
        Gson gson1 = new Gson();
        List<String> listOfSortFavoritesApps = gson1.fromJson(jsonListOfSortedFavorites, new TypeToken<List<String>>() {
        }.getType());

        if (listOfSortFavoritesApps != null) {

            if (!listOfSortFavoritesApps.contains(SYSTEM_SETTING)) {
                for (int i = 0; i < listOfSortFavoritesApps.size(); i++) {
                    if (TextUtils.isEmpty(listOfSortFavoritesApps.get(i).trim())) {
                        boolean isEnable = UIUtils.isAppInstalledAndEnabled(context, SYSTEM_SETTING);
                        if (isEnable) {
                            listOfSortFavoritesApps.set(i, SYSTEM_SETTING);
                            if (list != null && !list.contains(SYSTEM_SETTING)) {
                                list.add(SYSTEM_SETTING);
                            }
                        }
                        break;
                    }
                }
            }
        } else {
            listOfSortFavoritesApps = new ArrayList<>();
            boolean isSystemSettingEnable = UIUtils.isAppInstalledAndEnabled(context, SYSTEM_SETTING);
            if (isSystemSettingEnable) {
                listOfSortFavoritesApps.add(SYSTEM_SETTING);
            }
            int remainingCount = 12 - listOfSortFavoritesApps.size();
            for (int j = 0; j < remainingCount; j++) {
                listOfSortFavoritesApps.add("");
            }

            if (list != null) {
                if (isSystemSettingEnable) {
                    list.add(SYSTEM_SETTING);
                }
            }
        }


        Gson gson2 = new Gson();
        String jsonListOfFavoriteApps = gson2.toJson(listOfSortFavoritesApps);
        PrefSiempo.getInstance(context).write(PrefSiempo.FAVORITE_SORTED_MENU, jsonListOfFavoriteApps);
        PrefSiempo.getInstance(context).write(PrefSiempo.FAVORITE_APPS, list);

        return items;
    }

    public static Drawable getDrawableImage(Context context, ApplicationInfo appInfo) {
        Drawable drawable;
        try {
            Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(appInfo);
            Configuration config = resourcesForApplication.getConfiguration();
            Configuration originalConfig = new Configuration(config);

            DisplayMetrics displayMetrics = resourcesForApplication.getDisplayMetrics();
            DisplayMetrics originalDisplayMetrics = resourcesForApplication.getDisplayMetrics();
            displayMetrics.densityDpi = DisplayMetrics.DENSITY_HIGH;


            resourcesForApplication.updateConfiguration(config, displayMetrics);
            if (appInfo.icon != 0) {
                drawable = resourcesForApplication.getDrawable(appInfo.icon, null);
            } else {
                drawable = appInfo.loadIcon(context.getPackageManager());
            }
            resourcesForApplication.updateConfiguration(originalConfig, originalDisplayMetrics);
        } catch (Exception e) {
            CoreApplication.Companion.getInstance().logException(e);
            drawable = appInfo.loadIcon(context.getPackageManager());
        }
        return drawable;
    }


    public synchronized static void addRecentItemList(MainListItem item, Context context) {

        if (item != null) {
            // Load RecentItem List from Storage
            Type baseType = new TypeToken<List<MainListItem>>() {
            }.getType();
            List<MainListItem> recentItemList;
            recentItemList = loadRecentItemsFromStore(context);

            // Validate if stored RecentItem List having this item or not.
            boolean isItemAvailable = false;
            MainListItem removeItem = null;
            for (int j = 0; j < recentItemList.size(); j++) {
                String title = recentItemList.get(j).getTitle();
                String packageName = recentItemList.get(j).getPackageName();

                if (TextUtils.isEmpty(item.getPackageName())) {
                    if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(item.getTitle()) && title.toLowerCase().trim().equalsIgnoreCase(item.getTitle().toLowerCase().trim())) {
                        isItemAvailable = true;
                        removeItem = recentItemList.get(j);
                    }
                } else {
                    if (!TextUtils.isEmpty(packageName) && packageName.trim().equalsIgnoreCase(item.getPackageName().trim())) {
                        isItemAvailable = true;
                        removeItem = recentItemList.get(j);
                    }
                }
            }

            /*
               1.) If RecentItem List do not contain this item, then add and store into the list.
               2.) If RecentItem lsit contain this item, then update it to first position
             */
            if (!isItemAvailable) {
                recentItemList.add(0, item);
            } else {
                if (removeItem != null) {
                    recentItemList.remove(removeItem);
                }
                recentItemList.add(0, item);
            }

            Gson gson = new Gson();
            String val_recentItemList = gson.toJson(recentItemList);
            PrefSiempo.getInstance(context).write(PrefSiempo.RECENT_ITEM_LIST, val_recentItemList);
        }
    }

    public synchronized static List<MainListItem> getListWithMostRecentData(List<MainListItem> allItems, Context context) {

        List<MainListItem> recentItemList;
        recentItemList = loadRecentItemsFromStore(context);

        List<MainListItem> removeList = new ArrayList<>();
        List<MainListItem> listWithMostRecentdata = new ArrayList<>();

        if (recentItemList != null) {
            for (int j = 0; j < recentItemList.size(); j++) {
                MainListItem mainListItem = recentItemList.get(j);

                if (null != mainListItem) {
                    String recentItemTitle = mainListItem.getTitle();
                    String recentItemPackageName = mainListItem.getPackageName();

                    for (int i = 0; i < allItems.size(); i++) {
                        MainListItem item = allItems.get(i);
                        String title = allItems.get(i).getTitle();
                        String packageName = allItems.get(i).getPackageName();

                        if (TextUtils.isEmpty(packageName) && TextUtils.isEmpty(recentItemPackageName) && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(recentItemTitle) && title.toLowerCase().trim().equalsIgnoreCase(recentItemTitle.toLowerCase().trim())) {
                            removeList.add(item);
                        } else if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(recentItemPackageName) && packageName.trim().equalsIgnoreCase(recentItemPackageName.trim())) {
                            removeList.add(item);
                        }
                    }
                }

            }

            allItems.removeAll(removeList);

            listWithMostRecentdata.addAll(removeList);
            listWithMostRecentdata.addAll(allItems);

            List<MainListItem> junkListItems = getJunkListItems(listWithMostRecentdata, context);
            listWithMostRecentdata.removeAll(junkListItems);
        }
        return listWithMostRecentdata;
    }

    private static List<MainListItem> loadRecentItemsFromStore(Context context) {
        Type baseType = new TypeToken<List<MainListItem>>() {
        }.getType();
        List<MainListItem> recentItemList = new ArrayList<>();
        String val_recentItems = PrefSiempo.getInstance(context).read(PrefSiempo.RECENT_ITEM_LIST, "");
        if (!TextUtils.isEmpty(val_recentItems)) {
            Gson gson = new GsonBuilder()
                    .setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
            recentItemList = gson.fromJson(val_recentItems, baseType);
        }
        return recentItemList;
    }


    private static List<MainListItem> getJunkListItems(List<MainListItem> allItems, Context context) {
        HashMap<Integer, AppMenu> toolSetting = CoreApplication.Companion.getInstance()
                .getToolsSettings();
        ArrayList<String> junkFoodAppList;
        Set<String> junkFoodList = PrefSiempo
                .getInstance(context).read
                        (PrefSiempo.JUNKFOOD_APPS, new HashSet<String>());
        junkFoodAppList = new ArrayList<>(junkFoodList);
        List<MainListItem> junkListItems = new ArrayList<>();

        // Check if tools contain junkfood apps then remove assign from corresponding tool
        Iterator it = toolSetting.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            for (String junkApp : junkFoodAppList) {
                if (((AppMenu) (pair.getValue())).getApplicationName()
                        .equalsIgnoreCase(junkApp) || ((AppMenu) (pair.getValue())).getApplicationName()
                        .equalsIgnoreCase("")) {
                    ((AppMenu) (pair.getValue())).setApplicationName("");
                }

            }
        }

        // Create junkListItems array which contain junkfood apps & tools
        for (MainListItem item : allItems) {
            if (!TextUtils.isEmpty(item.getPackageName())) {
                for (String junkApp : junkFoodAppList) {
                    if (item.getPackageName().equalsIgnoreCase(junkApp)) {
                        junkListItems.add(item);
                    }
                }
            } else {
                AppMenu appMenu = toolSetting
                        .get(item.getId());
                if (null != appMenu && TextUtils
                        .isEmpty(appMenu.getApplicationName()) && item
                        .getItemType() != MainListItemType.DEFAULT) {
                    junkListItems.add(item);
                }
            }
        }

        return junkListItems;
    }

    public static int forTwoHours(int hour) {
        ArrayList<Integer> everyTwoHourList = new ArrayList<>(Arrays.asList(0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22));
        if (hour >= 22) {
            return 0;
        } else {
            for (Integer integer : everyTwoHourList) {
                if (integer > hour) {
                    return integer;
                }
            }
        }
        return 0;
    }

    public static int forFourHours(int hour) {
        ArrayList<Integer> everyFourHoursList = new ArrayList<>(Arrays.asList(0, 4, 8, 12, 16, 20));
        if (hour >= 20) {
            return 0;
        } else {
            for (Integer integer : everyFourHoursList) {
                if (integer > hour) {
                    return integer;
                }
            }
        }
        return 0;
    }

    public static class HoursComparator implements Comparator<AlarmData> {
        @Override
        public int compare(AlarmData o1, AlarmData o2) {
            if (o1.getHours() == o2.getHours()) {
                return o1.getMinute() - o2.getMinute();
            }
            return o1.getHours() - o2.getHours();
        }
    }
}
