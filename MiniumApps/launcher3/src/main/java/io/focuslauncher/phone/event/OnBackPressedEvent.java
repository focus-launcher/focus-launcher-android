package io.focuslauncher.phone.event;

/**
 * Created by RajeshJadi on 8/12/2016.
 */
public class OnBackPressedEvent {

    private boolean isBackPressed;

    public OnBackPressedEvent(boolean isBackPressed) {
        this.isBackPressed = isBackPressed;
    }

    public boolean isBackPressed() {
        return isBackPressed;
    }
}
