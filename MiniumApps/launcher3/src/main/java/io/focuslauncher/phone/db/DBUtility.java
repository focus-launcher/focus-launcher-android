package io.focuslauncher.phone.db;

import io.focuslauncher.phone.app.Launcher3App;
import io.focuslauncher.phone.app.Launcher3App_;


public class DBUtility {
    private static TableNotificationSmsDao notificationDao = null;

    private DBUtility() {

    }

    public static TableNotificationSmsDao getNotificationDao() {
        if (notificationDao == null) {
            notificationDao = Launcher3App_.getInstance().getDaoSession().getTableNotificationSmsDao();
        }
        return notificationDao;

    }


}
