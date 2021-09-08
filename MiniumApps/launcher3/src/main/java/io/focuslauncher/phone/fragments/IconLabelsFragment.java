package io.focuslauncher.phone.fragments;

import android.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Switch;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.focuslauncher.R;
import io.focuslauncher.phone.utils.PrefSiempo;

@EFragment(R.layout.fragment_icon_labels)
public class IconLabelsFragment extends CoreFragment {

    @ViewById
    Toolbar toolbar;

    @ViewById
    Switch switchIconToolsVisibility;

    @ViewById
    Switch switchIconFavoriteVisibility;

    @ViewById
    Switch switchIconJunkFoodVisibility;




    public IconLabelsFragment() {
        // Required empty public constructor
    }


    @AfterViews
    void afterViews() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.icon_label);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });

        switchIconToolsVisibility.setChecked(PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_ICON_TOOLS_TEXT_VISIBILITY_ENABLE, false));
        switchIconToolsVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sb = (Switch) v;
                if (sb.isChecked()) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_TOOLS_TEXT_VISIBILITY_ENABLE, true);
                } else  {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_TOOLS_TEXT_VISIBILITY_ENABLE, false);
                }
            }
        });

        switchIconFavoriteVisibility.setChecked(PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_ICON_FAVORITE_TEXT_VISIBILITY_ENABLE, false));
        switchIconFavoriteVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sb = (Switch) v;
                if (sb.isChecked()) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_FAVORITE_TEXT_VISIBILITY_ENABLE, true);
                } else  {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_FAVORITE_TEXT_VISIBILITY_ENABLE, false);
                }
            }
        });

        switchIconJunkFoodVisibility.setChecked(PrefSiempo.getInstance(getActivity()).read(PrefSiempo.DEFAULT_ICON_JUNKFOOD_TEXT_VISIBILITY_ENABLE, false));
        switchIconJunkFoodVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sb = (Switch) v;
                if (sb.isChecked()) {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_JUNKFOOD_TEXT_VISIBILITY_ENABLE, true);
                } else  {
                    PrefSiempo.getInstance(getActivity()).write(PrefSiempo.DEFAULT_ICON_JUNKFOOD_TEXT_VISIBILITY_ENABLE, false);
                }
            }
        });

    }

}
