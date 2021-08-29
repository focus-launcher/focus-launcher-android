package io.focuslauncher.phone.event;

import io.focuslauncher.phone.db.TableNotificationSms;

/**
 * Created by rajeshjadi on 7/9/17.
 * This class is used for send the data to notification fragment when new message or miscalled.
 */

public class NewNotificationEvent {
    private TableNotificationSms topTableNotificationSmsDao;


    public NewNotificationEvent(TableNotificationSms topTableNotificationSmsDao) {
        this.topTableNotificationSmsDao = topTableNotificationSmsDao;
    }

    public TableNotificationSms getTopTableNotificationSmsDao() {
        return topTableNotificationSmsDao;
    }

}
