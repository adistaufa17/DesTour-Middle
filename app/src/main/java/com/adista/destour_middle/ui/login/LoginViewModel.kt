package com.adista.destour_middle.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adista.destour_middle.data.model.AuthResponse
import com.adista.destour_middle.core.network.ApiService
import com.adista.destour_middle.data.request.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _loginResponse = MutableLiveData<AuthResponse>()
    val loginResponse: LiveData<AuthResponse> = _loginResponse

    fun loginUser(email: String, password: String) {
        val request = LoginRequest(
            email = email,
            password = password
        )

        apiService.login(request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    _loginResponse.value = response.body()
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