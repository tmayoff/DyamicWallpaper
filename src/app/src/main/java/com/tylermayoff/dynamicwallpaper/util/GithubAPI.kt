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
import com.google.gson.GsonBuilder
import com.tylermayoff.dynamicwallpaper.model.DownloadableItem
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.concurrent.TimeUnit

class GithubAPI {

    companion object {
        fun getThemeUrlFromName(themeName: String): String {
            return "https://api.github.com/repos/tmayoff/DyamicWallpaper/contents/$themeName?ref=downloads"
        }


        fun getTheme(context: Context, themeUrl: String): Array<GithubThemeItem> {
            val requestQueue = VolleyRequestSingleton.getInstance(context).requestQueue

            val future: RequestFuture<JSONArray> = RequestFuture.newFuture()
            val request = JsonArrayRequest(Request.Method.GET, themeUrl, JSONArray(), future, future)
            requestQueue.add(request)

            return try {
                val response = future.get(10, TimeUnit.SECONDS)
                val array: ArrayList<GithubThemeItem> = arrayListOf()
                for (i in 0 until response.length()) {
                    val gson = Gson()
                    val gitTheme: GithubThemeItem = gson.fromJson(response[i].toString(), GithubThemeItem::class.java)
                    gitTheme.image = downloadImage(context, gitTheme.download_url)
                    array.add(gitTheme)
                }

                array.toTypedArray()
            }catch (e: Exception) {
                arrayOf()
            }
        }

        fun getThemesFromGithub (context: Context): Array<DownloadableItem>? {
            val requestQueue = VolleyRequestSingleton.getInstance(context).requestQueue

            val future: RequestFuture<JSONArray> = RequestFuture.newFuture()
            val url = "https://api.github.com/repos/tmayoff/DyamicWallpaper/contents?ref=downloads"
            val request = JsonArrayRequest(Request.Method.GET, url, JSONArray(), future, future)
            requestQueue.add(request)

            return try {
                val response = future.get(10, TimeUnit.SECONDS)
                val gson: Gson = GsonBuilder().create()
                val items: Array<DownloadableItem> = gson.fromJson(response.toString(), Array<DownloadableItem>::class.java)
                for (item: DownloadableItem in items) {
                    item.previewImage = getPreviewImage(context, "https://api.github.com/repos/tmayoff/DyamicWallpaper/contents/" + item.name + "?ref=downloads")
                }

                items

            } catch (e: Exception) {
                Log.d("Github API", e.message!!)
                null
            }
        }

        fun getPreviewImage (context: Context, gitUrl: String): Bitmap? {
            val requestQueue = VolleyRequestSingleton.getInstance(context).requestQueue

            // Get image list
            val future: RequestFuture<JSONArray> = RequestFuture.newFuture()
            val request = JsonArrayRequest(Request.Method.GET, gitUrl, JSONArray(), future, future)
            requestQueue.add(request)

            return try {
                val res = future.get(10, TimeUnit.SECONDS)
                val previewIndex = res.length() / 2
                val preview: JSONObject = res[previewIndex] as JSONObject
                downloadImage(context, preview["download_url"].toString())
            } catch (e: Exception) {
                null
            }
        }

        fun downloadImage (context: Context, downloadUrl: String): Bitmap? {
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

    inner class GithubThemeItem {
        var name: String = ""
        var download_url: String = ""
        var image: Bitmap? = null
    }
}