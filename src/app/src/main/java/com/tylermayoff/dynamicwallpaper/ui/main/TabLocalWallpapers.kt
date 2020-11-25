package com.tylermayoff.dynamicwallpaper.ui.main

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.model.LocalThemesViewAdapter
import com.tylermayoff.dynamicwallpaper.model.ThemeItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.nio.file.Files
import java.util.*

class TabLocalWallpapers : UpdateableFragment() {

    override fun update() {}

    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var themeViewAdapter: LocalThemesViewAdapter

    var themes: Array<ThemeItem> = arrayOf()

    private var imageTypeFilter = FileFilter { pathname: File ->
        try {
            val mimeType = Files.probeContentType(pathname.toPath())
            return@FileFilter mimeType.startsWith("image/")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        false
    }

    private var notContained = FileFilter { pathname: File ->
        for (t in themes) {
            if (t.name == pathname.name) {
                return@FileFilter false
            }
        }
        true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_change_wallpaper, container, false)

        // Swipe Refresh
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout_LocalRefresh)
        swipeRefreshLayout.isRefreshing = true

        // Recycler
        themeViewAdapter = LocalThemesViewAdapter(requireContext(), themes)
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = themeViewAdapter

        getLocalThemes()

        // Events
        swipeRefreshLayout.setOnRefreshListener { getLocalThemes() }

        return root
    }

    private fun getLocalThemes () {
        swipeRefreshLayout.isRefreshing = true

        GlobalScope.launch {
            val dir = File(requireContext().filesDir.toString() + requireContext().getString(R.string.themes_relative_path))
            val themeFolders: Array<File>? = dir.listFiles(notContained)
            if (themeFolders != null) {
                var tmpThemes = mutableListOf<ThemeItem>()

                for (theme in themeFolders) {
                    val images: Array<File> = theme.listFiles(imageTypeFilter) ?: continue

                    if (images.isEmpty()) continue
                    val r = Random()
                    val rImg = r.nextInt(images.size)
                    val img = BitmapFactory.decodeFile(images[rImg].absolutePath)
                    tmpThemes.add(ThemeItem(theme.name, img))
                }
                themes = tmpThemes.toTypedArray()
            }

            activity?.runOnUiThread {
                themeViewAdapter.themes = themes
                themeViewAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}