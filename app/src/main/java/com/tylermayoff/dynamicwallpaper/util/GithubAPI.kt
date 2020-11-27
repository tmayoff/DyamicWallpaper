package com.tylermayoff.dynamicwallpaper.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.RequestFuture
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.concurrent.TimeUnit

class GithubAPI {

    companion object {

        fun getThemesFromGithub (context: Context): Array<GithubThemeItem>? {
            val appSettings = AppSettings.getInstance(context)

            val requestQueue = VolleyRequestSingleton.getInstance(context).requestQueue

            val future: RequestFuture<JSONArray> = RequestFuture.newFuture()
            val url = "https://api.github.com/repos/tmayoff/DyamicWallpaper/contents?ref=downloads"
            val request = JsonArrayRequest(Request.Method.GET, url, JSONArray(), future, future)
            requestQueue.add(request)

            return try {
                val response = future.get(10, TimeUnit.SECONDS)
                val items: ArrayList<GithubThemeItem> = arrayListOf()
                for (i in 0 until response.length()) {
                    val item: JSONObject = response.get(i) as JSONObject

                    // Skip if it's already downloaded
                    var found = false
                    for (local in appSettings.localThemes)
                        if (local.name == item["name"]) {
                            found = true
                            break
                        }
                    if (found) continue

                    val themeItem = getThemeContent(context, item["name"].toString(), "https://api.github.com/repos/tmayoff/DyamicWallpaper/contents/" + item["name"].toString() + "?ref=downloads")
                    if (themeItem != null)
                        items.add(themeItem)
                }
                items.toTypedArray()
            } catch (e: Exception) {
                null
            }
        }

        private fun getThemeContent (context: Context, themeName: String, gitUrl: String): GithubThemeItem? {
            val requestQueue = VolleyRequestSingleton.getInstance(context).requestQueue

            // Get image list
            val future: RequestFuture<JSONArray> = RequestFuture.newFuture()
            val request = JsonArrayRequest(Request.Method.GET, gitUrl, JSONArray(), future, future)
            requestQueue.add(request)

            return try {
                val res = future.get(10, TimeUnit.SECONDS)
                var downloadUrl = ""
                var imageDownloadUrl = ""
                for (i in 0 until res.length()) {
                    val file: JSONObject = res.get(i) as JSONObject
                    if (file["name"].toString().startsWith("Theme_Preview")) {
                        imageDownloadUrl = file["download_url"].toString()
                        continue
                    } else {
                        downloadUrl = file["download_url"].toString()
                    }
                }

                val previewIndex = res.length() / 2
                val preview: JSONObject = res[previewIndex] as JSONObject
                downloadImage(context, preview["download_url"].toString())

                return GithubThemeItem(themeName, downloadUrl, downloadImage(context, imageDownloadUrl))
            } catch (e: Exception) {
                null
            }
        }

        private fun downloadImage (context: Context, downloadUrl: String): Bitmap? {
            val requestQueue = VolleyRequestSingleton.getInstance(context).requestQueue

            val future: RequestFuture<Bitmap> = RequestFuture.newFuture()
            val request = ImageRequest(downloadUrl, future, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, future)
            requestQueue.add(request)
            return try {
                future.get()
            } catch (e: Exception) {
                null
            }
        }
    }

    class GithubThemeItem (var name: String, var downloadUrl: String, var previewImage: Bitmap?)
}