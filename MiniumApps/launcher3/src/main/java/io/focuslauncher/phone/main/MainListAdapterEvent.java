package io.focuslauncher.phone.main;

import java.util.List;

import io.focuslauncher.phone.models.MainListItem;

/**
 * Created by Shahab on 2/17/2017.
 */

public class MainListAdapterEvent {

    private int dataSize;
    private List<MainListItem> filteredData;

    public MainListAdapterEvent(int dataSize) {
        this.dataSize = dataSize;
    }

    public MainListAdapterEvent(List<MainListItem> filteredData) {
        this.filteredData = filteredData;
    }

    public int getDataSize() {
        return dataSize;
    }

    public List<MainListItem> getData() {
        return filteredData;
    }
}
