package com.adista.destour_middle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel() {
    private val _profileResponse = MutableLiveData<ProfileResponse>()
    val profileResponse: LiveData<ProfileResponse> = _profileResponse

    fun getProfile(token: String) {
        RetrofitClient.instance.getProfile(token = token).enqueue(object :
            Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    _profileResponse.value = response.body()
                    Log.d("Profile", "Success: ${response.body()}")
                } else {
                    Log.e("Profile", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.e("Profile", "Failure: ${t.message}")
                _profileResponse.value = ProfileResponse(
                    status = "failed", code = 500, message = "Gagal memuat data.", data = null
                )
            }
        })
    }
}