package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.adista.destour_middle.databinding.ActivityProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
        val token = sharedPreferences.getString("user_token", null)

        if (isLoggedIn && !token.isNullOrEmpty()) {
            viewModel.getProfile(token)
        } else {
            Toast.makeText(this, "Token tidak ditemukan, silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        viewModel.profileResponse.observe(this) { response ->
            if (response?.status == "success") {
                response.data?.let { profile ->
                    binding.textViewNama.text = profile.namaLengkap
                    binding.textViewEmail.text = profile.email
                    binding.textViewNomorHp.text = profile.nomorHp
                } ?: run {
                    Toast.makeText(this, "Data profil tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            } else if (response?.code == 401) {
                Toast.makeText(this, "Token tidak valid. Silakan login lagi.", Toast.LENGTH_SHORT).show()
                logoutUser()
            } else {
                Toast.makeText(this, "Gagal memuat profil: ${response?.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Tambahkan aksi untuk tombol logout
        binding.buttonLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Hapus semua data login
        editor.commit() // Gunakan commit() untuk memastikan data dihapus segera

        Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show()

        // Arahkan pengguna ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Hapus semua aktivitas sebelumnya
        startActivity(intent)
        finish() // Tutup ProfileActivity
    }
}