package io.focuslauncher.phone.msg;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.log.Tracer;

/**
 * Created by Shahab on 5/10/2016.
 */
public class SmsObserver extends ContentObserver {
    private static final Handler handler = new Handler();
    private static final Uri uri = Uri.parse("content://sms/");

    private final Context context;
    private final ContentResolver resolver;
    private final String address;
    private final String body;

    public SmsObserver(Context context, String address, String body) {
        super(handler);
        this.context = context;
        this.resolver = context.getContentResolver();
        this.address = address;
        this.body = body;

    }

    public void start() {
        if (resolver != null) {
            resolver.registerContentObserver(uri, true, this);
        } else {
            throw new IllegalStateException(
                    "Current SmsObserver instance is invalid");
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Cursor cursor = null;

        try {
            cursor = resolver.query(uri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                final int type = cursor.getInt(
                        cursor.getColumnIndex(Telephony.Sms.TYPE));

                if (type == Telephony.Sms.Sent.MESSAGE_TYPE_SENT) {
                    final String address = cursor.getString(
                            cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    final String body = cursor.getString(
                            cursor.getColumnIndex(Telephony.Sms.BODY));

                    if (PhoneNumberUtils.compare(address, this.address) &&
                            body.equals(this.body)) {

                        resolver.unregisterContentObserver(this);
                    }
                }
            }
        } catch (IllegalStateException e) {
            CoreApplication.Companion.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public interface OnSmsSentListener {
        void onSmsSent(int threadId);
    }
}