package io.focuslauncher.phone.event;

/**
 * Created by rajeshjadi on 14/3/18.
 */

public class NotifyFavortieView {
    boolean isNotify;

    public NotifyFavortieView(boolean isNotify) {
        this.isNotify = isNotify;
    }

    public boolean isNotify() {
        return isNotify;
    }
}
