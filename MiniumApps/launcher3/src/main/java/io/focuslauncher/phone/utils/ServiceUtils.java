package io.focuslauncher.phone.utils;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.List;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.log.Tracer;

/**
 * Created by Shahab on 5/17/2016.
 */
public class ServiceUtils {

    private static final TextUtils.SimpleStringSplitter COLON_SPLITTER = new TextUtils.SimpleStringSplitter(':');
    private static String ENABLED_NOTIFICATION_LISTENERS = null;

    /**
     * @return True if a given {@link NotificationListenerService} is enabled.
     */
    public static <T extends NotificationListenerService> boolean isNotificationListenerServiceRunning(Context context, Class<T> clazz) {
        return isSettingsServiceEnabled(context, getEnabledNotificationListeners(), getServiceComponentNames(clazz));
    }

    private static String[] getServiceComponentNames(Class<?> clazz) {
        return new String[]{
                clazz.getPackage().getName() + '/' + clazz.getName(),
                clazz.getPackage().getName() + "/." + clazz.getSimpleName(),
                clazz.getSimpleName()};
    }

    /**
     * API 4+, check if the given {@link AccessibilityService} is enabled.
     *
     * @return True if id is an enabled {@link AccessibilityService}.
     */
    private static boolean isSettingsServiceEnabled(Context context, String setting, String[] ids) {
        // Check the list of system settings to see if a service is running.
        String eServices = Settings.Secure.getString(context.getContentResolver(), setting);
        if (!TextUtils.isEmpty(eServices) && null != ids) {
            TextUtils.SimpleStringSplitter splitter = COLON_SPLITTER;
            splitter.setString(eServices);
            while (splitter.hasNext()) {
                String aService = splitter.next();
//                if (contains(ids, aService)) {
//                    return true;
//                }
                if (contains(aService, ids)) return true;
            }
        }

        return false;
    }

    private static boolean contains(String service, String... ids) {
        try {
            if (ids != null) {
                for (String id : ids) {
                    if (service != null && !service.equalsIgnoreCase("") && service.substring(service.lastIndexOf(".")).equals(id.substring(id.lastIndexOf("."))))
                        return true;
                }
            }
        } catch (Exception e) {
            // CoreApplication.getInstance().logException(e);
            e.printStackTrace();

        }

        return false;
    }

    private static String getEnabledNotificationListeners() {
        try {
            Field field = Settings.Secure.class.getDeclaredField("ENABLED_NOTIFICATION_LISTENERS");
            if (null != field) {
                field.setAccessible(true);
                ENABLED_NOTIFICATION_LISTENERS = (String) field.get(null);
                return ENABLED_NOTIFICATION_LISTENERS;
            }
        } catch (Throwable t) {
            CoreApplication.getInstance().logException(t);
            Tracer.e(t, "getEnabledNotificationListeners()");
        }
        ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
        return ENABLED_NOTIFICATION_LISTENERS;
    }

    /**
     * Checks that value is present as at least one of the elements of the array.
     *
     * @param array the array to check in
     * @param value the value to check for
     * @return true if the value is present in the array
     */
    public static <T> boolean contains(T[] array, T value) {
        for (T element : array) {
            if (element == null) {
                if (value == null) return true;
            } else {
                if (value != null && element.equals(value)) return true;
            }
        }
        return false;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        return isMyServiceRunning(context, serviceClass.getName());
    }

    private static boolean isMyServiceRunning(Context context, String serviceClass) {
        if (null == serviceClass || null == context) return false;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = null;
        if (manager != null) {
            services = manager.getRunningServices(Integer.MAX_VALUE);
        }
        for (ActivityManager.RunningServiceInfo service : services) {
            if (serviceClass.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
