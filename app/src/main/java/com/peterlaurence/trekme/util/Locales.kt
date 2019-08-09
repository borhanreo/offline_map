package com.peterlaurence.trekme.util

import android.content.Context
import android.os.Build
import java.util.*


fun getCurrentLocale(context: Context): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales.get(0)
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale
    }
}

fun isFrench(context: Context): Boolean {
    return getCurrentLocale(context).language.startsWith("fr")
}

fun isEnglish(context: Context): Boolean {
    return getCurrentLocale(context).language.startsWith("en")
}