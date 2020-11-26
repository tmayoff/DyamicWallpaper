package com.tylermayoff.dynamicwallpaper

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tylermayoff.dynamicwallpaper.ui.main.TabsPageAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences : SharedPreferences
    lateinit var activeTheme : String

    lateinit var pageAdapter: TabsPageAdapter
    lateinit var viewPager: ViewPager2
    lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
        activeTheme = sharedPreferences.getString(getString(R.string.preferences_active_theme), "")!!


        pageAdapter = TabsPageAdapter(this)
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tabs)


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

    fun noSettings () {
        viewPager.setCurrentItem(1, true)
    }
}