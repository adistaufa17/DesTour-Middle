package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.adista.destour_middle.databinding.ActivityDetailWisataBinding
import com.bumptech.glide.Glide
import timber.log.Timber

class DetailWisataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailWisataBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: WisataViewModel by viewModels()
    private var wisataId: Int = 0
    private var isBookmarked: Boolean = false
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailWisataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("user_token", null)

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

        // Observe bookmark response
        viewModel.bookmarkResponse.observe(this) { response ->
            if (response?.status == "success") {
                Toast.makeText(this, if (isBookmarked) "Bookmark ditambahkan" else "Bookmark dihapus", Toast.LENGTH_SHORT).show()
                Timber.d("Bookmark updated via API: ${response.message}")
            } else {
                Toast.makeText(this, "Gagal memperbarui bookmark", Toast.LENGTH_SHORT).show()
                Timber.e("Failed to update bookmark: ${response?.message}")
            }
        }
    }

    private fun updateBookmarkIcon() {
        binding.detailBookmark.setImageResource(if (isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_bookmark)
    }

    private fun toggleBookmark() {
        isBookmarked = !isBookmarked

        // Simpan status bookmark di SharedPreferences
        sharedPreferences.edit().apply {
            putBoolean("BOOKMARK_$wisataId", isBookmarked)
            commit() // Gunakan commit() untuk memastikan data tersimpan segera
        }

        // Perbarui icon bookmark
        updateBookmarkIcon()

        // Gunakan API untuk update bookmark
        token?.let { safeToken ->
            if (isBookmarked) {
                viewModel.addBookmark(safeToken, wisataId)
            } else {
                viewModel.removeBookmark(safeToken, wisataId)
            }
        }

        // Kirim hasil ke MainActivity
        val resultIntent = Intent().apply {
            putExtra("WISATA_ID", wisataId)
            putExtra("IS_BOOKMARKED", isBookmarked)
        }
        setResult(RESULT_OK, resultIntent)
    }
}