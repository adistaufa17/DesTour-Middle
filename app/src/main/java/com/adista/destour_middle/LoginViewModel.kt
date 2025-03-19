package com.adista.destour_middle

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.internal.Contexts.getApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {
    private val _loginResponse = MutableLiveData<AuthResponse>()
    val loginResponse: LiveData<AuthResponse> = _loginResponse

    fun loginUser(email: String, password: String) {
        val request = LoginRequest(
            email = email,
            password = password
        )

        RetrofitClient.instance.login(request).enqueue(object : Callback<AuthResponse> {
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