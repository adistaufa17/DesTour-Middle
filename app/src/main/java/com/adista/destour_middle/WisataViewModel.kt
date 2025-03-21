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

    fun getWisata(token: String) {
        RetrofitClient.instance.getListWisata(token).enqueue(object : Callback<WisataResponse> {
            override fun onResponse(call: Call<WisataResponse>, response: Response<WisataResponse>) {
                if (response.isSuccessful) {
                    allWisataList = response.body()?.data?.wisataList ?: emptyList()
                    _wisataResponse.value = allWisataList
                } else {
                    Timber.e("Error fetching wisata data: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WisataResponse>, t: Throwable) {
                Timber.e("Failure: ${t.message}")
            }
        })
    }
    fun searchWisataOffline(query: String) {
        val filteredList = allWisataList.filter { it.title.contains(query, ignoreCase = true) }
        _wisataResponse.value = filteredList
    }

    // ✅ Fungsi untuk menambah atau menghapus bookmark berdasarkan statusnya
    fun toggleBookmark(token: String, idWisata: Int, isBookmarked: Boolean) {
        if (isBookmarked) {
            // 🔴 Jika sudah di-bookmark, maka hapus dari server
            RetrofitClient.instance.removeBookmark(token, idWisata).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        _bookmarkResponse.value = response.body()
                        Timber.d("Bookmark removed: ${response.body()}")
                    } else {
                        Timber.e("Error removing bookmark: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Timber.e("Failure: ${t.message}")
                }
            })
        } else {
            // ✅ Jika belum di-bookmark, maka tambahkan ke server
            RetrofitClient.instance.addBookmark(token, idWisata).enqueue(object : Callback<ApiResponse> {
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
}

