package com.adista.destour_middle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _registerResponse = MutableLiveData<AuthResponse?>()
    val registerResponse: LiveData<AuthResponse?> = _registerResponse

    fun registerUser(nama: String, email: String, nomorHp: String, password: String, confirmPassword: String) {
        val request = RegisterRequest(
            nama_lengkap = nama,
            email = email,
            nomor_hp = nomorHp,
            password = password,
            confirm_password = confirmPassword
        )

        RetrofitClient.instance.register(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    _registerResponse.value = response.body()
                    Log.d("Register", "Success: ${response.body()}")
                } else {
                    Log.e("Register", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Log.e("Register", "Failure: ${t.message}")
            }
        })
    }
}


