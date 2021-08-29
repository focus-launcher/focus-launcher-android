package io.focuslauncher.phone.db;

import java.util.List;

import io.focuslauncher.phone.log.Tracer;


public class DBClient {

    public void deleteMsgByType(int type) {
        Tracer.i("Deleting Msg by type");
        try {
            List<TableNotificationSms> notificationSmsesList = DBUtility.getNotificationDao().queryBuilder()
                    .where(TableNotificationSmsDao.Properties.Notification_type.eq(type))
                    .list();

            DBUtility.getNotificationDao().deleteInTx(notificationSmsesList);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deleteMsgById(int type) {
        Tracer.i("Deleting Msg by type");
        try {
            List<TableNotificationSms> notificationSmsesList = DBUtility.getNotificationDao().queryBuilder()
                    .where(TableNotificationSmsDao.Properties.Id.eq(type))
                    .list();

            DBUtility.getNotificationDao().deleteInTx(notificationSmsesList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMsgByPackageName(String packageName) {
        try {
            List<TableNotificationSms> tableNotificationSms = DBUtility.getNotificationDao().queryBuilder()
                    .where(TableNotificationSmsDao.Properties.PackageName.eq(packageName)).list();
            if (tableNotificationSms != null && tableNotificationSms.size() > 0) {
                Tracer.i("Deleting Msg by PackageName");
                DBUtility.getNotificationDao().deleteInTx(tableNotificationSms);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
