package io.focuslauncher.phone.db;

import java.util.List;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.interfaces.DeleteStrategy;
import io.focuslauncher.phone.models.Notification;

/**
 * Created by tkb on 2017-04-03.
 */


public class MultipleItemDelete implements DeleteStrategy {
    @Override
    public void delete(Notification notification) {
        try {
            List<TableNotificationSms> notificationSmsesList = DBUtility.getNotificationDao().queryBuilder()
                    .where(TableNotificationSmsDao.Properties._contact_title.eq(notification.getNumber()),
                            TableNotificationSmsDao.Properties.Notification_type.eq(notification.getNotificationType()))
                    .list();

            DBUtility.getNotificationDao().deleteInTx(notificationSmsesList);
        } catch (Exception e) {
            CoreApplication.Companion.getInstance().logException(e);
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAll() {
        DBUtility.getNotificationDao().deleteAll();
    }
}
