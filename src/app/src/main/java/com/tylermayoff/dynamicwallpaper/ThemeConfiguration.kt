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

    @SuppressLint("MissingPermission")
    constructor(context: Context, themeName : String, useLocation: Boolean = false) : this() {
        val themeDir = File(context.filesDir.absolutePath + "/theme/" + themeName)

        val imageFiles : Array<File>? = themeDir.listFiles(imageFilter)
        if (imageFiles == null) return

        // TODO Sort not properly sorting
        Arrays.sort(imageFiles) {f1, f2 -> compareNatural(f1.name, f2.name) }

        for (image : File in imageFiles) {
            val b : Bitmap = BitmapFactory.decodeFile(image.absolutePath)
            images.add(b)
        }

        // Get theme.json configuration
        val configFile : Array<File> = themeDir.listFiles(jsonFilter)
        if (configFile.isNotEmpty()) {
            val gson : Gson = GsonBuilder().create()
            val jsonString : String = FileUtils.readFileToString(configFile[0], "UTF-8")
            themeConfig = gson.fromJson(jsonString, ThemeConfig::class.java)

            // Setup sunset / sunrise times
            var sunrise : Calendar
            var sunset : Calendar
            if (useLocation) {
                var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->
                    sunrise = SunriseSunsetCalculator.getSunrise(location.latitude, location.longitude, TimeZone.getTimeZone("America/New_York"), Calendar.getInstance(), Zenith.CIVIL.degrees().toDouble())
                    sunset = SunriseSunsetCalculator.getSunset(location.latitude, location.longitude, TimeZone.getTimeZone("America/New_York"), Calendar.getInstance(), Zenith.CIVIL.degrees().toDouble())
                }
            }
        }

        if (!useLocation) {
            val timeIncrements = 24 * 60 / images.size
            val startCal : Calendar = GregorianCalendar()

            startCal.roll(Calendar.MINUTE, true)
            startCal.roll(Calendar.HOUR_OF_DAY, true)
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