package com.adista.destour_middle.ui.wisata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adista.destour_middle.data.model.WisataItem
import com.adista.destour_middle.data.model.WisataResponse
import com.adista.destour_middle.core.network.ApiService
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import com.crocodic.core.base.viewmodel.CoreViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WisataViewModel @Inject constructor(
    private val apiService: ApiService
) : CoreViewModel() {

    private val _wisataResponse = MutableLiveData<List<WisataItem>>()
    val wisataResponse: LiveData<List<WisataItem>> = _wisataResponse

    private var allWisataList: List<WisataItem> = emptyList()

    fun getWisata(token: String) {
        ApiObserver(
            { apiService.getListWisata(token) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    try {
                        val wisataResponse = Gson().fromJson(response.toString(), WisataResponse::class.java)
                        Timber.d("Received wisataList with ${wisataResponse.data.wisataList.size} items")
                        allWisataList = wisataResponse.data.wisataList
                        _wisataResponse.postValue(allWisataList)
                        _apiResponse.emit(ApiResponse().responseSuccess("Data berhasil dimuat"))
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing wisata data")
                        _apiResponse.emit(ApiResponse().responseError(e))
                    }
                }

                override suspend fun onError(response: ApiResponse) {
                    Timber.e("Error getting wisata data: ${response.message}")
                    _apiResponse.emit(response)
                }
            }
        )
    }

    fun searchWisataOffline(query: String) {
        val filteredList = allWisataList.filter { it.title.contains(query, ignoreCase = true) }
        _wisataResponse.value = filteredList
    }

    suspend fun addBookmark(token: String, idWisata: Int) {
        _apiResponse.emit(ApiResponse().responseLoading())

        ApiObserver(
            { apiService.addBookmark(endpoint = "addBookmarks", token = token, idWisata = idWisata) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    Timber.d("Bookmark added: $response")
                    _apiResponse.emit(ApiResponse().responseSuccess("Bookmark berhasil ditambahkan", data = "bookmark_add"))
                }

                override suspend fun onError(response: ApiResponse) {
                    Timber.e("Failed to add bookmark: ${response.message}")
                    _apiResponse.emit(response)
                }
            }
        )
    }

    suspend fun removeBookmark(token: String, idWisata: Int) {
        _apiResponse.emit(ApiResponse().responseLoading())

        ApiObserver(
            { apiService.removeBookmark(endpoint = "removeBookmarks", token = token, idWisata = idWisata) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    Timber.d("Bookmark removed: $response")
                    _apiResponse.emit(ApiResponse().responseSuccess("Bookmark berhasil dihapus", data = "bookmark_remove"))
                }

                override suspend fun onError(response: ApiResponse) {
                    Timber.e("Failed to remove bookmark: ${response.message}")
                    _apiResponse.emit(response)
                }
            }
        )
    }

    suspend fun likeWisata(token: String, idWisata: Int) {
        _apiResponse.emit(ApiResponse().responseLoading())
        Timber.d("Mengirim permintaan LIKE untuk wisata: $idWisata")

        ApiObserver(
            { apiService.likeWisata(token = token, idWisata = idWisata) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    Timber.d("LIKE BERHASIL: $response")
                    _apiResponse.emit(ApiResponse().responseSuccess("Berhasil menyukai wisata", data = "like_add"))
                }

                override suspend fun onError(response: ApiResponse) {
                    Timber.e("LIKE GAGAL: ${response.message}")
                    _apiResponse.emit(response)
                }
            }
        )
    }

    suspend fun unlikeWisata(token: String, idWisata: Int) {
        _apiResponse.emit(ApiResponse().responseLoading())
        Timber.d("Mengirim permintaan UNLIKE untuk wisata: $idWisata")

        ApiObserver(
            { apiService.unlikeWisata(token = token, idWisata = idWisata) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    Timber.d("UNLIKE BERHASIL: $response")
                    _apiResponse.emit(ApiResponse().responseSuccess("Berhasil membatalkan suka", data = "like_remove"))
                }

                override suspend fun onError(response: ApiResponse) {
                    Timber.e("UNLIKE GAGAL: ${response.message}")
                    _apiResponse.emit(response)
                }
            }
        )
    }

    suspend fun toggleBookmark(token: String, idWisata: Int, isCurrentlyBookmarked: Boolean) {
        if (isCurrentlyBookmarked) {
            removeBookmark(token, idWisata)
        } else {
            addBookmark(token, idWisata)
        }
    }

    suspend fun toggleLike(token: String, idWisata: Int, isCurrentlyLiked: Boolean) {
        if (isCurrentlyLiked) {
            unlikeWisata(token, idWisata)
        } else {
            likeWisata(token, idWisata)
        }
    }

    override fun apiRenewToken() {
        // Not implemented for this project
        // This would be used for token refresh logic
        CoroutineScope(Dispatchers.IO).launch {
            val apiResponse = ApiResponse()
            apiResponse.message = "Token tidak dapat diperbaharui"
            _apiResponse.emit(apiResponse.responseError())
        }
    }

    override fun apiLogout() {
        // Implement if needed
        // This would be used for logout logic
        logoutSuccess()
    }
}