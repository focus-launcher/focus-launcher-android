package io.focuslauncher.phone.event;

/**
 * Created by rajeshjadi on 14/3/18.
 */

public class NotifyBottomView {
    boolean isNotify;

    public NotifyBottomView(boolean isNotify) {
        this.isNotify = isNotify;
    }

    public NotifyBottomView() {
    }

    public boolean isNotify() {
        return isNotify;
    }
}
