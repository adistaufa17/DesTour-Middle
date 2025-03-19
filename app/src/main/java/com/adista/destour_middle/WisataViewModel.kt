package com.adista.destour_middle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WisataViewModel : ViewModel() {
    private val _wisataResponse = MutableLiveData<List<WisataItem>>()
    val wisataResponse: LiveData<List<WisataItem>> = _wisataResponse

    private var allWisataList: List<WisataItem> = emptyList() // ✅ Simpan semua data wisata

    fun getWisata(token: String) {
        RetrofitClient.instance.getListWisata(token = token).enqueue(object : Callback<WisataResponse> {
            override fun onResponse(call: Call<WisataResponse>, response: Response<WisataResponse>) {
                if (response.isSuccessful) {
                    allWisataList = response.body()?.data?.wisataList ?: emptyList() // ✅ Simpan semua data wisata
                    _wisataResponse.value = allWisataList
                } else {
                    Log.e("Wisata", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WisataResponse>, t: Throwable) {
                Log.e("Wisata", "Failure: ${t.message}")
            }
        })
    }

    // 🔹 Lakukan pencarian di dalam Android jika backend tidak mendukung substring search
    fun searchWisataOffline(query: String) {
        val filteredList = allWisataList.filter { it.title.contains(query, ignoreCase = true) }
        _wisataResponse.value = filteredList
    }
}
