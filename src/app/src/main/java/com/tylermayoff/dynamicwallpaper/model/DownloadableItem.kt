package com.tylermayoff.dynamicwallpaper.model

import android.graphics.Bitmap

class DownloadableItem {

    var name: String = ""
    var themeUrl: String = ""

    var previewImage: Bitmap? = null

    var _links: Link? = null

    inner class Link {
        var self: String = ""
    }
}