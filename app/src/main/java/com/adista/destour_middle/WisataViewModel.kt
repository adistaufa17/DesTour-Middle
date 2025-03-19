package com.adista.destour_middle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WisataViewModel : ViewModel() {
    private val _wisataResponse = MutableLiveData<WisataResponse>()
    val wisataResponse: LiveData<WisataResponse> = _wisataResponse

    fun getWisata(token: String) {
        RetrofitClient.instance.getListWisata(token = token).enqueue(object : Callback<WisataResponse> {
            override fun onResponse(call: Call<WisataResponse>, response: Response<WisataResponse>) {
                if (response.isSuccessful) {
                    _wisataResponse.value = response.body()  // Menyimpan data di LiveData
                } else {
                    Log.e("Wisata", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WisataResponse>, t: Throwable) {
                Log.e("Wisata", "Failure: ${t.message}")
            }
        })
    }
}
