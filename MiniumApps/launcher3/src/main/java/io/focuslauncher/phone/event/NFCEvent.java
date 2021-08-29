package io.focuslauncher.phone.event;

import android.nfc.Tag;

/**
 * Created by Shahab on 1/9/2017.
 */

public class NFCEvent {

    private boolean isConnected;

    private Tag tag;

    public NFCEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public NFCEvent(boolean isConnected, Tag tag) {
        this(isConnected);
        this.tag = tag;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
