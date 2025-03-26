package com.adista.destour_middle.ui.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.adista.destour_middle.MainActivity
import com.adista.destour_middle.R
import com.adista.destour_middle.data.model.AuthResponse
import com.adista.destour_middle.databinding.ActivityRegisterBinding
import com.adista.destour_middle.ui.login.LoginActivity
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.base.activity.CoreActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : CoreActivity<ActivityRegisterBinding, RegisterViewModel>(R.layout.activity_register) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                viewModel.onRegisterClick()
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        lifecycleScope.launch {
            viewModel.apiResponse.collect { response ->
                when(response.status) {
                    ApiStatus.LOADING -> loadingDialog.show()
                    ApiStatus.SUCCESS -> {
                        loadingDialog.dismiss()

                        val authResponse = response.dataAs<AuthResponse>()
                        authResponse?.data?.token?.let { token ->
                            getSharedPreferences("user_pref", Context.MODE_PRIVATE).edit().apply {
                                putBoolean("IS_LOGGED_IN", true)
                                putString("user_token", token)
                                apply()
                            }
                        }

                        Toast.makeText(this@RegisterActivity, response.message, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    }
                    ApiStatus.ERROR -> {
                        loadingDialog.dismiss()
                        Toast.makeText(this@RegisterActivity, response.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        loadingDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val nama = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val nomorHp = binding.etNomorHp.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (nama.isEmpty() || email.isEmpty() || nomorHp.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Harap isi semua data!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Password dan Konfirmasi Password tidak cocok!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}