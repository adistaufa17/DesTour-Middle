package com.adista.destour_middle.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.adista.destour_middle.R  // Add this import
import com.adista.destour_middle.data.model.ProfileResponse
import com.adista.destour_middle.databinding.ActivityProfileBinding
import com.adista.destour_middle.ui.login.LoginActivity
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.base.activity.CoreActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : CoreActivity<ActivityProfileBinding, ProfileViewModel>(R.layout.activity_profile) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.lifecycleOwner = this

        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("IS_LOGGED_IN", false)
        val token = sharedPreferences.getString("user_token", null)

        if (isLoggedIn && !token.isNullOrEmpty()) {
            lifecycleScope.launch {
                viewModel.getProfile(token)
            }
        } else {
            Toast.makeText(this, "Token tidak ditemukan, silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.buttonLogout.setOnClickListener {
            logoutUser()
        }

        lifecycleScope.launch {
            viewModel.apiResponse.collect { response ->
                when(response.status) {
                    ApiStatus.LOADING -> loadingDialog.show()
                    ApiStatus.SUCCESS -> {
                        loadingDialog.dismiss()

                        val profileResponse = response.dataAs<ProfileResponse>()
                        profileResponse?.data?.let { profile ->
                            binding.tvNama.text = profile.namaLengkap
                            binding.tvEmail.text = profile.email
                            binding.tvNomorHp.text = profile.nomorHp
                        } ?: run {
                            Toast.makeText(this@ProfileActivity, "Data profil tidak tersedia", Toast.LENGTH_SHORT).show()
                        }
                    }
                    ApiStatus.ERROR -> {
                        loadingDialog.dismiss()
                        Toast.makeText(this@ProfileActivity, response.message, Toast.LENGTH_SHORT).show()

                        if (response.rawResponse?.contains("401") == true) {
                            Toast.makeText(this@ProfileActivity, "Token tidak valid. Silakan login lagi.", Toast.LENGTH_SHORT).show()
                            logoutUser()
                        }
                    }
                    else -> {
                        loadingDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Hapus semua data login
        editor.apply()

        Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show()

        // Arahkan pengguna ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}