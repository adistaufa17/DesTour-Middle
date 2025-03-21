package com.adista.destour_middle

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("exec")
    fun register(@Body registerRequest: RegisterRequest): Call<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("exec")
    fun login(@Body loginRequest: LoginRequest): Call<AuthResponse>

    @GET("exec")
    fun getListWisata(
        @Query("token") token: String,
        @Query("endpoint") endpoint: String = "listwisata"
    ): Call<WisataResponse>

    @GET("exec")
    fun searchWisata(
        @Query("endpoint") endpoint: String = "searchwisata",
        @Query("token") token: String,
        @Query("keyword") keyword: String
    ): Call<WisataResponse>

    @GET("exec")
    fun getProfile(
        @Query("endpoint") endpoint: String = "profile",
        @Query("token") token: String
    ): Call<ProfileResponse>

    @POST("exec")
    fun addBookmark(
        @Query("endpoint") endpoint: String = "addBookmarks",
        @Query("token") token: String,
        @Query("id_wisata") idWisata: Int
    ): Call<ApiResponse>

    @POST("exec")
    fun removeBookmark(
        @Query("endpoint") endpoint: String = "removeBookmarks",
        @Query("token") token: String,
        @Query("id_wisata") idWisata: Int
    ): Call<ApiResponse>

    @GET("exec")
    fun getBookmarks(
        @Query("endpoint") endpoint: String = "getBookmarks",
        @Query("token") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Call<WisataResponse>

    @POST("exec")
    fun likeWisata(
        @Query("endpoint") endpoint: String = "likeWisata",
        @Query("token") token: String,
        @Query("id_wisata") idWisata: Int
    ): Call<ApiResponse>

    @POST("exec")
    fun unlikeWisata(
        @Query("endpoint") endpoint: String = "unlikeWisata",
        @Query("token") token: String,
        @Query("id_wisata") idWisata: Int
    ): Call<ApiResponse>

}