package com.goraya.downloader

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.goraya.downloader.databinding.ActivityMainBinding
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val qualityOptions = listOf("Best","1080p","720p","480p","360p","MP3 Audio")
    private val downloadedFiles = mutableListOf<String>()
    private lateinit var adapter: DownloadAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initYoutubeDL()
        binding.spinnerQuality.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, qualityOptions)
        adapter = DownloadAdapter(downloadedFiles)
        binding.rvDownloads.layoutManager = LinearLayoutManager(this)
        binding.rvDownloads.adapter = adapter
        binding.btnDownload.setOnClickListener {
            val url = binding.etVideoUrl.text.toString().trim()
            if (url.isEmpty()) { Toast.makeText(this,"لنک درج کریں",Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            startDownload(url, binding.spinnerQuality.selectedItemPosition)
        }
        loadDownloadedFiles()
    }

    private fun initYoutubeDL() {
        lifecycleScope.launch(Dispatchers.IO) {
            try { YoutubeDL.getInstance().init(applicationContext) }
            catch (e: YoutubeDLException) { Log.e("Goraya", "init failed", e) }
        }
    }

    private fun startDownload(url: String, q: Int) {
        binding.layoutProgress.visibility = View.VISIBLE
        binding.btnDownload.isEnabled = false
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "GorayaVideoDownloader")
                if (!dir.exists()) dir.mkdirs()
                val req = YoutubeDLRequest(url)
                req.addOption("-o", "${dir.absolutePath}/%(title)s.%(ext)s")
                when (q) {
                    0 -> req.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best")
                    1 -> req.addOption("-f", "bestvideo[height<=1080][ext=mp4]+bestaudio[ext=m4a]/best[height<=1080][ext=mp4]/best")
                    2 -> req.addOption("-f", "bestvideo[height<=720][ext=mp4]+bestaudio[ext=m4a]/best[height<=720][ext=mp4]/best")
                    3 -> req.addOption("-f", "bestvideo[height<=480][ext=mp4]+bestaudio[ext=m4a]/best[height<=480][ext=mp4]/best")
                    4 -> req.addOption("-f", "bestvideo[height<=360][ext=mp4]+bestaudio[ext=m4a]/best[height<=360][ext=mp4]/best")
                    5 -> req.addOption("-x", "--audio-format", "mp3")
                }
                YoutubeDL.getInstance().execute(req) { p, eta, _ ->
                    runOnUiThread {
                        binding.progressBar.progress = p.toInt()
                        binding.tvProgressStatus.text = "${p.toInt()}% | ETA: ${eta}s"
                    }
                }
                withContext(Dispatchers.Main) {
                    binding.progressBar.progress = 100
                    binding.tvProgressStatus.text = "✅ مکمل"
                    binding.btnDownload.isEnabled = true
                    loadDownloadedFiles()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnDownload.isEnabled = true
                    binding.tvProgressStatus.text = "❌ ${e.message}"
                }
            }
        }
    }

    private fun loadDownloadedFiles() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "GorayaVideoDownloader")
            val files = dir.listFiles()?.filter { it.isFile }?.map { it.name } ?: emptyList()
            withContext(Dispatchers.Main) {
                downloadedFiles.clear(); downloadedFiles.addAll(files); adapter.notifyDataSetChanged()
            }
        }
    }
}
