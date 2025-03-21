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
    private var isLiked: Boolean = false
    private var token: String? = null
    private var resultChanged = false // Flag untuk melacak apakah ada perubahan

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

        // Ambil status dari SharedPreferences
        isBookmarked = sharedPreferences.getBoolean("BOOKMARK_$wisataId", false)
        isLiked = sharedPreferences.getBoolean("LIKE_$wisataId", false)

        // Perbarui ikon sesuai status
        updateBookmarkIcon()
        updateLikeIcon()


        binding.detailBookmark.setOnClickListener {
            toggleBookmark()
        }

        binding.detailLike.setOnClickListener {
            toggleLike()
        }

        // Observe bookmark response
        viewModel.bookmarkResponse.observe(this) { response ->
            if (response?.status == "success") {
                Toast.makeText(this, if (isBookmarked) "Bookmark ditambahkan" else "Bookmark dihapus", Toast.LENGTH_SHORT).show()
                Timber.d("Bookmark updated via API: ${response.message}")
            } else if (response != null) {
                Toast.makeText(this, "Gagal memperbarui bookmark: ${response.message}", Toast.LENGTH_SHORT).show()
                Timber.e("Failed to update bookmark: ${response.message}")

                // Kembalikan status jika gagal
                isBookmarked = !isBookmarked
                updateBookmarkIcon()

                // Perbarui SharedPreferences
                sharedPreferences.edit().putBoolean("BOOKMARK_$wisataId", isBookmarked).apply()
            }
        }

        viewModel.likeResponse.observe(this) { response ->
            if (response?.status == "success") {
                Toast.makeText(this, if (isLiked) "Wisata disukai" else "Batal menyukai wisata", Toast.LENGTH_SHORT).show()
                Timber.d("Like updated via API: ${response.message}")
            } else if (response != null) {
                Toast.makeText(this, "Gagal memperbarui like: ${response.message}", Toast.LENGTH_SHORT).show()
                Timber.e("Failed to update like: ${response.message}")

                // Kembalikan status jika gagal
                isLiked = !isLiked
                updateLikeIcon()

                // Perbarui SharedPreferences
                sharedPreferences.edit().putBoolean("LIKE_$wisataId", isLiked).apply()
            }
        }
    }

    private fun updateBookmarkIcon() {
        binding.detailBookmark.setImageResource(if (isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_bookmark)
    }

    private fun updateLikeIcon() {
        binding.detailLike.setImageResource(if (isLiked) R.drawable.ic_liked else R.drawable.ic_like)
    }

    private fun toggleBookmark() {
        isBookmarked = !isBookmarked
        resultChanged = true

        // Perbarui status di SharedPreferences
        sharedPreferences.edit().putBoolean("BOOKMARK_$wisataId", isBookmarked).apply()

        // Perbarui ikon
        updateBookmarkIcon()

        // Panggil API
        token?.let { safeToken ->
            if (isBookmarked) {
                viewModel.addBookmark(safeToken, wisataId)
            } else {
                viewModel.removeBookmark(safeToken, wisataId)
            }
        }
    }

    private fun toggleLike() {
        isLiked = !isLiked
        resultChanged = true

        // Perbarui status di SharedPreferences
        sharedPreferences.edit().putBoolean("LIKE_$wisataId", isLiked).apply()

        // Perbarui ikon
        updateLikeIcon()

        // Panggil API
        token?.let { safeToken ->
            if (isLiked) {
                viewModel.likeWisata(safeToken, wisataId)
            } else {
                viewModel.unlikeWisata(safeToken, wisataId)
            }
        }
    }

}