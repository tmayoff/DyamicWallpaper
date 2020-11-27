package com.tylermayoff.dynamicwallpaper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.Zenith
import com.tylermayoff.dynamicwallpaper.util.AppSettings
import com.tylermayoff.dynamicwallpaper.util.CustomUtilities.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileFilter
import java.nio.file.Files
import java.util.*

class ThemeConfiguration() {

    interface ThemeConfigTimeChangeListener {
        fun onTimeChangedListener()
    }

    private val imageFilter : FileFilter = FileFilter { pathname ->
        val mimeType = Files.probeContentType(pathname.toPath())
        mimeType.startsWith("image/")
    }

    private val jsonFilter : FileFilter = FileFilter { pathname ->
        val mimeType = Files.probeContentType(pathname.toPath())
        mimeType == "application/json"
    }

    var onTimesChanges : ThemeConfigTimeChangeListener? = null

    var images = mutableListOf<Bitmap>()
    var wallpaperChangeTimes = mutableListOf<Calendar>()
    var usingSunsetSunriseTime : Boolean = false
    lateinit var themeConfig : ThemeConfig

    constructor(context: Context, themeName : String) : this() {
        val themeDir = File(context.filesDir.absolutePath + "/themes/" + themeName)

        val imageFiles : Array<File> = themeDir.listFiles(imageFilter) ?: return

        Arrays.sort(imageFiles) {f1, f2 -> compareNatural(f1.name, f2.name) }

        for (image : File in imageFiles) {
            val b : Bitmap = BitmapFactory.decodeFile(image.absolutePath)
            images.add(b)
        }

        val appSettings = AppSettings.getInstance(context)
        var useSunsetSunrise = appSettings.useSunsetSunrise
        if (appSettings.sunsetTime == null || appSettings.sunriseTime == null)
            useSunsetSunrise = false

        // Get theme.json configuration
        val configFile : Array<File>? = themeDir.listFiles(jsonFilter)
        if (configFile != null) {
            val gson : Gson = GsonBuilder().create()
            val jsonString : String = FileUtils.readFileToString(configFile[0], "UTF-8")
            themeConfig = gson.fromJson(jsonString, ThemeConfig::class.java)

            if (useSunsetSunrise) {
                // Day times
                val sunsetSunriseLength = 10
                val dayNightInterval: Int = ((appSettings.sunsetTime!!.timeInMillis - appSettings.sunriseTime!!.timeInMillis).toInt() / 1000 / 60) - sunsetSunriseLength
                val dayIntervals = dayNightInterval / themeConfig.dayImageList.size
                val nightIntervals = dayNightInterval / themeConfig.nightImageList.size
                val sunriseIntervals = sunsetSunriseLength / themeConfig.sunriseImageList.size
                val sunsetIntervals = sunsetSunriseLength / themeConfig.sunsetImageList.size

                // Times 00:00 - Sunrise
                var startCal : Calendar = GregorianCalendar()
//                startCal.set(0, 0, 0, 0, 0, 0)
//                for (i in 0..themeConfig.nightImageList.size / 2) {
//                    wallpaperChangeTimes.add(startCal.clone() as Calendar)
//                    startCal.add(Calendar.MINUTE, nightIntervals)
//                }

                // Times Sunrise
                startCal = appSettings.sunriseTime!!
                startCal.add(Calendar.MINUTE, -sunsetSunriseLength / 2)
                for (i in 0..themeConfig.sunriseImageList.size) {
                    wallpaperChangeTimes.add(startCal.clone() as Calendar)
                    startCal.add(Calendar.MINUTE, sunriseIntervals)
                }

                // Times Sunrise - Sunset
                startCal = appSettings.sunriseTime!!
                startCal.add(Calendar.MINUTE, sunsetSunriseLength / 2)
                for (i in 0..themeConfig.dayImageList.size) {
                    wallpaperChangeTimes.add(startCal.clone() as Calendar)
                    startCal.add(Calendar.MINUTE, dayIntervals)
                }

                // Times Sunset - 00:00
                startCal = appSettings.sunsetTime!!
                startCal.add(Calendar.MINUTE, dayIntervals)
//                for (i in 0..themeConfig.nightImageList.size / 2) {
//                    wallpaperChangeTimes.add(startCal.clone() as Calendar)
//                    startCal.add(Calendar.MINUTE, nightIntervals)
//                }
            }
        }

        if (!useSunsetSunrise) {
            val timeIncrements = 24 * 60 / images.size
            val startCal : Calendar = GregorianCalendar()

            startCal.set(0, 0, 0, 0, 0, 0)
            for (i in 0..images.size) {
                wallpaperChangeTimes.add(startCal.clone() as Calendar)
                startCal.add(Calendar.MINUTE, timeIncrements)
            }
        }
    }

    fun getCurrentTimeIntervalIndex () : Int {
        if (wallpaperChangeTimes.size == 0) return -1

        var lastCal = wallpaperChangeTimes[0]
        val now = Calendar.getInstance()
        // Clear data we don't need / can't use
        now.set(Calendar.YEAR, 0)
        now.set(Calendar.DAY_OF_MONTH, 0)
        now.set(Calendar.MONTH, 0)

        for (i in 1 until wallpaperChangeTimes.size) {
            val currentIndexTime = wallpaperChangeTimes[i]
            val lastIndexTime = lastCal
            if (now >= lastIndexTime && now <= currentIndexTime) {
                return i - 1
            }
            lastCal = wallpaperChangeTimes[i]
        }

        return 0
    }

    fun getNextTimeChange() : Calendar? {
        val lastIndex = getCurrentTimeIntervalIndex()
        if (lastIndex == -1) return null
        val index = (lastIndex + 1) % wallpaperChangeTimes.size
        return wallpaperChangeTimes[index]
    }

    inner class ThemeConfig {
        var imageFilename : String = ""
        var imageCredits : String = ""
        var sunriseImageList: Array<Int> = arrayOf()
        var dayImageList: Array<Int> = arrayOf()
        var sunsetImageList: Array<Int> = arrayOf()
        var nightImageList: Array<Int> = arrayOf()
    }
}