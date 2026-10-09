package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface PhishLensApiService {

    @GET("health")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("api/v1/analyze/text")
    suspend fun analyzeText(
        @Body request: TextAnalyzeRequest
    ): Response<NetworkAnalyzeResponse>

    @POST("api/v1/analyze/url")
    suspend fun analyzeUrl(
        @Body request: UrlAnalyzeRequest
    ): Response<NetworkAnalyzeResponse>
}

object ApiClient {
    // 10.0.2.2 points to host localhost in Android Emulator
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"

    @Volatile
    private var currentBaseUrl = DEFAULT_BASE_URL

    @Volatile
    private var apiService: PhishLensApiService? = null

    fun getBaseUrl(): String = currentBaseUrl

    fun setBaseUrl(newUrl: String) {
        val sanitized = if (!newUrl.endsWith("/")) "$newUrl/" else newUrl
        if (sanitized != currentBaseUrl) {
            currentBaseUrl = sanitized
            apiService = null
        }
    }

    fun getService(): PhishLensApiService {
        return apiService ?: synchronized(this) {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(PhishLensApiService::class.java)
            apiService = service
            service
        }
    }
}
