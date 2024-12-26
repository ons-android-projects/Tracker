package com.onnetsolution.calldetect.data.di

import com.google.gson.GsonBuilder
import com.onnetsolution.calldetect.util.Constants.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private fun provideRetrofit(): Retrofit{
        val gson=GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(OkHttpClient())
            .build()
    }
    fun getInstance(): Retrofit{
        return provideRetrofit()
    }
}