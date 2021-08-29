package io.focuslauncher.phone.utils;

import android.os.Build;

public class DeviceUtil {
    public static String getDeviceInfo() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String versionRelease = Build.VERSION.RELEASE;
        return String.format("%s %s %s",
                manufacturer,
                model,
                versionRelease);
    }
}
