package com.example.foodhelp.di

import com.example.foodhelp.data.retrofit.RecetaApiService
import com.example.foodhelp.repository.RecetaRepository
import com.example.foodhelp.repository.RecetaRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRecetaApi(
        apiService: RecetaApiService
    ): RecetaRepository{
        return RecetaRepositoryImpl(apiService)
    }
}