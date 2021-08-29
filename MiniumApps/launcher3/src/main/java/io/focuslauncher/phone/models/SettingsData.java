package io.focuslauncher.phone.models;

/**
 * Created by hardik on 17/8/17.
 */

public class SettingsData {

    private String settingType;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSettingType() {
        return settingType;
    }

    public void setSettingType(String settingType) {
        this.settingType = settingType;
    }
}
