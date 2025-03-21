package com.adista.destour_middle

import com.google.gson.annotations.SerializedName

data class WisataResponse(
    @SerializedName("status") val status: String,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: WisataData
)

data class WisataData(
    @SerializedName("wisataList") val wisataList: List<WisataItem>
) {
}

data class WisataItem(
    @SerializedName("id") val id: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("title") val title: String,
    @SerializedName("lokasi") val lokasi: String,
    @SerializedName("deskripsi") val deskripsi: String,
    @SerializedName("is_bookmarked") val isBookmarked: Boolean = false

)