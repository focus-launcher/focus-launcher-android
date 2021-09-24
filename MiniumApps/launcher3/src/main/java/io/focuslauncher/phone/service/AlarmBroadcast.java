package io.focuslauncher.phone.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;

/**
 * Created by rajeshjadi on 8/1/18.
 */

public class AlarmBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Alarm", "" + Calendar.getInstance().getTime());
        try {
            Intent intent1 = new Intent(context, AlarmService.class);
            context.startService(intent1);
            int tempoType = PrefSiempo.getInstance(context).read(PrefSiempo
                    .TEMPO_TYPE, 0);
            if (tempoType == 1) {
                Log.d("Alarm", "Batch Receiver:");
                if (CoreApplication.Companion.getInstance() != null)
                    PackageUtil.enableDisableAlarm(PackageUtil.batchMode(context), 0);
            } else if (tempoType == 2) {
                Log.d("Alarm", "Only at Receiver:");
                if (CoreApplication.Companion.getInstance() != null)
                    PackageUtil.enableDisableAlarm(PackageUtil.getOnlyAt(context), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
