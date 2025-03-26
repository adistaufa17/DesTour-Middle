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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.adista.destour_middle.databinding.ActivityMainBinding
import com.adista.destour_middle.ui.login.LoginActivity
import com.adista.destour_middle.ui.profile.ProfileActivity
import com.adista.destour_middle.ui.wisata.BottomSheetFilterWisata
import com.adista.destour_middle.ui.wisata.WisataAdapter
import com.adista.destour_middle.ui.wisata.WisataViewModel
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.base.activity.CoreActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : CoreActivity<ActivityMainBinding, WisataViewModel>(R.layout.activity_main) {

    private lateinit var adapter: WisataAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var token: String? = null
    private var currentFilter: String = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        binding.rvWisata.setHasFixedSize(true)

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

        viewModel.wisataResponse.observe(this) { wisataList ->
            Timber.d("wisataResponse observer triggered")
            if (wisataList != null) {
                Timber.d("Received ${wisataList.size} items in MainActivity")
                adapter.updateData(wisataList)
                adapter.setFilter(currentFilter, sharedPreferences)
            } else {
                Timber.e("Received null wisataList in MainActivity")
            }
        }

        // Observe API responses
        lifecycleScope.launch {
            viewModel.apiResponse.collect { response ->
                when(response.status) {
                    ApiStatus.LOADING -> loadingDialog.show()
                    ApiStatus.SUCCESS -> {
                        loadingDialog.dismiss()
                        response.message?.let {
                            Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                    ApiStatus.ERROR -> {
                        loadingDialog.dismiss()
                        response.message?.let {
                            Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        loadingDialog.dismiss()
                    }
                }
            }
        }

        // Ambil data wisata pertama kali
        token?.let {
            lifecycleScope.launch {
                viewModel.getWisata(it)
            }
        }
    }

    private fun setupSearchFunctionality() {
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }

        binding.btnClose.setOnClickListener {
            resetSearch()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnClose.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)

        if (query.isNotEmpty()) {
            viewModel.searchWisataOffline(query)
        } else {
            Toast.makeText(this, "Masukkan kata kunci pencarian!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetSearch() {
        binding.etSearch.text.clear()

        token?.let {
            lifecycleScope.launch {
                viewModel.getWisata(it)
            }
        }

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val wisataId = result.data?.getIntExtra("WISATA_ID", -1) ?: -1
                val isBookmarked = result.data?.getBooleanExtra("IS_BOOKMARKED", false) ?: false
                val isLiked = result.data?.getBooleanExtra("IS_LIKED", false) ?: false

                if (wisataId != -1) {
                    token?.let { safeToken ->
                        lifecycleScope.launch {
                            viewModel.toggleBookmark(safeToken, wisataId, isBookmarked)
                            viewModel.toggleLike(safeToken, wisataId, isLiked)
                        }
                    }

                    adapter.updateBookmarkStatus(wisataId, isBookmarked)
                    adapter.setFilter(currentFilter, sharedPreferences)
                }
            }
        }
}