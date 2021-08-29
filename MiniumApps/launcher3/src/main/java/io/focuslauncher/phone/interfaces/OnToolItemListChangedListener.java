package io.focuslauncher.phone.interfaces;

import java.util.ArrayList;

import io.focuslauncher.phone.models.MainListItem;

/**
 * Created by rajeshjadi on 18/10/17.
 */

public interface OnToolItemListChangedListener {

    void onToolItemListChanged(ArrayList<MainListItem> customers,int position);
}
