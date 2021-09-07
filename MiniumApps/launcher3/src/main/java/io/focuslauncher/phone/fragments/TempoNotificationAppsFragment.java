package io.focuslauncher.phone.fragments;

import android.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.CoreActivity;

@EFragment(R.layout.fragment_tempo_notifications)
public class TempoNotificationAppsFragment extends CoreFragment {


    @ViewById
    Toolbar toolbar;

    @ViewById
    TextView txtAllowAppsText;
    @ViewById
    TextView txtAllowApps;


    public TempoNotificationAppsFragment() {
        // Required empty public constructor
    }


    @AfterViews
    void afterViews() {

        toolbar.setTitle(R.string.allow_specific_apps);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });

    }


    @Click
    void relAllowSpecificApps() {

        ((CoreActivity) getActivity()).loadChildFragment(TempoNotificationFragment_.builder().build(), R.id.tempoView);


    }


}
