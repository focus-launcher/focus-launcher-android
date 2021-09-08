package io.focuslauncher.phone.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.focuslauncher.phone.receivers.PhoneCallReceiver;
import io.focuslauncher.R;
import io.focuslauncher.phone.app.Constants;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.app.Launcher3App;
import io.focuslauncher.phone.db.DBUtility;
import io.focuslauncher.phone.db.DaoSession;
import io.focuslauncher.phone.db.TableNotificationSms;
import io.focuslauncher.phone.db.TableNotificationSmsDao;
import io.focuslauncher.phone.event.NewNotificationEvent;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.utils.NotificationUtility;
import io.focuslauncher.phone.utils.NotificationUtils;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.UIUtils;
import de.greenrobot.event.EventBus;

import static io.focuslauncher.phone.utils.NotificationUtils.ANDROID_CHANNEL_ID;

/**
 * Created by Shahab on 5/16/2017.
 */

@EService
public class SiempoNotificationListener extends NotificationListenerService {

    public static final String TAG = SiempoNotificationListener.class.getName();


    @SystemService
    AudioManager audioManager;

    @SystemService
    NotificationManager notificationManager;

    Context context;
    ArrayList<String> enableNotificationList = new ArrayList<>();
    Set<String> blockedAppList = new HashSet<>();
    NotificationUtils notificationUtils;
    int volumeLevel = 1;

    CountDownTimer countDownTimer;

