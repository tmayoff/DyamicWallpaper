package com.tylermayoff.dynamicwallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class ThemeConfig {

    public List<Bitmap> images;

    private List<Calendar> displayChangeTimes;

    public ThemeConfig (Context c, String themeName) {
        this(new File(c.getFilesDir() + "/theme/" + themeName));
    }

    public ThemeConfig (File themeFolder) {
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
        if (images == null)
            return;

        Arrays.sort(images, (o1, o2) -> o1.getName().compareTo(o2.getName()));

        for (File image : images) {
            Bitmap b = BitmapFactory.decodeFile(image.getAbsolutePath());
            this.images.add(b);
        }

        int timeIncrements = 24 * 60 / images.length;
        Calendar calendar = new GregorianCalendar();
        calendar.roll(Calendar.MINUTE, true);
        calendar.roll(Calendar.HOUR_OF_DAY, true);
        calendar.set(0, 0, 0, 0, 0, 0);
        for (int i = 0; i < images.length; i++) {
            displayChangeTimes.add((Calendar) calendar.clone());
            calendar.add(Calendar.MINUTE, timeIncrements);
        }
    }

    public int GetLastTimeIndex () {
        Calendar lastCal = displayChangeTimes.get(0);
        Calendar now = Calendar.getInstance();

        for(int i = 0;  i < displayChangeTimes.size(); i++) {
            int nH = now.get(Calendar.HOUR_OF_DAY);
            int h = displayChangeTimes.get(i).get(Calendar.HOUR_OF_DAY);
            int lastH = lastCal.get(Calendar.HOUR_OF_DAY);

            if (nH >= lastH && nH <= h)
                return i;
            lastCal = displayChangeTimes.get(i);
        }

        return 0;
    }

    public Calendar GetNextTime (int index) {
        index += 1;
        if (index >= displayChangeTimes.size())
            index = 0;
        return displayChangeTimes.get(index);
    }
}
