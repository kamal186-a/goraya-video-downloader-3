package com.goraya.downloader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DownloadAdapter(private val files: List<String>) : RecyclerView.Adapter<DownloadAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) { val tv: TextView = v.findViewById(android.R.id.text1) }
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(android.R.layout.simple_list_item_1, p, false))
    override fun onBindViewHolder(h: VH, i: Int) { h.tv.text = "📄 ${files[i]}" }
    override fun getItemCount() = files.size
}
