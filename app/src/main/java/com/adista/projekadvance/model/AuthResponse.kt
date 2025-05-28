package com.adista.projekadvance.model

import com.google.gson.annotations.SerializedName

sealed class AuthResponse {
    data class Success(
        val success: Boolean = true,
        val message: String,
        val data: UserData?
    ) : AuthResponse()

    data class Error(
        val success: Boolean = false,
        val message: String,
        val errorCode: Int? = null
    ) : AuthResponse()
}

data class UserData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("school")
    val school: String?,

    @SerializedName("photo")
    val photo: String?
)
