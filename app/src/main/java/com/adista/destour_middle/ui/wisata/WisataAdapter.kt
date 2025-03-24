package com.adista.destour_middle.ui.wisata

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adista.destour_middle.MainActivity
import com.adista.destour_middle.R
import com.adista.destour_middle.data.model.WisataItem
import com.adista.destour_middle.databinding.ItemWisataBinding
import com.bumptech.glide.Glide
import timber.log.Timber

class WisataAdapter(
    private var wisataData: List<WisataItem>,
    private val context: Context,
    private val onBookmarkClick: (WisataItem) -> Unit,
) : RecyclerView.Adapter<WisataAdapter.WisataViewHolder>() {

    class WisataViewHolder(val binding: ItemWisataBinding) : RecyclerView.ViewHolder(binding.root)

    private var fullData: List<WisataItem> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataViewHolder {
        val binding = ItemWisataBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WisataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WisataViewHolder, position: Int) {
        val wisataItem = wisataData[position]
        val sharedPreferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        val isBookmarked = sharedPreferences.getBoolean("BOOKMARK_${wisataItem.id}", false)
        val isLiked = sharedPreferences.getBoolean("LIKE_${wisataItem.id}", false)

        with(holder.binding) {
            // Set data
            tvNamaWisata.text = wisataItem.title
            tvLocation.text = wisataItem.lokasi
            tvDescription.text = wisataItem.deskripsi

            val imageUrl = wisataItem.imageUrl
            val imageId = imageUrl.split("/")[5]
            val directImageUrl = "https://drive.google.com/uc?export=view&id=$imageId"

            Glide.with(root.context)
                .load(directImageUrl)
                .into(ivWisata)

            // Like button
            btnLike.setImageResource(if (isLiked) R.drawable.ic_liked else R.drawable.ic_like)

            // Bookmark
            if (isBookmarked) {
                btnBookmark.visibility = View.VISIBLE
                btnBookmark.setImageResource(R.drawable.ic_bookmarked)
            } else {
                btnBookmark.visibility = View.GONE
            }

            // Click listeners
            btnBookmark.setOnClickListener {
                onBookmarkClick(wisataItem)
                notifyItemChanged(position)
            }

            btnLike.setOnClickListener {
                sharedPreferences.edit()
                    .putBoolean("LIKE_${wisataItem.id}", !isLiked)
                    .apply()
                notifyItemChanged(position)
            }


            // Item click
            root.setOnClickListener {
                val intent = Intent(root.context, DetailWisataActivity::class.java).apply {
                    putExtra("WISATA_ID", wisataItem.id)
                    putExtra("WISATA_TITLE", wisataItem.title)
                    putExtra("WISATA_LOKASI", wisataItem.lokasi)
                    putExtra("WISATA_DESKRIPSI", wisataItem.deskripsi)
                    putExtra("WISATA_IMAGE", directImageUrl)
                }
                (root.context as MainActivity).resultLauncher.launch(intent)
            }
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
