package com.adista.destour_middle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.adista.destour_middle.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // **Cek apakah pengguna sudah login sebelumnya**
        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)

        if (isLoggedIn) {
            // Jika sudah login, langsung masuk ke MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // **Ketika tombol login ditekan**
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.loginUser(email, password)
            } else {
                Toast.makeText(this, "Harap isi semua data!", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loginResponse.observe(this) { response ->
            response?.let {
                if (it.status == "success") {
                    Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()

                    // **Simpan status login ke SharedPreferences**
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("IS_LOGGED_IN", true) // ✅ Menandai bahwa pengguna sudah login
                    editor.putString("user_token", it.data?.token) // ✅ Menyimpan token
                    editor.apply()

                    // Pindah ke MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
