package io.focuslauncher.phone.msg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.app.Launcher3App;
import io.focuslauncher.phone.db.DBUtility;
import io.focuslauncher.phone.db.DaoSession;
import io.focuslauncher.phone.db.TableNotificationSms;
import io.focuslauncher.phone.db.TableNotificationSmsDao;
import io.focuslauncher.phone.event.NewNotificationEvent;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.utils.NotificationUtility;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;
import de.greenrobot.event.EventBus;

/**
 * Created by Shahab on 7/29/2016.
 */

public class SmsReceiver extends BroadcastReceiver {

    Set<String> blockedApps = new HashSet<>();
    private String mAddress;
    private String mBody;
    private Date mDate;
    private AudioManager audioManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Tracer.d("SmsReceiver: onReceive");
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (intent != null && intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Tracer.d("SmsReceiver: onReceive" + bundle.toString());
                Object messages[] = (Object[]) bundle.get("pdus");
                SmsMessage smsMessage[] = new SmsMessage[0];
                if (messages != null) {
                    smsMessage = new SmsMessage[messages.length];
                }
                for (int n = 0; n < messages.length; n++) {
                    smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
                }

                SmsMessage sms = smsMessage[0];
                if (smsMessage.length == 1 || sms.isReplace()) {
                    mBody = sms.getDisplayMessageBody();
                } else {
                    StringBuilder bodyText = new StringBuilder();
                    for (SmsMessage message : smsMessage) {
                        if (message != null) {
                            bodyText.append(message.getMessageBody());
                        }
                    }
                    mBody = bodyText.toString();
                }

                mAddress = sms.getDisplayOriginatingAddress(); // sms..getOriginatingAddress();
                mDate = new Date(sms.getTimestampMillis());


                if (PackageUtil.isSiempoLauncher(context)) {
                    int tempoType = PrefSiempo.getInstance(context).read(PrefSiempo.TEMPO_TYPE, 0);
                    blockedApps = PrefSiempo.getInstance(context).read(PrefSiempo.BLOCKED_APPLIST,
                            new HashSet<String>());
                    Tracer.d("SmsReceiver: onReceive blockedApps" + blockedApps.size());
                    String messagingAppPackage = Telephony.Sms.getDefaultSmsPackage(context);
                    if (null != blockedApps && blockedApps.size() > 0 && !TextUtils.isEmpty(messagingAppPackage)) {
                        for (String blockedApp : blockedApps) {
                            if (blockedApp.equalsIgnoreCase(messagingAppPackage)) {
                                if (PrefSiempo.getInstance(context).read(PrefSiempo
                                        .TEMPO_TYPE, 0) != 0) {
                                    Tracer.d("SmsReceiver: onReceive saveMessage");
                                    if (tempoType == 1 || tempoType == 2) {
                                        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                            int sound = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
                                            if (sound != 1) {
                                                Tracer.d("SiempoNotificationListener:audioManager");
                                                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                            }
                                        }
                                    }
                                    // saveMessage(mAddress, mBody, mDate, context);
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private void saveMessage(String address, String body, Date date, Context context) {
        try {
            DaoSession daoSession = ((Launcher3App) CoreApplication.Companion.getInstance()).getDaoSession();
            TableNotificationSmsDao smsDao = daoSession.getTableNotificationSmsDao();
            TableNotificationSms notificationSms = DBUtility.getNotificationDao().queryBuilder()
                    .where(TableNotificationSmsDao.Properties._contact_title.eq(address),
                            TableNotificationSmsDao.Properties.Notification_type.eq(NotificationUtility.NOTIFICATION_TYPE_EVENT))
                    .unique();
            if (notificationSms == null) {
                notificationSms = new TableNotificationSms();
                notificationSms.set_is_read(false);
                notificationSms.set_contact_title(address);
                notificationSms.set_message(body);
                notificationSms.set_date(date);
                notificationSms.setNotification_date(System.currentTimeMillis());
                notificationSms.setNotification_type(NotificationUtility.NOTIFICATION_TYPE_SMS);
                notificationSms.setPackageName(Telephony.Sms.getDefaultSmsPackage(context));
                long id = smsDao.insert(notificationSms);
                notificationSms.setId(id);
                EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
            } else {
                boolean is_Read = notificationSms.get_is_read();
                String message = notificationSms.get_message();
                if (is_Read) {
                    if ((!TextUtils.isEmpty(message) &&
                            !message.equalsIgnoreCase(body))) {
                        notificationSms.set_date(date);
                        notificationSms.set_is_read(false);
                        notificationSms.setNotification_date(System.currentTimeMillis());
                        notificationSms.set_contact_title(address);
                        notificationSms.set_message(message + "\n" + body);
                        smsDao.update(notificationSms);
                        EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                    }
                } else {
                    notificationSms.set_date(date);
                    notificationSms.set_is_read(false);
                    notificationSms.setNotification_date(System.currentTimeMillis());
                    notificationSms.set_contact_title(address);
                    notificationSms.set_message(message + "\n" + body);
                    smsDao.update(notificationSms);
                    EventBus.getDefault().post(new NewNotificationEvent(notificationSms));
                }
            }
        } catch (Exception e) {
            Tracer.d("SmsReceiver: onReceive saveMessage" + e.getMessage());
            e.printStackTrace();
            CoreApplication.Companion.getInstance().logException(e);
        }
    }


}
