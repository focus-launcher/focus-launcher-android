package co.siempo.phone.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import co.siempo.phone.R;
import co.siempo.phone.app.Constants;
import co.siempo.phone.app.CoreApplication;
import co.siempo.phone.event.HomePressEvent;
import co.siempo.phone.log.Tracer;
import co.siempo.phone.utils.PermissionUtil;
import co.siempo.phone.utils.UIUtils;
import de.greenrobot.event.Subscribe;

@EActivity(R.layout.activity_permission)
public class SiempoPermissionActivity extends CoreActivity {

    @ViewById
    Toolbar toolbar;
    @ViewById
    Switch switchContactPermission;
    @ViewById
    Switch switchCallPermission;
    @ViewById
    Switch switchSmsPermission;

    @ViewById
    Switch switchFilePermission;
    @ViewById
    Switch switchNotificationAccess;
    @ViewById
    Switch switchOverlayAccess;
    @ViewById
    Button btnContinue;
    @ViewById
    TextView txtPermissionLabel;

    @ViewById
    TableRow tblLocation;
    @ViewById
    TableRow tblCalls;
    @ViewById
    TableRow tblContact;
    @ViewById
    TableRow tblSMS;
    @ViewById
    TableRow tblNotification;
    @ViewById
    TableRow tblDrawOverlay;
    @ViewById
    TableRow tblStorage;
    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Log.d("TAG", "Permission granted");

        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            UIUtils.toast(SiempoPermissionActivity.this, "Permission denied");

            TedPermission.with(SiempoPermissionActivity.this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("If you reject this permission," +
                            "you can not use Siempo\nPlease turn on" +
                            " " +
                            "permissions at [Setting] > [Permission]")
                    .setPermissions(Constants.PERMISSIONS)
                    .check();
        }
    };
    //    @Pref
//    Launcher3Prefs_ launcher3Prefs;
    CompoundButton.OnClickListener onClickListener = new CompoundButton.OnClickListener()

    {
        @Override
        public void onClick(View v) {

//            Switch aSwitch = (Switch) v;
//            if (aSwitch.isChecked()) {
//                UIUtils.toastShort(SiempoPermissionActivity.this, R.string.runtime_permission_text);
//
//            }
//            startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())), PermissionUtil.APP_PERMISSION);


            switch (v.getId()) {
                case R.id.tblCalls:
                    TedPermission.with(SiempoPermissionActivity.this)
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage("If you reject this permission," +
                                    "you can not use Siempo\nPlease turn on" +
                                    " " +
                                    "permissions at [Setting] > [Permission]")
                            .setPermissions(Manifest.permission.CALL_PHONE)
                            .check();

                    break;

                case R.id.tblContact:
                    TedPermission.with(SiempoPermissionActivity.this)
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage("If you reject this permission," +
                                    "you can not use Siempo\nPlease turn on" +
                                    " " +
                                    "permissions at [Setting] > [Permission]")
                            .setPermissions(Manifest.permission
                                    .READ_CONTACTS, Manifest.permission
                                    .WRITE_CONTACTS)
                            .check();
                    break;
                case R.id.tblSMS:

                    TedPermission.with(SiempoPermissionActivity.this)
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage("If you reject this permission," +
                                    "you can not use Siempo\nPlease turn on" +
                                    " " +
                                    "permissions at [Setting] > [Permission]")
                            .setPermissions(Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.SEND_SMS, Manifest
                                            .permission.READ_SMS)
                            .check();
                    break;
                case R.id.tblStorage:
                    TedPermission.with(SiempoPermissionActivity.this)
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage("If you reject this permission," +
                                    "you can not use Siempo\nPlease turn on" +
                                    " " +
                                    "permissions at [Setting] > [Permission]")
                            .setPermissions(Manifest.permission
                                    .WRITE_EXTERNAL_STORAGE, Manifest
                                    .permission.READ_EXTERNAL_STORAGE)
                            .check();
                    break;


            }


        }
    };
    private PermissionUtil permissionUtil;
    private boolean isFromHome;
    private ProgressDialog pd;

    @AfterViews
    void afterViews() {
        Log.d("Test", "P4");
        permissionUtil = new PermissionUtil(this);
        setSupportActionBar(toolbar);

        tblSMS.setOnClickListener(onClickListener);
        tblContact.setOnClickListener(onClickListener);
        tblCalls.setOnClickListener(onClickListener);
        tblStorage.setOnClickListener(onClickListener);

        switchSmsPermission.setClickable(false);
        switchContactPermission.setClickable(false);
        switchCallPermission.setClickable(false);
        switchFilePermission.setClickable(false);
        switchNotificationAccess.setClickable(false);

        tblNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchNotificationAccess.isChecked()) {
                    if (!new PermissionUtil(SiempoPermissionActivity.this)
                            .hasGiven(PermissionUtil
                                    .NOTIFICATION_ACCESS)) {
                        startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), PermissionUtil.NOTIFICATION_ACCESS);
                    }
                } else {
                    startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), PermissionUtil.NOTIFICATION_ACCESS);
                }
            }
        });


        Intent intent = getIntent();
        if (intent != null) {
            isFromHome = intent.getBooleanExtra(DashboardActivity.IS_FROM_HOME, false);
        }
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);


    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("Test", "P5");
        if (permissionUtil.hasGiven(PermissionUtil.CONTACT_PERMISSION)) {
            switchContactPermission.setChecked(true);
        } else {
            switchContactPermission.setChecked(false);
        }
        if (permissionUtil.hasGiven(PermissionUtil.CALL_PHONE_PERMISSION)) {
            switchCallPermission.setChecked(true);
        } else {
            switchCallPermission.setChecked(false);
        }
        if (permissionUtil.hasGiven(PermissionUtil.SEND_SMS_PERMISSION)) {
            switchSmsPermission.setChecked(true);
        } else {
            switchSmsPermission.setChecked(false);
        }
        if (permissionUtil.hasGiven(PermissionUtil.WRITE_EXTERNAL_STORAGE_PERMISSION)) {
            switchFilePermission.setChecked(true);
        } else {
            switchFilePermission.setChecked(false);
        }
        if (permissionUtil.hasGiven(PermissionUtil.NOTIFICATION_ACCESS)) {
            switchNotificationAccess.setChecked(true);
        } else {
            switchNotificationAccess.setChecked(false);
        }
        //Added for bug solve SSA-1324
