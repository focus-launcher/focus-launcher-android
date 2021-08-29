package io.focuslauncher.phone.models;

/**
 * Created by rajeshjadi on 7/2/18.
 */

public class AppMenu {
    private boolean isVisible = false;
    private boolean isBottomDoc = false;
    private String applicationName = "";

    public AppMenu(boolean isVisible, boolean isBottomDoc, String applicationName) {
        this.isVisible = isVisible;
        this.isBottomDoc = isBottomDoc;
        this.applicationName = applicationName;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public boolean isBottomDoc() {
        return isBottomDoc;
    }

    public void setBottomDoc(boolean bottomDoc) {
        isBottomDoc = bottomDoc;
    }
}
