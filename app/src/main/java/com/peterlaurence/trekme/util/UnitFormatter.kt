package com.peterlaurence.trekme.util

import android.text.format.DateUtils

object UnitFormatter {
    /**
     * Given a distance in meters, format this distance to return a value expressed either in meters
     * or in km.
     */
    fun formatDistance(dist: Double): String {
        if (dist <= 1000) {
            return "%.0f m".format(dist)
        } else {
            return "%.2f km".format(dist / 1000.0)
        }
    }

    fun formationDuration(durationInSec: Long): String {
        return DateUtils.formatElapsedTime(durationInSec)
    }
}
