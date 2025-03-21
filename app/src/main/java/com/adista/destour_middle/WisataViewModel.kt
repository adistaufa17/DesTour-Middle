// Perbaikan pada WisataViewModel.kt
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

    private val _likeResponse = MutableLiveData<ApiResponse>()
    val likeResponse: LiveData<ApiResponse> = _likeResponse

    private var allWisataList: MutableList<WisataItem> = mutableListOf()

    // Fungsi untuk mendapatkan wisata
    fun getWisata(token: String) {
        Timber.d("Getting wisata list with token: $token")
        RetrofitClient.instance.getListWisata(token = token).enqueue(object : Callback<WisataResponse> {
            override fun onResponse(call: Call<WisataResponse>, response: Response<WisataResponse>) {
                if (response.isSuccessful) {
                    allWisataList = response.body()?.data?.wisataList?.toMutableList() ?: mutableListOf()
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

    fun addBookmark(token: String, idWisata: Int) {
        Timber.d("Adding bookmark with token: $token, idWisata: $idWisata")

        // Update local list immediately for better UX
        updateLocalBookmarkStatus(idWisata, true)

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

                    // Jangan revert status jika kode error 409 (sudah dibookmark sebelumnya)
                    // Ini artinya status bookmark sebenarnya memang true
                    if (response.code() != 409) {
                        // Revert local change if API failed with error selain 409
                        updateLocalBookmarkStatus(idWisata, false)
                    }

                    // Tampilkan pesan error yang sesuai
                    if (response.code() == 409) {
                        _bookmarkResponse.value = ApiResponse(
                            status = "success", // Anggap sukses karena memang sudah dibookmark
                            code = 200,
                            message = "Wisata sudah di-bookmark sebelumnya."
                        )
                    } else {
                        _bookmarkResponse.value = ApiResponse(
                            status = "failed",
                            code = response.code(),
                            message = "Gagal menambahkan bookmark: $errorBody"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Timber.e(t, "Failure adding bookmark: ${t.message}")

                // Revert local change if API failed
                updateLocalBookmarkStatus(idWisata, false)

                _bookmarkResponse.value = ApiResponse(
                    status = "failed",
                    code = -1,
                    message = "Gagal menambahkan bookmark: ${t.message}"
                )
            }
        })
    }

    fun removeBookmark(token: String, idWisata: Int) {
        Timber.d("Removing bookmark with token: $token, idWisata: $idWisata")

        // Update local list immediately for better UX
        updateLocalBookmarkStatus(idWisata, false)

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

                    // Jangan revert status jika kode error 404 (belum dibookmark sebelumnya)
                    // Ini artinya status bookmark sebenarnya memang false
                    if (response.code() != 404) {
                        // Revert local change if API failed with error selain 404
                        updateLocalBookmarkStatus(idWisata, true)
                    }

                    // Tampilkan pesan error yang sesuai
                    if (response.code() == 404) {
                        _bookmarkResponse.value = ApiResponse(
                            status = "success", // Anggap sukses karena memang belum dibookmark
                            code = 200,
                            message = "Wisata memang belum di-bookmark."
                        )
                    } else {
                        _bookmarkResponse.value = ApiResponse(
                            status = "failed",
                            code = response.code(),
                            message = "Gagal menghapus bookmark: $errorBody"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Timber.e(t, "Failure removing bookmark: ${t.message}")

                // Revert local change if API failed
                updateLocalBookmarkStatus(idWisata, true)

                _bookmarkResponse.value = ApiResponse(
                    status = "failed",
                    code = -1,
                    message = "Gagal menghapus bookmark: ${t.message}"
                )
            }
        })
    }

    // Fungsi untuk toggle bookmark
    fun toggleBookmark(token: String, idWisata: Int, isCurrentlyBookmarked: Boolean) {
        if (isCurrentlyBookmarked) {
            removeBookmark(token, idWisata)
        } else {
            addBookmark(token, idWisata)
        }
    }

    fun likeWisata(token: String, idWisata: Int) {
        Timber.d("Liking wisata with token: $token, idWisata: $idWisata")

        // Update local list immediately for better UX
        updateLocalLikeStatus(idWisata, true)

        RetrofitClient.instance.likeWisata(
            endpoint = "likeWisata",
            token = token,
            idWisata = idWisata
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    _likeResponse.value = response.body()
                    Timber.d("Wisata liked successfully: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Timber.e("Error liking wisata: code=${response.code()}, body=$errorBody")

                    // Jangan revert status jika kode error 409 (sudah dilike sebelumnya)
                    // Ini artinya status like sebenarnya memang true
                    if (response.code() != 409) {
                        // Revert local change if API failed with error selain 409
                        updateLocalLikeStatus(idWisata, false)
                    }

                    // Tampilkan pesan error yang sesuai
                    if (response.code() == 409) {
                        _likeResponse.value = ApiResponse(
                            status = "success", // Anggap sukses karena memang sudah dilike
                            code = 200,
                            message = "Wisata sudah disukai sebelumnya."
                        )
                    } else {
                        _likeResponse.value = ApiResponse(
                            status = "failed",
                            code = response.code(),
                            message = "Gagal menyukai wisata: $errorBody"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Timber.e(t, "Failure liking wisata: ${t.message}")

                // Revert local change if API failed
                updateLocalLikeStatus(idWisata, false)

                _likeResponse.value = ApiResponse(
                    status = "failed",
                    code = -1,
                    message = "Gagal menyukai wisata: ${t.message}"
                )
            }
        })
    }

    fun unlikeWisata(token: String, idWisata: Int) {
        Timber.d("Unliking wisata with token: $token, idWisata: $idWisata")

        // Update local list immediately for better UX
        updateLocalLikeStatus(idWisata, false)

        RetrofitClient.instance.unlikeWisata(
            endpoint = "unlikeWisata",
            token = token,
            idWisata = idWisata
        ).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    _likeResponse.value = response.body()
                    Timber.d("Wisata unliked successfully: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Timber.e("Error unliking wisata: code=${response.code()}, body=$errorBody")

                    // Jangan revert status jika kode error 404 (belum dilike sebelumnya)
                    // Ini artinya status like sebenarnya memang false
                    if (response.code() != 404) {
                        // Revert local change if API failed with error selain 404
                        updateLocalLikeStatus(idWisata, true)
                    }

                    // Tampilkan pesan error yang sesuai
                    if (response.code() == 404) {
                        _likeResponse.value = ApiResponse(
                            status = "success", // Anggap sukses karena memang belum dilike
                            code = 200,
                            message = "Wisata memang belum disukai."
                        )
                    } else {
                        _likeResponse.value = ApiResponse(
                            status = "failed",
                            code = response.code(),
                            message = "Gagal batal menyukai wisata: $errorBody"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Timber.e(t, "Failure unliking wisata: ${t.message}")

                // Revert local change if API failed
                updateLocalLikeStatus(idWisata, true)

                _likeResponse.value = ApiResponse(
                    status = "failed",
                    code = -1,
                    message = "Gagal batal menyukai wisata: ${t.message}"
                )
            }
        })
    }


    // Fungsi untuk toggle like
    fun toggleLike(token: String, idWisata: Int, isCurrentlyLiked: Boolean) {
        if (isCurrentlyLiked) {
            unlikeWisata(token, idWisata)
        } else {
            likeWisata(token, idWisata)
        }
    }

    // Helper function untuk memperbarui status bookmark di list lokal
    private fun updateLocalBookmarkStatus(idWisata: Int, isBookmarked: Boolean) {
        for (i in allWisataList.indices) {
            if (allWisataList[i].id == idWisata) {
                // Buat salinan item dengan status bookmark yang diperbarui
                val updatedItem = allWisataList[i].copy(isBookmarked = isBookmarked)
                allWisataList[i] = updatedItem
                // Perbarui live data
                _wisataResponse.value = allWisataList.toList()
                break
            }
        }
    }

    // Helper function untuk memperbarui status like di list lokal
    private fun updateLocalLikeStatus(idWisata: Int, isLiked: Boolean) {
        for (i in allWisataList.indices) {
            if (allWisataList[i].id == idWisata) {
                // Buat salinan item dengan status like yang diperbarui
                val updatedItem = allWisataList[i].copy(isLiked = isLiked)
                allWisataList[i] = updatedItem
                // Perbarui live data
                _wisataResponse.value = allWisataList.toList()
                break
            }
        }
    }
}