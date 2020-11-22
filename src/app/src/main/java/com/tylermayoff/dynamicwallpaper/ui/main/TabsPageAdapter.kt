package com.tylermayoff.dynamicwallpaper.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabsPageAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var tabsCount = 2
    private var fragments = mutableListOf<Fragment>()

    override fun getItemCount(): Int {
        return tabsCount
    }

    override fun createFragment(position: Int): Fragment {

        var frag : Fragment = when (position) {
            0 -> TabWallpaperSettings()
            1 -> ChangeWallpaper()
            else -> TabWallpaperSettings()
        }

        fragments.add(frag);
        return frag;
    }

    fun updateFragment(position: Int) {
        (fragments[position] as UpdateableFragment).update();
    }
}