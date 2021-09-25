package io.focuslauncher.phone.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.joanzapata.iconify.IconDrawable
import de.greenrobot.event.EventBus
import de.greenrobot.event.Subscribe
import io.focuslauncher.R
import io.focuslauncher.databinding.ActivitySiempoAlphaSettingsBinding
import io.focuslauncher.phone.app.CoreApplication
import io.focuslauncher.phone.event.LocationUpdateEvent
import io.focuslauncher.phone.event.StartLocationEvent
import io.focuslauncher.phone.helper.ActivityHelper
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.models.UserModel
import io.focuslauncher.phone.utils.*

/**
 * Created by hardik on 17/8/17.
 */
open class AlphaSettingsActivity : CoreActivity() {

    private var permissionUtil: PermissionUtil? = null
    private var locationRequest: LocationRequest? = null
    private var startTime: Long = 0

    private var dialog: ProgressDialog? = null

    private var binding: ActivitySiempoAlphaSettingsBinding? by lifecycleProperty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindView(ActivitySiempoAlphaSettingsBinding::inflate)
        initView()
        onClickEvents()
    }

    private val gpsLocationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val locationManager = context.locationManager
            //If Action is Location
            if (intent.action?.matches(Regex(BROADCAST_ACTION)) == true) {
                //Check if GPS is turned ON or OFF
                if (locationManager != null
                    && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ) {
                    //If GPS turned OFF show Location Dialog
                    if (dialog != null && dialog!!.isShowing) {
                        dialog!!.dismiss()
                    }
                    binding?.switchLocation?.isChecked = false
                    binding?.longitude!!.visibility = View.GONE
                    binding?.latitude!!.visibility = View.GONE
                    PrefSiempo.getInstance(context).write(
                        PrefSiempo.LOCATION_STATUS,
                        false
                    )
                    EventBus.getDefault().post(StartLocationEvent(false))
                }
            }
        }
    }

    fun initView() {
        binding?.toolbar?.apply {
            setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp)
            setTitle(R.string.alpha_settings)
            setNavigationOnClickListener { onBackPressed() }
        }
        binding?.iconPermissions?.setImageDrawable(
            IconDrawable(this, "fa-bell")
                .colorRes(R.color.text_primary)
                .sizeDp(18)
        )
        try {
            binding?.iconSuppressedNotifications?.setImageDrawable(
                IconDrawable(
                    this,
                    "fa-exclamation"
                ).colorRes(R.color.text_primary).sizeDp(18)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding?.iconUserId?.setImageDrawable(
            IconDrawable(this, "fa-user-secret")
                .colorRes(R.color.text_primary)
                .sizeDp(18)
        )
        binding?.txtUserId?.text = String.format("UserId: %s", CoreApplication.getInstance().deviceId)
        binding?.switchAlphaRestriction?.isChecked =
            PrefSiempo.getInstance(this).read(PrefSiempo.JUNK_RESTRICTED, false)
        dialog = ProgressDialog(this@AlphaSettingsActivity)
        permissionUtil = PermissionUtil(this)
    }

    fun onClickEvents() {
        binding?.lnSuppressedNotifications?.setOnClickListener { ActivityHelper(this).openSiempoSuppressNotificationsSettings() }
        binding?.lnPermissions?.setOnClickListener {
            val intent = Intent(this@AlphaSettingsActivity, SiempoPermissionActivity_::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra(DashboardActivity.IS_FROM_HOME, false)
            startActivity(intent)
        }
        binding?.relRestrictions?.setOnClickListener {
            if (binding?.switchAlphaRestriction?.isChecked == true) {
                binding?.switchAlphaRestriction?.isChecked = false
                PrefSiempo.getInstance(this).write(
                    PrefSiempo.JUNK_RESTRICTED,
                    false
                )
            } else {
                binding?.switchAlphaRestriction?.isChecked = true
                PrefSiempo.getInstance(this).write(
                    PrefSiempo.JUNK_RESTRICTED,
                    true
                )
            }
        }
        binding?.relLocation?.setOnClickListener {
            if (binding?.switchLocation?.isChecked == true) {
                binding?.switchLocation?.isChecked = false
                binding?.longitude!!.visibility = View.GONE
                binding?.latitude!!.visibility = View.GONE
                PrefSiempo.getInstance(this).write(
                    PrefSiempo.LOCATION_STATUS,
                    false
                )
                EventBus.getDefault().post(StartLocationEvent(false))
            } else {
                checkPermissionAndFindLocation()
                PrefSiempo.getInstance(this).write(
                    PrefSiempo.LOCATION_STATUS,
                    true
                )
            }
        }
        val loc_switch_state = PrefSiempo.getInstance(this).read(
            PrefSiempo.LOCATION_STATUS,
            false
        )
        if (!loc_switch_state) {
            binding?.switchLocation?.isChecked = false
            EventBus.getDefault().post(StartLocationEvent(false))
        } else {
            binding?.switchLocation?.isChecked = true
            checkPermissionAndFindLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseHelper.getInstance().logScreenUsageTime(this@AlphaSettingsActivity.javaClass.simpleName, startTime)
        unregisterReceiver(gpsLocationReceiver)
    }

    override fun onResume() {
        super.onResume()
        val locationManager = locationManager
        registerReceiver(gpsLocationReceiver, IntentFilter(BROADCAST_ACTION))
        startTime = System.currentTimeMillis()
        if (permissionUtil?.hasGiven(PermissionUtil.LOCATION_PERMISSION) == false
            || locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false
            && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            if (dialog != null && dialog!!.isShowing) {
                dialog!!.dismiss()
            }
            binding?.switchLocation?.isChecked = false
            EventBus.getDefault().post(StartLocationEvent(false))
            PrefSiempo.getInstance(this).write(PrefSiempo.LOCATION_STATUS, false)
            EventBus.getDefault().post(StartLocationEvent(false))
            binding?.longitude!!.visibility = View.GONE
            binding?.latitude!!.visibility = View.GONE
        }
    }

    //For Checking Location Permission
    private fun checkPermissionAndFindLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permissionUtil!!.hasGiven(PermissionUtil.LOCATION_PERMISSION)) {
            try {
                TedPermission.with(this)
                    .setPermissionListener(object : PermissionListener {
                        override fun onPermissionGranted() {
                            showLocation()
                        }

                        override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
                    })
                    .setDeniedMessage(R.string.msg_permission_denied)
                    .setPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET
                    )
                    .check()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            showLocation()
        }
    }

    //Fetching Location
    private fun showLocation() {
        if (locationRequest == null) {
            locationRequest = LocationRequest.create()
        }
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest).build()
        val client = LocationServices.getSettingsClient(this)
        //Location Request Dialog
        val task = client
            .checkLocationSettings(settingsRequest)
        task.addOnSuccessListener(this) {
            if (dialog != null) {
                dialog!!.show()
                dialog!!.setMessage("Fetching Location")
                dialog!!.setCancelable(false)
            }
            EventBus.getDefault().post(StartLocationEvent(true))
        }
        task.addOnFailureListener(this) { e ->
            val statusCode = (e as ApiException).statusCode
            if (statusCode
                == LocationSettingsStatusCodes.RESOLUTION_REQUIRED
            ) {
                try {
                    val resolvable = e as ResolvableApiException
                    resolvable.startResolutionForResult(
                        this@AlphaSettingsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
    }

    //Updating Location Lat,Long values
    @Subscribe(sticky = true)
    fun LocationUpdateEvent(event: LocationUpdateEvent?) {
        val switch_status = PrefSiempo.getInstance(this).read(PrefSiempo.LOCATION_STATUS, false)
        if (switch_status) {
            if (dialog != null && dialog!!.isShowing && event != null) {
                dialog!!.dismiss()
            }
            binding?.longitude!!.text = "longitude: " + event!!.longitude
            binding?.latitude!!.text = "latitude: " + event.latitude
            val userEmail = PrefSiempo.getInstance(this).read(PrefSiempo.USER_EMAILID, "")
            storeDataToFirebase(CoreApplication.getInstance().deviceId, userEmail, event.latitude, event.longitude)
            binding?.switchLocation?.isChecked = true
            binding?.longitude!!.visibility = View.VISIBLE
            binding?.latitude!!.visibility = View.VISIBLE
        }
        EventBus.getDefault().removeStickyEvent(event)
    }

    private fun storeDataToFirebase(userId: String, emailId: String, latitude: Double, longitude: Double) {
        try {
            val mDatabase = FirebaseDatabase.getInstance().getReference("users")
            val user = UserModel(userId, emailId, latitude, longitude)
            val key = mDatabase.child(userId).key
            if (key != null) {
                val map: MutableMap<String, Any> = HashMap()
                map["emailId"] = emailId
                map["userId"] = userId
                map["latitude"] = latitude
                map["longitude"] = longitude
                mDatabase.child(userId).updateChildren(map)
            } else {
                mDatabase.child(userId).setValue(user)
                mDatabase.child(userId).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.d(
                            "Firebase", dataSnapshot.key + "  " + dataSnapshot.getValue(
                                UserModel::class.java
                            )
                                .toString()
                        )
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("Firebase RealTime", "Failed to read value.", error.toException())
                    }
                })
            }
            Log.d("Key", mDatabase.child(userId).key)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                RESULT_OK -> {
                    PrefSiempo.getInstance(this).write(PrefSiempo.LOCATION_STATUS, true)
                    showLocation()
                }
                RESULT_CANCELED -> {
                    if (dialog != null && dialog!!.isShowing) {
                        dialog!!.dismiss()
                    }
                    binding?.switchLocation?.isChecked = false
                    PrefSiempo.getInstance(this).write(PrefSiempo.LOCATION_STATUS, false)
                    EventBus.getDefault().post(StartLocationEvent(false))
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 0x1
        private const val BROADCAST_ACTION = "android.location.PROVIDERS_CHANGED"
    }
}