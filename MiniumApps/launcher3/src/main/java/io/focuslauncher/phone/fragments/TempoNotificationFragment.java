package io.focuslauncher.phone.fragments;

import android.app.FragmentManager;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.NotificationActivity;
import io.focuslauncher.phone.utils.PrefSiempo;

/**
 * Note : AllowPicking related stuff is now disable.
 */
@EFragment(R.layout.fragment_tempo_notifications)
public class TempoNotificationFragment extends CoreFragment {

    @ViewById
    Toolbar toolbar;

    @ViewById
    TextView titleActionBar;

    @ViewById
    TextView txtAllowAppsText;
    @ViewById
    TextView txtAllowApps;

    @ViewById
    Switch switchAllowPicking;


    @ViewById
    RelativeLayout relContainerAllowSpecificApps;

    @ViewById
    RelativeLayout relAllowPicking;
    @ViewById
    TextView txtAllowPickingtxt;

    public TempoNotificationFragment() {
        // Required empty public constructor
    }


    @AfterViews
    void afterViews() {
        toolbar.setTitle(R.string.string_notification_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });
        switchAllowPicking.setChecked(PrefSiempo.getInstance(context).read(PrefSiempo.ALLOW_PEAKING, true));
        if (switchAllowPicking.isChecked()) {
            txtAllowPickingtxt.setText(getString(R.string.msg_allowpeakingon));
        } else {
            txtAllowPickingtxt.setText(getString(R.string.msg_allowpeaking));
        }
    }

    @Click
    void relContainerAllowSpecificApps() {
        Intent i = new Intent(getActivity(), NotificationActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    @Click
    void relAllowPicking() {
        boolean allowPeaking = PrefSiempo.getInstance(context).read(PrefSiempo.ALLOW_PEAKING, true);

        allowPeaking = !allowPeaking;
        switchAllowPicking.setChecked(allowPeaking);
        PrefSiempo.getInstance(context).write(PrefSiempo.ALLOW_PEAKING, allowPeaking);
        if (switchAllowPicking.isChecked()) {
            txtAllowPickingtxt.setText(getString(R.string.msg_allowpeakingon));
        } else {
            txtAllowPickingtxt.setText(getString(R.string.msg_allowpeaking));
        }
    }


}
