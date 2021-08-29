package io.focuslauncher.phone.event;

public class StartLocationEvent {

    boolean isLocationOn;

    public StartLocationEvent(boolean isLocationOn) {
        this.isLocationOn = isLocationOn;
    }

    public boolean getIsLocationOn() {
        return isLocationOn;
    }
}
