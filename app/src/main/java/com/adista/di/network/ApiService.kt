package com.adista.di.network

import com.adista.projekadvance.model.AuthResponse
import com.adista.projekadvance.request.LoginRequest
import com.adista.projekadvance.request.RegisterRequest
import retrofit2.http.*

interface ApiService {

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse


    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

//    @GET("profile")
//    suspend fun getProfile(): String
//
//    @POST("logout")
//    suspend fun logout(): String
//
//    @GET("exec")
//    suspend fun getListWisata(
//        @Query("token") token: String,
//        @Query("endpoint") endpoint: String = "listwisata"
//    ): String
//
//    @GET("exec")
//    suspend fun searchWisata(
//        @Query("endpoint") endpoint: String = "searchwisata",
//        @Query("token") token: String,
//        @Query("keyword") keyword: String
//    ): String
//
//    @GET("exec")
//    suspend fun getProfile(
//        @Query("endpoint") endpoint: String = "profile",
//        @Query("token") token: String
//    ): String
//
//    @POST("exec")
//    suspend fun addBookmark(
//        @Query("endpoint") endpoint: String = "addBookmarks",
//        @Query("token") token: String,
//        @Query("id_wisata") idWisata: Int
//    ): String
//
//    @POST("exec")
//    suspend fun removeBookmark(
//        @Query("endpoint") endpoint: String = "removeBookmarks",
//        @Query("token") token: String,
//        @Query("id_wisata") idWisata: Int
//    ): String
//
//    @GET("exec")
//    suspend fun getBookmarks(
//        @Query("endpoint") endpoint: String = "getBookmarks",
//        @Query("token") token: String,
//        @Query("page") page: Int = 1,
//        @Query("limit") limit: Int = 10
//    ): String
//
//    @POST("exec")
//    suspend fun likeWisata(
//        @Query("endpoint") endpoint: String = "likeWisata",
//        @Query("token") token: String,
//        @Query("id_wisata") idWisata: Int
//    ): String
//
//    @POST("exec")
//    suspend fun unlikeWisata(
//        @Query("endpoint") endpoint: String = "unlikeWisata",
//        @Query("token") token: String,
//        @Query("id_wisata") idWisata: Int
//    ): String
}
