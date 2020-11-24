package com.tylermayoff.dynamicwallpaper.ui.main

import android.Manifest
import android.annotation.SuppressLint
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
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.Zenith
import com.tylermayoff.dynamicwallpaper.DynamicWallpaperService
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.ThemeConfiguration
import java.util.*

class TabWallpaperSettings : UpdateableFragment () {

    private lateinit var sharedPreferences : SharedPreferences

    lateinit var activeTheme : String
    private lateinit var themeConfig : ThemeConfiguration
    private var useLocation : Boolean = false

    // UI
    private lateinit var textViewNextChange : TextView
    private lateinit var textViewActiveTheme : TextView
    private lateinit var floatingActionButtonSetWallpaper : FloatingActionButton

    // Activity Launchers
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        result ->
            if (result[Manifest.permission.ACCESS_BACKGROUND_LOCATION]!! && result[Manifest.permission.ACCESS_COARSE_LOCATION]!!) {
                useLocation = true
                if (activeTheme != null || activeTheme != "") {
                    themeConfig = ThemeConfiguration(requireContext(), activeTheme, useLocation)
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_wallpaper_settings, container, false)

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
        activeTheme = sharedPreferences.getString(getString(R.string.preferences_active_theme), "")!!

        // Get Location Permission
        if (!checkPermissions()) {
//            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            useLocation = false
        } else {
            useLocation = true
        }

        // UI Initialization
        textViewNextChange = root.findViewById(R.id.textView_NextChange)
        textViewActiveTheme = root.findViewById(R.id.theme_name_TextView)
        floatingActionButtonSetWallpaper = root.findViewById(R.id.button_SetWallpaper)

        // Listeners
        floatingActionButtonSetWallpaper.setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(requireContext(), DynamicWallpaperService::class.java))
            startActivity(intent)
        }

        // Get theme config
        if (activeTheme != null || activeTheme != "") {
            themeConfig = ThemeConfiguration(requireContext(), activeTheme, useLocation)
        }

        return root
    }

    override fun update() {
        activeTheme = sharedPreferences.getString(getString(R.string.preferences_active_theme), "")!!
        textViewActiveTheme.text = activeTheme

        var nextTime = themeConfig.getNextTimeChange()!!
        textViewNextChange.text = nextTime.time.toString()
    }

    private fun checkPermissions () : Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}