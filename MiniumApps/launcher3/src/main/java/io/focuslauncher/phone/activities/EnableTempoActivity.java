package io.focuslauncher.phone.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import io.focuslauncher.R;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PermissionUtil;
import io.focuslauncher.phone.utils.UIUtils;

public class EnableTempoActivity extends CoreActivity {
    PermissionUtil permissionUtil;
    ImageView imgStep, imgCenter;
    Toolbar toolbar;
    Button btnSubmit;
    TextView on_the_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_tempo);

        permissionUtil = new PermissionUtil(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgStep = findViewById(R.id.imgStep);
        imgCenter = findViewById(R.id.imgCenter);
        btnSubmit = findViewById(R.id.btnSubmit);
        on_the_next = findViewById(R.id.on_the_next);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (btnSubmit.getText().toString().equalsIgnoreCase(getString(R.string.enable_setting_a))) {
                        startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), PermissionUtil.NOTIFICATION_ACCESS);
                    } else if (btnSubmit.getText().toString().equalsIgnoreCase(getString(R.string.enable_setting_b))) {
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    } else if (btnSubmit.getText().toString().equalsIgnoreCase(getString(R.string.enable_setting_c))) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Exception e) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } else if (btnSubmit.getText().toString().equalsIgnoreCase(getString(R.string.tempo_enabled))) {
                        btnSubmit.setBackground(ContextCompat.getDrawable(EnableTempoActivity.this, R.drawable.button_bg_enable));
                        btnSubmit.setText(getString(R.string.tempo_enabled));
                        setResult(Activity.RESULT_OK, new Intent());
                        finish();
                    }
                } else {
                    if (btnSubmit.getText().toString().equalsIgnoreCase(getString(R.string.enable_setting_a))) {
                        startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), PermissionUtil.NOTIFICATION_ACCESS);
                    } else if (btnSubmit.getText().toString().equalsIgnoreCase(getString(R.string.enable_setting_b))) {
                        askForPermission(new String[]{
                                /*Manifest.permission.CALL_PHONE,*/
                                /*Manifest.permission.READ_PHONE_STATE,*/
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS
                                /*Manifest.permission.RECEIVE_SMS,*/
                                /*Manifest.permission.SEND_SMS,*/
                                /*Manifest.permission.READ_SMS*/});
                    } else if (btnSubmit.getText().toString().equalsIgnoreCase(getString(R.string.enable_setting_c))) {
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    } else if (btnSubmit.getText().toString().equalsIgnoreCase(getString(R.string.enable_setting_d))) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Exception e) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } else if (btnSubmit.getText().toString().equalsIgnoreCase(getString(R.string.tempo_enabled))) {
                        btnSubmit.setBackground(ContextCompat.getDrawable(EnableTempoActivity.this, R.drawable.button_bg_enable));
                        btnSubmit.setText(getString(R.string.tempo_enabled));
                        setResult(Activity.RESULT_OK, new Intent());
                        finish();
                    }
                }
            }
        });
    }

    private void askForPermission(String[] PERMISSIONS) {
        try {
            TedPermission.with(EnableTempoActivity.this)
                    .setPermissionListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            bindUi();
                        }

                        @Override
                        public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                        }
                    })
                    .setDeniedMessage(R.string.msg_permission_denied1)
                    .setPermissions(PERMISSIONS)
                    .check();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindUi();
    }

    private void bindUi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!permissionUtil.hasGiven(PermissionUtil.NOTIFICATION_ACCESS)) {
                on_the_next.setText(R.string.on_the_next);
                imgCenter.setBackground(ContextCompat.getDrawable(this, R.drawable.screenshot_notification));
                imgStep.setBackground(ContextCompat.getDrawable(this, R.drawable.progress_a));
                btnSubmit.setText(getString(R.string.enable_setting_a));
            } else if (!UIUtils.hasUsageStatsPermission(this)) {
                on_the_next.setText(R.string.on_the_next);
                imgCenter.setBackground(ContextCompat.getDrawable(this, R.drawable.app_usage));
                imgStep.setBackground(ContextCompat.getDrawable(this, R.drawable.progress_b));
                btnSubmit.setText(getString(R.string.enable_setting_b));
            } else if (!PackageUtil.isSiempoLauncher(this)) {
                on_the_next.setText(R.string.launcher_text);
                imgCenter.setBackground(ContextCompat.getDrawable(this, R.drawable.screenshot_launcher));
                imgStep.setBackground(ContextCompat.getDrawable(this, R.drawable.progress_c));
                btnSubmit.setText(getString(R.string.enable_setting_c));
            } else {
                on_the_next.setText(R.string.launcher_text);
                imgCenter.setBackground(ContextCompat.getDrawable(this, R.drawable.screenshot_launcher));
                on_the_next.setVisibility(View.GONE);
                imgCenter.setVisibility(View.GONE);
                imgStep.setBackground(ContextCompat.getDrawable(this, R.drawable.progress_d));
                btnSubmit.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_enable));
                btnSubmit.setText(getString(R.string.tempo_enabled));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(Activity.RESULT_OK, new Intent());
                        finish();
                    }
                }, 1000);
            }
        } else {
            if (!permissionUtil.hasGiven(PermissionUtil.NOTIFICATION_ACCESS)) {
                on_the_next.setText(R.string.on_the_next);
                imgCenter.setBackground(ContextCompat.getDrawable(this, R.drawable.screenshot_notification));
                imgStep.setBackground(ContextCompat.getDrawable(this, R.drawable.progress_aa));
                btnSubmit.setText(getString(R.string.enable_setting_a));
            } else if (/*!permissionUtil.hasGiven(PermissionUtil.CALL_PHONE_PERMISSION)
                    || !permissionUtil.hasGiven(PermissionUtil.SEND_SMS_PERMISSION)
                    || */!permissionUtil.hasGiven(PermissionUtil.CONTACT_PERMISSION)) {
                on_the_next.setText(R.string.permission_msg_enable_tempo);
                imgCenter.setBackground(ContextCompat.getDrawable(this, R.drawable.screenshot_call_sms_contact));
                imgStep.setBackground(ContextCompat.getDrawable(this, R.drawable.progress_bb));
                btnSubmit.setText(getString(R.string.enable_setting_b));
            } else if (!UIUtils.hasUsageStatsPermission(this)) {
                on_the_next.setText(R.string.on_the_next);
                imgCenter.setBackground(ContextCompat.getDrawable(this, R.drawable.app_usage));
                imgStep.setBackground(ContextCompat.getDrawable(this, R.drawable.progress_cc));
                btnSubmit.setText(getString(R.string.enable_setting_c));
            } else if (!PackageUtil.isSiempoLauncher(this)) {
                on_the_next.setText(R.string.launcher_text);
                imgCenter.setBackground(ContextCompat.getDrawable(this, R.drawable.screenshot_launcher));
                imgStep.setBackground(ContextCompat.getDrawable(this, R.drawable.progress_dd));
                btnSubmit.setText(getString(R.string.enable_setting_d));
            } else {
                on_the_next.setText(R.string.launcher_text);
                imgCenter.setBackground(ContextCompat.getDrawable(this, R.drawable.screenshot_launcher));
                on_the_next.setVisibility(View.GONE);
                imgCenter.setVisibility(View.GONE);
                imgStep.setBackground(ContextCompat.getDrawable(this, R.drawable.progress_ee));
                btnSubmit.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_enable));
                btnSubmit.setText(getString(R.string.tempo_enabled));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setResult(Activity.RESULT_OK, new Intent());
                        finish();
                    }
                }, 1000);
            }
        }
    }
}
