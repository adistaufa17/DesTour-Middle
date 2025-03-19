package com.adista.destour_middle

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbx9gfJuausUXfsI2YEAA3L6Djx71Mbxsx0pE6R-itHBqcLz-iUyyayFVgn6mfusPNMT/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
