package com.tylermayoff.dynamicwallpaper.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tylermayoff.dynamicwallpaper.MainActivity
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.model.LocalThemesViewAdapter
import com.tylermayoff.dynamicwallpaper.model.LocalThemeItem
import com.tylermayoff.dynamicwallpaper.util.AppSettings
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter
import java.util.*

class TabLocalWallpapers : UpdateableFragment() {

    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var themeViewAdapter: LocalThemesViewAdapter

    var themes: Array<LocalThemeItem> = arrayOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_change_wallpaper, container, false)

        // Swipe Refresh
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout_LocalRefresh)
        swipeRefreshLayout.isRefreshing = true

        // Recycler
        themeViewAdapter = LocalThemesViewAdapter(requireContext(), themes)
        themeViewAdapter.onThemeClickedListener.add {
            (activity as MainActivity).setActiveTheme(it)
        }
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = themeViewAdapter

        getLocalThemes()

        // Events
        swipeRefreshLayout.setOnRefreshListener { getLocalThemes() }

        return root
    }

    override fun update() {
        getLocalThemes()
    }

    private fun getLocalThemes () {
        swipeRefreshLayout.isRefreshing = true

        val appSettings = AppSettings.getInstance(requireContext())

        appSettings.getLocalThemes {
            activity?.runOnUiThread {
                themeViewAdapter.themes = appSettings.localThemes
                themeViewAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}