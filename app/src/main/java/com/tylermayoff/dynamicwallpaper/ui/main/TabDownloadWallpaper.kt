package com.tylermayoff.dynamicwallpaper.ui.main

import android.app.AlertDialog
import android.database.Cursor
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.model.DownloadsViewAdapter
import com.tylermayoff.dynamicwallpaper.util.CustomUtilities
import com.tylermayoff.dynamicwallpaper.util.GithubAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.concurrent.ExecutionException


class TabDownloadWallpaper : UpdateableFragment() {

    private lateinit var downloadsAdapter: DownloadsViewAdapter
    private lateinit var themes: Array<GithubAPI.GithubThemeItem>
    lateinit var requestQueue: RequestQueue

    // UI
    private lateinit var fabImportTheme: FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressUI: ConstraintLayout

    private var onImportTheme = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        val contentResolver = requireContext().contentResolver

        val dst = File(requireContext().filesDir.absolutePath + "/tmp/")
        val cursor: Cursor? = contentResolver.query(it, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val inputStream = contentResolver.openInputStream(it)

                val displayName = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                val newFile = File(dst, displayName)
                FileUtils.copyInputStreamToFile(inputStream, newFile)
                if (newFile.exists()) {

                    val themesDir = File(requireContext().filesDir.absolutePath + requireContext().getString(R.string.themes_relative_path))
                    if (!themesDir.exists()) themesDir.mkdir()

                    val themeName = FilenameUtils.getBaseName(newFile.absolutePath)

                    // Clear the theme directory
                    val themeDir = File(themesDir.absolutePath + "/" + themeName)
                    themeDir.delete()
                    themeDir.mkdir()
                    CustomUtilities.unpackZip(newFile, File(requireContext().filesDir.toString() + "/" + requireContext().getString(R.string.themes_relative_path) + "/" + themeName))
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_download_wallpaper, container, false)

        // Create Http Request queue
        requestQueue = Volley.newRequestQueue(requireContext())

        // UI
        fabImportTheme = root.findViewById(R.id.fab_ImportTheme)

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

        fabImportTheme.setOnClickListener {
            // TODO Check for permissions
            onImportTheme.launch(arrayOf("application/zip"))
        }

        return root
    }

    override fun update() { }

    private fun populateRecyclerView() {
        GlobalScope.launch {
            try {
                val nullThemes = GithubAPI.getThemesFromGithub(requireContext())
                themes = nullThemes
            } catch (e: ExecutionException) {
                activity?.runOnUiThread {
                    val alertB = AlertDialog.Builder(requireContext())
                    alertB.setTitle(e.message)
                    alertB.create().show()
                }
            }

            activity?.runOnUiThread {
                downloadsAdapter.themes = themes
                downloadsAdapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}