package com.tylermayoff.dynamicwallpaper.ui.main

import android.app.TimePickerDialog
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tylermayoff.dynamicwallpaper.DynamicWallpaperService
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.ThemeConfiguration
import com.tylermayoff.dynamicwallpaper.util.AppSettings
import java.text.SimpleDateFormat
import java.util.*

class TabWallpaperSettings : UpdateableFragment () {

    private lateinit var appSettings: AppSettings
    private val formatter: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var themeConfig: ThemeConfiguration? = null

    // Settings UI
    private lateinit var root: View
    private lateinit var buttonSetSunrise: Button
    private lateinit var buttonSetSunset: Button
    private lateinit var checkBoxUseSunsetSunrise: CheckBox
    private lateinit var layoutSunrise: ConstraintLayout
    private lateinit var layoutSunset: ConstraintLayout

    // Active Theme UI
    private lateinit var textViewActiveTheme: TextView
    private lateinit var textViewTimes: TextView
    private lateinit var layoutActiveTheme: LinearLayout
    private lateinit var fabSetWallpaper: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_wallpaper_settings, container, false)

        appSettings = AppSettings.getInstance(requireContext())

        initializeAppSettings()
        initializeActiveTheme()

        return root
    }

    private fun initializeAppSettings () {
        buttonSetSunrise = root.findViewById(R.id.button_SetSunrise)
        buttonSetSunset = root.findViewById(R.id.button_SetSunset)
        checkBoxUseSunsetSunrise = root.findViewById(R.id.checkBox_UseSunTimes)
        layoutSunrise = root.findViewById(R.id.layout_Sunrise)
        layoutSunset = root.findViewById(R.id.layout_Sunset)

        checkBoxUseSunsetSunrise.isChecked = appSettings.useSunsetSunrise
        if (!checkBoxUseSunsetSunrise.isChecked) {
            layoutSunrise.visibility = View.GONE
            layoutSunset.visibility = View.GONE
        }

        // Set sunrise time in settings
        if (appSettings.sunriseTime != null) {
            buttonSetSunrise.text = formatter.format(appSettings.sunriseTime!!.time)
        }
        // Set sunset time in settings
        if (appSettings.sunsetTime != null) {
            buttonSetSunset.text = formatter.format(appSettings.sunsetTime!!.time)
        }

        // Event listeners
        buttonSetSunrise.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                val calendar = GregorianCalendar()
                calendar.set(0, 0, 0, 0, 0, 0)
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                appSettings.sunriseTime = calendar
                buttonSetSunrise.text = formatter.format(appSettings.sunriseTime!!.time)
            }, if (appSettings.sunriseTime != null) appSettings.sunriseTime!!.get(Calendar.HOUR_OF_DAY) else 0,
                    if (appSettings.sunriseTime != null) appSettings.sunriseTime!!.get(Calendar.MINUTE) else 0, false).show()
        }
        buttonSetSunset.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                val calendar = GregorianCalendar()
                calendar.set(0, 0, 0, 0, 0, 0)
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                appSettings.sunsetTime = calendar
                buttonSetSunset.text = formatter.format(appSettings.sunsetTime!!.time)
            }, if (appSettings.sunsetTime != null) appSettings.sunsetTime!!.get(Calendar.HOUR_OF_DAY) else 0,
                    if (appSettings.sunsetTime != null) appSettings.sunsetTime!!.get(Calendar.MINUTE) else 0, false).show()
        }
        checkBoxUseSunsetSunrise.setOnCheckedChangeListener { _, isChecked ->
            appSettings.useSunsetSunrise = isChecked
            if (isChecked) {
                layoutSunrise.visibility = View.VISIBLE
                layoutSunset.visibility = View.VISIBLE
            } else {
                layoutSunrise.visibility = View.GONE
                layoutSunset.visibility = View.GONE
            }
        }
    }

    private fun initializeActiveTheme () {
        textViewActiveTheme = root.findViewById(R.id.textView_ActiveTheme)
        textViewTimes = root.findViewById(R.id.textView_NextChange)
        layoutActiveTheme = root.findViewById(R.id.layout_ActiveTheme)
        fabSetWallpaper = root.findViewById(R.id.fab_SetWallpaper)

        // Listeners
        fabSetWallpaper.setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(requireContext(), DynamicWallpaperService::class.java))
            startActivity(intent)
        }
        if (appSettings.activeTheme.isNotBlank()) {
            themeConfig = ThemeConfiguration(requireContext(), appSettings.activeTheme)
        }
    }

    override fun update() {
        if (appSettings.activeTheme.isBlank()) {
            layoutActiveTheme.visibility = View.GONE
            fabSetWallpaper.visibility = View.GONE
        } else {
            layoutActiveTheme.visibility = View.VISIBLE
            fabSetWallpaper.visibility = View.VISIBLE
            textViewActiveTheme.text = appSettings.activeTheme
            val timesString = StringBuilder()
            for (i in themeConfig!!.wallpaperChangeTimes) {
                timesString.append(formatter.format(i.time) + "\n")
            }

            textViewTimes.text = timesString
        }
    }
}