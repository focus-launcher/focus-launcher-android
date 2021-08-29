package io.focuslauncher.phone.fragments;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.focuslauncher.BuildConfig;
import io.focuslauncher.R;
import io.focuslauncher.phone.activities.CoreActivity;
import io.focuslauncher.phone.helper.ActivityHelper;
import io.focuslauncher.phone.utils.PrefSiempo;

@EFragment(R.layout.fragment_tempo_settings)
public class TempoSettingsFragment extends CoreFragment {

    @ViewById
    Toolbar toolbar;
    @ViewById
    RelativeLayout relHome;
    @ViewById
    RelativeLayout relAppMenu;
    @ViewById
    RelativeLayout relNotification;
    @ViewById
    RelativeLayout relDoubleTap;
    @ViewById
    RelativeLayout relAccount;
    @ViewById
    RelativeLayout relAlphaSettings;
    @ViewById
    TextView titleActionBar;

    public TempoSettingsFragment() {
        // Required empty public constructor
    }

    @AfterViews
    void afterViews() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.settings);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });


        if (BuildConfig.FLAVOR.equalsIgnoreCase(context.getString(R.string.alpha))) {
            relAlphaSettings.setVisibility(View.VISIBLE);
        } else {
            if (PrefSiempo.getInstance(context).read(PrefSiempo
                    .IS_ALPHA_SETTING_ENABLE, false)) {
                relAlphaSettings.setVisibility(View.VISIBLE);
            } else {
                relAlphaSettings.setVisibility(View.GONE);
            }
        }


    }


    @Click
    void relHome() {
        ((CoreActivity) getActivity()).loadChildFragment(TempoHomeFragment_.builder()
                .build(), R.id.tempoView);
    }

    @Click
    void relAppMenu() {
        ((CoreActivity) getActivity()).loadChildFragment(AppMenuFragment.newInstance(false), R.id.tempoView);
    }

    @Click
    void relNotification() {
        ((CoreActivity) getActivity()).loadChildFragment(TempoNotificationFragment_.builder().build(), R.id.tempoView);
    }

    @Click
    void relDoubleTap() {
        ((CoreActivity) getActivity()).loadChildFragment(DoubleTapControlsFragment_.builder().build(), R.id.tempoView);
    }

    @Click
    void relAccount() {
        ((CoreActivity) getActivity()).loadChildFragment(AccountSettingFragment_.builder().build(), R.id.tempoView);
    }

    @Click
    void relAlphaSettings() {

        new ActivityHelper(context).openSiempoAlphaSettingsApp();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }
}
