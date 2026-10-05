package com.goraya.downloader

import android.app.Application
import com.yausername.youtubedl_android.YoutubeDL

class DownloaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try { YoutubeDL.getInstance().init(this) } catch (_: Exception) {}
    }
}
