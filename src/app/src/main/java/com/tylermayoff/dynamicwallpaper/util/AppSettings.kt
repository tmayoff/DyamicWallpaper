package com.tylermayoff.dynamicwallpaper.util

import SingletonHolder
import android.content.Context
import android.content.SharedPreferences
import com.tylermayoff.dynamicwallpaper.R
import java.util.*

class AppSettings(val context: Context)  {

    var sharedPreferences: SharedPreferences = context.getSharedPreferences(context.getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
    var editor: SharedPreferences.Editor = sharedPreferences.edit()

    var sunsetTime: Calendar? = null
    set(value) {
        field = value?.clone() as Calendar
        editor.putLong(context.getString(R.string.preferences_sunset_time), value.timeInMillis)
        editor.apply()
    }

    var sunriseTime: Calendar? = null
    set(value) {
        field = value?.clone() as Calendar
        editor.putLong(context.getString(R.string.preferences_sunrise_time), value.timeInMillis)
        editor.apply()
    }

    var useSunsetSunrise: Boolean = false
    set(value) {
        field = value
        editor.putBoolean(context.getString(R.string.preferences_use_sunset_sunrise), value)
    }

    init {
        // Sunset / Sunrise
        useSunsetSunrise = sharedPreferences.getBoolean(context.getString(R.string.preferences_use_sunset_sunrise), false)

        var timeInMilli: Long = sharedPreferences.getLong(context.getString(R.string.preferences_sunrise_time), -1)
        val tmpCalendar = Calendar.getInstance()

        // Sunrise Time
        if (timeInMilli != 0L) {
            tmpCalendar.timeInMillis = timeInMilli
            sunriseTime = tmpCalendar.clone() as Calendar
        }

        // Sunset Time
        timeInMilli = sharedPreferences.getLong(context.getString(R.string.preferences_sunset_time), -1)
        if (timeInMilli != 0L) {
            tmpCalendar.timeInMillis = timeInMilli
            sunsetTime = tmpCalendar.clone() as Calendar
        }
    }


    companion object: SingletonHolder<AppSettings, Context>(::AppSettings)
}