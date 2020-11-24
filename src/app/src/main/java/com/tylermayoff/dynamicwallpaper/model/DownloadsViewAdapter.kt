package com.tylermayoff.dynamicwallpaper.model

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tylermayoff.dynamicwallpaper.R
import com.tylermayoff.dynamicwallpaper.util.GithubAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class DownloadsViewAdapter(var context: Context, var themes: Array<DownloadableItem>) : RecyclerView.Adapter<DownloadsViewAdapter.DownloadsHolder>() {

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

    inner class DownloadsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewName: TextView = itemView.findViewById(R.id.textView_ThemeName)
        var imageViewPreview: ImageView = itemView.findViewById(R.id.imageView_DownloadPreview)
        var cardView: CardView = itemView.findViewById(R.id.cardView_DownloadableItem)
        lateinit var themeItem: DownloadableItem

        init {
            cardView.setOnClickListener {
                GlobalScope.launch {
                    val themesDir = File(context.filesDir.absolutePath + context.getString(R.string.themes_relative_path))
                    if (!themesDir.exists()) themesDir.mkdir()

                    val themeDir = File(themesDir.absolutePath + "/" + themeItem.name)
                    themeDir.delete()
                    themeDir.mkdir()
                    val files: Array<GithubAPI.GithubThemeItem> = GithubAPI.getTheme(context, themeItem._links?.self!!)
                    for (i in files.indices) {
                        val githubThemeItem = files[i]
                        val file = File(themeDir, githubThemeItem.name)
                        // Save the images
                        val outputStream = FileOutputStream(file)
                        githubThemeItem.image?.compress(Bitmap.CompressFormat.PNG, 85, outputStream)
                        outputStream.flush()
                    }
                }
            }
        }
    }
}