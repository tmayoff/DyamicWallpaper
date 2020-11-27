package com.tylermayoff.dynamicwallpaper.util

import SingletonHolder
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.model.LocalThemeItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.nio.file.Files
import java.util.*

class AppSettings(val context: Context)  {

    var sharedPreferences: SharedPreferences = context.getSharedPreferences(context.getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
    var editor: SharedPreferences.Editor = sharedPreferences.edit()

    // App Settings
    var activeTheme: String = ""
    set(value) {
        field = value
        editor.putString(context.getString(R.string.preferences_active_theme), activeTheme)
        editor.apply()
    }

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

    var localThemes: Array<LocalThemeItem> = arrayOf()

    init {
        // Active theme
        activeTheme = sharedPreferences.getString(context.getString(R.string.preferences_active_theme), "").toString()

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

    fun getLocalThemes (onLoadedLocalThemes: () -> Unit) {
         GlobalScope.launch {
             loadLocalThemes()
             onLoadedLocalThemes.invoke()
        }
    }

    private var imageTypeFilter = FileFilter { pathname: File ->
        try {
            val mimeType = Files.probeContentType(pathname.toPath())
            return@FileFilter mimeType.startsWith("image/")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        false
    }

    private fun loadLocalThemes () {
        val dir = File(context.filesDir.toString() + context.getString(R.string.themes_relative_path))
        val themeFolders: Array<File>? = dir.listFiles()
        if (themeFolders != null) {
            val tmpThemes = mutableListOf<LocalThemeItem>()

            for (theme in themeFolders) {
                val images: Array<File> = theme.listFiles(imageTypeFilter) ?: continue

                if (images.isEmpty()) continue
                val r = Random()
                val rImg = r.nextInt(images.size)
                val img = BitmapFactory.decodeFile(images[rImg].absolutePath)
                tmpThemes.add(LocalThemeItem(theme.name, img))
            }
            localThemes = tmpThemes.toTypedArray()
        }
    }

    companion object: SingletonHolder<AppSettings, Context>(::AppSettings)
}