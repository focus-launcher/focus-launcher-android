package co.siempo.phone.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.joanzapata.iconify.IconDrawable;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.siempo.phone.R;
import co.siempo.phone.app.CoreApplication;
import co.siempo.phone.event.LocationUpdateEvent;
import co.siempo.phone.event.StartLocationEvent;
import co.siempo.phone.helper.ActivityHelper;
import co.siempo.phone.helper.FirebaseHelper;
import co.siempo.phone.models.UserModel;
import co.siempo.phone.utils.PermissionUtil;
import co.siempo.phone.utils.PrefSiempo;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

import static co.siempo.phone.activities.DashboardActivity.IS_FROM_HOME;

/**
 * Created by hardik on 17/8/17.
 */

@EActivity(R.layout.activity_siempo_alpha_settings)
public class AlphaSettingsActivity extends CoreActivity {
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final String BROADCAST_ACTION = "android.location.PROVIDERS_CHANGED";
    @ViewById
    ImageView icon_UserId;
    @ViewById
    TextView txt_UserId;
    PermissionUtil permissionUtil;
    ProgressDialog dialog;
    private Context context;
    private long startTime = 0;
    private LinearLayout ln_suppressedNotifications;
    private RelativeLayout rel_restrictions;
    private Switch switch_alphaRestriction;
    private ImageView icon_SuppressedNotifications;
    private LinearLayout ln_permissions;
    private ImageView icon_permissions, icon_in_app;
    private Toolbar toolbar;
    private RelativeLayout rel_location;
    private Switch switch_location;
    private TextView longitude, latitude;
    private LocationRequest locationRequest;
    private LocationManager locationManager;

