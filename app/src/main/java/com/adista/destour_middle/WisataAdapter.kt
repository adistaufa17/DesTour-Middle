package com.adista.destour_middle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WisataAdapter(private val wisataData: WisataData) : RecyclerView.Adapter<WisataAdapter.WisataViewHolder>() {

    // ViewHolder untuk setiap item wisata
    class WisataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewWisata)
        val title: TextView = itemView.findViewById(R.id.textViewNamaWisata)
        val lokasi: TextView = itemView.findViewById(R.id.textViewLokasi)
        val deskripsi: TextView = itemView.findViewById(R.id.textViewDeskripsi)
    }

    // Mengikat data ke tampilan item wisata
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wisata, parent, false)
        return WisataViewHolder(view)
    }

    override fun onBindViewHolder(holder: WisataViewHolder, position: Int) {
        // Mengakses wisataList di dalam WisataData
        val wisataItem = wisataData.wisataList[position]  // Memperbaiki akses data

        // Mengikat data gambar, judul, lokasi, dan deskripsi
        holder.title.text = wisataItem.title
        holder.lokasi.text = wisataItem.lokasi
        holder.deskripsi.text = wisataItem.deskripsi

        // Menggunakan Glide untuk menampilkan gambar dari URL
        Glide.with(holder.itemView.context)
            .load(wisataItem.imageUrl)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return wisataData.wisataList.size  // Mengembalikan ukuran daftar wisata
    }
}

