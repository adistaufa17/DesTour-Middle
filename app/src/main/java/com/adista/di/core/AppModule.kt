package com.adista.di.core

import android.content.Context
import android.content.SharedPreferences
import com.adista.di.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // private const val BASE_URL = "https://script.google.com/macros/s/AKfycbx9gfJuausUXfsI2YEAA3L6Djx71Mbxsx0pE6R-itHBqcLz-iUyyayFVgn6mfusPNMT/"

    @Provides
    @Singleton
    fun provideAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                // Add any headers or authentication tokens if needed
                .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.9:8000/api/") // Replace with your actual base URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)
    }
    //@Provides
    //    @Singleton
    //    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    //        return HttpLoggingInterceptor().apply {
    //            level = HttpLoggingInterceptor.Level.BODY
    //        }
    //    }

    //@Provides
    //    @Singleton
    //    fun provideRetrofit(client: OkHttpClient): Retrofit {
    //        return Retrofit.Builder()
    //            .baseUrl(BASE_URL)
    //            .client(client)
    //            .addConverterFactory(ScalarsConverterFactory.create()) // Add this first
    //            .addConverterFactory(GsonConverterFactory.create())    // Keep this second
    //            .build()
    //    }
}
