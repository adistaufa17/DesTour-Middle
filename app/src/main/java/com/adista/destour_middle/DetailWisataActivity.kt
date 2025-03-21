package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adista.destour_middle.databinding.ActivityDetailWisataBinding
import com.bumptech.glide.Glide

class DetailWisataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailWisataBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var wisataId: Int = 0
    private var isBookmarked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailWisataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        wisataId = intent.getIntExtra("WISATA_ID", 0)
        val title = intent.getStringExtra("WISATA_TITLE") ?: "Nama Wisata"
        val lokasi = intent.getStringExtra("WISATA_LOKASI") ?: "Lokasi Tidak Diketahui"
        val deskripsi = intent.getStringExtra("WISATA_DESKRIPSI") ?: "Deskripsi tidak tersedia"
        val imageUrl = intent.getStringExtra("WISATA_IMAGE") ?: ""

        binding.detailTitle.text = title
        binding.detailLokasi.text = lokasi
        binding.detailDeskripsi.text = deskripsi

        Glide.with(this)
            .load(imageUrl)
            .into(binding.detailImage)

        isBookmarked = sharedPreferences.getBoolean("BOOKMARK_$wisataId", false)
        updateBookmarkIcon()

        binding.detailBookmark.setOnClickListener {
            toggleBookmark()
        }
    }

    private fun updateBookmarkIcon() {
        binding.detailBookmark.setImageResource(if (isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_bookmark)
    }

    private fun toggleBookmark() {
        isBookmarked = !isBookmarked
        sharedPreferences.edit().apply {
            putBoolean("BOOKMARK_$wisataId", isBookmarked)
            apply()
        }

        Toast.makeText(this, if (isBookmarked) "Bookmark ditambahkan" else "Bookmark dihapus", Toast.LENGTH_SHORT).show()
        updateBookmarkIcon()

        val resultIntent = Intent().apply {
            putExtra("WISATA_ID", wisataId)
            putExtra("IS_BOOKMARKED", isBookmarked)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
