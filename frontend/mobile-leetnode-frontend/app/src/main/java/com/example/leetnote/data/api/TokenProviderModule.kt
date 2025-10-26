package com.example.leetnote.data.api

import com.example.leetnote.data.auth.AuthTokenProvider
import com.example.leetnote.data.auth.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
 * This module tells Hilt whenever TokenProvider is requested, provide an instance of AuthTokenProvider.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TokenProviderModule {
    @Binds
    @Singleton
    abstract fun bindTokenProvider(authTokenProvider: AuthTokenProvider): TokenProvider
}