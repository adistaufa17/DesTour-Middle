package com.adista.destour_middle

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import timber.log.Timber

class WisataAdapter(
    private var wisataData: List<WisataItem>,
    private val context: Context,
    private val onBookmarkClick: (WisataItem) -> Unit,
) : RecyclerView.Adapter<WisataAdapter.WisataViewHolder>() {

    // ✅ Simpan data asli untuk filtering
    private var fullData: List<WisataItem> = listOf()

    class WisataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewWisata)
        val title: TextView = itemView.findViewById(R.id.textViewNamaWisata)
        val lokasi: TextView = itemView.findViewById(R.id.textViewLokasi)
        val deskripsi: TextView = itemView.findViewById(R.id.textViewDeskripsi)
        val bookmarkButton: ImageView = itemView.findViewById(R.id.buttonBookmark)
        val likeButton: ImageView = itemView.findViewById(R.id.buttonLike)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wisata, parent, false)
        return WisataViewHolder(view)
    }

    override fun onBindViewHolder(holder: WisataViewHolder, position: Int) {
        val wisataItem = wisataData[position]
        val sharedPreferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        val isBookmarked = sharedPreferences.getBoolean("BOOKMARK_${wisataItem.id}", false)
        val isLiked = sharedPreferences.getBoolean("LIKE_${wisataItem.id}", false)

        holder.title.text = wisataItem.title
        holder.lokasi.text = wisataItem.lokasi
        holder.deskripsi.text = wisataItem.deskripsi
        holder.likeButton.setImageResource(if (isLiked) R.drawable.ic_liked else R.drawable.ic_like)

        val imageUrl = wisataItem.imageUrl
        val imageId = imageUrl.split("/")[5]
        val directImageUrl = "https://drive.google.com/uc?export=view&id=$imageId"

        Glide.with(holder.itemView.context)
            .load(directImageUrl)
            .into(holder.imageView)

        if (isBookmarked) {
            holder.bookmarkButton.visibility = View.VISIBLE
            holder.bookmarkButton.setImageResource(R.drawable.ic_bookmarked)
        } else {
            holder.bookmarkButton.visibility = View.GONE
        }

        holder.bookmarkButton.setOnClickListener {
            onBookmarkClick(wisataItem)
            notifyItemChanged(position)
        }

        holder.likeButton.setOnClickListener {
            sharedPreferences.edit().putBoolean("LIKE_${wisataItem.id}", !isLiked).apply()
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<WisataItem>) {
        fullData = newData
        wisataData = newData
        notifyDataSetChanged()
    }

    fun updateBookmarkStatus(wisataId: Int, isBookmarked: Boolean) {
        for (i in wisataData.indices) {
            if (wisataData[i].id == wisataId) {
                // Update sharedPreferences biar statusnya langsung nyambung ke icon
                val editor = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE).edit()
                editor.putBoolean("BOOKMARK_$wisataId", isBookmarked)
                editor.apply()

                notifyItemChanged(i)
                break
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setFilter(filterType: String, sharedPreferences: SharedPreferences) {
        Timber.d("Filtering with type: $filterType")

        wisataData = when (filterType) {
            "Bookmark" -> fullData.filter {
                val status = sharedPreferences.getBoolean("BOOKMARK_${it.id}", false)
                Timber.d("Bookmark check - ID: ${it.id}, Status: $status")
                status
            }
            "Like" -> fullData.filter {
                val status = sharedPreferences.getBoolean("LIKE_${it.id}", false)
                Timber.d("Like check - ID: ${it.id}, Status: $status")
                status
            }
            else -> fullData
        }

        Timber.d("Filter result size: ${wisataData.size}")
        notifyDataSetChanged()
    }


}
