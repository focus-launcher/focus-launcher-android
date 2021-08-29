package io.focuslauncher.phone.event;

/**
 * Created by hardik on 21/3/18.
 */

public class NotifySearchRefresh {

    boolean isNotify;

    public NotifySearchRefresh(boolean isNotify) {
        this.isNotify = isNotify;
    }

    public boolean isNotify() {
        return isNotify;
    }
}
