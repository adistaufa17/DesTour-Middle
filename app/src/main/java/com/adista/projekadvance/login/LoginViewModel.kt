package com.adista.projekadvance.login

import com.adista.di.network.ApiService
import com.adista.projekadvance.request.LoginRequest
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    val phone = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = LoginRequest(phone, password)
                val response = apiService.login(request)
                _loginResult.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login failed"
                _loginResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onLoginClick() {
        val currentPhone = phone.value.orEmpty()
        val currentPassword = password.value.orEmpty()
        login(currentPhone, currentPassword)
    }
}
