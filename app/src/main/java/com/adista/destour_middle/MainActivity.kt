package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.adista.destour_middle.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WisataViewModel by viewModels()
    private lateinit var adapter: WisataAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
        token = sharedPreferences.getString("user_token", null)

        if (!isLoggedIn || token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        adapter = WisataAdapter(emptyList(), this) { wisataItem ->
            val isBookmarked = sharedPreferences.getBoolean("BOOKMARK_${wisataItem.id}", false)
            sharedPreferences.edit().putBoolean("BOOKMARK_${wisataItem.id}", !isBookmarked).apply()
            adapter.setFilter(currentFilter, sharedPreferences)
        }

        binding.recyclerViewWisata.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewWisata.adapter = adapter

        binding.btnOpenFilter.setOnClickListener {
            val bottomSheet = BottomSheetFilterWisata { selectedFilter ->
                Timber.d("Filter dipilih: $selectedFilter")
                currentFilter = selectedFilter
                adapter.setFilter(selectedFilter, sharedPreferences)
            }
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
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

        viewModel.wisataResponse.observe(this) { wisataList ->
            wisataList?.let {
                Timber.d("Total data diterima: ${it.size}")
                adapter.updateData(it)
                adapter.setFilter(currentFilter, sharedPreferences)
            }
        }

        viewModel.bookmarkResponse.observe(this) { response ->
            if (response?.status == "success") {
                Toast.makeText(this, "Bookmark diperbarui!", Toast.LENGTH_SHORT).show()
                token?.let { viewModel.getWisata(it) }
            } else {
                val msg = if (response?.code == 409) "Wisata sudah dibookmark." else "Gagal memperbarui bookmark"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.getWisata(token!!)
    }

    private var currentFilter: String = "Semua"

    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val wisataId = result.data?.getIntExtra("WISATA_ID", -1) ?: -1
                val isBookmarked = result.data?.getBooleanExtra("IS_BOOKMARKED", false) ?: false
                val isLiked = result.data?.getBooleanExtra("IS_LIKED", false) ?: false

                if (wisataId != -1) {
                    token?.let { safeToken ->
                        viewModel.toggleBookmark(safeToken, wisataId, isBookmarked)
                        viewModel.toggleLike(safeToken, wisataId, isLiked) // ✅ LIKE
                    }

                    adapter.updateBookmarkStatus(wisataId, isBookmarked)
                    adapter.setFilter(currentFilter, sharedPreferences)
                }
            }
        }


}
