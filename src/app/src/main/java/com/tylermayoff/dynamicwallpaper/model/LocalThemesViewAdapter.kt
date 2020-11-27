package com.tylermayoff.dynamicwallpaper.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.tylermayoff.dynamicwallpaper.R

class LocalThemesViewAdapter(var context: Context, var themes: Array<LocalThemeItem>): RecyclerView.Adapter<LocalThemesViewAdapter.ThemeHolder>() {

    var onThemeClickedListener = ArrayList<(name: String) -> Unit>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.theme_item, parent, false)
        return ThemeHolder(view, context)
    }

    override fun onBindViewHolder(holder: ThemeHolder, position: Int) {
        holder.previewImg.setImageBitmap(themes[position].image)
        holder.theme = themes[position]
    }

    override fun getItemCount(): Int {
        return themes.size
    }

    inner class ThemeHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
        var previewImg: ImageView = itemView.findViewById(R.id.preview_ImageView)
        lateinit var theme: LocalThemeItem

        init {

            // Setup UI

            // Listeners
            previewImg.setOnClickListener {
                onThemeClickedListener.forEach { it(theme.name) }
            }
        }
    }
}