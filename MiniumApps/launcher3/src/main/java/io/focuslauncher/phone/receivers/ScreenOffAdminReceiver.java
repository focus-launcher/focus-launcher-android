package io.focuslauncher.phone.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.focuslauncher.R;

/**
 * Receiver class which shows notifications when the Device Administrator status
 * of the application changes.
 */

public class ScreenOffAdminReceiver extends DeviceAdminReceiver {
    private void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context,
                context.getString(R.string.admin_receiver_status_enabled));
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context,
                context.getString(R.string.admin_receiver_status_disabled));
    }

}
