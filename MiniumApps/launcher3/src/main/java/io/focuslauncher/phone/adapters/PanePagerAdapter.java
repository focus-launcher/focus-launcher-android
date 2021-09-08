package io.focuslauncher.phone.adapters;


import android.app.Fragment;
import android.app.FragmentManager;
import androidx.legacy.app.FragmentPagerAdapter;

import io.focuslauncher.phone.fragments.FavoritePaneFragment;
import io.focuslauncher.phone.fragments.JunkFoodPaneFragment;
import io.focuslauncher.phone.fragments.ToolsPaneFragment;

/**
 * A simple pager adapter that represents 2 Fragment objects(Intention,Pane) in
 * sequence.
 * Created by rajeshjadi on 2/2/18.
 */

public class PanePagerAdapter extends FragmentPagerAdapter {
    public PanePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = JunkFoodPaneFragment.newInstance();
                break;
            case 1:
                fragment = FavoritePaneFragment.newInstance();
                break;
            case 2:
                fragment = ToolsPaneFragment.newInstance();
                break;
            default:
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

}

