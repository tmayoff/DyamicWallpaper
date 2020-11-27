package com.tylermayoff.dynamicwallpaper.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.ui.main.TabDownloadWallpaper
import com.tylermayoff.dynamicwallpaper.util.CustomUtilities
import com.tylermayoff.dynamicwallpaper.util.GithubAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class DownloadsViewAdapter(var tabDownloadWallpaper: TabDownloadWallpaper, var context: Context, var themes: Array<GithubAPI.GithubThemeItem>) : RecyclerView.Adapter<DownloadsViewAdapter.DownloadsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.download_item, parent, false)
        return DownloadsHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadsHolder, position: Int) {
        val downloadableItem = themes[position]
        holder.themeItem = themes[position]
        holder.textViewName.text = downloadableItem.name

        if (downloadableItem.previewImage != null)
            holder.imageViewPreview.setImageBitmap(downloadableItem.previewImage)

        holder.cardView.visibility = View.VISIBLE
        holder.initialize()
    }

    override fun getItemCount(): Int = themes.size

    fun removeItem(githubThemeItem: GithubAPI.GithubThemeItem) {
        val themeList = arrayListOf(*themes)
        themeList.remove(githubThemeItem)
        themes = themeList.toTypedArray()
        notifyDataSetChanged()
    }

    inner class DownloadsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var textViewName: TextView = itemView.findViewById(R.id.textView_ThemeName)
        var imageViewPreview: ImageView = itemView.findViewById(R.id.imageView_DownloadPreview)
        var cardView: CardView = itemView.findViewById(R.id.cardView_DownloadableItem)
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar_Download)
        lateinit var themeItem: GithubAPI.GithubThemeItem

        private var fetchListener: FetchListener = object : FetchListener {
            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = download.progress
            }

            override fun onCompleted(download: Download) {
                val f = File(download.file)
                if (CustomUtilities.unpackZip(f, File(context.filesDir.toString() + "/" + context.getString(R.string.themes_relative_path) + "/" + themeItem.name))) {
                    removeItem(themeItem)
                }
                tabDownloadWallpaper.activity?.runOnUiThread {
                    Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                cardView.setOnClickListener(downloadClickListener)
                tabDownloadWallpaper.activity?.runOnUiThread {
                    Toast.makeText(context, "Download error" + error.name, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = download.progress
            }

            override fun onPaused(download: Download) {}
            override fun onResumed(download: Download) {}
            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {}
            override fun onWaitingNetwork(download: Download) {}
            override fun onAdded(download: Download) {}
            override fun onCancelled(download: Download) {}
            override fun onRemoved(download: Download) {}
            override fun onDeleted(download: Download) {}
            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) { }
        }

        private var downloadClickListener = View.OnClickListener {
            cardView.setOnClickListener(null)

            GlobalScope.launch {
                val themesDir = File(context.filesDir.absolutePath + context.getString(R.string.themes_relative_path))
                if (!themesDir.exists()) themesDir.mkdir()

                // Clear the theme directory
                val themeDir = File(themesDir.absolutePath + "/" + themeItem.name)
                themeDir.delete()
                themeDir.mkdir()

                // Download the .zip Content
                val fetchConfig = FetchConfiguration.Builder(context).setDownloadConcurrentLimit(4).build()
                val fetch = Fetch.Impl.getInstance(fetchConfig)
                val request = Request(themeItem.downloadUrl, context.filesDir.absolutePath + "tmp/" + themeItem.name + ".zip")
                fetch.enqueue(request, {}, {})
                fetch.addListener(fetchListener)
            }
        }

        init {
            initialize()
        }

        fun initialize () {
            progressBar.visibility = View.INVISIBLE
            cardView.setOnClickListener(downloadClickListener)
        }
    }
}