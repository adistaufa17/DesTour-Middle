package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.adista.destour_middle.databinding.ActivityMainBinding
import com.adista.destour_middle.ui.login.LoginActivity
import com.adista.destour_middle.ui.profile.ProfileActivity
import com.adista.destour_middle.ui.wisata.BottomSheetFilterWisata
import com.adista.destour_middle.ui.wisata.WisataAdapter
import com.adista.destour_middle.ui.wisata.WisataViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WisataViewModel by viewModels()
    private lateinit var adapter: WisataAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var token: String? = null
    private var currentFilter: String = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
        token = sharedPreferences.getString("user_token", null)

        // Cek status login
        if (!isLoggedIn || token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Inisialisasi Adapter
        adapter = WisataAdapter(emptyList(), this) { wisataItem ->
            val isBookmarked = sharedPreferences.getBoolean("BOOKMARK_${wisataItem.id}", false)
            sharedPreferences.edit().putBoolean("BOOKMARK_${wisataItem.id}", !isBookmarked).apply()
            adapter.setFilter(currentFilter, sharedPreferences)
        }

        // Setup RecyclerView
        binding.rvWisata.layoutManager = LinearLayoutManager(this)
        binding.rvWisata.adapter = adapter

        // Setup Search Functionality
        setupSearchFunctionality()

        // Setup Filter Button
        binding.btnFilter.setOnClickListener {
            val bottomSheet = BottomSheetFilterWisata { selectedFilter ->
                Timber.d("Filter dipilih: $selectedFilter")
                currentFilter = selectedFilter
                adapter.setFilter(selectedFilter, sharedPreferences)
            }
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        // Setup Profile Button
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Observer untuk response wisata
        viewModel.wisataResponse.observe(this) { wisataList ->
            wisataList?.let {
                Timber.d("Total data diterima: ${it.size}")
                adapter.updateData(it)
                adapter.setFilter(currentFilter, sharedPreferences)
            }
        }

        // Observer untuk bookmark response
        viewModel.bookmarkResponse.observe(this) { response ->
            if (response?.status == "success") {
                Toast.makeText(this, "Bookmark diperbarui!", Toast.LENGTH_SHORT).show()
                token?.let { viewModel.getWisata(it) }
            } else {
                val msg = if (response?.code == 409) "Wisata sudah dibookmark." else "Gagal memperbarui bookmark"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        // Ambil data wisata pertama kali
        viewModel.getWisata(token!!)
    }

    private fun setupSearchFunctionality() {
        // Listener untuk tombol search
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        // Listener untuk enter di keyboard
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }

        // Listener untuk tombol close/clear
        binding.btnClose.setOnClickListener {
            resetSearch()
        }

        // Tambahkan listener untuk perubahan teks
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Sembunyikan tombol close jika search kosong
                binding.btnClose.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()

        // Sembunyikan keyboard
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)

        if (query.isNotEmpty()) {
            // Lakukan pencarian
            viewModel.searchWisataOffline(query)
        } else {
            // Tampilkan pesan jika query kosong
            Toast.makeText(this, "Masukkan kata kunci pencarian!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetSearch() {
        // Bersihkan text pencarian
        binding.etSearch.text.clear()

        // Kembalikan data ke kondisi awal
        token?.let {
            viewModel.getWisata(it)
        }

        // Sembunyikan keyboard
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    // Result Launcher untuk handling result dari aktivitas lain
    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val wisataId = result.data?.getIntExtra("WISATA_ID", -1) ?: -1
                val isBookmarked = result.data?.getBooleanExtra("IS_BOOKMARKED", false) ?: false
                val isLiked = result.data?.getBooleanExtra("IS_LIKED", false) ?: false

                if (wisataId != -1) {
                    token?.let { safeToken ->
                        viewModel.toggleBookmark(safeToken, wisataId, isBookmarked)
                        viewModel.toggleLike(safeToken, wisataId, isLiked)
                    }

                    adapter.updateBookmarkStatus(wisataId, isBookmarked)
                    adapter.setFilter(currentFilter, sharedPreferences)
                }
            }
        }
}