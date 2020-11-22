package com.tylermayoff.dynamicwallpaper.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tylermayoff.dynamicwallpaper.R;

import java.util.List;

public class ThemeViewAdapter extends RecyclerView.Adapter<ThemeViewAdapter.ThemeHolder> {

    private List<Theme> themes;
    private Context context;

    public ThemeViewAdapter (Context context, List<Theme> themes) {
        this.context = context;
        this.themes = themes;
    }

    @NonNull
    @Override
    public ThemeViewAdapter.ThemeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.theme_item, parent, false);

        return new ThemeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewAdapter.ThemeHolder holder, int position) {
        holder.previewImg.setImageBitmap(themes.get(position).image);
        holder.theme = themes.get(position);
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

    public static class ThemeHolder extends RecyclerView.ViewHolder {

        public ImageView previewImg;
        public Theme theme;

        public ThemeHolder(@NonNull View itemView) {
            super(itemView);
            previewImg = itemView.findViewById(R.id.preview_ImageView);

            previewImg.setOnClickListener(view -> {
                
            });
        }
    }
}
