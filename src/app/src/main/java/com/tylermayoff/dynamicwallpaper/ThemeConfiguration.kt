package com.tylermayoff.dynamicwallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileFilter
import java.nio.file.Files
import java.util.*

class ThemeConfiguration() {

    private val imageFilter : FileFilter = FileFilter { pathname ->
        var mimeType = Files.probeContentType(pathname.toPath())
        mimeType.startsWith("image/")
    }

    private val jsonFilter : FileFilter = FileFilter { pathname ->
        var mimeType = Files.probeContentType(pathname.toPath())
        mimeType == "application/json"
    }

    var images = mutableListOf<Bitmap>()
    var wallpaperChangeTimes = mutableListOf<Calendar>()
    var usingSunsetSunriseTime : Boolean = false

    constructor(context: Context, themeName : String) : this() {
        var themeDir : File = File(context.filesDir.absolutePath + "/theme/" + themeName)

        var imageFiles : Array<File>? = themeDir.listFiles(imageFilter)
        if (imageFiles == null) return

        // TODO Sort not properly sorting
        Arrays.sort(imageFiles) { f1, f2 -> f1.name.compareTo(f2.name) }

        for (image : File in imageFiles) {
            var b : Bitmap = BitmapFactory.decodeFile(image.absolutePath)
            images.add(b)
        }

        // Get theme.json configuration
        var configFile : Array<File> = themeDir.listFiles(jsonFilter)
        if (configFile.isNotEmpty()) {
            var gson : Gson = GsonBuilder().create()
            var jsonString : String = FileUtils.readFileToString(configFile[0], "UTF-8")
            var conf: ThemeConfig = gson.fromJson(jsonString, ThemeConfig::class.java)

        }

        if (!usingSunsetSunriseTime) {
            var timeIncrements = 24 * 60 / images.size
            var startCal : Calendar = GregorianCalendar()

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
        var lastCal = wallpaperChangeTimes[0]
        var now = Calendar.getInstance()

        for (i in 1..wallpaperChangeTimes.size) {
            var currentHour = now.get(Calendar.HOUR_OF_DAY)
            var currentIndexHour = wallpaperChangeTimes[i].get(Calendar.HOUR_OF_DAY)
            var lastIndexHour = lastCal.get(Calendar.HOUR_OF_DAY)
            if (currentHour in lastIndexHour until currentIndexHour) {
                return i - 1
            }
            lastCal = wallpaperChangeTimes[i]
        }

        return 0
    }

    fun getNextTimeChange() : Calendar {
        var lastIndex = getCurrentTimeIntervalIndex()
        var index = (lastIndex + 1) % wallpaperChangeTimes.size
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