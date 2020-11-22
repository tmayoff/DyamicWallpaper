package com.tylermayoff.dynamicwallpaper.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tylermayoff.dynamicwallpaper.R;

import java.util.List;

public class ThemeViewAdapter extends RecyclerView.Adapter<ThemeViewAdapter.ThemeHolder> {

    public List<Theme> themes;
    private final Context context;

    public ThemeViewAdapter (Context context, List<Theme> themes) {
        this.context = context;
        this.themes = themes;
    }

    @NonNull
    @Override
    public ThemeViewAdapter.ThemeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.theme_item, parent, false);

        return new ThemeHolder(view, context);
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

        public ThemeHolder(@NonNull View itemView, Context context) {
            super(itemView);

            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Setup UI
            previewImg = itemView.findViewById(R.id.preview_ImageView);

            // Listeners
            previewImg.setOnClickListener(view -> {
                editor.putString(context.getString(R.string.preferences_active_theme), theme.name);
                Toast.makeText(context, "Set theme to " + theme.name, Toast.LENGTH_SHORT).show();
            });
        }
    }
}
