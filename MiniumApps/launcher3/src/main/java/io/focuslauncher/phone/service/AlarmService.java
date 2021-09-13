package io.focuslauncher.phone.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.focuslauncher.phone.app.Constants;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.db.DBUtility;
import io.focuslauncher.phone.db.TableNotificationSms;
import io.focuslauncher.phone.db.TableNotificationSmsDao;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.models.CustomNotification;
import io.focuslauncher.phone.utils.NotificationUtils;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.Sorting;
import io.focuslauncher.phone.utils.UIUtils;
import io.focuslauncher.R;

import static io.focuslauncher.phone.utils.NotificationUtils.ANDROID_CHANNEL_ID;

/**
 * Created by rajeshjadi on 8/1/18.
 */

public class AlarmService extends IntentService {


    Context context;
    NotificationManagerCompat n;
    boolean isTempoVolume = false;
    private AudioManager audioManager;
    private ArrayList<Integer> everyHourList = new ArrayList<>();
    private ArrayList<Integer> everyTwoHourList = new ArrayList<>();
    private ArrayList<Integer> everyFourHoursList = new ArrayList<>();
    private Vibrator vibrator;
    private NotificationUtils notificationUtils;

    public AlarmService() {
        super("MyServerOrWhatever");
    }


    public AlarmService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        notificationUtils = new NotificationUtils(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("")
                        .setPriority(Notification.PRIORITY_LOW)
                        .setAutoCancel(true);
                Notification notification = builder.build();
                notificationUtils.createChannels();
                startForeground(Constants.ALARM_SERVICE_ID, notification);
            } catch (Throwable e) {
                Log.e("Notifications", "Couldn't start AlarmService foreground", e);
            }
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        context = this;
        Tracer.d("AlarmService: onHandleIntent");
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        everyHourList.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24));
        everyTwoHourList.addAll(Arrays.asList(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23));
        everyFourHoursList.addAll(Arrays.asList(1, 4, 8, 12, 16, 20, 24));
        n = NotificationManagerCompat.from(context);
        List<TableNotificationSms> notificationList = DBUtility.getNotificationDao().queryBuilder().where(TableNotificationSmsDao.Properties._is_read.eq(false)).orderDesc(TableNotificationSmsDao.Properties.Notification_date).build().list();
        Tracer.d("AlarmService: notificationList.size" + notificationList.size());
        if (notificationList.size() > 0) {
            run(notificationList);
        }
    }

    public void run(List<TableNotificationSms> notificationList) {
        if (PackageUtil.isSiempoLauncher(context)) {
            Calendar calendar = Calendar.getInstance();
            int systemHours = calendar.get(Calendar.HOUR_OF_DAY);
            int systemMinutes = calendar.get(Calendar.MINUTE);
            int tempoType = PrefSiempo.getInstance(context).read(PrefSiempo
                    .TEMPO_TYPE, 0);
            Tracer.d("AlarmService: TempoType::" + tempoType);
            if (tempoType == 1) {
                Tracer.d("AlarmService: TempoType::Batch");
                int batchTime = PrefSiempo.getInstance(context).read(PrefSiempo
                        .BATCH_TIME, 15);
                Tracer.d("AlarmService: batchTime" + batchTime);
                if (batchTime == 15) {
                    if (systemMinutes == 0 || systemMinutes == 15 || systemMinutes == 30 || systemMinutes == 45) {
                        createNotification(notificationList, context);
                    }
                } else if (batchTime == 30) {
                    if (systemMinutes == 0 || systemMinutes == 30) {
                        createNotification(notificationList, context);
                    }
                } else if (batchTime == 1) {
                    if (everyHourList.contains(systemHours) && systemMinutes == 0) {
                        createNotification(notificationList, context);
                    }
                } else if (batchTime == 2) {
                    if (systemHours % 2 == 0 && systemMinutes == 0) {
                        createNotification(notificationList, context);
                    }
                } else if (batchTime == 4) {
                    if (systemHours % 4 == 0 && systemMinutes == 0) {
                        createNotification(notificationList, context);
                    }
                }

            } else if (tempoType == 2) {
                Tracer.d("AlarmService: TempoType::OnlyAt");
                String strTimeData = PrefSiempo.getInstance(context).read(PrefSiempo
                        .ONLY_AT, "12:01");
                Tracer.d("AlarmService: onlyAt start");
                if (!strTimeData.equalsIgnoreCase("")) {
                    String strTime[] = strTimeData.split(",");
                    Tracer.d("AlarmService: onlyAt strTime.length" + strTime.length);
                    for (String str : strTime) {
                        int hours = Integer.parseInt(str.split(":")[0]);
                        int minutes = Integer.parseInt(str.split(":")[1]);
                        Tracer.d("AlarmService: Time " + "User" + hours + ":" + minutes + "System:" + systemHours + ":" + systemMinutes);
                        if (hours == systemHours && minutes == systemMinutes) {
                            Tracer.d("AlarmService: onlyAt match condition" + str);
                            createNotification(notificationList, context);
                        }
                    }
                }
            }

        }
    }

    public void createNotification(List<TableNotificationSms> notificationList, Context context) {
        try {
            Tracer.d("AlarmService: createNotification");

            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                int sound = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                if (sound == 1) {
                    isTempoVolume = true;
                    sound = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM) / 2;
                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, sound, 0);
                }
                Tracer.d("AlarmService: audioManager");
            }
            Set<String> packageList = new HashSet<>();
            if (notificationList.size() > 0) {
                for (TableNotificationSms sms : notificationList) {
                    packageList.add(sms.getPackageName());
                }
            }

            ArrayList<CustomNotification> customNotifications = new ArrayList<>();
            for (String string : packageList) {
                CustomNotification customNotification = new CustomNotification();
                ArrayList<TableNotificationSms> tableNotificationSms = new ArrayList<>();
                for (TableNotificationSms sms : notificationList) {
                    if (sms.getPackageName().equalsIgnoreCase(string)) {
                        tableNotificationSms.add(sms);
                    }
                }
                Sorting.sortNotificationByDate(tableNotificationSms);
                customNotification.setDate(tableNotificationSms.get(0).get_date());
                customNotification.setNotificationSms(tableNotificationSms);
                customNotification.setPackagename(tableNotificationSms.get(0).getPackageName());
                customNotifications.add(customNotification);
            }
            Sorting.sortNotificationByDate1(customNotifications);

            for (CustomNotification customNotification : customNotifications) {
                if (Build.VERSION.SDK_INT < 24) {
                    generateBelow24(context, customNotification.getNotificationSms());
                } else {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(
                            NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= 26) {
                        CharSequence channelName = CoreApplication.getInstance().getListApplicationName().get(customNotification.getPackagename());
                        int importance;
                        if (!PrefSiempo.getInstance(context).read(PrefSiempo.ALLOW_PEAKING, true)) {
                            importance = NotificationManager.IMPORTANCE_DEFAULT;
                        } else {
                            importance = NotificationManager.IMPORTANCE_HIGH;
                        }
                        NotificationChannel notificationChannel = new NotificationChannel("" + channelName, channelName, importance);
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Color.RED);
                        notificationChannel.enableVibration(true);
                        notificationChannel.setSound(null, null);
                        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                        notificationChannel.setVibrationPattern(new long[]{1000});

                        if (notificationManager != null) {
                            notificationManager.createNotificationChannel(notificationChannel);
                        }
                    }
                    for (TableNotificationSms tableNotificationSms : customNotification.getNotificationSms()) {
                        Notification notification =
                                createSingleNotification(tableNotificationSms, true);
                        if (notificationManager != null) {
                            notificationManager.notify(tableNotificationSms.getId().intValue(), notification);
                        }
                    }
                    Notification summary = createGroupNotification(customNotification.getPackagename(), customNotification.getNotificationSms());
                    if (notificationManager != null) {
                        notificationManager.notify(customNotification.getNotificationSms().get(0).getApp_icon(), summary);
                    }
                }
            }

            if (notificationList.size() >= 1) {
                Tracer.d("AlarmService: deleteAll");
                playNotificationSoundVibrate();
//                DBUtility.getNotificationDao().deleteAll();
                for (TableNotificationSms tableNotificationSms : notificationList) {
                    tableNotificationSms.set_is_read(true);
                    DBUtility.getNotificationDao().update(tableNotificationSms);
                }

                try {
                    File file = context.getDatabasePath("noti-db");
                    double fileinKb = file.length() / 1024;
                    double fileinMb = fileinKb / 1024;
                    Log.d("FileSize", "Size:" + file.length() + " fileinKb:" + fileinKb + " fileinMb:" + fileinMb);
                    if (fileinMb > 10) {
                        long list = DBUtility.getNotificationDao().queryBuilder().count();
                        List<TableNotificationSms> listForDelete = DBUtility.getNotificationDao().queryBuilder().
                                orderAsc(TableNotificationSmsDao.Properties.Notification_date)
                                .limit((int) (list / 2)).list();
                        DBUtility.getNotificationDao().deleteInTx(listForDelete);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            CoreApplication.getInstance().logException(e);
        }
    }

    private Notification createGroupNotification(String packageName, ArrayList<TableNotificationSms> notificationSms) {
        String applicationName = CoreApplication.getInstance().getListApplicationName().get(packageName);
        Bitmap bitmap = CoreApplication.getInstance().getBitmapFromMemCache(packageName);
        PendingIntent contentIntent = PackageUtil.getPendingIntent(context, notificationSms.get(0));
        return new NotificationCompat.Builder(context, applicationName)
                .setSmallIcon(R.drawable.ic_focus_launcher_notification)
                .setContentTitle(applicationName)
                .setContentIntent(contentIntent)
                .setContentText(notificationSms.size() == 1 ? "1 new message" : notificationSms.size() + " new messages")
                .setLargeIcon(bitmap)
                .setGroupSummary(true)
                .setAutoCancel(true)
                //  .setDefaults(Notification.DEFAULT_SOUND)
                .setGroup(applicationName)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .build();
    }

    private Notification createSingleNotification(TableNotificationSms tableNotificationSms, boolean isGrouped) {
        String applicationName = CoreApplication.getInstance().getListApplicationName().get(tableNotificationSms.getPackageName());
        Bitmap bitmapApplication = CoreApplication.getInstance().getBitmapFromMemCache(tableNotificationSms.getPackageName());
        int priority = !PrefSiempo.getInstance(context).read(PrefSiempo.ALLOW_PEAKING, true) ? Notification.PRIORITY_DEFAULT : Notification.PRIORITY_HIGH;
        PendingIntent contentIntent = PackageUtil.getPendingIntent(context, tableNotificationSms);
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_card);
        DateFormat sdf = new SimpleDateFormat(PackageUtil.getTimeFormat(context), Locale.getDefault());
        String time = sdf.format(tableNotificationSms.get_date());
        String title = PackageUtil.getNotificationTitle(tableNotificationSms.get_contact_title(), tableNotificationSms.getPackageName(), context);
        Bitmap bitmapUserIcon = tableNotificationSms.getUser_icon() != null ? UIUtils.convertBytetoBitmap(tableNotificationSms.getUser_icon()) : null;
        contentView.setImageViewBitmap(R.id.imgAppIcon, bitmapApplication);
        contentView.setImageViewBitmap(R.id.imgUserImage, bitmapUserIcon);
        contentView.setTextViewText(R.id.txtUserName, title);
        contentView.setTextViewText(R.id.txtMessage, tableNotificationSms.get_message());
        contentView.setTextViewText(R.id.txtTime, time);
        contentView.setTextViewText(R.id.txtAppName, applicationName);
        if (applicationName.equalsIgnoreCase(title)) {
            contentView.setViewVisibility(R.id.txtUserName, View.GONE);
        }

        RemoteViews collapsedViews = new RemoteViews(context.getPackageName(), R.layout.custom_notification_card_collapse);
        collapsedViews.setImageViewBitmap(R.id.imgAppIcon, bitmapApplication);
        collapsedViews.setImageViewBitmap(R.id.imgUserImage, bitmapUserIcon);
        collapsedViews.setTextViewText(R.id.txtUserName, title);
        collapsedViews.setTextViewText(R.id.txtMessage, tableNotificationSms.get_message());
        collapsedViews.setTextViewText(R.id.txtTime, time);
        collapsedViews.setViewVisibility(R.id.txtTime, View.GONE);
        collapsedViews.setViewVisibility(R.id.dot, View.GONE);
        collapsedViews.setTextViewText(R.id.txtAppName, title);

        NotificationCompat.Builder newMessageNotification = new NotificationCompat.Builder(context, applicationName)
                .setAutoCancel(true)
                .setWhen(tableNotificationSms.get_date().getTime())
                .setSmallIcon(R.drawable.ic_focus_launcher_notification)
                .setPriority(priority)
                .setContentIntent(contentIntent)
                .setCustomContentView(collapsedViews)
                .setCustomBigContentView(contentView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setLights(Color.MAGENTA, 500, 500)
                //.setDefaults(Notification.DEFAULT_SOUND)
                .setContentInfo("Info");

        if (isGrouped) {
            newMessageNotification.setGroup(applicationName);
            newMessageNotification.setSound(null);
        }

        return newMessageNotification
                .build();
    }

    /*
        Generate Notification for the older then 24 sdk.
     */
    private void generateBelow24(Context context, ArrayList<TableNotificationSms> notificationSms) {
        String packageName = notificationSms.get(0).getPackageName();
        String applicationName = CoreApplication.getInstance().getListApplicationName().get(packageName);
        Bitmap bitmapApplication = CoreApplication.getInstance().getBitmapFromMemCache(packageName);
        NotificationManagerCompat n = NotificationManagerCompat.from(this);
        int priority = !PrefSiempo.getInstance(context).read(PrefSiempo.ALLOW_PEAKING, true) ? Notification.PRIORITY_DEFAULT : Notification.PRIORITY_HIGH;
        if (notificationSms.size() == 1) {
            TableNotificationSms tableNotificationSms = notificationSms.get(0);
            PendingIntent contentIntent = PackageUtil.getPendingIntent(context, notificationSms.get(0));
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_card);
            DateFormat sdf = new SimpleDateFormat(PackageUtil.getTimeFormat(context), Locale.getDefault());
            String time = sdf.format(tableNotificationSms.get_date());
            String title = PackageUtil.getNotificationTitle(tableNotificationSms.get_contact_title(), tableNotificationSms.getPackageName(), context);
            Bitmap bitmapUserIcon = tableNotificationSms.getUser_icon() != null ? UIUtils.convertBytetoBitmap(tableNotificationSms.getUser_icon()) : null;
            contentView.setImageViewBitmap(R.id.imgAppIcon, bitmapApplication);
            contentView.setImageViewBitmap(R.id.imgUserImage, bitmapUserIcon);
            contentView.setTextViewText(R.id.txtUserName, title);
            contentView.setTextViewText(R.id.txtMessage, tableNotificationSms.get_message());
            contentView.setTextViewText(R.id.txtTime, time);
            contentView.setViewVisibility(R.id.txtUserName, View.GONE);
            contentView.setTextViewText(R.id.txtAppName, title);


            RemoteViews collapsedViews = new RemoteViews(context.getPackageName(), R.layout.notification_blog3_collapsed);
            collapsedViews.setTextViewText(R.id.content_title, title);
            collapsedViews.setTextViewText(R.id.content_message, tableNotificationSms.get_message());
            collapsedViews.setImageViewBitmap(R.id.big_icon, bitmapApplication);
            collapsedViews.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(this, notificationSms.get(0).get_date().getTime(), DateUtils.FORMAT_SHOW_TIME));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "")
                    .setSmallIcon(R.drawable.ic_focus_launcher_notification)
                    .setContentTitle("")
                    .setPriority(priority)
                    .setContentText("")
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .setLights(Color.MAGENTA, 500, 500)
                    //.setDefaults(Notification.DEFAULT_SOUND)
                    .setCustomContentView(collapsedViews)
                    .setCustomBigContentView(contentView);
            n.notify(notificationSms.get(0).getId().intValue(), builder.build());
        } else {
            NotificationCompat.InboxStyle inboxStyle;
            inboxStyle = new NotificationCompat.InboxStyle();
            for (int i = 0; i < notificationSms.size(); i++) {
                String title = PackageUtil.getNotificationTitle
                        (notificationSms.get(i).get_contact_title(), packageName, context);
                inboxStyle.addLine(title + ": " + notificationSms.get(i).get_message());
            }
            inboxStyle.setSummaryText("You have " + notificationSms.size() + " unread message");
            PendingIntent contentIntent = PackageUtil.getPendingIntent(context, notificationSms.get(0));

            Notification newMessageNotification = new NotificationCompat.Builder(context, applicationName)
                    .setSmallIcon(R.drawable.ic_focus_launcher_notification)
                    .setContentTitle(applicationName)
                    .setContentText(notificationSms.size() + " new messages")
                    .setLargeIcon(bitmapApplication)
                    .setContentIntent(contentIntent)
                    .setGroupSummary(true)
                    .setGroup(applicationName)
                    .setStyle(inboxStyle)
                    .setAutoCancel(true)
                    .setPriority(priority)
                    .setLights(Color.MAGENTA, 500, 500)
//                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                    .build();
            n.notify(notificationSms.get(0).getApp_icon(), newMessageNotification);
        }
    }


    public void playNotificationSoundVibrate() {
        try {
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                vibrator.vibrate(500);
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
                mediaPlayer.setDataSource(getApplicationContext(), alert);
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        if (audioManager != null) {
                            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                if (isTempoVolume)
                                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, 0);
                            }
                        }
                    }
                });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
