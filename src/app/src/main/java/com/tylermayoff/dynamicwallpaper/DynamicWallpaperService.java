package com.tylermayoff.dynamicwallpaper;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.File;

public class DynamicWallpaperService extends WallpaperService {

    DynamicWallpaperEngine engine;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean load = !intent.getStringExtra("load").isEmpty();
        if (load && engine != null)
            engine.themeConfig = new ThemeConfig(new File(getFilesDir() + "/theme"));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public Engine onCreateEngine() {
        engine = new DynamicWallpaperEngine();
        return engine;
    }

    private class DynamicWallpaperEngine extends Engine {

        public ThemeConfig themeConfig;

        private final Handler handler = new Handler();
        private final Runnable drawRunner = () -> draw();

        private int scrHeight;
        private int scrWidth;
        private int currentIndex = 0;
        private boolean visible = true;

        // Animation Rules
        private int FADE_MILLI = 500;
        private int FADE_STEP = 5;
        private int ALPHA_STEP = 255 / (FADE_MILLI / FADE_STEP);

        private Paint alphaPaint = new Paint();
        private int currentAlpha = 0;
        private boolean changingImage = true;

        public DynamicWallpaperEngine() {
            if (themeConfig == null)
                themeConfig = new ThemeConfig(new File(getFilesDir() + "/theme"));

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
            this.scrWidth = width;
            this.scrHeight = height;

            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            // Do nothing
        }

        private void draw() {

            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            if (!changingImage) return;

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    if (themeConfig.images.size() == 0)
                        return;

                    int originalWidth = themeConfig.images.get(currentIndex).getWidth();
                    int originalHeight = themeConfig.images.get(currentIndex).getHeight();

                    int scale = scrHeight / originalHeight * 2;

                    float xTranslation = (scrWidth - originalWidth * scale) / 2.0f;
                    float yTranslation = (scrHeight - originalHeight * scale) / 2.0f;

                    Matrix transformation = new Matrix();
                    transformation.postTranslate(xTranslation, yTranslation);
                    transformation.preScale(scale, scale);

                    alphaPaint.setAlpha(currentAlpha);
                    currentAlpha += ALPHA_STEP;
                    alphaPaint.setFilterBitmap(true);
                    canvas.drawBitmap(themeConfig.images.get(currentIndex), transformation, alphaPaint);

                    if (currentAlpha >= 255) {
                        currentAlpha = 0;
                        currentIndex++;
                        if (currentIndex >= themeConfig.images.size()) currentIndex = 0;
                    }
                }
            }
            finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }

            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.postDelayed(drawRunner, FADE_STEP);
            }
        }
    }
}
