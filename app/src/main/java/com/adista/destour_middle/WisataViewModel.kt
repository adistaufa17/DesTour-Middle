package com.adista.destour_middle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class WisataViewModel : ViewModel() {
    private val _wisataResponse = MutableLiveData<List<WisataItem>>()
    val wisataResponse: LiveData<List<WisataItem>> = _wisataResponse

    private val _bookmarkResponse = MutableLiveData<ApiResponse>()
    val bookmarkResponse: LiveData<ApiResponse> = _bookmarkResponse

    private var allWisataList: List<WisataItem> = emptyList()

    // Fungsi untuk mendapatkan wisata
    fun getWisata(token: String) {
        RetrofitClient.instance.getListWisata(token = token).enqueue(object : Callback<WisataResponse> {
            override fun onResponse(call: Call<WisataResponse>, response: Response<WisataResponse>) {
                if (response.isSuccessful) {
                    allWisataList = response.body()?.data?.wisataList ?: emptyList()
                    _wisataResponse.value = allWisataList
                    Timber.d("Wisata data retrieved: $allWisataList")
                } else {
                    Timber.e("Error fetching wisata data: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WisataResponse>, t: Throwable) {
                Timber.e("Failure: ${t.message}")
            }
        })
    }

    // Fungsi pencarian wisata lokal berdasarkan query
    fun searchWisataOffline(query: String) {
        val filteredList = allWisataList.filter { it.title.contains(query, ignoreCase = true) }
        _wisataResponse.value = filteredList
    }

    // Fungsi untuk menambahkan wisata ke bookmarks
    fun addBookmark(token: String, idWisata: Int) {
        RetrofitClient.instance.addBookmark(token = token, idWisata = idWisata).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    _bookmarkResponse.value = response.body()
                    Timber.d("Bookmark added: ${response.body()}")
                } else {
                    Timber.e("Error adding bookmark: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Timber.e("Failure: ${t.message}")
            }
        })
    }
}
