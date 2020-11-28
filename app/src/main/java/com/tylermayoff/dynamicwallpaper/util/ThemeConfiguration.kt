package com.tylermayoff.dynamicwallpaper.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tylermayoff.dynamicwallpaper.util.CustomUtilities.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileFilter
import java.nio.file.Files
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

class ThemeConfiguration() {

    private val imageFilter : FileFilter = FileFilter { pathname ->
        val mimeType = Files.probeContentType(pathname.toPath())
        mimeType.startsWith("image/")
    }

    private val jsonFilter : FileFilter = FileFilter { pathname ->
        val mimeType = Files.probeContentType(pathname.toPath())
        mimeType == "application/json"
    }
    var images = mutableListOf<Bitmap>()
    var wallpaperChangeTimes = mutableListOf<LocalTime>()
    var nightTimes = mutableListOf<LocalTime>()
    var sunriseTimes = mutableListOf<LocalTime>()
    var dayTimes = mutableListOf<LocalTime>()
    var sunsetTimes = mutableListOf<LocalTime>()

    var useSunsetSunrise : Boolean = false
    lateinit var themeConfig : ThemeConfig

    constructor(context: Context, themeName : String, useSun: Boolean, sunrise: LocalTime?, sunset: LocalTime?) : this() {
        useSunsetSunrise = useSun

        val themeDir = File(context.filesDir.absolutePath + "/themes/" + themeName)

        val imageFiles : Array<File> = themeDir.listFiles(imageFilter) ?: return

        Arrays.sort(imageFiles) {f1, f2 -> compareNatural(f1.name, f2.name) }

        for (image : File in imageFiles) {
            val b : Bitmap = BitmapFactory.decodeFile(image.absolutePath)
            images.add(b)
        }

        // Get theme.json configuration
        val configFile : Array<File>? = themeDir.listFiles(jsonFilter)
        if (configFile != null) {
            val gson : Gson = GsonBuilder().create()
            val jsonString : String = FileUtils.readFileToString(configFile[0], "UTF-8")
            themeConfig = gson.fromJson(jsonString, ThemeConfig::class.java)

            if (sunrise == null || sunset == null) useSunsetSunrise = false

            if (useSunsetSunrise) {
                // Day times
                val sunsetSunriseLength = 10

                val dayLength: Int =  abs(ChronoUnit.MINUTES.between(sunset, sunrise).toInt() - sunsetSunriseLength)
                val nightLength: Int = (24 * 60) - dayLength - sunsetSunriseLength

                val dayIntervals = dayLength / themeConfig.dayImageList.size
                val nightIntervals = nightLength / themeConfig.nightImageList.size
                val sunriseIntervals = sunsetSunriseLength / themeConfig.sunriseImageList.size
                val sunsetIntervals = sunsetSunriseLength / themeConfig.sunsetImageList.size

                // Sunrise
                var startTime = sunrise
                for (i in themeConfig.sunriseImageList) {
                    sunriseTimes.add(startTime!!)
                    startTime = startTime.plusMinutes(sunriseIntervals.toLong())
                }

                // Times day
                for (i in themeConfig.dayImageList) {
                    dayTimes.add(startTime!!)
                    startTime = startTime.plusMinutes(dayIntervals.toLong())
                }

                // Sunset
                startTime = sunset
                for (i in themeConfig.sunsetImageList) {
                    sunsetTimes.add(startTime!!)
                    startTime = startTime.plusMinutes(sunsetIntervals.toLong())
                }

                // Night
                for (i in themeConfig.nightImageList) {
                    nightTimes.add(startTime!!)
                    startTime = startTime.plusMinutes(nightIntervals.toLong())
                }
            }
        }

        if (!useSunsetSunrise) {
            val timeIncrements = 24 * 60 / images.size
            var startCal = LocalTime.of(0, 0, 0)

            for (i in 0..images.size) {
                wallpaperChangeTimes.add(startCal)
                startCal = startCal.plusMinutes(timeIncrements.toLong())
            }
        }
    }

    fun getCurrentBitmap (): Bitmap {
        return if (useSunsetSunrise)
            getBitmap(getCurrentTimeIndex())
        else {
            images[getCurrentTimeIndexWithoutSun()]
        }
    }

    fun getNextChangeTime(): LocalTime {
        return if (useSunsetSunrise)
            getTime(getCurrentTimeIndex() + 1)
        else
            getNextTimeChangeWithoutSun()
    }

    private fun getCurrentTimeIndex(): Int {
        val now = LocalTime.now()

        var last = sunriseTimes[0]
        for (i in 1 until sunriseTimes.size) {
            if (now  >= last && now <= sunriseTimes[i]) {
                return i - 1
            }
            last = sunriseTimes[i]
        }

        for (i in 0 until dayTimes.size) {
            if (now  >= last && now <= dayTimes[i]) {
                return  i + sunriseTimes.size - 1
            }
            last = dayTimes[i]
        }

        for (i in 0 until sunsetTimes.size) {
            if (now  >= last && now <= sunsetTimes[i]) {
                return i + sunriseTimes.size + dayTimes.size - 1
            }
            last = sunsetTimes[i]
        }

        for (i in 0 until nightTimes.size) {
            if (now  >= last && now <= nightTimes[i]) {
                return i + sunriseTimes.size + dayTimes.size + sunsetTimes.size - 1
            }
            last = nightTimes[i]
        }

        return 0
    }

    private fun getTime(i: Int): LocalTime {
        var index: Int = i
        if (index < sunriseTimes.size) return sunriseTimes[index]
        index -= sunriseTimes.size
        if (index < dayTimes.size) return dayTimes[index]
        index -= dayTimes.size
        if (index < sunsetTimes.size) return sunsetTimes[index]
        index -= sunsetTimes.size
        if (index < nightTimes.size) return nightTimes[index]

        return nightTimes[0]
    }

    private fun getBitmap (i: Int): Bitmap {
        var index: Int = i
        if (index < sunriseTimes.size) return images[themeConfig.sunriseImageList[index] - 1]
        index -= sunriseTimes.size
        if (index < dayTimes.size) return images[themeConfig.dayImageList[index] - 1]
        index -= dayTimes.size
        if (index < sunsetTimes.size) return images[themeConfig.sunsetImageList[index] - 1]
        index -= sunsetTimes.size
        if (index < nightTimes.size) return images[themeConfig.nightImageList[index] - 1]

        return images[themeConfig.nightImageList[0]]
    }

    private fun getCurrentTimeIndexWithoutSun () : Int {
        if (wallpaperChangeTimes.size == 0) return -1

        var lastCal = wallpaperChangeTimes[0]
        val now = LocalTime.now()

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

    private fun getNextTimeChangeWithoutSun() : LocalTime {
        val lastIndex = getCurrentTimeIndexWithoutSun()
        if (lastIndex == -1) return wallpaperChangeTimes[0]
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