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
import java.time.LocalTime
import java.util.*

class AppSettings(val context: Context)  {

    var sharedPreferences: SharedPreferences = context.getSharedPreferences(context.getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
    var editor: SharedPreferences.Editor = sharedPreferences.edit()

    // App Settings
    var themeConfig: ThemeConfiguration? = null

    private var _activeTheme: String = ""
    var activeTheme: String
        get() {
            return _activeTheme
        }
        set(value) {
            _activeTheme = value
            editor.putString(context.getString(R.string.preferences_active_theme), activeTheme)
            editor.apply()
            themeConfig = ThemeConfiguration(context, activeTheme, useSunsetSunrise, sunriseTime, sunsetTime)
        }

    private var _sunsetTime: LocalTime? = null
    var sunsetTime: LocalTime?
        get() {
            return _sunsetTime
        }
        set(value) {
            _sunsetTime = value
            editor.putString(context.getString(R.string.preferences_sunset_time), value.toString())
            editor.apply()
            themeConfig = ThemeConfiguration(context, activeTheme, useSunsetSunrise, sunriseTime, sunsetTime)
        }

    private var _sunriseTime: LocalTime? = null
    var sunriseTime: LocalTime?
        get() {
            return _sunriseTime
        }
        set(value) {
            _sunriseTime = value
            editor.putString(context.getString(R.string.preferences_sunrise_time), value.toString())
            editor.apply()
            themeConfig = ThemeConfiguration(context, activeTheme, useSunsetSunrise, sunriseTime, sunsetTime)
        }

    private var _useSunsetSunrise: Boolean = false
    var useSunsetSunrise: Boolean
        get() {
            return _useSunsetSunrise
        }
        set(value) {
            _useSunsetSunrise = value
            editor.putBoolean(context.getString(R.string.preferences_use_sunset_sunrise), value)
            themeConfig = ThemeConfiguration(context, activeTheme, useSunsetSunrise, sunriseTime, sunsetTime)
        }

    var localThemes: Array<LocalThemeItem> = arrayOf()

    init {
        // Active theme
        _activeTheme = sharedPreferences.getString(context.getString(R.string.preferences_active_theme), "").toString()

        // Sunset / Sunrise
        _useSunsetSunrise = sharedPreferences.getBoolean(context.getString(R.string.preferences_use_sunset_sunrise), false)

        val sunriseText = sharedPreferences.getString(context.getString(R.string.preferences_sunrise_time), "")
        val sunsetText = sharedPreferences.getString(context.getString(R.string.preferences_sunset_time), "")
        if (sunriseText!!.isNotBlank())
            _sunriseTime = LocalTime.parse(sunriseText)
        if (sunsetText!!.isNotBlank())
            _sunsetTime = LocalTime.parse(sunsetText)

        if (activeTheme.isNotBlank()) {
            themeConfig = ThemeConfiguration(context, activeTheme, useSunsetSunrise, sunriseTime, sunsetTime)
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