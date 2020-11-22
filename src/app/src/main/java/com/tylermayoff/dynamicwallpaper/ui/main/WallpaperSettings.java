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

public class WallpaperSettings extends Fragment {

    private View root;

    public WallpaperSettings() { }

    public static WallpaperSettings newInstance() {
        WallpaperSettings fragment = new WallpaperSettings();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_wallpaper_settings, container, false);

        // Get Preferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(getContext().getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String themeName = sharedPreferences.getString(getContext().getString(R.string.preferences_active_theme), "");

        // Setup UI
        TextView themeName_TextView = root.findViewById(R.id.theme_name_TextView);
        themeName_TextView.setText(themeName);

        return root;
    }
}