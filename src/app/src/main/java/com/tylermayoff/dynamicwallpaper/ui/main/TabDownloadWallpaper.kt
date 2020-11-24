package com.tylermayoff.dynamicwallpaper.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.model.DownloadableItem
import com.tylermayoff.dynamicwallpaper.model.DownloadsViewAdapter
import com.tylermayoff.dynamicwallpaper.util.GithubAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TabDownloadWallpaper : UpdateableFragment() {

    private lateinit var downloadsAdapter: DownloadsViewAdapter
    private lateinit var themes: Array<DownloadableItem>
    lateinit var requestQueue: RequestQueue

    // UI
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_download_wallpaper, container, false)

        // Create Http Request queue
        requestQueue = Volley.newRequestQueue(requireContext())

        // UI
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout_DownloadsRefresh)
        swipeRefreshLayout.isRefreshing = true

        themes = arrayOf(DownloadableItem())
        downloadsAdapter = DownloadsViewAdapter(themes)
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView_Downloads)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = downloadsAdapter

        // Populate recycler
        populateRecyclerView()

        // Event listener
        swipeRefreshLayout.setOnRefreshListener {
            populateRecyclerView()
        }

        return root
    }

    override fun update() { }

    private fun populateRecyclerView() {
        GlobalScope.launch {
            val nullThemes = GithubAPI.getThemesFromGithub(requireContext())
            if (nullThemes != null)
                themes = nullThemes

            activity?.runOnUiThread {
                downloadsAdapter.themes = themes
                downloadsAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}