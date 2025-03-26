package com.adista.destour_middle.ui.profile

import com.adista.destour_middle.core.network.ApiService
import com.adista.destour_middle.data.model.ProfileResponse
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import com.crocodic.core.base.viewmodel.CoreViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: ApiService
) : CoreViewModel() {

    suspend fun getProfile(token: String) {
        _apiResponse.emit(ApiResponse().responseLoading())

        ApiObserver(
            { apiService.getProfile(token = token) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val profileResponse = Gson().fromJson(response.toString(), ProfileResponse::class.java)
                    _apiResponse.emit(ApiResponse().responseSuccess(
                        "Profil berhasil dimuat",
                        data = profileResponse
                    ))
                }

                override suspend fun onError(response: ApiResponse) {
                    _apiResponse.emit(response)
                }
            }
        )
    }

    override fun apiRenewToken(){
    }

    override fun apiLogout() {
        logoutSuccess()
    }
}