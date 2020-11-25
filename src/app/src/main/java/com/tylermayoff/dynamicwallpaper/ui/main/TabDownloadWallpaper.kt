package com.tylermayoff.dynamicwallpaper.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.model.DownloadsViewAdapter
import com.tylermayoff.dynamicwallpaper.util.GithubAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class TabDownloadWallpaper : UpdateableFragment() {

    private lateinit var downloadsAdapter: DownloadsViewAdapter
    private lateinit var themes: Array<GithubAPI.GithubThemeItem>
    lateinit var requestQueue: RequestQueue

    // UI
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressUI: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_download_wallpaper, container, false)

        // Create Http Request queue
        requestQueue = Volley.newRequestQueue(requireContext())

        // UI
        progressUI = root.findViewById(R.id.progressUI)
        progressUI.visibility = View.INVISIBLE

        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout_DownloadsRefresh)
        swipeRefreshLayout.isRefreshing = true

        themes = arrayOf()
        downloadsAdapter = DownloadsViewAdapter(this, requireContext(), themes)
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

    fun startDownload() {
        activity?.runOnUiThread {
            progressUI.visibility = View.VISIBLE
        }
    }

    fun stopDownload() {
        activity?.runOnUiThread {
            progressUI.visibility = View.INVISIBLE
        }
    }

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