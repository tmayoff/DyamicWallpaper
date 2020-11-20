package com.tylermayoff.dynamicwallpaper;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.File;
import java.util.LinkedList;

public class DynamicWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new DynamicWallpaperEngine();
    }

    private class DynamicWallpaperEngine extends Engine {
        private final Handler handler = new Handler();
        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private int height;
        private int width;
        private boolean visible = true;

        private SharedPreferences sharedPreferences;

        private String themePath;
        private LinkedList<Bitmap> images;

        public DynamicWallpaperEngine() {
            images = new LinkedList<>();
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            themePath = sharedPreferences.getString("themePath", "");
            getImages();

            handler.post(drawRunner);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
                this.visible = visible;
                if(visible)
                    handler.post(drawRunner);
                else
                    handler.removeCallbacks(drawRunner);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            this.width = width;
            this.height = height;

            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            // Do nothing
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            if (images.size() == 0) {
                getImages();
            }

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    if (images.size() == 0)
                        return;

                    int width = images.get(0).getWidth();
                    int height = images.get(0).getHeight();
                    canvas.drawBitmap(images.get(0), new Rect(0, 0, width, height), new Rect(0,0,width,height), null);
                }
            }
            finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }

            handler.removeCallbacks(drawRunner);
            if(visible) handler.postDelayed(drawRunner, 5000);
        }

        void getImages() {
            if (themePath == "")
                themePath = sharedPreferences.getString("themePath", "");
            if (themePath == "")
                return;


                File dir = new File(themePath);
            File[] files = dir.listFiles();
            if (files.length == 0)
                return;

            images = new LinkedList<>();
            for (File f: files) {
                Bitmap img = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (img != null)
                    images.add(img);
            }
        }
    }
}
