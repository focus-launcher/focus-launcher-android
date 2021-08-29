package io.focuslauncher.phone.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkUtil {



    public static final int corePoolSize = 60;
    public static final int maximumPoolSize = 80;
    public static final int keepAliveTime = 15;

    /**
     * isOnline method is used to check the network connectivity of the mobile device.
     * @param context
     * @return
     */

    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager conMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            return conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable()
                    && conMgr.getActiveNetworkInfo().isConnected();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
