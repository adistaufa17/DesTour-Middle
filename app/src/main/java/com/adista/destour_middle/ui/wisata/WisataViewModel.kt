package com.adista.destour_middle.ui.wisata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adista.destour_middle.core.network.ApiResponse
import com.adista.destour_middle.data.model.WisataItem
import com.adista.destour_middle.data.model.WisataResponse
import com.adista.destour_middle.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WisataViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _wisataResponse = MutableLiveData<List<WisataItem>>()
    val wisataResponse: LiveData<List<WisataItem>> = _wisataResponse

    private val _bookmarkResponse = MutableLiveData<ApiResponse>()
    val bookmarkResponse: LiveData<ApiResponse> = _bookmarkResponse

    private val _likeResponse = MutableLiveData<ApiResponse>()
    val likeResponse: LiveData<ApiResponse> = _likeResponse

    private var allWisataList: List<WisataItem> = emptyList()

    fun getWisata(token: String) {
        Timber.d("Getting wisata list with token: $token")
        apiService.getListWisata(token = token).enqueue(object : Callback<WisataResponse> {
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

    fun searchWisataOffline(query: String) {
        val filteredList = allWisataList.filter { it.title.contains(query, ignoreCase = true) }
        _wisataResponse.value = filteredList
    }

    fun addBookmark(token: String, idWisata: Int) {
        apiService.addBookmark(endpoint = "addBookmarks", token = token, idWisata = idWisata)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    _bookmarkResponse.value = response.body()
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    _bookmarkResponse.value = ApiResponse("failed", -1, "Gagal menambahkan bookmark: ${t.message}")
                }
            })
    }

    fun removeBookmark(token: String, idWisata: Int) {
        apiService.removeBookmark(endpoint = "removeBookmarks", token = token, idWisata = idWisata)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    _bookmarkResponse.value = response.body()
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    _bookmarkResponse.value = ApiResponse("failed", -1, "Gagal menghapus bookmark: ${t.message}")
                }
            })
    }

    fun toggleBookmark(token: String, idWisata: Int, isCurrentlyBookmarked: Boolean) {
        if (isCurrentlyBookmarked) {
            removeBookmark(token, idWisata)
        } else {
            addBookmark(token, idWisata)
        }
    }

    fun likeWisata(token: String, idWisata: Int) {
        Timber.d("Mengirim permintaan LIKE untuk wisata: $idWisata")
        apiService.likeWisata(token = token, idWisata = idWisata)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        Timber.d("LIKE BERHASIL: ${response.body()?.message}")
                        _likeResponse.value = response.body()
                    } else if (response.code() == 409) { // Sudah disukai sebelumnya
                        Timber.e("LIKE GAGAL: Wisata sudah disukai sebelumnya.")
                        _likeResponse.value = ApiResponse("failed", 409, "Wisata sudah disukai sebelumnya.")
                    } else {
                        Timber.e("LIKE GAGAL: ${response.errorBody()?.string()}")
                        _likeResponse.value = ApiResponse("failed", response.code(), "Gagal melakukan like.")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Timber.e("LIKE ERROR: ${t.message}")
                    _likeResponse.value = ApiResponse("failed", -1, "Gagal melakukan like: ${t.message}")
                }
            })
    }

    fun unlikeWisata(token: String, idWisata: Int) {
        Timber.d("Mengirim permintaan UNLIKE untuk wisata: $idWisata")
        apiService.unlikeWisata(token = token, idWisata = idWisata)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        Timber.d("UNLIKE BERHASIL: ${response.body()?.message}")
                        _likeResponse.value = response.body()
                    } else {
                        Timber.e("UNLIKE GAGAL: ${response.errorBody()?.string()}")
                        _likeResponse.value = ApiResponse("failed", response.code(), "Gagal melakukan unlike.")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Timber.e("UNLIKE ERROR: ${t.message}")
                    _likeResponse.value = ApiResponse("failed", -1, "Gagal melakukan unlike: ${t.message}")
                }
            })
    }

    fun toggleLike(token: String, idWisata: Int, isCurrentlyLiked: Boolean) {
        if (isCurrentlyLiked) {
            unlikeWisata(token, idWisata)
        } else {
            likeWisata(token, idWisata)
        }
    }


}