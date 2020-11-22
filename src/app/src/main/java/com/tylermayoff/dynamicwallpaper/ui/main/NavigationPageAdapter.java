package com.tylermayoff.dynamicwallpaper.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.tylermayoff.dynamicwallpaper.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class NavigationPageAdapter extends FragmentStateAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_wallpaper_settings, R.string.tab_text_change_wallpaper};

    private Fragment[] fragments;

    public NavigationPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new WallpaperSettings();
            case 1:
                return new ChangeWallpaper();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return this.TAB_TITLES.length;
    }
}