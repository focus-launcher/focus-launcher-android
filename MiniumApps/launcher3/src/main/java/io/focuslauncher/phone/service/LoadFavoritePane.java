package io.focuslauncher.phone.service;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.util.ArrayList;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.event.NotifyFavortieView;
import io.focuslauncher.phone.models.MainListItem;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;
import de.greenrobot.event.EventBus;

/**
 * Created by rajeshjadi on 14/3/18.
 */

public class LoadFavoritePane extends AsyncTask<String, String, ArrayList<MainListItem>> {

    PrefSiempo prefSiempo;

    public LoadFavoritePane(PrefSiempo prefSimepo) {
        this.prefSiempo = prefSimepo;
    }

    @Override
    protected ArrayList<MainListItem> doInBackground(String... strings) {
        ArrayList<MainListItem> items;
        /**
         * Changes for SSA-1770 for checking whether Favourite Item list is empty or not.
         */
        items = PackageUtil.getFavoriteList(false);

        int itemsSize = items.size();
        int tempFavSize = 0;
        for (MainListItem favListItems : items) {
            if (TextUtils.isEmpty(favListItems.getPackageName())) {
                tempFavSize++;
            }
        }
        if (itemsSize == tempFavSize) {
            prefSiempo.write(PrefSiempo.FAVORITE_SORTED_MENU, "");
            items = PackageUtil.getFavoriteList( true);
        } else {
            items = PackageUtil.getFavoriteList( false);
        }
        return items;
    }

    @Override
    protected void onPostExecute(ArrayList<MainListItem> s) {
        super.onPostExecute(s);
        CoreApplication.getInstance().setFavoriteItemsList(s);
        EventBus.getDefault().postSticky(new NotifyFavortieView(true));
    }
}
