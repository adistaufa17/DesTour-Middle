package com.adista.destour_middle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.adista.destour_middle.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WisataViewModel by viewModels()
    private lateinit var adapter: WisataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = "c5d98b11-546f-4f03-b92a-aa4a1deb5c89"

        adapter = WisataAdapter(emptyList())
        binding.recyclerViewWisata.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewWisata.adapter = adapter

        viewModel.wisataResponse.observe(this) { wisataList ->
            Log.d("SearchWisata", "Memperbarui RecyclerView dengan data: $wisataList") // 🔍 Debug log
            wisataList?.let {
                adapter.updateData(it)
            }
        }

        viewModel.getWisata(token)

        // Menangani pencarian
        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString().trim()

            if (query.isNotEmpty()) {
                Log.d("SearchWisata", "Melakukan pencarian lokal untuk: $query")
                viewModel.searchWisataOffline(query)
            } else {
                Toast.makeText(this, "Masukkan kata kunci pencarian!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnProfile.setOnClickListener{
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }
}
