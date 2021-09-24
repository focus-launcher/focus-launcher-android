package io.focuslauncher.phone.db;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.interfaces.DeleteStrategy;
import io.focuslauncher.phone.models.Notification;
import io.focuslauncher.phone.utils.NotificationUtility;

/**
 * Created by tkb on 2017-04-03.
 */


public class SingleItemDelete implements DeleteStrategy {
    @Override
    public void delete(Notification notification) {
        try {
            TableNotificationSms notificationSms;
            if (notification.getNotificationType() == NotificationUtility.NOTIFICATION_TYPE_EVENT) {
                notificationSms = DBUtility.getNotificationDao().queryBuilder()
                        .where(TableNotificationSmsDao.Properties.Id.eq(notification.getId()))
                        .unique();
            } else {
                notificationSms = DBUtility.getNotificationDao().queryBuilder()
                        .where(TableNotificationSmsDao.Properties._contact_title.eq(notification.getNumber()),
                                TableNotificationSmsDao.Properties.Notification_type.eq(notification.getNotificationType()),
                                TableNotificationSmsDao.Properties.Id.eq(notification.getId()))
                        .unique();
            }
            if (notificationSms != null) {
                DBUtility.getNotificationDao().delete(notificationSms);
            }
        } catch (Exception e) {
            CoreApplication.Companion.getInstance().logException(e);
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAll() {

    }
}
