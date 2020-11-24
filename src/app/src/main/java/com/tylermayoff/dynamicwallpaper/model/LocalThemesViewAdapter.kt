package com.tylermayoff.dynamicwallpaper.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.tylermayoff.dynamicwallpaper.R

class LocalThemesViewAdapter(var context: Context, var themes: Array<ThemeItem>): RecyclerView.Adapter<LocalThemesViewAdapter.ThemeHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.theme_item, parent, false)
        return ThemeHolder(view, context)
    }

    override fun onBindViewHolder(holder: ThemeHolder, position: Int) {
        holder.previewImg.setImageBitmap(themes!![position].image)
        holder.theme = themes!![position]
    }

    override fun getItemCount(): Int {
        return themes!!.size
    }


    class ThemeHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
        var previewImg: ImageView
        var theme: ThemeItem? = null

        init {
            val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preferences_file_key), Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // Setup UI
            previewImg = itemView.findViewById(R.id.preview_ImageView)

            // Listeners
            previewImg.setOnClickListener { view: View? ->
                editor.putString(context.getString(R.string.preferences_active_theme), theme!!.name)
                editor.apply()
                Toast.makeText(context, "Set theme to " + theme!!.name, Toast.LENGTH_SHORT).show()
            }
        }
    }
}