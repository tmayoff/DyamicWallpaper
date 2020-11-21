package com.tylermayoff.dynamicwallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.content.MimeTypeFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ThemeConfig {

    List<Bitmap> images;

    private File themeFolder;

    private List<Calendar> displayChangeTimes;

    public ThemeConfig (File themeFolder) {
        this.themeFolder = themeFolder;
        this.images = new LinkedList<>();
        displayChangeTimes = new LinkedList<>();

        File[] images = themeFolder.listFiles(pathname -> {
            try {
                String mimeType = Files.probeContentType(pathname.toPath());
                return mimeType.startsWith("image/");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        });

        Arrays.sort(images, (o1, o2) -> o1.getName().compareTo(o2.getName()));

        for (int i = 0; i < images.length; i++) {
            Bitmap b = BitmapFactory.decodeFile(images[i].getAbsolutePath());
            this.images.add(b);
        }

        int timeIncrements = 24 * 60 / images.length;
        Calendar calendar = Calendar.getInstance();
        calendar.roll(Calendar.MINUTE, true);
        calendar.roll(Calendar.HOUR_OF_DAY, true);
        calendar.set(0, 0, 0, 0, 0);
        for (int i = 0; i < images.length; i++) {
            displayChangeTimes.add(calendar);
            calendar.add(Calendar.MINUTE, timeIncrements);
        }

        Log.d("Time", displayChangeTimes.get(0).getTime().toString());
    }
}
