package io.focuslauncher.phone.event;


public class AppInstalledEvent {

    private boolean isAppInstalled;
    private int installedOrRemoved;

    public AppInstalledEvent(int installedOrRemoved) {
        this.installedOrRemoved = installedOrRemoved;
    }

    public AppInstalledEvent(boolean isAppInstalled) {
        this.isAppInstalled = isAppInstalled;
    }

    public boolean isAppInstalledSuccessfully() {
        return isAppInstalled;
    }

    public int getInstalledOrRemoved() {
        return installedOrRemoved;
    }
}
