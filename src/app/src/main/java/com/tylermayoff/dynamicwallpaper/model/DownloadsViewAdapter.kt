package com.tylermayoff.dynamicwallpaper.model

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.ui.main.TabDownloadWallpaper
import com.tylermayoff.dynamicwallpaper.ui.main.TabWallpaperSettings
import com.tylermayoff.dynamicwallpaper.util.CustomUtilities
import com.tylermayoff.dynamicwallpaper.util.GithubAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class DownloadsViewAdapter(var tabDownloadWallpaper: TabDownloadWallpaper, var context: Context, var themes: Array<GithubAPI.GithubThemeItem>) : RecyclerView.Adapter<DownloadsViewAdapter.DownloadsHolder>() {

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

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
        lateinit var themeItem: GithubAPI.GithubThemeItem
        var downloadId: Long = -1

        // Downloads
        private var onComplete: BroadcastReceiver? = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context?, intent: Intent?) {
                if (intent == null) return

                val referenceId: Long = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                if (downloadId == referenceId) {

                    tabDownloadWallpaper.stopDownload()
                    context.unregisterReceiver(this)
                    // TODO Unzip the folder and prep the directory
                    val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                    if (cursor.moveToFirst()) {
                        val uri: Uri = Uri.parse(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
                        val f = File(uri.path!!)
                        if (CustomUtilities.unpackZip(f, File(context.filesDir.toString() + "/" + context.getString(R.string.themes_relative_path) + "/" + themeItem.name))) {
                            removeItem(themeItem)
                        }
                    }
                }
            }
        }

        init {
            cardView.setOnClickListener {
                GlobalScope.launch {
                    val themesDir = File(context.filesDir.absolutePath + context.getString(R.string.themes_relative_path))
                    if (!themesDir.exists()) themesDir.mkdir()

                    // Clear the theme directory
                    val themeDir = File(themesDir.absolutePath + "/" + themeItem.name)
                    themeDir.delete()
                    themeDir.mkdir()

                    // Download the .zip Content
                    context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                    val request = DownloadManager.Request(Uri.parse(themeItem.downloadUrl))
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_WIFI)
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    request.setDestinationInExternalFilesDir(context, null, "tmp/" + themeItem.name + ".zip")
                    downloadId = downloadManager.enqueue(request)

                    tabDownloadWallpaper.startDownload()
                }
            }
        }
    }
}