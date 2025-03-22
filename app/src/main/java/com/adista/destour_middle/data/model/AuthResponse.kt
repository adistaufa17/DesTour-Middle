package com.adista.destour_middle.data.model
import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserData?
)

data class UserData(
    @SerializedName("token") val token: String?,
    @SerializedName("user") val user: User?
)

data class User(
    @SerializedName("id") val id: Long?,
    @SerializedName("nama_lengkap") val namaLengkap: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("nomor_hp") val nomorHp: String?
)

