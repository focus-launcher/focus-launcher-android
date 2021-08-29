package io.focuslauncher.phone.models;

import io.focuslauncher.phone.interfaces.DeleteStrategy;

/**
 * Created by tkb on 2017-04-03.
 */


public class DeleteItem {
    private DeleteStrategy deleteStrategy;

    public DeleteItem(DeleteStrategy deleteStrategy) {
        this.deleteStrategy = deleteStrategy;
    }

    public void executeDelete(Notification notification) {
        deleteStrategy.delete(notification);
    }

    public void deleteAll() {
        deleteStrategy.deleteAll();
    }
}
