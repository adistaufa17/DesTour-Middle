package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WisataAdapter(
    private var wisataData: List<WisataItem>,
    private val context: Context,
    private val onBookmarkClick: (WisataItem) -> Unit
) : RecyclerView.Adapter<WisataAdapter.WisataViewHolder>() {

    class WisataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewWisata)
        val title: TextView = itemView.findViewById(R.id.textViewNamaWisata)
        val lokasi: TextView = itemView.findViewById(R.id.textViewLokasi)
        val deskripsi: TextView = itemView.findViewById(R.id.textViewDeskripsi)
        val bookmarkButton: ImageView = itemView.findViewById(R.id.buttonBookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wisata, parent, false)
        return WisataViewHolder(view)
    }

    override fun onBindViewHolder(holder: WisataViewHolder, position: Int) {
        val wisataItem = wisataData[position]
        val sharedPreferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        val isBookmarked = sharedPreferences.getBoolean("BOOKMARK_${wisataItem.id}", false)

        holder.title.text = wisataItem.title
        holder.lokasi.text = wisataItem.lokasi
        holder.deskripsi.text = wisataItem.deskripsi

        val imageUrl = wisataItem.imageUrl
        val imageId = imageUrl.split("/")[5]
        val directImageUrl = "https://drive.google.com/uc?export=view&id=$imageId"

        Glide.with(holder.itemView.context)
            .load(directImageUrl)
            .into(holder.imageView)

        // Jika sudah di-bookmark, tampilkan ikon bookmark, jika belum, sembunyikan
        if (isBookmarked) {
            holder.bookmarkButton.visibility = View.VISIBLE
            holder.bookmarkButton.setImageResource(R.drawable.ic_bookmarked)
        } else {
            holder.bookmarkButton.visibility = View.GONE
        }

        // Tombol bookmark tetap ada, tapi disembunyikan jika belum di-bookmark
        holder.bookmarkButton.setOnClickListener {
            onBookmarkClick(wisataItem)
            notifyItemChanged(position)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailWisataActivity::class.java).apply {
                putExtra("WISATA_ID", wisataItem.id)
                putExtra("WISATA_TITLE", wisataItem.title)
                putExtra("WISATA_LOKASI", wisataItem.lokasi)
                putExtra("WISATA_DESKRIPSI", wisataItem.deskripsi)
                putExtra("WISATA_IMAGE", directImageUrl)
            }
            (holder.itemView.context as MainActivity).resultLauncher.launch(intent)
        }
    }

    override fun getItemCount(): Int {
        return wisataData.size
    }

    fun updateData(newData: List<WisataItem>) {
        wisataData = newData
        notifyDataSetChanged()
    }

    fun updateBookmarkStatus(wisataId: Int, isBookmarked: Boolean) {
        for (i in wisataData.indices) {
            if (wisataData[i].id == wisataId) {
                notifyItemChanged(i)
                break
            }
        }
    }
}