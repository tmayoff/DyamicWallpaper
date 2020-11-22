package com.tylermayoff.dynamicwallpaper.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tylermayoff.dynamicwallpaper.R;

public class WallpaperSettings extends Fragment {


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
        return inflater.inflate(R.layout.fragment_wallpaper_settings, container, false);
    }
}