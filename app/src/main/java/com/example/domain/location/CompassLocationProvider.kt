package com.example.domain.location

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.GeomagneticField
import android.location.Location
import android.location.LocationManager

/**
 * Provides geographical location data and calculates Magnetic Declination
 * using Android's built-in [GeomagneticField], enabling True North computation.
 */
class CompassLocationProvider(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    data class LocationInfo(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val altitudeMeters: Double = 0.0,
        val declinationDegrees: Float = 0f,
        val hasLocation: Boolean = false
    )

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): LocationInfo {
        if (locationManager == null) return LocationInfo()

        var bestLocation: Location? = null
        try {
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }
        } catch (_: SecurityException) {
            // Location permission not yet granted; declination defaults to 0
            return LocationInfo()
        }

        return if (bestLocation != null) {
            val geomagneticField = GeomagneticField(
                bestLocation.latitude.toFloat(),
                bestLocation.longitude.toFloat(),
                bestLocation.altitude.toFloat(),
                System.currentTimeMillis()
            )
            LocationInfo(
                latitude = bestLocation.latitude,
                longitude = bestLocation.longitude,
                altitudeMeters = bestLocation.altitude,
                declinationDegrees = geomagneticField.declination,
                hasLocation = true
            )
        } else {
            LocationInfo()
        }
    }
}
