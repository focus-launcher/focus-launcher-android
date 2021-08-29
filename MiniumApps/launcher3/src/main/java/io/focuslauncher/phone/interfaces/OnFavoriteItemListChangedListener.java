package io.focuslauncher.phone.interfaces;

import java.util.ArrayList;

import io.focuslauncher.phone.models.MainListItem;

/**
 * Created by rajeshjadi on 18/10/17.
 */

public interface OnFavoriteItemListChangedListener {

    void onFavoriteItemListChanged(ArrayList<MainListItem> customers);
}
