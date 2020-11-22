package com.tylermayoff.dynamicwallpaper.model;

import android.graphics.Bitmap;

public class Theme {

    public Bitmap image;
    public String name;

    public Theme (String name, Bitmap image) {
        this.name = name;
        this.image = image;
    }
}
