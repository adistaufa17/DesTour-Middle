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
        Timber.d("Getting wisata list with token: $token")
        RetrofitClient.instance.getListWisata(token = token).enqueue(object : Callback<WisataResponse> {
            override fun onResponse(call: Call<WisataResponse>, response: Response<WisataResponse>) {
                if (response.isSuccessful) {
                    allWisataList = response.body()?.data?.wisataList ?: emptyList()
                    _wisataResponse.value = allWisataList
                    Timber.d("Wisata data retrieved: ${allWisataList.size} items")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Timber.e("Error fetching wisata data: code=${response.code()}, body=$errorBody")
                }
            }

            override fun onFailure(call: Call<WisataResponse>, t: Throwable) {
                Timber.e(t, "Failure getting wisata data: ${t.message}")
            }
        })
    }

    // Fungsi pencarian wisata lokal berdasarkan query
    fun searchWisataOffline(query: String) {
        val filteredList = allWisataList.filter { it.title.contains(query, ignoreCase = true) }
        _wisataResponse.value = filteredList
    }

    // Fungsi untuk menambahkan wisata ke bookmarks (POST method)
    fun addBookmark(token: String, idWisata: Int) {
        Timber.d("Adding bookmark with token: $token, idWisata: $idWisata")
        RetrofitClient.instance.addBookmark(
            endpoint = "addBookmarks",
            token = token,
            idWisata = idWisata
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    _bookmarkResponse.value = response.body()
                    Timber.d("Bookmark added successfully: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Timber.e("Error adding bookmark: code=${response.code()}, body=$errorBody")
                    // Kirim response error ke UI
                    _bookmarkResponse.value = ApiResponse(
                        status = "failed",
                        code = response.code(),
                        message = "Gagal menambahkan bookmark: $errorBody"
                    )
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Timber.e(t, "Failure adding bookmark: ${t.message}")
                _bookmarkResponse.value = ApiResponse(
                    status = "failed",
                    code = -1,
                    message = "Gagal menambahkan bookmark: ${t.message}"
                )
            }
        })
    }

    // Fungsi untuk menghapus wisata dari bookmarks (POST method)
    fun removeBookmark(token: String, idWisata: Int) {
        Timber.d("Removing bookmark with token: $token, idWisata: $idWisata")
        RetrofitClient.instance.removeBookmark(
            endpoint = "removeBookmarks",
            token = token,
            idWisata = idWisata
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    _bookmarkResponse.value = response.body()
                    Timber.d("Bookmark removed successfully: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Timber.e("Error removing bookmark: code=${response.code()}, body=$errorBody")
                    // Kirim response error ke UI
                    _bookmarkResponse.value = ApiResponse(
                        status = "failed",
                        code = response.code(),
                        message = "Gagal menghapus bookmark: $errorBody"
                    )
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Timber.e(t, "Failure removing bookmark: ${t.message}")
                _bookmarkResponse.value = ApiResponse(
                    status = "failed",
                    code = -1,
                    message = "Gagal menghapus bookmark: ${t.message}"
                )
            }
        })
    }

    // Fungsi untuk toggle bookmark (menambah atau menghapus)
    fun toggleBookmark(token: String, idWisata: Int, isCurrentlyBookmarked: Boolean) {
        if (isCurrentlyBookmarked) {
            removeBookmark(token, idWisata)
        } else {
            addBookmark(token, idWisata)
        }
    }
}