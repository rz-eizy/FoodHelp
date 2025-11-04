package com.example.foodhelp.di

import com.example.foodhelp.data.retrofit.RecetaApiService
import com.example.foodhelp.data.retrofit.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRecetaServiceApi(): RecetaApiService {
        return RetrofitClient.apiService
    }
}