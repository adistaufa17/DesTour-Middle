package com.adista.destour_middle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WisataAdapter(private var wisataData: List<WisataItem>) :
    RecyclerView.Adapter<WisataAdapter.WisataViewHolder>() {

    class WisataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewWisata)
        val title: TextView = itemView.findViewById(R.id.textViewNamaWisata)
        val lokasi: TextView = itemView.findViewById(R.id.textViewLokasi)
        val deskripsi: TextView = itemView.findViewById(R.id.textViewDeskripsi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wisata, parent, false)
        return WisataViewHolder(view)
    }

    override fun onBindViewHolder(holder: WisataViewHolder, position: Int) {
        val wisataItem = wisataData[position]
        holder.title.text = wisataItem.title
        holder.lokasi.text = wisataItem.lokasi
        holder.deskripsi.text = wisataItem.deskripsi

        // 🔹 Ambil ID gambar dari Google Drive URL
        val imageUrl = wisataItem.imageUrl
        val imageId = imageUrl.split("/")[5] // Mengambil bagian ke-5 dari URL

        // 🔹 Buat URL tampilan langsung dari Google Drive
        val directImageUrl = "https://drive.google.com/uc?export=view&id=$imageId"

        // 🔹 Tampilkan gambar menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(directImageUrl)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return wisataData.size
    }

    fun updateData(newData: List<WisataItem>) {
        wisataData = newData
        notifyDataSetChanged()
    }
}
