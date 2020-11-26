package com.tylermayoff.dynamicwallpaper.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.Zenith
import com.tylermayoff.dynamicwallpaper.DynamicWallpaperService
import com.tylermayoff.dynamicwallpaper.MainActivity
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.ThemeConfiguration
import com.tylermayoff.dynamicwallpaper.util.AppSettings
import java.text.SimpleDateFormat
import java.util.*

class TabWallpaperSettings : UpdateableFragment () {

    // UI
    private lateinit var buttonSetSunrise: Button
    private lateinit var buttonSetSunset: Button
    private lateinit var checkBoxUseSunsetSunrise: CheckBox
    private lateinit var layoutSunrise: ConstraintLayout
    private lateinit var layoutSunset: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_wallpaper_settings, container, false)

        val appSettings = AppSettings.getInstance(requireContext())

        val formatter = SimpleDateFormat("hh:mm", Locale.getDefault())

        // UI Initialization
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

//        activeTheme = sharedPreferences.getString(getString(R.string.preferences_active_theme), "")!!
//        if (activeTheme.isNotEmpty()) {
//
//            // UI Initialization
//            textViewNextChange = root.findViewById(R.id.textView_NextChange)
//            textViewActiveTheme = root.findViewById(R.id.theme_name_TextView)
//            floatingActionButtonSetWallpaper = root.findViewById(R.id.button_SetWallpaper)
//
//            // Listeners
//            floatingActionButtonSetWallpaper.setOnClickListener {
//                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
//                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(requireContext(), DynamicWallpaperService::class.java))
//                startActivity(intent)
//            }
//
//            // Get theme config
//            if (activeTheme.isNotEmpty()) {
//                themeConfig = ThemeConfiguration(requireContext(), activeTheme, useLocation)
//            }
//        } else {
//            // TODO redirect
//            if (activity != null) {
//                (activity as MainActivity).noSettings()
//            }
//        }

        return root
    }

    override fun update() { }
}