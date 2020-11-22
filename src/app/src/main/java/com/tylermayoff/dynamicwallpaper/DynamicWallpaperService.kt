package com.tylermayoff.dynamicwallpaper

import android.service.wallpaper.WallpaperService

class DynamicWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return DynamicWallpaperEngine()
    }

    private inner class DynamicWallpaperEngine : WallpaperService.Engine() {

    }
}