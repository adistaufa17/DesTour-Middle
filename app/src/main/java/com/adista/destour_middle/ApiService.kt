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

    @Headers("Content-Type: application/json")
    @GET("exec")
    fun getListWisata(
        @Query("endpoint") endpoint: String = "listwisata",
        @Query("token") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Call<WisataResponse>
}
