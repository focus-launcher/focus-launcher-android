package io.focuslauncher.phone.fragments;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.CoreActivity;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.launcher.FakeLauncherActivity;
import io.focuslauncher.phone.utils.PrefSiempo;

@EFragment(R.layout.fragment_tempo_account_settings)
public class AccountSettingFragment extends CoreFragment {


    @ViewById
    TextView txt_privacyPolicy;

    @ViewById
    Toolbar toolbar;

    @ViewById
    TextView titleActionBar;

    @ViewById
    Switch swtch_analytics;


    @ViewById
    RelativeLayout relUpdateEmail;


    @ViewById
    RelativeLayout relAnalytics;


    @ViewById
    RelativeLayout relChangeHome;
    private long startTime = 0;


    public AccountSettingFragment() {
        // Required empty public constructor
    }

    @AfterViews
    void afterViews() {
        toolbar.setTitle(R.string.string_account_service_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });

        txt_privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CoreActivity) getActivity()).loadChildFragment(PrivacyPolicyFragment_.builder().build(), R.id.tempoView);
            }
        });


        if (PrefSiempo.getInstance(context).read(PrefSiempo
                .IS_FIREBASE_ANALYTICS_ENABLE, true)) {
            swtch_analytics.setChecked(true);
        } else {
            swtch_analytics.setChecked(false);
        }
        swtch_analytics.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PrefSiempo.getInstance(context).write(PrefSiempo
                            .IS_FIREBASE_ANALYTICS_ENABLE, true);
                    FirebaseHelper.getInstance().getFirebaseAnalytics().setAnalyticsCollectionEnabled(true);
                } else {

                    FirebaseHelper.getInstance().getFirebaseAnalytics().setAnalyticsCollectionEnabled(false);
                    PrefSiempo.getInstance(context).write(PrefSiempo
                            .IS_FIREBASE_ANALYTICS_ENABLE, false);
                }
            }
        });
    }

    @Click
    void relUpdateEmail() {
        ((CoreActivity) getActivity()).loadChildFragment(TempoUpdateEmailFragment_.builder().build(), R.id.tempoView);
    }


    @Click
    void relAnalytics() {
        swtch_analytics.performClick();
    }

    @Click
    void relChangeHome() {
        showAlertForFirstTime();
    }

    @Override
    public void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        FirebaseHelper.getInstance().logScreenUsageTime(this.getClass().getSimpleName(), startTime);
    }

    /**
     * This dialog used when user press exit siempo service.
     */
    private void showAlertForFirstTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.exiting_siempo));
        builder.setMessage(R.string.exiting_siempo_msg);
        builder.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                resetPreferredLauncherAndOpenChooser(context);

            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.dialog_blue));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.dialog_blue));
    }

    /**
     * Method to reset the launcher activity and show default chooser
     *
     * @param context
     */
    private void resetPreferredLauncherAndOpenChooser(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, FakeLauncherActivity.class);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            Intent selector = new Intent(Intent.ACTION_MAIN);
            selector.addCategory(Intent.CATEGORY_HOME);
            selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(selector);

            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

        } catch (Exception e) {
            e.printStackTrace();
            //In case of exception start the default setting screen
        }
    }

}
