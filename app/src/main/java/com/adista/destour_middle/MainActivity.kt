package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.adista.destour_middle.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WisataViewModel by viewModels()
    private lateinit var adapter: WisataAdapter
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("user_token", null)

        if (token != null) {
            viewModel.getWisata(token!!)
        } else {
            Toast.makeText(this, "Token tidak ditemukan, silakan login", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        adapter = WisataAdapter(emptyList(), this) { wisataItem ->
            toggleBookmark(wisataItem.id)
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
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        viewModel.bookmarkResponse.observe(this) { response ->
            if (response?.status == "success") {
                Toast.makeText(this, "Bookmark diperbarui!", Toast.LENGTH_SHORT).show()
                viewModel.getWisata(token!!)
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
