package com.tylermayoff.dynamicwallpaper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tylermayoff.dynamicwallpaper.ui.main.TabsPageAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        var pageAdapter = TabsPageAdapter(this)
        var viewPager : ViewPager2 = findViewById(R.id.view_pager)
        var tabLayout : TabLayout = findViewById(R.id.tabs)

        viewPager.apply {
            adapter = pageAdapter
            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    pageAdapter.updateFragment(position)
                }
            })
        }

        TabLayoutMediator(tabLayout, viewPager) {currentTab, currentPosition ->
            currentTab.text = when (currentPosition) {
                0 -> getString(R.string.tab_text_wallpaper_settings)
                1 -> getString(R.string.tab_text_change_wallpaper)
                2 -> getString(R.string.tab_text_download_wallpaper)
                else -> "Missing String"
            }
        }.attach()
    }
}