package io.focuslauncher.phone.event;

/**
 * Created by Shahab on 1/5/2017.
 */

public class DownloadApkEvent {

    private String path;

    public DownloadApkEvent(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