    private static boolean isInteger(String s, int radix) {
        try {
            if (s.isEmpty()) {
                return false;
            }
            int i = 0;
            while (i < s.length()) {
                if (i == 0 && s.charAt(i) == '-') {
                    if (s.length() == 1) {
                        return false;
                    }
                } else if (Character.digit(s.charAt(i), radix) < 0) {
                    return false;
                }
                i++;
            }
            return true;
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            return true;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("")
                        .setPriority(Notification.PRIORITY_LOW)
                        .setAutoCancel(true);
                Notification notification = builder.build();
                notificationUtils.createChannels();
                startForeground(Constants.NOTIFICIONLISTENER_SERVICE_ID, notification);
            }
        } catch (Throwable e) {
            Log.e("Notifications", "Couldn't start SiempoNotificationListener foreground", e);
        }
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Tracer.d("SiempoNotificationListener: connected");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Tracer.d("SiempoNotificationListener: onListenerDisconnected");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Notification listener disconnected - requesting rebind
            try {
                Tracer.d("SiempoNotificationListener: requestRebind1");
                requestRebind(new ComponentName(this, SiempoNotificationListener_.class));
                Tracer.d("SiempoNotificationListener: requestRebind2");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onNotificationChannelModified(String pkg, UserHandle user, NotificationChannel channel, int modificationType) {
        super.onNotificationChannelModified(pkg, user, channel, modificationType);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Tracer.d("SiempoNotificationListener: onNotificationChannelModified");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        super.onNotificationPosted(notification);
        context = this;

        int tempoType = PrefSiempo.getInstance(context).read(PrefSiempo.TEMPO_TYPE, 0);
        if (tempoType == 1 || tempoType == 2) {
            Tracer.d("SiempoNotificationListener: tempoType" + tempoType);
            Notification notification1 = notification.getNotification();
            printLog(notification);
            if (PackageUtil.isSiempoLauncher(context)) {
                try {
                    if (notification1 != null) {
                        if ((notification1.flags & Notification.FLAG_ONGOING_EVENT) == Notification.FLAG_ONGOING_EVENT) {
                            Tracer.d("SiempoNotificationListener: Not forwarding notification, FLAG_ONGOING_EVENT is set. Notification flags: " + notification1.flags);
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                /* do not display messages from "android"
                 * This includes keyboard selection message, usb connection messages, etc
                 * Hope it does not filter out too much, we will see...
                 */
                try {
                    if (notification != null) {
                        if (notification.getPackageName().equals("android") ||
                                notification.equals("com.android.systemui") ||
                                notification.equals("com.cyanogenmod.eleven")) {
                            Tracer.d("SiempoNotificationListener: Not forwarding notification, is a system event");
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String strEnableApps = PrefSiempo.getInstance(context).read(PrefSiempo.HELPFUL_ROBOTS, "");

                if (!TextUtils.isEmpty(strEnableApps)) {
                    Type type = new TypeToken<ArrayList<String>>() {
                    }.getType();
                    enableNotificationList = new Gson().fromJson(strEnableApps, type);
                }
                Tracer.d("SiempoNotificationListener:enableNotificationList.size" + enableNotificationList.size());

                blockedAppList = PrefSiempo.getInstance(context).read(PrefSiempo.BLOCKED_APPLIST,
                        new HashSet<String>());
                Tracer.d("SiempoNotificationListener:blockedAppList.size" + blockedAppList.size());
                if (null != blockedAppList && blockedAppList.size() > 0 && blockedAppList.contains(notification.getPackageName())) {
                    if (!notification.getPackageName().equalsIgnoreCase(getPackageName())) {
                        SiempoNotificationListener.this.cancelNotification(notification.getKey());
                        Tracer.d("SiempoNotificationListener:cancelNotification");
                        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                            Tracer.d("SiempoNotificationListener:cancelNotification");
                            if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE ||
                                    audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                                int sound = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                                Log.d("countDownTimer", "CountDownTimer " + PhoneCallReceiver.isCallRunning);
                                if (sound != 1 && !PhoneCallReceiver.isCallRunning) {
                                    volumeLevel = sound;
                                    PrefSiempo.getInstance(this).write(PrefSiempo.USER_VOLUME, audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
                                    Log.d("AudioManager", "" + sound);
                                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                    Log.d("AudioManager : ", "" + audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
                                }
                            }
                        }
                        if (notification.getPackageName() != null
                                && !notification.getPackageName().equalsIgnoreCase(StatusBarService.packagename)) {
                            startCountDownTimer();
                            filterByCategory(notification);
                        }
                    }
                } else {
                    //Check for ringer mode
                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                        int volume = PrefSiempo.getInstance(this).read(PrefSiempo.USER_VOLUME, audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
                        int sound = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                        if (sound == 1) {
                            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, 0);
                        }
                    }
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
                }
            } else {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
            }
        }
    }

    private void startCountDownTimer() {
        if (countDownTimer == null && !PhoneCallReceiver.isCallRunning) {
            try {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        countDownTimer = new CountDownTimer(5000, 500) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                Log.d("countDownTimer", "" + millisUntilFinished);
                            }

                            @Override
                            public void onFinish() {
                                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volumeLevel, 0);
                                    Log.d("AudioManager : ", "" + audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
                                }
                                Log.d("countDownTimer", "CountDownTimer cancel");
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                    countDownTimer = null;
                                }
                            }
                        }.start();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volumeLevel, 0);
                    Log.d("AudioManager : ", "" + audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
                }
            }

        }
    }

    private void printLog(StatusBarNotification notification) {
        String strName;
        char[] array;
        long time;
        byte[] userImage;


        Bundle bundle = notification.getNotification().extras;
        String strKey;
        String strValue;
        StringBuilder finalString = new StringBuilder();
        String strTitle;
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                strKey = " Key :: " + key != null ? key : "";
                if (null != key && key.equalsIgnoreCase("android" +
                        ".textLines")) {
                    strValue = " Value ::" + value != null ? "" + (value != null ? value.toString() : null) : "";
                } else {
                    strValue = " Value ::" + value != null ? "" + value : "";
                }
                finalString.append("\n").append(strKey).append(" :").append(strValue);

            }
        }
        Tracer.d("SiempoNotificationListener : " + " Package: " + notification.getPackageName()
                + "\n" + " Id: " + notification.getId()
                + "\n" + " Post time: " + SimpleDateFormat.getDateTimeInstance().format(new Date(notification.getPostTime()))
                + "\n" + " Details: " + notification.getNotification().toString()
                + "\n" + " Category: " + notification.getNotification().category
                + "\n" + " Ticker: " + notification.getNotification().tickerText
                + "\n" + " Bundle AlarmData:" + finalString);
    }

    /**
     * Used for the filter the Notification based on package name and parsing the notification.
     *
     * @param statusBarNotification
     */
    private synchronized void filterByCategory(StatusBarNotification statusBarNotification) {

        String strPackageName;//getPackageName
        String strTitle = null;//android.title
        String strText = null;//android.text
        Date date;
        StringBuilder data = new StringBuilder();
        String strBigText = null;//android.subText
        String tickerText = "";

        int icon = 0;//android.icon
        byte[] largeIcon = new byte[0];// android.largeIcon
        strPackageName = statusBarNotification.getPackageName();

        date = new Date(statusBarNotification.getPostTime());

        if (statusBarNotification.getNotification().extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES) != null) {
            CharSequence[] test = statusBarNotification.getNotification().extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            ArrayList<String> list = new ArrayList<>();
            if (test != null) {
                for (CharSequence charSequence : test) {
                    list.add("" + charSequence);
                }
            }
            Collections.reverse(list);
            for (String string : list) {
                data.append(string).append("\n");
            }

        }
        try {
            if (statusBarNotification.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE) != null) {
                strTitle = statusBarNotification.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CoreApplication.getInstance().logException(e);
        }

        try {
            if (statusBarNotification.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT) != null) {
                CharSequence charText = statusBarNotification.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString();
                strText = charText.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            CoreApplication.getInstance().logException(e);
        }


        try {
            if (statusBarNotification.getNotification().tickerText != null) {
                CharSequence charText = statusBarNotification.getNotification().tickerText;
                tickerText = charText.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Tracer.d(e.getMessage());
        }

        if (statusBarNotification.getNotification().extras.getCharSequence(Notification.EXTRA_BIG_TEXT) != null) {
            CharSequence charBigText = statusBarNotification.getNotification().extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
            strBigText = charBigText.toString();
        }
        try {
            if (statusBarNotification.getNotification().extras.getInt(Notification.EXTRA_SMALL_ICON) != 0) {
                icon = statusBarNotification.getNotification().extras.getInt(Notification.EXTRA_SMALL_ICON);
            }
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            Tracer.d(e.getMessage());
        }

        try {
            if (statusBarNotification.getNotification().extras.getParcelable(Notification.EXTRA_LARGE_ICON) != null) {
                Bitmap iconUser = statusBarNotification.getNotification().extras.getParcelable(Notification.EXTRA_LARGE_ICON);
                largeIcon = UIUtils.convertBitmapToByte(iconUser);
            }
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            Tracer.d(e.getMessage());
        }
        String strCount = null;
        try {
            if (statusBarNotification.getNotification().extras.getCharSequence(NotificationCompat.EXTRA_SUMMARY_TEXT) != null) {
                strCount = statusBarNotification.getNotification().extras.getCharSequence(NotificationCompat.EXTRA_SUMMARY_TEXT).toString();
                if (strCount != null && Character.isDigit(strCount.charAt(0))) {
                    String str[] = strCount.split(" ");
                    int count = Integer.parseInt(str[0]);
                    logFirebaseCount(strPackageName, count);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            CoreApplication.getInstance().logException(e);
        }

        if (statusBarNotification.getNotification().category != null && statusBarNotification.getNotification().category.equals(Notification.CATEGORY_PROGRESS)) {
            return;
        }

        //Parse the Whats App messages.
        if (statusBarNotification.getPackageName().equalsIgnoreCase(Constants.WHATSAPP_PACKAGE)) {
            Tracer.d("SiempoNotificationListener:parseWhatsappMessage");
            parseWhatsappMessage(statusBarNotification, strPackageName, date, data, icon, largeIcon);

        }
        //Parse the Telegram.
        else if (statusBarNotification.getPackageName().equalsIgnoreCase(Constants.TELEGRAM_PACKAGES)) {
            Tracer.d("SiempoNotificationListener:parseGoogleCalender");
            parseTelegramMessenger(statusBarNotification, strPackageName, strTitle, strText, date, icon, largeIcon);
        }
        //Parse the Google Calendar
        else if (statusBarNotification.getPackageName().equalsIgnoreCase(Constants.GOOGLE_CALENDAR_PACKAGES)) {
            Tracer.d("SiempoNotificationListener:parseGoogleCalender");
            parseGoogleCalender(statusBarNotification, strPackageName, strTitle, strText, date, strBigText, icon, largeIcon);
            // Facebook
        } else if (statusBarNotification.getPackageName().equalsIgnoreCase(Constants.FACEBOOK_PACKAGE)) {
            Tracer.d("SiempoNotificationListener:parseFacebook");
            parseFacebook(statusBarNotification, strPackageName, strTitle, strText, date, icon, largeIcon);
            // Facebook Messenger
        } else if (statusBarNotification.getPackageName().equalsIgnoreCase(Constants.FACEBOOK_MESSENGER_PACKAGE)) {
            Tracer.d("SiempoNotificationListener:parseFacebookMessenger");
            parseFacebookMessenger(statusBarNotification, strPackageName, strTitle, strText, date, icon, largeIcon);
            // Facebook Lite Messenger
        } else if (statusBarNotification.getPackageName().equalsIgnoreCase(Constants.FACEBOOK_LITE_PACKAGE)) {
            Tracer.d("SiempoNotificationListener:parseFacebookLite");
            parseFacebookLite(statusBarNotification, strPackageName, strTitle, strText, date, data, icon, largeIcon);
            //Parse HangOut message
        } else if (statusBarNotification.getPackageName().equalsIgnoreCase(Constants.GOOGLE_HANGOUTS_PACKAGES)) {
            Tracer.d("SiempoNotificationListener:parseHangOutMessage");
            parseHangOutMessage(statusBarNotification, strPackageName, strTitle, strText, date, tickerText, icon, largeIcon);
        } else {
            Tracer.d("SiempoNotificationListener:parseOtherMessages");
            parseOtherMessages(statusBarNotification, strPackageName, strTitle, strText, date, strBigText, icon, largeIcon, strCount);
            try {
                if (strCount == null || !Character.isDigit(strCount.charAt(0))) {
                    List<TableNotificationSms> notificationSms
                            = DBUtility.getNotificationDao().queryBuilder()
                            .where(TableNotificationSmsDao.Properties.PackageName.eq(strPackageName),
                                    TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                            .list();
                    logFirebaseCount(strPackageName, notificationSms.size());
                }
            } catch (Exception e) {
                CoreApplication.getInstance().logException(e);
            }
        }
    }

    private void parseOtherMessages(StatusBarNotification statusBarNotification, String strPackageName, String strTitle, String strText, Date date, String strBigText, int icon, byte[] largeIcon, String strCount) {
        if (statusBarNotification.getNotification() != null
                && statusBarNotification.getNotification().tickerText != null
                && statusBarNotification.getNotification().tickerText.toString().equalsIgnoreCase("USB debugging connected")) {
            return;
        }

        if (statusBarNotification.getNotification().category == null
                || (!statusBarNotification.getNotification().category.equalsIgnoreCase(Notification.CATEGORY_CALL) &&
                !statusBarNotification.getNotification().category.equalsIgnoreCase(Notification.CATEGORY_PROGRESS) &&
                !statusBarNotification.getNotification().category.equalsIgnoreCase(Notification.CATEGORY_TRANSPORT) &&
                !statusBarNotification.getNotification().category.equalsIgnoreCase(Notification.CATEGORY_SERVICE) &&
                !statusBarNotification.getPackageName().equalsIgnoreCase("com.google.android.talk")
                //&& !statusBarNotification.getPackageName().equalsIgnoreCase("com.google.android.apps.messaging")
                && !statusBarNotification.getPackageName().trim().equalsIgnoreCase("android"))) {


//            if (launcherPrefs.getSharedPreferences().getBoolean(Constants.CALL_RUNNING, false)) {
//            if (PrefSiempo.getInstance(context).read(PrefSiempo.CALL_RUNNING, false)) {
//                Log.d(TAG, "OnGoing Call is Running.. no need to generate notification");
//                return;
//            }
            try {
                DaoSession daoSession = ((Launcher3App) CoreApplication.getInstance()).getDaoSession();
                TableNotificationSmsDao smsDao = daoSession.getTableNotificationSmsDao();
                TableNotificationSms notificationSms = DBUtility.getNotificationDao().queryBuilder()
                        .where(TableNotificationSmsDao.Properties._contact_title.eq(strTitle),
                                TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                        .unique();
                if (notificationSms == null) {
                    notificationSms = new TableNotificationSms();
                    notificationSms.set_contact_title(strTitle);
                    notificationSms.set_message(strText);
                    notificationSms.set_date(date);
                    notificationSms.set_is_read(false);
                    notificationSms.setNotification_date(statusBarNotification.getPostTime());
                    notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_EVENT);
                    notificationSms.setPackageName(strPackageName);
                    notificationSms.setApp_icon(icon);
                    notificationSms.setUser_icon(largeIcon);
                    notificationSms.setNotification_id(statusBarNotification.getId());
                    long id = smsDao.insert(notificationSms);
                    notificationSms.setId(id);
                    EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                } else {
                    boolean is_Read = notificationSms.get_is_read();
                    String message = notificationSms.get_message();
                    if (is_Read) {
                        if ((!TextUtils.isEmpty(message.trim()) &&
                                !message.equalsIgnoreCase(strText.trim()))) {
                            notificationSms.setApp_icon(icon);
                            notificationSms.set_date(date);
                            notificationSms.set_is_read(false);
                            notificationSms.setPackageName(strPackageName);
                            notificationSms.setUser_icon(largeIcon);
                            notificationSms.setNotification_date(statusBarNotification.getPostTime());
                            notificationSms.set_contact_title(strTitle);
                            if (strBigText == null) {
                                notificationSms.set_message(strText);
                            } else {
                                notificationSms.set_message(strBigText);
                            }
                            smsDao.update(notificationSms);
                            EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                        }
                    } else {
                        notificationSms.setApp_icon(icon);
                        notificationSms.set_date(date);
                        notificationSms.set_is_read(false);
                        notificationSms.setPackageName(strPackageName);
                        notificationSms.setUser_icon(largeIcon);
                        notificationSms.setNotification_date(statusBarNotification.getPostTime());
                        notificationSms.set_contact_title(strTitle);
                        if (strBigText == null) {
                            notificationSms.set_message(strText);
                        } else {
                            notificationSms.set_message(strBigText);
                        }
                        smsDao.update(notificationSms);
                        EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Tracer.d("SiempoNotificationListener:parseOtherMessages" + e.getMessage());
                CoreApplication.getInstance().logException(e);
            }
        }
    }

    private void parseHangOutMessage(StatusBarNotification statusBarNotification, String strPackageName, String strTitle, String strText, Date date, String tickerText, int icon, byte[] largeIcon) {
        try {
            DaoSession daoSession = ((Launcher3App) CoreApplication.getInstance()).getDaoSession();
            TableNotificationSmsDao smsDao = daoSession.getTableNotificationSmsDao();
            if (!strTitle.trim().endsWith("new messages")) {
                String groupname = "";
                if (strTitle.contains(":")) {
                    String[] separated = strTitle.split(":");
                    strTitle = separated[0];
                    strText = separated[1] + ": " + strText;
                }
                if (statusBarNotification.getNotification().category != null
                        && !statusBarNotification.getNotification().category.equalsIgnoreCase(Notification.CATEGORY_CALL)) {
                    TableNotificationSms notificationSms
                            = DBUtility.getNotificationDao().queryBuilder()
                            .where(TableNotificationSmsDao.Properties.PackageName.eq(strPackageName),
                                    TableNotificationSmsDao.Properties._contact_title.eq(strTitle),
                                    TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                            .unique();
                    if (notificationSms == null) {
                        notificationSms = new TableNotificationSms();
                        notificationSms.set_contact_title(strTitle);
                        notificationSms.set_message(strText);
                        notificationSms.set_date(date);
                        notificationSms.set_is_read(false);
                        notificationSms.setNotification_date(statusBarNotification.getPostTime());
                        notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_EVENT);
                        notificationSms.setPackageName(strPackageName);
                        notificationSms.setApp_icon(icon);
                        notificationSms.setUser_icon(largeIcon);
                        notificationSms.setNotification_id(statusBarNotification.getId());
                        long id = smsDao.insert(notificationSms);
                        notificationSms.setId(id);
                        EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                    } else {
                        boolean is_Read = notificationSms.get_is_read();
                        String message = notificationSms.get_message().split("\n")[0];
                        if (is_Read) {
                            if ((!TextUtils.isEmpty(message.trim()) &&
                                    !message.equalsIgnoreCase(strText.trim()))) {
                                notificationSms.setPackageName(strPackageName);
                                notificationSms.set_date(date);
                                notificationSms.set_is_read(false);
                                notificationSms.setNotification_date(statusBarNotification.getPostTime());
                                notificationSms.set_message(strText + "\n" + notificationSms.get_message());
                                notificationSms.set_contact_title(strTitle);
                                smsDao.update(notificationSms);
                                EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                            }
                        } else {
                            notificationSms.setPackageName(strPackageName);
                            notificationSms.set_date(date);
                            notificationSms.set_is_read(false);
                            notificationSms.setNotification_date(statusBarNotification.getPostTime());
                            notificationSms.set_message(strText + "\n" + notificationSms.get_message());
                            notificationSms.set_contact_title(strTitle);
                            smsDao.update(notificationSms);
                            EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                        }
                    }
                } else {
                    if (!strText.equalsIgnoreCase("Incoming voice call")
                            && !strText.equalsIgnoreCase("Incoming video call")) {

                        if (tickerText.equalsIgnoreCase("Missed call")
                                && strText.equalsIgnoreCase("Missed call")) {
                            strText = strTitle;
                            strTitle = "Missed Call";
                        } else {
                            if (strTitle.contains("missed calls")) {
                                strTitle = "Missed Call";
                            }
                        }
                        TableNotificationSms notificationSms
                                = DBUtility.getNotificationDao().queryBuilder()
                                .where(TableNotificationSmsDao.Properties.PackageName.eq(strPackageName),
                                        TableNotificationSmsDao.Properties._contact_title.eq(strTitle),
                                        TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                                .unique();

                        if (notificationSms == null) {
                            notificationSms = new TableNotificationSms();
                            notificationSms.set_contact_title(strTitle);
                            notificationSms.set_message(strText);
                            notificationSms.set_date(date);
                            notificationSms.set_is_read(false);
                            notificationSms.setNotification_date(statusBarNotification.getPostTime());
                            notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_EVENT);
                            notificationSms.setPackageName(strPackageName);
                            notificationSms.setApp_icon(icon);
                            notificationSms.setUser_icon(largeIcon);
                            notificationSms.setNotification_id(statusBarNotification.getId());
                            long id = smsDao.insert(notificationSms);
                            notificationSms.setId(id);
                            EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                        } else {
                            boolean is_Read = notificationSms.get_is_read();
                            String message = notificationSms.get_message().split("\n")[0];
                            if (is_Read) {
                                if ((!TextUtils.isEmpty(message.trim()) &&
                                        !message.equalsIgnoreCase(strText.trim()))) {
                                    notificationSms.set_date(date);
                                    notificationSms.set_is_read(false);
                                    notificationSms.setNotification_date(statusBarNotification.getPostTime());
                                    notificationSms.set_message(strText);
                                    notificationSms.set_contact_title(strTitle);
                                    if (!tickerText.equalsIgnoreCase("Missed call")
                                            && !strText.equalsIgnoreCase("Missed call")) {
                                        smsDao.update(notificationSms);
                                        EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                                    }
                                }
                            } else {
                                notificationSms.set_date(date);
                                notificationSms.set_is_read(false);
                                notificationSms.setNotification_date(statusBarNotification.getPostTime());
                                notificationSms.set_message(strText);
                                notificationSms.set_contact_title(strTitle);
                                if (!tickerText.equalsIgnoreCase("Missed call")
                                        && !strText.equalsIgnoreCase("Missed call")) {
                                    smsDao.update(notificationSms);
                                    EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                                }
                            }
                        }
                    }
                }
            }
            List<TableNotificationSms> notificationSms
                    = DBUtility.getNotificationDao().queryBuilder()
                    .where(TableNotificationSmsDao.Properties.PackageName.eq(strPackageName),
                            TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                    .list();
            int count = 0;
            for (TableNotificationSms tableNotificationSms : notificationSms) {
                count = count + tableNotificationSms.get_message().split("\n").length;
            }
            logFirebaseCount(strPackageName, count);
        } catch (Exception e) {
            Tracer.d("SiempoNotificationListener:parseHangOutMessage" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseFacebookLite(StatusBarNotification statusBarNotification, String strPackageName, String strTitle, String strText, Date date, StringBuilder data, int icon, byte[] largeIcon) {
        try {
            DaoSession daoSession = ((Launcher3App) CoreApplication.getInstance()).getDaoSession();
            TableNotificationSmsDao smsDao = daoSession.getTableNotificationSmsDao();
            TableNotificationSms notificationSms
                    = DBUtility.getNotificationDao().queryBuilder()
                    .where(TableNotificationSmsDao.Properties.PackageName.eq(strPackageName),
                            TableNotificationSmsDao.Properties._contact_title.eq(strTitle),
                            TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                    .unique();
            if (notificationSms == null) {
                notificationSms = new TableNotificationSms();
                notificationSms.set_contact_title(strTitle);
                notificationSms.set_message(strText);
                notificationSms.set_date(date);
                notificationSms.set_is_read(false);
                notificationSms.setNotification_date(statusBarNotification.getPostTime());
                notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_EVENT);
                notificationSms.setPackageName(strPackageName);
                notificationSms.setApp_icon(icon);
                notificationSms.setUser_icon(largeIcon);
                notificationSms.setNotification_id(statusBarNotification.getId());
                long id = smsDao.insert(notificationSms);
                notificationSms.setId(id);
                EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
            } else {
                boolean is_Read = notificationSms.get_is_read();
                String message = notificationSms.get_message().split("\n")[0];
                if (is_Read) {
                    if ((!TextUtils.isEmpty(message.trim()) &&
                            !message.equalsIgnoreCase(strText.trim()))) {
                        notificationSms.setPackageName(strPackageName);
                        notificationSms.set_date(date);
                        notificationSms.set_is_read(false);
                        notificationSms.setNotification_date(statusBarNotification.getPostTime());
                        if (data != null && !data.toString().equalsIgnoreCase("")) {
                            notificationSms.set_message(data.toString());
                        } else {
                            notificationSms.set_message(strText + "\n" + notificationSms.get_message());
                        }
                        notificationSms.set_contact_title(strTitle);
                        smsDao.update(notificationSms);
                        EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                    }
                } else {
                    notificationSms.setPackageName(strPackageName);
                    notificationSms.set_date(date);
                    notificationSms.set_is_read(false);
                    notificationSms.setNotification_date(statusBarNotification.getPostTime());
                    if (data != null && !data.toString().equalsIgnoreCase("")) {
                        notificationSms.set_message(data.toString());
                    } else {
                        notificationSms.set_message(strText + "\n" + notificationSms.get_message());
                    }
                    notificationSms.set_contact_title(strTitle);
                    smsDao.update(notificationSms);
                    EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            Tracer.d("SiempoNotificationListener:parseFacebookLite" + e.getMessage());
            CoreApplication.getInstance().logException(e);
        }
    }

    private void parseFacebookMessenger(StatusBarNotification statusBarNotification, String strPackageName, String strTitle, String strText, Date date, int icon, byte[] largeIcon) {
        try {
            DaoSession daoSession = ((Launcher3App) CoreApplication.getInstance()).getDaoSession();
            TableNotificationSmsDao smsDao = daoSession.getTableNotificationSmsDao();
            TableNotificationSms notificationSms
                    = DBUtility.getNotificationDao().queryBuilder()
                    .where(TableNotificationSmsDao.Properties.PackageName.eq(strPackageName),
                            TableNotificationSmsDao.Properties._contact_title.eq(strTitle),
                            TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                    .unique();
            if (strTitle != null & !strTitle.equalsIgnoreCase("Chat heads active")) {
                if (notificationSms == null) {
                    notificationSms = new TableNotificationSms();
                    notificationSms.set_contact_title(strTitle);
                    notificationSms.set_message(strText);
                    notificationSms.set_date(date);
                    notificationSms.set_is_read(false);
                    notificationSms.setNotification_date(statusBarNotification.getPostTime());
                    notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_EVENT);
                    notificationSms.setPackageName(strPackageName);
                    notificationSms.setApp_icon(icon);
                    notificationSms.setUser_icon(largeIcon);
                    notificationSms.setNotification_id(statusBarNotification.getId());
                    long id = smsDao.insert(notificationSms);
                    notificationSms.setId(id);
                } else {
                    boolean is_Read = notificationSms.get_is_read();
                    String message = notificationSms.get_message().split("\n")[0];
                    if (is_Read) {
                        if ((!TextUtils.isEmpty(message.trim()) &&
                                !message.equalsIgnoreCase(strText.trim()))) {
                            notificationSms.setPackageName(strPackageName);
                            notificationSms.set_date(date);
                            notificationSms.set_is_read(false);
                            notificationSms.setNotification_date(statusBarNotification.getPostTime());
                            notificationSms.set_message(strText + "\n" + notificationSms.get_message());
                            notificationSms.set_contact_title(strTitle);
                            smsDao.update(notificationSms);
                        }
                    } else {
                        notificationSms.setPackageName(strPackageName);
                        notificationSms.set_date(date);
                        notificationSms.set_is_read(false);
                        notificationSms.setNotification_date(statusBarNotification.getPostTime());
                        notificationSms.set_message(strText + "\n" + notificationSms.get_message());
                        notificationSms.set_contact_title(strTitle);
                        smsDao.update(notificationSms);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Tracer.d("SiempoNotificationListener:parseFacebookMessenger" + e.getMessage());
            CoreApplication.getInstance().logException(e);
        }
    }

    private void parseTelegramMessenger(StatusBarNotification statusBarNotification, String strPackageName, String strTitle, String strText, Date date, int icon, byte[] largeIcon) {
        try {
            DaoSession daoSession = ((Launcher3App) CoreApplication.getInstance()).getDaoSession();
            TableNotificationSmsDao smsDao = daoSession.getTableNotificationSmsDao();
            if (strTitle != null && strText != null && !strText.equalsIgnoreCase("") && !strTitle.contains("Telegram")) {
                strTitle = checkTextContainForTelegram(strTitle).trim();
                TableNotificationSms notificationSms
                        = DBUtility.getNotificationDao().queryBuilder()
                        .where(TableNotificationSmsDao.Properties.PackageName.eq(strPackageName),
                                TableNotificationSmsDao.Properties._contact_title.eq(strTitle),
                                TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                        .unique();
                if (notificationSms == null) {
                    notificationSms = new TableNotificationSms();
                    notificationSms.set_contact_title(strTitle);
                    notificationSms.set_message(strText);
                    notificationSms.set_date(date);
                    notificationSms.set_is_read(false);
                    notificationSms.setNotification_date(statusBarNotification.getPostTime());
                    notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_EVENT);
                    notificationSms.setPackageName(strPackageName);
                    notificationSms.setApp_icon(icon);
                    notificationSms.setUser_icon(largeIcon);
                    notificationSms.setNotification_id(statusBarNotification.getId());
                    long id = smsDao.insert(notificationSms);
                    notificationSms.setId(id);
                } else {
                    boolean is_Read = notificationSms.get_is_read();
                    String message = notificationSms.get_message().split("\n")[0];
                    if (is_Read) {
                        if ((!TextUtils.isEmpty(message.trim()) &&
                                !message.equalsIgnoreCase(strText.trim()))) {
                            notificationSms.setPackageName(strPackageName);
                            notificationSms.set_date(date);
                            notificationSms.set_is_read(false);
                            notificationSms.setNotification_date(statusBarNotification.getPostTime());
                            notificationSms.set_message(strText);
                            notificationSms.set_contact_title(strTitle);
                            smsDao.update(notificationSms);
                        }
                    } else {
                        notificationSms.setPackageName(strPackageName);
                        notificationSms.set_date(date);
                        notificationSms.set_is_read(false);
                        notificationSms.setNotification_date(statusBarNotification.getPostTime());
                        notificationSms.set_message(strText);
                        notificationSms.set_contact_title(strTitle);
                        smsDao.update(notificationSms);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            Tracer.d("SiempoNotificationListener:parseFacebookMessenger" + e.getMessage());
            CoreApplication.getInstance().logException(e);
        }
    }

    private String checkTextContainForTelegram(String strTitle) {
        String title = strTitle;
        if (strTitle.endsWith("message)") || strTitle.endsWith("messages)")) {
            title = strTitle.substring(0, strTitle.indexOf("("));
        }
        return title;
    }


    private void parseFacebook(StatusBarNotification statusBarNotification, String strPackageName, String strTitle, String strText, Date date, int icon, byte[] largeIcon) {
        try {
            DaoSession daoSession = ((Launcher3App) CoreApplication.getInstance()).getDaoSession();
            TableNotificationSmsDao smsDao = daoSession.getTableNotificationSmsDao();
            TableNotificationSms notificationSms = new TableNotificationSms();
            notificationSms.set_contact_title(strTitle);
            notificationSms.set_message(strText);
            notificationSms.set_is_read(false);
            notificationSms.set_date(date);
            notificationSms.setNotification_date(statusBarNotification.getPostTime());
            notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_EVENT);
            notificationSms.setPackageName(strPackageName);
            notificationSms.setApp_icon(icon);
            notificationSms.setUser_icon(largeIcon);
            notificationSms.setNotification_id(statusBarNotification.getId());
            long id = smsDao.insert(notificationSms);
            notificationSms.setId(id);
        } catch (Exception e) {
            Tracer.d("SiempoNotificationListener:parseFacebook" + e.getMessage());
            CoreApplication.getInstance().logException(e);
            e.printStackTrace();
        }
    }

    private void parseGoogleCalender(StatusBarNotification statusBarNotification, String strPackageName, String strTitle, String strText, Date date, String strBigText, int icon, byte[] largeIcon) {
        try {
            DaoSession daoSession = ((Launcher3App) CoreApplication.getInstance()).getDaoSession();
            TableNotificationSmsDao smsDao = daoSession.getTableNotificationSmsDao();
            TableNotificationSms notificationSms = DBUtility.getNotificationDao().queryBuilder()
                    .where(TableNotificationSmsDao.Properties._contact_title.eq(strTitle),
                            TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                    .unique();
            if (notificationSms == null) {
                notificationSms = new TableNotificationSms();
                notificationSms.set_contact_title(strTitle);
                notificationSms.set_message(strText);
                notificationSms.set_date(date);
                notificationSms.set_is_read(false);
                notificationSms.setNotification_date(statusBarNotification.getPostTime());
                notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_EVENT);
                notificationSms.setPackageName(strPackageName);
                notificationSms.setApp_icon(icon);
                notificationSms.setUser_icon(largeIcon);
                notificationSms.setNotification_id(statusBarNotification.getId());
                long id = smsDao.insert(notificationSms);
                notificationSms.setId(id);
                EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
            } else {
                boolean is_Read = notificationSms.get_is_read();
                String message = notificationSms.get_message();
                if (is_Read) {
                    if ((!TextUtils.isEmpty(message.trim()) &&
                            !message.equalsIgnoreCase(strText.trim()))) {
                        notificationSms.set_date(date);
                        notificationSms.set_is_read(false);
                        notificationSms.setNotification_date(statusBarNotification.getPostTime());
                        notificationSms.set_contact_title(strTitle);
                        if (strBigText == null) {
                            notificationSms.set_message(strText);
                        } else {
                            notificationSms.set_message(strBigText);
                        }
                        smsDao.update(notificationSms);
                        EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                    }
                } else {
                    notificationSms.set_date(date);
                    notificationSms.set_is_read(false);
                    notificationSms.setNotification_date(statusBarNotification.getPostTime());
                    notificationSms.set_contact_title(strTitle);
                    if (strBigText == null) {
                        notificationSms.set_message(strText);
                    } else {
                        notificationSms.set_message(strBigText);
                    }
                    smsDao.update(notificationSms);
                    EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                }
            }

        } catch (Exception e) {
            Tracer.d("SiempoNotificationListener:parseGoogleCalender" + e.getMessage());
            CoreApplication.getInstance().logException(e);
            e.printStackTrace();
        }
    }

    private void parseWhatsappMessage(StatusBarNotification statusBarNotification, String
            strPackageName, Date date, StringBuilder data, int icon, byte[] largeIcon) {
        DaoSession daoSession = ((Launcher3App) CoreApplication.getInstance()).getDaoSession();
        TableNotificationSmsDao smsDao = daoSession.getTableNotificationSmsDao();
        try {
            if (statusBarNotification.getNotification().extras != null) {
                if (Constants.WHATSAPP_PACKAGE.equals(statusBarNotification.getPackageName())) {
                    Bundle extras = statusBarNotification.getNotification().extras;
                    String title = "";
                    String text = "";
                    Bitmap bitmap = null;

                    if (extras != null) {
                        title = extras.getCharSequence(NotificationCompat.EXTRA_TITLE) == null ? "" : extras.getCharSequence(NotificationCompat.EXTRA_TITLE).toString();
                        if (extras.getCharSequence(NotificationCompat.EXTRA_TEXT) == null) {
                            text = "";
                        } else {
                            text = extras.getCharSequence(NotificationCompat.EXTRA_TEXT).toString();
                        }
                        if (title.equalsIgnoreCase("You may have a new messages")) {
                            return;
                        }
                        text = Build.VERSION.SDK_INT >= 21 ? getNotificationTextLegacy(statusBarNotification.getNotification(), text) : getNotificationTextLegacy(statusBarNotification.getNotification(), text);
                        if (title == null || title.isEmpty() || Constants.WHATSAPP.equals(title.trim())) {
                            title = getTitleLegacy(text);
                            text = fixTextLegacy(text);
                        }
                        if (!Constants.WHATSAPP.equals(title.trim())) {
                            String[] text_comp = text.split(" ");
                            if (text_comp != null && text_comp.length > 0) {
                                String text_p1 = text_comp[0] != null ? text_comp[0] : "";
                                if (isInteger(text_p1, 10)) {
                                    return;
                                }
                            }
                            if (title.contains("@")) {
                                String title_p1 = title.substring(0, title.indexOf("@")).trim();
                                title = title.substring(title.indexOf("@") + 1).trim();
                                text = title_p1 + ": " + text;
                            }
                            if (Build.VERSION.SDK_INT > 23) {
                                title = removeXnewMessageFromSender(title);
                                if (!"CODE_IGNORE_ME".equals(title)) {
                                    title = removeColFromTitle(title);
                                    if (title.contains(":")) {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            }
                        } else {
                            return;
                        }
                    }

                    if (statusBarNotification.getNotification().category == null
                            || !statusBarNotification.getNotification().category.equalsIgnoreCase(Notification.CATEGORY_CALL)) {

                        TableNotificationSms notificationSms = DBUtility.getNotificationDao().queryBuilder()
                                .where(TableNotificationSmsDao.Properties.PackageName.eq(statusBarNotification.getPackageName()),
                                        TableNotificationSmsDao.Properties._contact_title.eq(title),
                                        TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                                .unique();
                        if (notificationSms == null) {
                            if (!title.contains("WhatsApp") && !title.equalsIgnoreCase("Checking for new messages")) {
                                notificationSms = new TableNotificationSms();
                                notificationSms.set_contact_title(title);
                                notificationSms.set_message(text);
                                notificationSms.set_date(date);
                                notificationSms.set_is_read(false);
                                notificationSms.setNotification_date(statusBarNotification.getPostTime());
                                notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_EVENT);
                                notificationSms.setPackageName(strPackageName);
                                notificationSms.setApp_icon(icon);
                                notificationSms.setUser_icon(largeIcon);
                                long id = smsDao.insertOrReplace(notificationSms);
                                notificationSms.setId(id);
                                Log.d("Test New","title : "+ title+ " Length:"+ title.length());
                                Log.d("Test New","message : "+ text+ " Length:"+ text.length());
                            }
                        } else {
                            if (!title.contains("WhatsApp") && !title.equalsIgnoreCase("Checking for new messages")) {
                                boolean is_Read = notificationSms.get_is_read();
                                String message = notificationSms.get_message().split("\n")[0];
                                Log.d("Test","is_Read "+is_Read);
                                Log.d("Test Update","title : "+ title+ " Length:"+ title.length());
                                Log.d("Test Update","text : "+ text+ " Length:"+ text.length());
                                Log.d("Test Update","message : "+ message+ " Length:"+ message.length());
                                if (is_Read) {
                                    if ((!TextUtils.isEmpty(message.trim()) &&
                                            !message.equalsIgnoreCase(text.trim()))) {
                                        notificationSms.set_is_read(false);
                                        notificationSms.set_date(date);
                                        notificationSms.setNotification_date(statusBarNotification.getPostTime());
                                        notificationSms.set_message(text /*+ "\n" + notificationSms.get_message()*/);
                                        smsDao.update(notificationSms);
                                    }
                                } else {
                                    notificationSms.set_is_read(false);
                                    notificationSms.set_date(date);
                                    notificationSms.setNotification_date(statusBarNotification.getPostTime());
                                    notificationSms.set_message(text /*+ "\n" + notificationSms.get_message()*/);
                                    smsDao.update(notificationSms);
                                }
                            }
                        }
                    }
                    if (statusBarNotification.getNotification().category == null
                            || statusBarNotification.getNotification().category.equalsIgnoreCase(Notification.CATEGORY_CALL)) {

                        if (title.toLowerCase().trim().contains("miss")
                                || title.toLowerCase().trim().contains("missed")) {
                            if (title.matches(".*\\d.*")) {
                                // contains a number
                                title = title.replaceAll("^([0-9]+)", "");
                            }
                            TableNotificationSms notificationSms = DBUtility.getNotificationDao().queryBuilder()
                                    .where(TableNotificationSmsDao.Properties.PackageName.eq(statusBarNotification.getPackageName()),
                                            TableNotificationSmsDao.Properties._contact_title.eq("Missed call"),
                                            TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                                    .unique();
                            if (notificationSms == null) {
                                if (!title.contains("WhatsApp")) {
                                    notificationSms = new TableNotificationSms();
                                    notificationSms.set_contact_title("Missed call");
                                    notificationSms.set_message(text);
                                    notificationSms.set_date(date);
                                    notificationSms.set_is_read(false);
                                    notificationSms.setNotification_date(statusBarNotification.getPostTime());
                                    notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_EVENT);
                                    notificationSms.setPackageName(strPackageName);
                                    notificationSms.setApp_icon(icon);
                                    notificationSms.setUser_icon(largeIcon);
                                    long id = smsDao.insertOrReplace(notificationSms);

                                    notificationSms.setId(id);
                                }
                            } else {
                                if (!title.contains("WhatsApp")) {
                                    boolean is_Read = notificationSms.get_is_read();
                                    String message = notificationSms.get_message().split("\n")[0];
                                    if (is_Read) {
                                        if ((!TextUtils.isEmpty(message.trim()) &&
                                                !message.equalsIgnoreCase(text.trim()))) {
                                            notificationSms.set_date(date);
                                            notificationSms.setUser_icon(null);
                                            notificationSms.set_is_read(false);
                                            notificationSms.set_contact_title("Missed call");
                                            notificationSms.setNotification_date(statusBarNotification.getPostTime());
                                            if (!data.toString().equalsIgnoreCase("")) {
                                                notificationSms.set_message(data.toString());
                                            } else {
                                                notificationSms.set_message(text + "\n" + notificationSms.get_message());
                                            }
                                            smsDao.updateInTx(notificationSms);
                                        }
                                    } else {
                                        notificationSms.set_date(date);
                                        notificationSms.setUser_icon(null);
                                        notificationSms.set_is_read(false);
                                        notificationSms.set_contact_title("Missed call");
                                        notificationSms.setNotification_date(statusBarNotification.getPostTime());
                                        if (!data.toString().equalsIgnoreCase("")) {
                                            notificationSms.set_message(data.toString());
                                        } else {
                                            notificationSms.set_message(text + "\n" + notificationSms.get_message());
                                        }
                                        smsDao.updateInTx(notificationSms);
                                    }

                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Tracer.d("SiempoNotificationListener:parseWhatsappMessage" + e.getMessage());
            CoreApplication.getInstance().logException(e);
        }
    }

    /**
     * For getting the application name from package name.
     *
     * @param packageName
     * @return
     */
    private String getAppName(String packageName) {
        ApplicationInfo ai;
        try {
            ai = getPackageManager().getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            CoreApplication.getInstance().logException(e);
            ai = null;
        }
        return (String) (ai != null ? getPackageManager().getApplicationLabel(ai) : "(unknown)");
    }

    /**
     * Send the suppressed notification count to firebase analytics.
     *
     * @param strPackageName
     * @param count
     */
    private void logFirebaseCount(String strPackageName, int count) {
        try {
            Log.d("Count Suppressed", "PackageName:" + strPackageName + " " + count);
            FirebaseHelper.getInstance().logSuppressedNotification(getAppName(strPackageName), count);
        } catch (Exception e) {
            e.printStackTrace();
            CoreApplication.getInstance().logException(e);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {
        super.onNotificationRemoved(notification);
        Tracer.d("Notification removed: " + getNotificationToString(notification));
    }

    private String getNotificationToString(StatusBarNotification notification) {
        if (notification != null && notification.getPackageName() != null
                && notification.getNotification() != null && notification.getNotification().tickerText != null) {
            return "package: " + notification.getPackageName()
                    + "Id: " + notification.getId()
                    + " Post time: " + SimpleDateFormat.getDateTimeInstance().format(new Date(notification.getPostTime()))
                    + " Details: " + notification.getNotification().toString()
                    + " Ticker: " + notification.getNotification().tickerText;
        } else {
            return "";
        }

    }

    private String getNotificationTextLegacy(Notification notification, String defaultText) {
        String notificationText = "";
        if (!(notification == null || notification.extras == null)) {
            CharSequence[] lines = notification.extras.getCharSequenceArray(NotificationCompat.EXTRA_TEXT_LINES);
            if (lines != null) {
                for (CharSequence msg : lines) {
                    if (msg != null) {
                        notificationText = msg.toString();
                    }
                }
            }
        }
        if (notificationText == null || notificationText.isEmpty()) {
            return defaultText;
        }
        return notificationText;
    }

    private String getTitleLegacy(String text) {
        if (text == null || text.indexOf(":") <= 0) {
            return text;
        }
        return text.substring(0, text.indexOf(":"));
    }

    private String fixTextLegacy(String text) {
        if (text == null || text.indexOf(":") <= 0 || text.indexOf(":") == text.length() - 1) {
            return text;
        }
        return text.substring(text.indexOf(":") + 1);
    }

    private String removeXnewMessageFromSender(String title) {
        if (title == null || !title.contains("(") || !title.contains(")")) {
            return title;
        }
        int lastParenthesiIndex = title.lastIndexOf("(");
        int lastEndParenthesisIndex = title.lastIndexOf(")");
        return "CODE_IGNORE_ME";
    }

    private String removeColFromTitle(String title) {
        String finalTitle = title == null ? null : title.trim();
        try {
            if (finalTitle != null)
                return (finalTitle.length() <= 3 || finalTitle.charAt(finalTitle.length() - 3) != ':') ? finalTitle : finalTitle.substring(0, finalTitle.length() - 3);
        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            return title;
        }
        return null;
    }

}
