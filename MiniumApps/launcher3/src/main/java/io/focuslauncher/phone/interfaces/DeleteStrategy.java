package io.focuslauncher.phone.interfaces;

import io.focuslauncher.phone.models.Notification;

/**
 * Created by tkb on 2017-04-03.
 */


public interface DeleteStrategy {
    void delete(Notification notification);

    void deleteAll();
}
