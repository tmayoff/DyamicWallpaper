package com.tylermayoff.dynamicwallpaper.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tylermayoff.dynamicwallpaper.R;

public class WallpaperSettings extends UpdateableFragment {

    private View root;
    private SharedPreferences sharedPreferences;

    // UI
    private TextView textView_ThemeName;

    public WallpaperSettings() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_wallpaper_settings, container, false);

        // Get Preferences
        sharedPreferences = getContext().getSharedPreferences(getContext().getString(R.string.preferences_file_key), Context.MODE_PRIVATE);

        // Setup UI
        textView_ThemeName = root.findViewById(R.id.theme_name_TextView);

        update();

        return root;
    }

    @Override
    public void update() {
        String themeName = sharedPreferences.getString(getContext().getString(R.string.preferences_active_theme), "");
        textView_ThemeName.setText(themeName);
    }
}