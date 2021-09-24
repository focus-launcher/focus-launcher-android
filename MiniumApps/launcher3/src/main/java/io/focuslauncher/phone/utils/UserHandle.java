package io.focuslauncher.phone.utils;

import android.os.Process;

import io.focuslauncher.phone.app.CoreApplication;


/**
 * Wrapper class for `android.os.UserHandle` that works with all Android versions
 */
public class UserHandle {
    private long serial;
    private Object handle; // android.os.UserHandle on Android 4.2 and newer

    public UserHandle() {
        this(0, null);
    }


    public UserHandle(long serial, android.os.UserHandle user) {
        if (user != null && Process.myUserHandle().equals(user)) {
            // For easier processing the current user is also stored as `null`, even
            // if there is multi-user support
            this.serial = 0;
            this.handle = null;
        } else {
            // Store the given user handle
            this.serial = serial;
            this.handle = user;
        }
    }


    public android.os.UserHandle getRealHandle() {
        if (this.handle != null) {
            return (android.os.UserHandle) this.handle;
        } else {
            return Process.myUserHandle();
        }
    }


    public boolean isCurrentUser() {
        return (this.handle == null);
    }


    public String addUserSuffixToString(String base, char separator) {
        if (this.handle == null) {
            return base;
        } else {
            String result = base + separator +
                    this.serial;
            return result;
        }
    }

    public boolean hasStringUserSuffix(String string, char separator) {
        long serial = 0;

        int index = string.lastIndexOf((int) separator);
        if (index > -1) {
            String serialText = string.substring(index);
            try {
                serial = Long.parseLong(serialText);
            } catch (NumberFormatException e) {
                CoreApplication.Companion.getInstance().logException(e);
            }
        }

        return (serial == this.serial);
    }
}
