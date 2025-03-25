package com.adista.destour_middle.ui.wisata

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.adista.destour_middle.R
import com.adista.destour_middle.databinding.ActivityDetailWisataBinding
import com.bumptech.glide.Glide
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.base.activity.CoreActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class DetailWisataActivity : CoreActivity<ActivityDetailWisataBinding, WisataViewModel>(R.layout.activity_detail_wisata) {

    private var wisataId: Int = 0
    private var isBookmarked: Boolean = false
    private var isLiked: Boolean = false
    private var token: String? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("user_token", null)

        // Retrieve data from intent
        wisataId = intent.getIntExtra("WISATA_ID", 0)
        val title = intent.getStringExtra("WISATA_TITLE") ?: "Nama Wisata"
        val lokasi = intent.getStringExtra("WISATA_LOKASI") ?: "Lokasi Tidak Diketahui"
        val deskripsi = intent.getStringExtra("WISATA_DESKRIPSI") ?: "Deskripsi tidak tersedia"
        val imageUrl = intent.getStringExtra("WISATA_IMAGE") ?: ""

        // Set UI components
        binding.tvTitleWisata.text = title
        binding.tvLocation.text = lokasi
        binding.tvDescription.text = deskripsi

        Glide.with(this)
            .load(imageUrl)
            .into(binding.ivWisata)

        // Initialize bookmark and like status from SharedPreferences
        isBookmarked = sharedPreferences.getBoolean("BOOKMARK_$wisataId", false)
        updateBookmarkIcon()

        isLiked = sharedPreferences.getBoolean("LIKE_$wisataId", false)
        updateLikeIcon()

        // Setup click listeners
        binding.btnBookmark.setOnClickListener {
            toggleBookmark()
        }

        binding.btnLike.setOnClickListener {
            toggleLike()
        }

        binding.btnBack.setOnClickListener {
            finish() // Use finish instead of starting a new activity
        }

        // Observe API responses
        lifecycleScope.launch {
            viewModel.apiResponse.collect { response ->
                when (response.status) {
                    ApiStatus.LOADING -> {
                        loadingDialog.show()
                    }
                    ApiStatus.SUCCESS -> {
                        loadingDialog.dismiss()
                        val message = when (response.data) {
                            "bookmark_add" -> "Bookmark ditambahkan"
                            "bookmark_remove" -> "Bookmark dihapus"
                            "like_add" -> "Disukai"
                            "like_remove" -> "Batal suka"
                            else -> response.message ?: "Berhasil"
                        }
                        Toast.makeText(this@DetailWisataActivity, message, Toast.LENGTH_SHORT).show()
                    }
                    ApiStatus.ERROR -> {
                        loadingDialog.dismiss()
                        Toast.makeText(this@DetailWisataActivity, response.message ?: "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        loadingDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun updateLikeIcon() {
        binding.btnLike.setIconResource(if (isLiked) R.drawable.ic_liked else R.drawable.ic_like)
    }

    private fun updateBookmarkIcon() {
        binding.btnBookmark.setIconResource(if (isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_bookmark)
    }

    private fun toggleBookmark() {
        isBookmarked = !isBookmarked

        // Simpan status bookmark di SharedPreferences
        sharedPreferences.edit().apply {
            putBoolean("BOOKMARK_$wisataId", isBookmarked)
            apply()
        }

        // Perbarui icon bookmark
        updateBookmarkIcon()

        // Gunakan API untuk update bookmark
        token?.let { safeToken ->
            lifecycleScope.launch {
                if (isBookmarked) {
                    viewModel.addBookmark(safeToken, wisataId)
                } else {
                    viewModel.removeBookmark(safeToken, wisataId)
                }
            }
        }

        val resultIntent = Intent().apply {
            putExtra("WISATA_ID", wisataId)
            putExtra("IS_BOOKMARKED", isBookmarked)
            putExtra("IS_LIKED", isLiked)
        }
        setResult(RESULT_OK, resultIntent)
    }

    private fun toggleLike() {
        Timber.d("Toggle Like untuk ID Wisata: $wisataId, Status Sebelum: $isLiked")

        isLiked = !isLiked
        updateLikeIcon()

        sharedPreferences.edit().apply {
            putBoolean("LIKE_$wisataId", isLiked)
            apply()
        }

        token?.let { safeToken ->
            lifecycleScope.launch {
                if (isLiked) {
                    Timber.d("Mengirim LIKE untuk wisata ID: $wisataId")
                    viewModel.likeWisata(safeToken, wisataId)
                } else {
                    Timber.d("Mengirim UNLIKE untuk wisata ID: $wisataId")
                    viewModel.unlikeWisata(safeToken, wisataId)
                }
            }
        }
    }
}