//        if (permissionUtil.hasGiven(PermissionUtil.DRAWING_OVER_OTHER_APPS)) {
//            switchOverlayAccess.setChecked(true);
//        } else {
//            switchOverlayAccess.setChecked(false);
//        }


        if (isFromHome) {
            switchContactPermission.setVisibility(View.VISIBLE);
            switchCallPermission.setVisibility(View.VISIBLE);
            switchSmsPermission.setVisibility(View.VISIBLE);
            switchFilePermission.setVisibility(View.VISIBLE);
            switchNotificationAccess.setVisibility(View.VISIBLE);
            switchOverlayAccess.setVisibility(View.VISIBLE);
            btnContinue.setVisibility(View.VISIBLE);
            tblLocation.setVisibility(View.GONE);
            txtPermissionLabel.setText(getString(R.string.permission_title));

            if (Build.VERSION.SDK_INT >= 23) {
                tblContact.setVisibility(View.VISIBLE);
                tblCalls.setVisibility(View.VISIBLE);
                tblDrawOverlay.setVisibility(View.GONE);
                tblStorage.setVisibility(View.VISIBLE);
                tblNotification.setVisibility(View.VISIBLE);
                tblSMS.setVisibility(View.VISIBLE);
            } else {
                tblContact.setVisibility(View.GONE);
                tblCalls.setVisibility(View.GONE);
                tblDrawOverlay.setVisibility(View.GONE);
                tblStorage.setVisibility(View.GONE);
                tblNotification.setVisibility(View.VISIBLE);
                tblSMS.setVisibility(View.GONE);
            }
        } else {
            switchContactPermission.setVisibility(View.GONE);
            switchCallPermission.setVisibility(View.GONE);
            switchSmsPermission.setVisibility(View.GONE);
            switchFilePermission.setVisibility(View.GONE);
            switchNotificationAccess.setVisibility(View.GONE);
            switchOverlayAccess.setVisibility(View.GONE);
            btnContinue.setVisibility(View.GONE);
            if (permissionUtil.hasGiven(PermissionUtil.LOCATION_PERMISSION)) {
                tblLocation.setVisibility(View.VISIBLE);
            } else {
                tblLocation.setVisibility(View.GONE);
            }
            txtPermissionLabel.setText(getString(R.string.permission_siempo_alpha_title));
        }
        Log.d("Test", "P5");
        if (isFromHome && permissionUtil.hasGiven(PermissionUtil
                .CONTACT_PERMISSION) &&
                permissionUtil.hasGiven(PermissionUtil.CALL_PHONE_PERMISSION)
                &&
                permissionUtil.hasGiven(PermissionUtil.WRITE_EXTERNAL_STORAGE_PERMISSION) && permissionUtil
                .hasGiven(PermissionUtil.SEND_SMS_PERMISSION) &&
                permissionUtil.hasGiven(PermissionUtil.NOTIFICATION_ACCESS)) {
            Log.d("Test", "P5");
            finish();
        }
    }

