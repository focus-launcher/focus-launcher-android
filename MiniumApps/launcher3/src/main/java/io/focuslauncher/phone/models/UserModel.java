package io.focuslauncher.phone.models;

/**
 * Created by rajeshjadi on 7/3/18.
 */

public class UserModel {
    private String userId;
    private String emailId;
    private double latitude;
    private double longitude;

    public UserModel() {
    }

    public UserModel(String userId, String emailId) {
        this.userId = userId;
        this.emailId = emailId;
    }

    public UserModel(String userId, String emailId,double latitude,double longitude) {
        this.userId = userId;
        this.emailId = emailId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
}
