package io.focuslauncher.phone.event;

import android.location.Location;

public class LocationUpdateEvent {
    private double longitude;
    private double latitude;

    public LocationUpdateEvent(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
