package com.adista.destour_middle.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.adista.destour_middle.MainActivity
import com.adista.destour_middle.databinding.ActivityRegisterBinding
import com.adista.destour_middle.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.buttonRegister.setOnClickListener {
            val nama = binding.editTextName.text.toString()
            val email = binding.editTextEmail.text.toString()
            val nomorHp = binding.editTextPhone.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            if (nama.isNotEmpty() && email.isNotEmpty() && nomorHp.isNotEmpty() &&
                password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    viewModel.registerUser(nama, email, nomorHp, password, confirmPassword)
                } else {
                    Toast.makeText(this, "Password dan Konfirmasi Password tidak cocok!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Harap isi semua data!", Toast.LENGTH_SHORT).show()
            }

        }


        viewModel.registerResponse.observe(this) { response ->
            response?.let {
                if (it.status == "success") {
                    Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()

                    // Setelah sukses daftar, langsung pindah ke MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Registrasi gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }
}