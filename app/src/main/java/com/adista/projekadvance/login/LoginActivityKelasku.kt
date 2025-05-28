package com.adista.projekadvance.login

import com.adista.projekadvance.MainActivityKelasku
import com.adista.projekadvance.register.RegisterActivityKelasku
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.adista.destour_middle.R
import com.adista.destour_middle.databinding.ActivityLoginKelaskuBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivityKelasku : AppCompatActivity() {

    private lateinit var binding: ActivityLoginKelaskuBinding
    private val viewModel: LoginViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_kelasku)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                // Simpan session login
                val prefs = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("IS_LOGGED_IN", true)
                    .putString("user_phone", viewModel.phone.value ?: "")
                    .apply()

                Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivityKelasku::class.java))
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivityKelasku::class.java))
        }
    }


}
