package io.focuslauncher.phone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;

import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;


public abstract class PhoneCallReceiver extends BroadcastReceiver {

    private static final String TAG = "PhoneCallReceiver";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber = "";  //because the passed incoming is only valid in ringing
    public static boolean isCallRunning = false;
    private int tempoType;
    private Context mContext;
    private int currentProfile = -1;
    private AudioManager audioManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        mContext = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int sound = 0;
        if (audioManager != null) {
            sound = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        }
        Tracer.i("VolumeCheck Call Coming", sound);
        changeSoundProfile(true);
        if (intent != null) {
            Log.d(TAG, "Phone Call Receiver :: " + intent.getAction() + currentProfile);
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                if (intent.getExtras() != null && intent.getExtras().containsKey("android.intent.extra.PHONE_NUMBER")) {
                    savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
                    PrefSiempo.getInstance(context).write(PrefSiempo
                            .CALL_RUNNING, true);
                    isCallRunning = true;
                }
            } else {
                String stateStr = "", number = "";
                int state = 0;
                if (intent.getExtras() != null && intent.getExtras().containsKey(TelephonyManager.EXTRA_STATE)) {
                    stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                }
                if (intent.getExtras() != null && intent.getExtras().containsKey(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
                    number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                }
                if (stateStr != null) {
                    if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        state = TelephonyManager.CALL_STATE_IDLE;
                    } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        state = TelephonyManager.CALL_STATE_OFFHOOK;
                    } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        state = TelephonyManager.CALL_STATE_RINGING;
                    }
                }
                onCallStateChanged(context, state, number);
            }
        }
    }


    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        changeSoundProfile(false);
    }


    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {

    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    protected void onMissedCall(Context ctx, String number, Date start) {

    }

    protected void onIncomingCallAnswered(Context context, String number, Date start) {

    }

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    private void onCallStateChanged(Context context, int state, String number) {
        if (TextUtils.isEmpty(number)) {
            return;
        }
        if (lastState == state) {
            //No change, debounce extras

            return;
        }

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;

                if (!isCallRunning) {
                    changeSoundProfile(true);
                    PrefSiempo.getInstance(context).write(PrefSiempo
                            .CALL_RUNNING, true);
                    isCallRunning = true;
                }
                onIncomingCallStarted(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                changeSoundProfile(false);
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, number, callStartTime);
                } else {
                    isIncoming = true;
                    callStartTime = new Date();
                    onIncomingCallAnswered(context, savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:

                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .CALL_RUNNING, false);
                changeSoundProfile(false);
                isCallRunning = false;
                break;
        }
        lastState = state;
    }

    private void changeSoundProfile(boolean isIncreaseSound) {
        if (PackageUtil.isSiempoLauncher(mContext)) {
            tempoType = PrefSiempo.getInstance(mContext).read(PrefSiempo.TEMPO_TYPE, 0);
            if (isIncreaseSound) {
                Tracer.i("VolumeCheck Call Coming When call comes");
                if (tempoType == 1 || tempoType == 2) {
                    int sound = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                    int soundMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
                    Tracer.i("VolumeCheck Call Coming + user sound", sound);
                    Tracer.i("VolumeCheck Call Coming + max sound", soundMax);
                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && sound == 1) {
                        int volume = PrefSiempo.getInstance(mContext).read(PrefSiempo.USER_VOLUME, audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
                        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, 0);
                        Tracer.i("VolumeCheck Call Coming Update Sound");
                    }
                }
            } else {
                Tracer.i("VolumeCheck Call Coming When call disconnected or miscall");
                if ((tempoType == 1 || tempoType == 2)) {
                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                        int volume = PrefSiempo.getInstance(mContext).read(PrefSiempo.USER_VOLUME, audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
                        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, 0);
//                        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        Tracer.i("VolumeCheck Call Coming Update Sound");
                    }
                }
            }
        }
    }
}
