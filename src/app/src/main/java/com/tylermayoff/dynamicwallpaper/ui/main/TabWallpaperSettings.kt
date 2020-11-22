package com.tylermayoff.dynamicwallpaper.ui.main

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tylermayoff.dynamicwallpaper.DynamicWallpaperService
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.ThemeConfig

class TabWallpaperSettings : UpdateableFragment () {

    private lateinit var sharedPreferences : SharedPreferences

    var activeTheme : String? = null
    private lateinit var themeConfig : ThemeConfig

    // UI
    private lateinit var textViewNextChange : TextView
    private lateinit var textViewActiveTheme : TextView
    private lateinit var floatingActionButtonSetWallpaper : FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_wallpaper_settings, container, false)

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
        activeTheme = sharedPreferences.getString(getString(R.string.preferences_active_theme), "")

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
        if (activeTheme != null || activeTheme != "")
            themeConfig = ThemeConfig(requireContext(), activeTheme)

        return root
    }

    override fun update() {
        activeTheme = sharedPreferences.getString(getString(R.string.preferences_active_theme), "")
        textViewActiveTheme.setText(activeTheme)

        var lastTimeIndex = themeConfig.GetLastTimeIndex()
        var nextTime = themeConfig.GetNextTime(lastTimeIndex)
        textViewNextChange.setText(nextTime.time.toString())
    }
}