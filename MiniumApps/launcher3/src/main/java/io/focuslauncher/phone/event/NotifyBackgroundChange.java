package io.focuslauncher.phone.event;

/**
 * Created by rajeshjadi on 14/3/18.
 */

public class NotifyBackgroundChange {
    boolean isNotify;

    public NotifyBackgroundChange(boolean isNotify) {
        this.isNotify = isNotify;
    }

    public boolean isNotify() {
        return isNotify;
    }
}
