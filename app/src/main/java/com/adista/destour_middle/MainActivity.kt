package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.adista.destour_middle.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WisataViewModel by viewModels()
    private lateinit var adapter: WisataAdapter
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Splash Screen selama 2 detik sebelum cek login
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000) // 2000ms = 2 detik
    }

    private fun checkLoginStatus() {
        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
        token = sharedPreferences.getString("user_token", null)

        if (!isLoggedIn || token.isNullOrEmpty()) {
            // Jika belum login, arahkan ke LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Tutup MainActivity agar tidak bisa kembali ke sini
        } else {
            // Jika sudah login, lanjutkan ke Home
            loadHomeScreen()
        }
    }

    private fun loadHomeScreen() {
        // Inisialisasi adapter terlebih dahulu
        adapter = WisataAdapter(
            emptyList(),
            this,
            onBookmarkClick = { wisataItem, isBookmarked ->
                token?.let {
                    viewModel.toggleBookmark(it, wisataItem.id, isBookmarked)
                }
            },
            onLikeClick = { wisataItem, isLiked ->
                token?.let {
                    viewModel.toggleLike(it, wisataItem.id, isLiked)
                }
            }
        )

        binding.recyclerViewWisata.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewWisata.adapter = adapter

        // Buat setelah adapter diinisialisasi
        token?.let { viewModel.getWisata(it) }

        // Observe wisataResponse
        viewModel.wisataResponse.observe(this) { wisataList ->
            wisataList?.let {
                adapter.updateData(it)
            }
        }

        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchWisataOffline(query)
            } else {
                Toast.makeText(this, "Masukkan kata kunci pencarian!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Observe bookmark response
        viewModel.bookmarkResponse.observe(this) { response ->
            if (response?.status == "success") {
                Toast.makeText(this, "Bookmark diperbarui!", Toast.LENGTH_SHORT).show()
            } else if (response != null) {
                Toast.makeText(this, "Gagal memperbarui bookmark: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe like response
        viewModel.likeResponse.observe(this) { response ->
            if (response?.status == "success") {
                Toast.makeText(this, "Like diperbarui!", Toast.LENGTH_SHORT).show()
            } else if (response != null) {
                Toast.makeText(this, "Gagal memperbarui like: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val wisataId = data?.getIntExtra("WISATA_ID", -1) ?: -1

                if (wisataId != -1) {
                    // Status bookmark
                    if (data?.hasExtra("IS_BOOKMARKED") == true) {
                        val isBookmarked = data.getBooleanExtra("IS_BOOKMARKED", false)

                        // Perbarui status di SharedPreferences
                        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putBoolean("BOOKMARK_$wisataId", isBookmarked).apply()

                        // Langsung panggil token untuk memperbarui API jika perlu
                        if (isBookmarked) {
                            token?.let { viewModel.addBookmark(it, wisataId) }
                        } else {
                            token?.let { viewModel.removeBookmark(it, wisataId) }
                        }
                    }

                    // Status like
                    if (data?.hasExtra("IS_LIKED") == true) {
                        val isLiked = data.getBooleanExtra("IS_LIKED", false)

                        // Perbarui status di SharedPreferences
                        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putBoolean("LIKE_$wisataId", isLiked).apply()

                        // Langsung panggil token untuk memperbarui API jika perlu
                        if (isLiked) {
                            token?.let { viewModel.likeWisata(it, wisataId) }
                        } else {
                            token?.let { viewModel.unlikeWisata(it, wisataId) }
                        }
                    }
                }
            }
        }

    // Method untuk memastikan data selalu segar saat aktivitas dimulai ulang
    override fun onResume() {
        super.onResume()
        token?.let { viewModel.getWisata(it) }
    }
}