    private BroadcastReceiver gpsLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //If Action is Location
            if (intent.getAction().matches(BROADCAST_ACTION)) {
                if (locationManager == null) {
                    locationManager = (LocationManager) context.getSystemService(Context
                            .LOCATION_SERVICE);
                }
                //Check if GPS is turned ON or OFF
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                } else {
                    //If GPS turned OFF show Location Dialog
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    switch_location.setChecked(false);
                    longitude.setVisibility(View.GONE);
                    latitude.setVisibility(View.GONE);
                    PrefSiempo.getInstance(context).write(PrefSiempo.LOCATION_STATUS,
                            false);
                    EventBus.getDefault().post(new StartLocationEvent(false));
                }
            }
        }
    };

    @AfterViews
    void afterViews() {
        initView();
        onClickEvents();
    }

    public void initView() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.alpha_settings);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        context = AlphaSettingsActivity.this;
        ln_suppressedNotifications = findViewById(R.id.ln_suppressedNotifications);
        rel_restrictions = findViewById(R.id.rel_restrictions);
        switch_alphaRestriction = findViewById(R.id.switch_alphaRestriction);
        ln_permissions = findViewById(R.id.ln_permissions);
        rel_location = findViewById(R.id.rel_location);
        switch_location = findViewById(R.id.switch_location);
        icon_SuppressedNotifications = findViewById(R.id.icon_SuppressedNotifications);
        icon_permissions = findViewById(R.id.icon_permissions);
        icon_in_app = findViewById(R.id.icon_in_app);
        icon_permissions.setImageDrawable(new IconDrawable(context, "fa-bell").colorRes(R.color.text_primary).sizeDp(18));
        try {
            icon_SuppressedNotifications.setImageDrawable(new IconDrawable(context, "fa-exclamation").colorRes(R.color.text_primary).sizeDp(18));
        } catch (Exception e) {
            //Todo log exception to fabric
            e.printStackTrace();
//            Crashlytics.logException(e);
        }
        icon_UserId.setImageDrawable(new IconDrawable(context, "fa-user-secret")
                .colorRes(R.color.text_primary)
                .sizeDp(18));
        icon_in_app.setImageDrawable(new IconDrawable(context, "fa-shopping-cart")
                .colorRes(R.color.text_primary)
                .sizeDp(18));
        txt_UserId.setText(String.format("UserId: %s", CoreApplication.getInstance().getDeviceId()));
        if (PrefSiempo.getInstance(this).read(PrefSiempo.JUNK_RESTRICTED, false)) {
            switch_alphaRestriction.setChecked(true);
        } else {
            switch_alphaRestriction.setChecked(false);
        }

        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        dialog = new ProgressDialog(AlphaSettingsActivity.this);
        permissionUtil = new PermissionUtil(this);
    }

    public void onClickEvents() {
        ln_suppressedNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ActivityHelper(context).openSiempoSuppressNotificationsSettings();
            }
        });

        ln_permissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlphaSettingsActivity.this, SiempoPermissionActivity_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(IS_FROM_HOME, false);
                startActivity(intent);
            }
        });
        rel_restrictions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_alphaRestriction.isChecked()) {
                    switch_alphaRestriction.setChecked(false);
                    PrefSiempo.getInstance(context).write(PrefSiempo.JUNK_RESTRICTED,
                            false);
                } else {
                    switch_alphaRestriction.setChecked(true);
                    PrefSiempo.getInstance(context).write(PrefSiempo.JUNK_RESTRICTED,
                            true);
                }
            }
        });
        rel_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_location.isChecked()) {
                    switch_location.setChecked(false);
                    longitude.setVisibility(View.GONE);
                    latitude.setVisibility(View.GONE);
                    PrefSiempo.getInstance(context).write(PrefSiempo.LOCATION_STATUS,
                            false);
                    EventBus.getDefault().post(new StartLocationEvent(false));
                } else {
                    checkPermissionAndFindLocation();
                    PrefSiempo.getInstance(context).write(PrefSiempo.LOCATION_STATUS,
                            true);
                }
            }
        });
        boolean loc_switch_state = PrefSiempo.getInstance(context).read(PrefSiempo.LOCATION_STATUS,
                false);
        if (!loc_switch_state) {
            switch_location.setChecked(false);
            EventBus.getDefault().post(new StartLocationEvent(false));
        } else {
            switch_location.setChecked(true);
            checkPermissionAndFindLocation();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseHelper.getInstance().logScreenUsageTime(AlphaSettingsActivity.this.getClass().getSimpleName(), startTime);
        unregisterReceiver(gpsLocationReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context
                    .LOCATION_SERVICE);
        }
        registerReceiver(gpsLocationReceiver, new IntentFilter(BROADCAST_ACTION));
        startTime = System.currentTimeMillis();
        if (!permissionUtil.hasGiven(PermissionUtil.LOCATION_PERMISSION) || (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            switch_location.setChecked(false);
            EventBus.getDefault().post(new StartLocationEvent(false));
            PrefSiempo.getInstance(this).write(PrefSiempo.LOCATION_STATUS, false);
            EventBus.getDefault().post(new StartLocationEvent(false));
            longitude.setVisibility(View.GONE);
            latitude.setVisibility(View.GONE);
        }
    }

    //For Checking Location Permission
    private void checkPermissionAndFindLocation() {

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permissionUtil.hasGiven
                (PermissionUtil.LOCATION_PERMISSION))) {
            try {
                TedPermission.with(this)
                        .setPermissionListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                showLocation();
                            }

                            @Override
                            public void onPermissionDenied(ArrayList<String> deniedPermissions) {

                            }
                        })
                        .setDeniedMessage(R.string.msg_permission_denied)
                        .setPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest
                                        .permission
                                        .ACCESS_FINE_LOCATION, Manifest.permission.INTERNET})
                        .check();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showLocation();
        }
    }

    //Fetching Location
    private void showLocation() {
        if (locationRequest == null) {
            locationRequest = LocationRequest.create();

        }
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();

        SettingsClient client = LocationServices.getSettingsClient(this);
        //Location Request Dialog
        Task<LocationSettingsResponse> task = client
                .checkLocationSettings(settingsRequest);

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (dialog != null) {
                    dialog.show();
                    dialog.setMessage("Fetching Location");
                    dialog.setCancelable(false);
                }
                EventBus.getDefault().post(new StartLocationEvent(true));
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                if (statusCode
                        == LocationSettingsStatusCodes
                        .RESOLUTION_REQUIRED) {
                    try {
                        ResolvableApiException resolvable =
                                (ResolvableApiException) e;
                        resolvable.startResolutionForResult
                                (AlphaSettingsActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        sendEx.printStackTrace();
                    }
                }
            }
        });
    }

    //Updating Location Lat,Long values
    @Subscribe(sticky = true)
    public void LocationUpdateEvent(LocationUpdateEvent event) {
        boolean switch_status = PrefSiempo.getInstance(this).read(PrefSiempo.LOCATION_STATUS, false);
        if (switch_status) {
            if (dialog != null && dialog.isShowing() && event != null) {
                dialog.dismiss();
            }
            longitude.setText("longitude: " + event.getLongitude());
            latitude.setText("latitude: " + event.getLatitude());

            String userEmail = PrefSiempo.getInstance(this).read(PrefSiempo
                    .USER_EMAILID, "");
            storeDataToFirebase(CoreApplication.getInstance().getDeviceId(), userEmail, event.getLatitude(), event.getLongitude());
            switch_location.setChecked(true);
            longitude.setVisibility(View.VISIBLE);
            latitude.setVisibility(View.VISIBLE);
        }
        EventBus.getDefault().removeStickyEvent(event);
    }

    private void storeDataToFirebase(String userId, String emailId, double latitude, double longitude) {
        try {

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
            UserModel user = new UserModel(userId, emailId, latitude, longitude);
            String key = mDatabase.child(userId).getKey();
            if (key != null) {
                Map map = new HashMap();
                map.put("emailId",emailId);
                map.put("userId",userId);
                map.put("latitude", latitude);
                map.put("longitude", longitude);
                mDatabase.child(userId).updateChildren(map);
            } else {
                mDatabase.child(userId).setValue(user);
                mDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("Firebase", dataSnapshot.getKey() + "  " + dataSnapshot.getValue(UserModel.class)
                                .toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("Firebase RealTime", "Failed to read value.", error.toException());
                    }
                });
            }
            Log.d("Key", mDatabase.child(userId).getKey());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        PrefSiempo.getInstance(this).write(PrefSiempo.LOCATION_STATUS, true);
                        showLocation();
                        break;
                    case RESULT_CANCELED:
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        switch_location.setChecked(false);
                        PrefSiempo.getInstance(this).write(PrefSiempo.LOCATION_STATUS, false);
                        EventBus.getDefault().post(new StartLocationEvent(false));
                        break;
                }
                break;
        }
    }
}

