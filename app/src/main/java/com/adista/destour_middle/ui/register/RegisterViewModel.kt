package com.adista.destour_middle.ui.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adista.destour_middle.core.network.ApiService
import com.adista.destour_middle.data.model.AuthResponse
import com.adista.destour_middle.data.request.RegisterRequest
import com.crocodic.core.api.ApiObserver
import com.crocodic.core.api.ApiResponse
import com.crocodic.core.base.viewmodel.CoreViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val apiService: ApiService
) : CoreViewModel() {

    val nama = MutableLiveData("")
    val email = MutableLiveData("")
    val nomorHp = MutableLiveData("")
    val password = MutableLiveData("")
    val confirmPassword = MutableLiveData("")

    fun onRegisterClick() {
        viewModelScope.launch {
            doRegister()
        }
    }

    private suspend fun doRegister() {
        _apiResponse.emit(ApiResponse().responseLoading())

        val request = RegisterRequest(
            nama_lengkap = nama.value ?: "".trim(),
            email = email.value ?: "".trim(),
            nomor_hp = nomorHp.value ?: "".trim(),
            password = password.value ?: "".trim(),
            confirm_password = confirmPassword.value ?: "".trim()
        )

        ApiObserver(
            { apiService.register(request) },
            false,
            object : ApiObserver.ResponseListener {
                override suspend fun onSuccess(response: JSONObject) {
                    val authResponse = Gson().fromJson(response.toString(), AuthResponse::class.java)
                    _apiResponse.emit(ApiResponse().responseSuccess(
                        "Registrasi berhasil",
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