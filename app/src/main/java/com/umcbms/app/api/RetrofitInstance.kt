package com.umcbms.app.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance {
   /* companion object{
        val apiService: ApiService by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit.create(ApiService::class.java)
        }
    }*/

    companion object {
        private var mInstance: RetrofitInstance? = null

        private val loginInterceptor = //if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }


           var  networkInterceptor= Interceptor { chain ->
                val response = chain.proceed(chain.request())
                val code = response.code
                if (code >= 500)
                    throw ServerError("Unknown server error", response.body!!.string())
                else if (code == 401 || code == 403)
                    throw AuthenticationException()
                response
           }

        private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loginInterceptor)
            .addInterceptor(networkInterceptor)
            .readTimeout(2, TimeUnit.MINUTES)
            .connectTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build()

        private var gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        fun getApiService(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            return retrofit.create(ApiService::class.java)
        }

        @Synchronized
        fun getInstance(): RetrofitInstance? {
            if (mInstance == null) mInstance = RetrofitInstance()
            return mInstance
        }

    }

}
