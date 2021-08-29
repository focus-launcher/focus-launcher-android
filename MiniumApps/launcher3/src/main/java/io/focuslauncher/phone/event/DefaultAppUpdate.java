package io.focuslauncher.phone.event;

/**
 * Created by Rajesh Jadi on 8/12/2016.
 * Used for the torch enable/disable.
 */
public class DefaultAppUpdate {

    private boolean isDefaultAppUpdate;

    public DefaultAppUpdate(boolean isRunning) {
        this.isDefaultAppUpdate = isRunning;
    }

    public boolean isDefaultAppUpdate() {
        return isDefaultAppUpdate;
    }
}
