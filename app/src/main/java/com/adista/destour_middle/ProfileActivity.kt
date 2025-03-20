package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.adista.destour_middle.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("user_token", null)

        if(token != null) {
            // Ambil data profil menggunakan token
            viewModel.getProfile(token)
        } else {
            Toast.makeText(this, "Token tidak ditemukan, silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Observasi perubahan data profil
        viewModel.profileResponse.observe(this) { response ->
            if (response?.status == "success") {
                // Memastikan bahwa data profil tidak null
                response.data?.let { profile ->
                    // Menampilkan data profil di UI
                    binding.textViewNama.text = profile.namaLengkap
                    binding.textViewEmail.text = profile.email
                    binding.textViewNomorHp.text = profile.nomorHp
                } ?: run {
                    // Jika data profil null, tampilkan pesan error
                    Toast.makeText(this, "Data profil tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            } else if (response?.code == 401) {
                // Token tidak valid, arahkan pengguna untuk login lagi
                Toast.makeText(this, "Token tidak valid. Silakan login lagi.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java)) // Arahkan pengguna ke login
                finish() // Selesai aktivitas ini
            } else {
                Toast.makeText(this, "Gagal memuat profil: ${response?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
