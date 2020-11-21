package com.tylermayoff.dynamicwallpaper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.File;
import java.util.Calendar;

public class DynamicWallpaperService extends WallpaperService {

    private final int ALARM_INTENT = 1;

    DynamicWallpaperEngine engine;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (startId == ALARM_INTENT) {
            engine.NextImage();
        }
        else {
            boolean load = !intent.getStringExtra("load").isEmpty();
            if (load && engine != null)
                engine.themeConfig = new ThemeConfig(new File(getFilesDir() + "/theme"));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public Engine onCreateEngine() {
        engine = new DynamicWallpaperEngine(this);
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

        private AlarmManager alarmManager;

        Context context;

        public DynamicWallpaperEngine(Context c) {
            context = c;

            if (themeConfig == null)
                themeConfig = new ThemeConfig(new File(getFilesDir() + "/theme"));

            // TODO finish alarm
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, DynamicWallpaperEngine.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            // Setup next background change
            currentIndex = themeConfig.GetLastTimeIndex();
            Calendar nextAlarm = themeConfig.GetNextTime(currentIndex);
            alarmManager.set(AlarmManager.RTC, nextAlarm.getTimeInMillis(), pendingIntent);

            changingImage = true;

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
                        changingImage = false;
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

        private void NextImage () {
            currentIndex++;

            Intent intent = new Intent(context, DynamicWallpaperEngine.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            // Setup next background change
            Calendar nextAlarm = themeConfig.GetNextTime(currentIndex);
            alarmManager.set(AlarmManager.RTC, nextAlarm.getTimeInMillis(), pendingIntent);
            changingImage = true;
        }
    }
}
