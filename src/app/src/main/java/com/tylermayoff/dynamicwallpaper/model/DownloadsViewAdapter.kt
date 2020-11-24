package com.tylermayoff.dynamicwallpaper.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tylermayoff.dynamicwallpaper.R

class DownloadsViewAdapter(var themes: Array<DownloadableItem>) : RecyclerView.Adapter<DownloadsViewAdapter.DownloadsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.download_item, parent, false)
        return DownloadsHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadsHolder, position: Int) {
        var d = themes[position]
        holder.textViewName.text = d.name
        if (d.previewImage != null)
            holder.imageViewPreview.setImageBitmap(d.previewImage)

    }

    override fun getItemCount(): Int = themes.size

    inner class DownloadsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewName: TextView = itemView.findViewById(R.id.textView_ThemeName)
        var imageViewPreview: ImageView = itemView.findViewById(R.id.imageView_DownloadPreview)
    }
}