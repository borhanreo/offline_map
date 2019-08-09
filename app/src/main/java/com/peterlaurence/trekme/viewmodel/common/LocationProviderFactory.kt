package com.peterlaurence.trekme.viewmodel.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

/**
 * Used by the MainActivity only, creates instances of [LocationProvider] based on how this class
 * is configured (configuration may change at runtime).
 * It needs a [Context] as some [LocationProvider] need one. This is also the reason why the
 * MainActivity is holding reference to [LocationProviderFactory], to avoid memory leak.
 */
class LocationProviderFactory(val context: Context) {
    private var source: LocationSource = LocationSource.GOOGLE_FUSE

    fun configure(source: LocationSource) {
        this.source = source
    }

    fun getLocationProvider(): LocationProvider {
        return when(source) {
            LocationSource.GOOGLE_FUSE -> GoogleLocationProvider(context)
            LocationSource.NMEA -> TODO()
        }
    }
}

enum class LocationSource {
    GOOGLE_FUSE, NMEA
}

sealed class LocationProvider {
    abstract fun start(locationCb: LocationCallback)
    abstract fun stop()
}

/**
 * [latitude] and [longitude] are in degrees
 * [speed] is in meters per second
 */
data class Location(val latitude: Double = 0.0, val longitude: Double = 0.0, val speed: Float = 0f)

/**
 * Use the Google api to receive location.
 * It's set to receive a new location at most every second.
 */
private class GoogleLocationProvider(val context: Context): LocationProvider() {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest()
    lateinit var googleLocationCallback: com.google.android.gms.location.LocationCallback

    init {
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun start(locationCb: LocationCallback) {
        if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        /**
         * Beware that this object holds a reference to the provided [locationCb] (which itself
         * hides a reference to a view), so [stop] method must be called when appropriate.
         */
        googleLocationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                for (loc in locationResult!!.locations) {
                    locationCb(Location(loc.latitude, loc.longitude, loc.speed))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                googleLocationCallback, null)
    }

    override fun stop() {
        fusedLocationClient.removeLocationUpdates(googleLocationCallback)
    }
}

typealias LocationCallback = (Location) -> Unit

