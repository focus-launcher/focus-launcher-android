package io.focuslauncher.phone.event;

/**
 * Created by Shahab on 2/16/2017.
 */

public class SearchLayoutEvent {

    private String str;

    public SearchLayoutEvent(String searchString) {
        this.str = searchString;
    }

    public String getString() {
        return str;
    }
}
