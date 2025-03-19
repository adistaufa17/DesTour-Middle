package com.adista.destour_middle

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adista.destour_middle.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WisataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        val token = "c5d98b11-546f-4f03-b92a-aa4a1deb5c89"

        val recyclerView: RecyclerView = binding.recyclerViewWisata
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.wisataResponse.observe(this) { response ->
            response?.let {
                if (it.status == "success") {
                    val adapter = WisataAdapter(it.data)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this, "Gagal mengambil data wisata: ${it.status}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.getWisata(token)



    }

}