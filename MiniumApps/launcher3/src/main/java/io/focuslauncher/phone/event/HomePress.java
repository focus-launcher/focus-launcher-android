package io.focuslauncher.phone.event;

/**
 * Created by rajeshjadi on 14/3/18.
 */

public class HomePress {

    private int currentIndexDashboard;
    private int currentIndexPaneFragment;

    public HomePress(int currentIndexDashboard, int currentIndexPaneFragment) {
        this.currentIndexDashboard = currentIndexDashboard;
        this.currentIndexPaneFragment = currentIndexPaneFragment;
    }

    public int getCurrentIndexDashboard() {
        return currentIndexDashboard;
    }

    public int getCurrentIndexPaneFragment() {
        return currentIndexPaneFragment;
    }
}
