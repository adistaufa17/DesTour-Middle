package com.adista.destour_middle.data.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("status") val status: String,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ProfileData?
)

data class ProfileData(
    @SerializedName("id") val id: Int,
    @SerializedName("nama_lengkap") val namaLengkap: String,
    @SerializedName("email") val email: String,
    @SerializedName("nomor_hp") val nomorHp: String
)