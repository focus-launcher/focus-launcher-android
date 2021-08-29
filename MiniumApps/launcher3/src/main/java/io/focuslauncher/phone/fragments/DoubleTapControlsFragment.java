package io.focuslauncher.phone.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.CoreActivity;
import io.focuslauncher.phone.receivers.ScreenOffAdminReceiver;
import io.focuslauncher.phone.utils.PermissionUtil;
import io.focuslauncher.phone.utils.PrefSiempo;

import static android.content.Context.NOTIFICATION_SERVICE;

@EFragment(R.layout.fragment_doubletap_control)
public class DoubleTapControlsFragment extends CoreFragment {

    @ViewById
    Toolbar toolbar;

    @ViewById
    Switch switchSleep;

    @ViewById
    RelativeLayout relSleep;

    @ViewById
    Switch switchDnD;

    @ViewById
    RelativeLayout relDnD;

    private PermissionUtil permissionUtil;

    public DoubleTapControlsFragment() {
        // Required empty public constructor
    }


    @AfterViews
    void afterViews() {
        // Download siempo images
        if (permissionUtil == null) {
            permissionUtil = new PermissionUtil(getActivity());
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.string_doubletap_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });
        switchSleep.setChecked(PrefSiempo.getInstance(getActivity()).read(PrefSiempo
                .IS_SLEEP_ENABLE, false));
        switchSleep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    checkDeviceAdminAccessGranted(false);
                } else {
                    if(checkDeviceAdminAccessGranted(true)) {
                       // ((CoreActivity) getActivity()).disableSleepMode();
                    }
                }

                PrefSiempo.getInstance(getActivity()).write(PrefSiempo
                        .IS_SLEEP_ENABLE, isChecked);
            }
        });

        if (PrefSiempo.getInstance(getActivity()).read(PrefSiempo
                .IS_DND_ENABLE, false)) {
            switchDnD.setChecked(true);
        } else {
            switchDnD.setChecked(false);
        }

        switchDnD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    checkNotificationAccessGranted(false);
                } else {
                    if(checkNotificationAccessGranted(true)) {
                        ((CoreActivity) getActivity()).changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);
                    }
                }
                PrefSiempo.getInstance(getActivity()).write(PrefSiempo
                        .IS_DND_ENABLE, isChecked);
            }
        });

    }

    private boolean checkNotificationAccessGranted(boolean onlyAccessCheck) {
        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // If api level minimum 23
            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                if (!onlyAccessCheck) {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivityForResult(intent, 111);
                }
                return false;
            }

            return true;

        }
            return false;
        }

        private boolean checkDeviceAdminAccessGranted(boolean onlyAccessCheck) {
            DevicePolicyManager policyManager = (DevicePolicyManager) context
                    .getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminReceiver = new ComponentName(context,
                    ScreenOffAdminReceiver.class);
            boolean admin = policyManager.isAdminActive(adminReceiver);
            if(onlyAccessCheck) {
                return admin;
            } else {
                if(!admin) {
                    // ask for device administration rights
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    ComponentName mDeviceAdmin = new ComponentName(getActivity(), ScreenOffAdminReceiver.class);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.device_admin_description);
                    startActivityForResult(intent, 112);
                }
                return false;
            }

        }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 111) {
            if(resultCode == Activity.RESULT_OK) {
                if(!checkNotificationAccessGranted(true)) {
                    Toast.makeText(getActivity(), "Please grant notification access", Toast.LENGTH_LONG).show();
                }
            } else {
                switchDnD.setChecked(false);
            }
        } else if(requestCode == 112) {
            if(resultCode == Activity.RESULT_OK) {
                if(!checkDeviceAdminAccessGranted(true)) {
                    Toast.makeText(getActivity(), "Please grant device admin access", Toast.LENGTH_LONG).show();
                }
            } else {
                switchSleep.setChecked(false);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Click
    void relSleep() {
        switchSleep.performClick();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Click
    void relDnD() {
        switchDnD.performClick();
    }
}
