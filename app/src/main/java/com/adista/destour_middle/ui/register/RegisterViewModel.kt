package com.adista.destour_middle.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adista.destour_middle.data.model.AuthResponse
import com.adista.destour_middle.core.network.ApiService
import com.adista.destour_middle.data.request.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
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

        apiService.register(request).enqueue(object : Callback<AuthResponse> {
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

