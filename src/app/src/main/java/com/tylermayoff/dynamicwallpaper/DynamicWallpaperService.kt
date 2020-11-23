package com.tylermayoff.dynamicwallpaper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class DynamicWallpaperService : WallpaperService() {

    lateinit var engine : DynamicWallpaperEngine

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.action?.equals("NEXT_IMG")!!) {
                engine.nextImage()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreateEngine(): Engine {
        engine = DynamicWallpaperEngine(this as Context)
        return engine
    }

    inner class DynamicWallpaperEngine(private val context: Context) : WallpaperService.Engine() {

        private var sharedPreferences : SharedPreferences = getSharedPreferences(context.getString(R.string.preferences_file_key), Context.MODE_PRIVATE)

        private val show : Runnable = Runnable { show() }
        private val changeImage : Runnable = Runnable { changeImage() }
        private val handler : Handler = Handler(Looper.getMainLooper())
        private val alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Theme Specifics
        private var themeConfig: ThemeConfiguration
        private var activeThemeName : String
        private var currentImageIndex : Int

        // Screen specifics
        private var screenHeight : Int = 0
        private var screenWidth : Int = 0
        private var wallpaperIsVisible : Boolean = true

        // Draw
        private val fadeMilli : Long = 500
        private val fadeStep : Long = 5
        private val alphaStep : Long = 255 / (fadeMilli / fadeStep)

        var alphaPaint : Paint = Paint()
        var currentAlpha : Int = 0
        var doneAnimating : Boolean = false

        init {

            activeThemeName = sharedPreferences.getString(context.getString(R.string.preferences_active_theme), "")!!

            if (activeThemeName.isEmpty()) {
                // TODO Redirect to activity
            }

            themeConfig = ThemeConfiguration(context, activeThemeName)

            // Setup background
            currentImageIndex = themeConfig.getCurrentTimeIntervalIndex()

            // Setup alarm
            val intent = Intent(context, DynamicWallpaperEngine::class.java)
            intent.action = "NEXT_IMG"
            val pendingIntent : PendingIntent = PendingIntent.getService(context, 0, intent, 0)

            val nextAlarm = themeConfig.getNextTimeChange()
            alarmManager.set(AlarmManager.RTC, nextAlarm.timeInMillis, pendingIntent)

            handler.post(changeImage)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            this.screenHeight = height
            this.screenWidth = width

            super.onSurfaceChanged(holder, format, width, height)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible){
                currentImageIndex = themeConfig.getCurrentTimeIntervalIndex()
                this.wallpaperIsVisible = visible
                doneAnimating = true
                handler.post(show)
            } else {
                handler.removeCallbacks(changeImage)
            }

            super.onVisibilityChanged(visible)
        }

        fun nextImage () {
            currentImageIndex++
            if (currentImageIndex >= themeConfig.images.size)
                currentImageIndex = 0

            currentAlpha = 0
            doneAnimating = false

            val intent = Intent(context, DynamicWallpaperEngine::class.java)
            intent.action = "NEXT_IMG"
            val pendingIntent : PendingIntent = PendingIntent.getService(context, 0, intent, 0)

            val nextAlarm = themeConfig.getNextTimeChange()
            alarmManager.set(AlarmManager.RTC, nextAlarm.timeInMillis, pendingIntent)
        }

        private fun show () {

            var canvas : Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    val img = themeConfig.images.get(currentImageIndex)
                    val originalHeight = img.height
                    val originalWidth = img.width

                    val scale : Float = screenHeight / originalHeight * 2.0f
                    val xTranslate = (screenWidth - originalWidth * scale) / 2.0f
                    val yTranslate = (screenHeight - originalHeight * scale) / 2.0f

                    val transformation = Matrix()
                    transformation.postTranslate(xTranslate, yTranslate)
                    transformation.preScale(scale, scale)

                    val p = Paint()
                    p.isFilterBitmap = true
                    canvas.drawBitmap(img, transformation, p)
                }
            } finally {
                if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }

        private fun changeImage () {
            var canvas : Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    val img = themeConfig.images[currentImageIndex]
                    val originalHeight = img.height
                    val originalWidth = img.width

                    val scale : Float = screenHeight / originalHeight * 2.0f
                    val xTranslate = (screenWidth - originalWidth * scale) / 2.0f
                    val yTranslate = (screenHeight - originalHeight * scale) / 2.0f

                    val transformation = Matrix()
                    transformation.postTranslate(xTranslate, yTranslate)
                    transformation.preScale(scale, scale)

                    alphaPaint.alpha = currentAlpha
                    alphaPaint.isFilterBitmap = true
                    currentAlpha += alphaStep.toInt()

                    canvas.drawBitmap(img, transformation, alphaPaint)
                    // Done animating
                    if (255 - currentAlpha < 10)
                        doneAnimating = true

                }
            } finally {
                if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas)
            }

            handler.removeCallbacks(changeImage)
            if (wallpaperIsVisible && !doneAnimating) {
                handler.postDelayed(changeImage, fadeStep)
            }
        }
    }
}