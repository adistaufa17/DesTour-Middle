package com.adista.projekadvance.register

import com.adista.di.network.ApiService
import com.adista.projekadvance.request.RegisterRequest
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    val name = MutableLiveData<String>()
    val phone = MutableLiveData<String>()
    val school = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()

    fun register(name: String, phone: String, school: String, password: String, passwordConfirmation: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = RegisterRequest(name, phone, school, password, passwordConfirmation)
                val response = apiService.register(request)
                _registerSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
                _registerSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onRegisterClick() {
        val nameValue = name.value.orEmpty()
        val phoneValue = phone.value.orEmpty()
        val schoolValue = school.value.orEmpty()
        val passwordValue = password.value.orEmpty()
        val confirmPasswordValue = confirmPassword.value.orEmpty()

        if (passwordValue != confirmPasswordValue) {
            _errorMessage.value = "Password tidak cocok"
            return
        }

        register(nameValue, phoneValue, schoolValue, passwordValue, confirmPasswordValue)
    }
}
