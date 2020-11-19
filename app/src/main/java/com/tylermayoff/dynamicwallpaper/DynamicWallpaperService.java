package com.tylermayoff.dynamicwallpaper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

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

        public DynamicWallpaperEngine() {
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

        private void draw(){
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setColor(Color.WHITE);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeJoin(Paint.Join.ROUND);
                    paint.setStrokeWidth(10f);

                    float widthQuater = width / 4;
                    float heightQuater = height / 4;

                    canvas.drawRect(widthQuater, height - heightQuater,
                            width - widthQuater, heightQuater, paint);
                }
            }
            finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }

            handler.removeCallbacks(drawRunner);
            if(visible) handler.postDelayed(drawRunner, 5000);
        }
    }
}
