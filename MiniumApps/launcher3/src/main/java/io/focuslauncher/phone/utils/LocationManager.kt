package io.focuslauncher.phone.utils

import android.content.Context
import android.location.LocationManager

val Context.locationManager: LocationManager?
    get() = getSystemService(Context.LOCATION_SERVICE) as? LocationManager