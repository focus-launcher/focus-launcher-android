package io.focuslauncher.phone.models;

/**
 * Created by itc on 20/02/17.
 */

public class Notification {
    private Long id;
    private String _text;
    private boolean _status;
    private String _time;
    private NotificationContactModel notificationContactModel;
    private int notificationType;
    private String number;

    private int app_icon;
    private byte[] user_icon;
    private String packageName;
    private int content_type;
    private int notification_id;
    private String strTitle;
    private long notitification_date;

    public Notification() {
    }

    public Notification(NotificationContactModel notificationContactModel, Long id, String number, String _text, String _time, boolean _status, int notificationType, String packageName) {
        this.notificationContactModel = notificationContactModel;
        this.id = id;
        this._text = _text;
        this._status = _status;
        this._time = _time;
        this.notificationType = notificationType;
        this.number = number;
        this.packageName = packageName;
    }

    public Notification(int notificationType, int app_icon, String packageName, String dateTime, String strTitle, String message) {
        this.notificationType = notificationType;
        this.app_icon = app_icon;
        this.packageName = packageName;
        this._time = dateTime;
        this.strTitle = strTitle;
        this._text = message;
    }

    public String get_text() {
        return _text;
    }

    public void set_text(String _text) {
        this._text = _text;
    }

    public boolean is_status() {
        return _status;
    }

    public void set_status(boolean _status) {
        this._status = _status;
    }

    public String get_time() {
        return _time;
    }

    public void set_time(String _time) {
        this._time = _time;
    }

    public NotificationContactModel getNotificationContactModel() {
        return notificationContactModel;
    }

    public void setNotificationContactModel(NotificationContactModel notificationContactModel) {
        this.notificationContactModel = notificationContactModel;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getApp_icon() {
        return app_icon;
    }

    public void setApp_icon(int app_icon) {
        this.app_icon = app_icon;
    }

    public byte[] getUser_icon() {
        return user_icon;
    }

    public void setUser_icon(byte[] user_icon) {
        this.user_icon = user_icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getContent_type() {
        return content_type;
    }

    public void setContent_type(int content_type) {
        this.content_type = content_type;
    }

    public int getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(int notification_id) {
        this.notification_id = notification_id;
    }

    public String getStrTitle() {
        return strTitle;
    }

    public void setStrTitle(String strTitle) {
        this.strTitle = strTitle;
    }

    public long getNotitification_date() {
        return notitification_date;
    }

    public void setNotitification_date(long notitification_date) {
        this.notitification_date = notitification_date;
    }
}
