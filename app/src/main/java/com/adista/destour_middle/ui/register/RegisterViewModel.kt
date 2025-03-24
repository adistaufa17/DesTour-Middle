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

    val nama = MutableLiveData("")
    val email = MutableLiveData("")
    val nomorHp = MutableLiveData("")
    val password = MutableLiveData("")
    val confirmPassword = MutableLiveData("")

    private val _registerResponse = MutableLiveData<AuthResponse?>()
    val registerResponse: LiveData<AuthResponse?> = _registerResponse

    fun registerUser() {
        val request = RegisterRequest(
            nama_lengkap = nama.value ?: "".trim(),
            email = email.value ?: "".trim(),
            nomor_hp = nomorHp.value ?: "".trim(),
            password = password.value ?: "".trim(),
            confirm_password = confirmPassword.value ?: "".trim()
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