//    @TargetApi(23)
//    @CheckedChange
//    void switchOverlayAccess(CompoundButton btn, boolean isChecked) {
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (isChecked) {
//                if (!Settings.canDrawOverlays(SiempoPermissionActivity.this)) {
//                    Toast.makeText(SiempoPermissionActivity.this, R.string.msg_overlay_settings, Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
//                    startActivityForResult(intent, PermissionUtil.DRAWING_OVER_OTHER_APPS);
//                }
//            } else {
//                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), PermissionUtil.DRAWING_OVER_OTHER_APPS);
//            }
//        }
//    }

    @TargetApi(22)
    @CheckedChange
    void switchNotificationAccess(CompoundButton btn, boolean isChecked) {
        if (isChecked) {
            if (!new PermissionUtil(this).hasGiven(PermissionUtil.NOTIFICATION_ACCESS)) {
                startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), PermissionUtil.NOTIFICATION_ACCESS);
            }
        } else {
            startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), PermissionUtil.NOTIFICATION_ACCESS);
        }
    }

    @Click(R.id.btnContinue)
    void myButtonWasClicked() {
        if (permissionUtil.hasGiven(PermissionUtil.CONTACT_PERMISSION) &&
                permissionUtil.hasGiven(PermissionUtil.WRITE_EXTERNAL_STORAGE_PERMISSION) &&
                permissionUtil.hasGiven(PermissionUtil.CALL_PHONE_PERMISSION) && permissionUtil.hasGiven(PermissionUtil.SEND_SMS_PERMISSION) &&
                permissionUtil.hasGiven(PermissionUtil.NOTIFICATION_ACCESS)) {
//            launcher3Prefs.isPermissionGivenAndContinued().put(true);
            finish();
        } else {
            UIUtils.toastShort(SiempoPermissionActivity.this, R.string.grant_all_to_proceed_text);
        }

    }

//    @OnActivityResult(PermissionUtil.DRAWING_OVER_OTHER_APPS)
//    void onResultDrawingAccess(int resultCode) {
//
//        try {
//            pd.setMessage("Please wait...");
//            pd.show();
//
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (pd != null && pd.isShowing() && !isFinishing()) {
//                        pd.dismiss();
//                    }
//                    if (!new PermissionUtil(SiempoPermissionActivity.this).hasGiven(PermissionUtil.DRAWING_OVER_OTHER_APPS)) {
//                        switchOverlayAccess.setChecked(false);
//                    } else {
//                        switchOverlayAccess.setChecked(true);
//                    }
//                }
//            }, 5000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    @OnActivityResult(PermissionUtil.NOTIFICATION_ACCESS)
    void onResultNotificationAccess(int resultCode) {
        if (!new PermissionUtil(this).hasGiven(PermissionUtil.NOTIFICATION_ACCESS)) {
            switchNotificationAccess.setChecked(false);
        } else {
            switchNotificationAccess.setChecked(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (isFromHome) {
            UIUtils.toastShort(SiempoPermissionActivity.this, R.string.permission_proceed_text);
        } else {
            super.onBackPressed();
        }

    }

    @Subscribe
    public void homePressEvent(HomePressEvent event) {
        try {
            if (event.isVisible() && UIUtils.isMyLauncherDefault(this)) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startActivity(startMain);
            }

        } catch (Exception e) {
            CoreApplication.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }
    }


}