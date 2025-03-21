package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WisataAdapter(
    private var wisataData: List<WisataItem>,
    private val context: Context,
    private val onBookmarkClick: (WisataItem, Boolean) -> Unit,
    private val onLikeClick: (WisataItem, Boolean) -> Unit
) : RecyclerView.Adapter<WisataAdapter.WisataViewHolder>() {

    private val clickInProgress = HashSet<Int>()

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

        // Ambil status dari SharedPreferences, prioritaskan isBookmarked dan isLiked dari model jika tersedia
        val isBookmarked = wisataItem.isBookmarked || sharedPreferences.getBoolean("BOOKMARK_${wisataItem.id}", false)
        val isLiked = wisataItem.isLiked || sharedPreferences.getBoolean("LIKE_${wisataItem.id}", false)

        // Update SharedPreferences jika berbeda dengan model (sinkronisasi)
        if (sharedPreferences.getBoolean("BOOKMARK_${wisataItem.id}", false) != isBookmarked) {
            sharedPreferences.edit().putBoolean("BOOKMARK_${wisataItem.id}", isBookmarked).apply()
        }

        if (sharedPreferences.getBoolean("LIKE_${wisataItem.id}", false) != isLiked) {
            sharedPreferences.edit().putBoolean("LIKE_${wisataItem.id}", isLiked).apply()
        }

        holder.title.text = wisataItem.title
        holder.lokasi.text = wisataItem.lokasi
        holder.deskripsi.text = wisataItem.deskripsi

        val imageUrl = wisataItem.imageUrl
        // Pastikan pengolahan URL gambar tidak error
        try {
            val imageId = imageUrl.split("/")[5]
            val directImageUrl = "https://drive.google.com/uc?export=view&id=$imageId"

            Glide.with(holder.itemView.context)
                .load(directImageUrl)
                .into(holder.imageView)
        } catch (e: Exception) {
            // Fallback jika format URL tidak sesuai ekspektasi
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.imageView)
        }

        // Update bookmark dan like icon
        holder.bookmarkButton.setImageResource(if (isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_bookmark)
        holder.likeButton.setImageResource(if (isLiked) R.drawable.ic_liked else R.drawable.ic_like)

        // Item click listener untuk navigasi ke detail
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailWisataActivity::class.java).apply {
                putExtra("WISATA_ID", wisataItem.id)
                putExtra("WISATA_TITLE", wisataItem.title)
                putExtra("WISATA_LOKASI", wisataItem.lokasi)
                putExtra("WISATA_DESKRIPSI", wisataItem.deskripsi)

                // Handle URL dengan aman
                try {
                    val imageId = imageUrl.split("/")[5]
                    val directImageUrl = "https://drive.google.com/uc?export=view&id=$imageId"
                    putExtra("WISATA_IMAGE", directImageUrl)
                } catch (e: Exception) {
                    putExtra("WISATA_IMAGE", imageUrl)
                }
            }
            if (context is MainActivity) {
                context.resultLauncher.launch(intent)
            } else {
                context.startActivity(intent)
            }
        }

        // Bookmark click listener
        holder.bookmarkButton.setOnClickListener {
            val newBookmarkState = !isBookmarked

            // Update SharedPreferences secara optimis
            sharedPreferences.edit().putBoolean("BOOKMARK_${wisataItem.id}", newBookmarkState).apply()

            // Update UI
            holder.bookmarkButton.setImageResource(if (newBookmarkState) R.drawable.ic_bookmarked else R.drawable.ic_bookmark)

            // Panggil callback
            onBookmarkClick(wisataItem, newBookmarkState)
        }

        // Like click listener
        holder.likeButton.setOnClickListener {
            val newLikeState = !isLiked

            // Update SharedPreferences secara optimis
            sharedPreferences.edit().putBoolean("LIKE_${wisataItem.id}", newLikeState).apply()

            // Update UI
            holder.likeButton.setImageResource(if (newLikeState) R.drawable.ic_liked else R.drawable.ic_like)

            // Panggil callback
            onLikeClick(wisataItem, newLikeState)
        }
    }

    override fun getItemCount(): Int {
        return wisataData.size
    }

    // Method untuk memperbarui status bookmark dari luar (DetailWisataActivity)
    fun updateBookmarkStatus(wisataId: Int, isBookmarked: Boolean) {
        // Temukan item dan perbarui
        for (i in wisataData.indices) {
            if (wisataData[i].id == wisataId) {
                // Simpan perubahan di SharedPreferences
                context.getSharedPreferences("user_pref", Context.MODE_PRIVATE).edit()
                    .putBoolean("BOOKMARK_$wisataId", isBookmarked).apply()

                // Beri tahu adapter untuk memperbarui item
                notifyItemChanged(i)
                break
            }
        }
    }

    // Method untuk memperbarui status like dari luar (DetailWisataActivity)
    fun updateLikeStatus(wisataId: Int, isLiked: Boolean) {
        // Temukan item dan perbarui
        for (i in wisataData.indices) {
            if (wisataData[i].id == wisataId) {
                // Simpan perubahan di SharedPreferences
                context.getSharedPreferences("user_pref", Context.MODE_PRIVATE).edit()
                    .putBoolean("LIKE_$wisataId", isLiked).apply()

                // Beri tahu adapter untuk memperbarui item
                notifyItemChanged(i)
                break
            }
        }
    }

    fun updateData(newData: List<WisataItem>) {
        // Implementasi DiffUtil untuk meningkatkan performa
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = wisataData.size
            override fun getNewListSize(): Int = newData.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return wisataData[oldItemPosition].id == newData[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = wisataData[oldItemPosition]
                val newItem = newData[newItemPosition]
                return oldItem.title == newItem.title &&
                        oldItem.lokasi == newItem.lokasi &&
                        oldItem.imageUrl == newItem.imageUrl &&
                        oldItem.isBookmarked == newItem.isBookmarked &&
                        oldItem.isLiked == newItem.isLiked
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        wisataData = newData
        diffResult.dispatchUpdatesTo(this)

        // Sinkronkan status bookmark dan like dari API dengan SharedPreferences
        val sharedPreferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        for (item in newData) {
            // Jika status dari API berbeda dengan SharedPreferences, prioritaskan status API
            if (item.isBookmarked) {
                editor.putBoolean("BOOKMARK_${item.id}", true)
            }
            if (item.isLiked) {
                editor.putBoolean("LIKE_${item.id}", true)
            }
        }

        editor.apply()
    }
}