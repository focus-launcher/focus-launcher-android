package io.focuslauncher.phone.models;

public class AlarmData {
    private int hours;
    private int minute;
    private String index;

    public AlarmData(int hours, int minute, String index) {
        this.hours = hours;
        this.minute = minute;
        this.index = index;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}