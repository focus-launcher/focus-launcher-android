package io.focuslauncher.phone.receivers;

import android.content.Context;

import java.util.Date;

import io.focuslauncher.phone.log.Tracer;

public class CallReceiver extends PhoneCallReceiver {


    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        Tracer.i("onIncomingCallStarted()");

    }


    @Override
    protected void onIncomingCallAnswered(Context context, String number, Date start) {
        Tracer.i("onOutgoingCallStarted()");
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Tracer.i("onOutgoingCallStarted()");
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Tracer.i("onIncomingCallEnded()");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Tracer.i("onOutgoingCallEnded()");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Tracer.i("onMissedCall()");

    }

}
