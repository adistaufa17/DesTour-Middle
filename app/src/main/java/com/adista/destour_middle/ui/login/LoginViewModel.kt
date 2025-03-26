package com.adista.destour_middle.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adista.destour_middle.core.network.ApiService
import com.adista.destour_middle.data.model.AuthResponse
import com.adista.destour_middle.data.request.LoginRequest
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import com.crocodic.core.base.viewmodel.CoreViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService
) : CoreViewModel() {

    val email = MutableLiveData("")
    val password = MutableLiveData("")

    fun onLoginClick() {
        viewModelScope.launch {
            doLogin()
        }
    }

    private suspend fun doLogin() {
        _apiResponse.emit(ApiResponse().responseLoading())

        val request = LoginRequest(
            email = email.value ?: "".trim(),
            password = password.value ?: "".trim()
        )

        ApiObserver(
            { apiService.login(request) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val authResponse = Gson().fromJson(response.toString(), AuthResponse::class.java)
                    _apiResponse.emit(ApiResponse().responseSuccess(
                        "Login berhasil",
                        data = authResponse
                    ))
                }

                override suspend fun onError(response: ApiResponse) {
                    _apiResponse.emit(response)
                }
            }
        )
    }

    override fun apiRenewToken() {
    }

    override fun apiLogout() {
        logoutSuccess()
    }
}