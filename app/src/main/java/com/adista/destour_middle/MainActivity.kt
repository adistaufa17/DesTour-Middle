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

        // **Splash Screen selama 2 detik sebelum cek login**
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000) // 2000ms = 2 detik
    }

    private fun checkLoginStatus() {
        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
        token = sharedPreferences.getString("user_token", null)

        if (!isLoggedIn || token.isNullOrEmpty()) {
            // **Jika belum login, arahkan ke LoginActivity**
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Tutup MainActivity agar tidak bisa kembali ke sini
        } else {
            // **Jika sudah login, lanjutkan ke Home**
            loadHomeScreen()
        }
    }

    private fun loadHomeScreen() {
        token?.let { viewModel.getWisata(it) }

        adapter = WisataAdapter(emptyList(), this) { wisataItem, isBookmarked ->
            token?.let {
                viewModel.toggleBookmark(it, wisataItem.id, isBookmarked)
            }
        }

        binding.recyclerViewWisata.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewWisata.adapter = adapter

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
            startActivity(Intent(this, ProfileActivity::class.java)) // **Pergi ke halaman profil (Logout di sana)**
        }

        viewModel.bookmarkResponse.observe(this) { response ->
            if (response?.status == "success") {
                Toast.makeText(this, "Bookmark diperbarui!", Toast.LENGTH_SHORT).show()
                token?.let { viewModel.getWisata(it) }
            } else {
                Toast.makeText(this, "Gagal memperbarui bookmark", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val wisataId = result.data?.getIntExtra("WISATA_ID", -1) ?: -1
                val isBookmarked = result.data?.getBooleanExtra("IS_BOOKMARKED", false) ?: false
                if (wisataId != -1) {
                    adapter.updateBookmarkStatus(wisataId, isBookmarked)
                }
            }
        }

    private fun toggleBookmark(wisataId: Int) {
        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val isBookmarked = sharedPreferences.getBoolean("BOOKMARK_$wisataId", false)
        val editor = sharedPreferences.edit()
        editor.putBoolean("BOOKMARK_$wisataId", !isBookmarked)
        editor.apply()
        adapter.updateBookmarkStatus(wisataId, !isBookmarked)
    }
}
