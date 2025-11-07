package com.example.leetnote.data.api

import com.example.leetnote.data.auth.TokenProvider
import com.example.leetnote.data.auth.TokenStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/*
 * This module tells Hilt how to provide the LeetnoteApiService dependency.
 * It uses the RetrofitInstance to create an instance of LeetnoteApiService.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides // function to provide the LeetnoteApiService dependency
    @Singleton // ensures that only one instance of LeetnoteApiService is created
    fun provideAuthInterceptor(
        tokenProvider: TokenProvider,
        tokenStorage: TokenStorage,
        auth: FirebaseAuth
    ): AuthInterceptor {
        return AuthInterceptor(tokenProvider, auth, tokenStorage)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // injected here
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            //.baseUrl("http://10.0.2.2:8080/") // local backend URL
            .baseUrl("http://ec2-3-20-194-184.us-east-2.compute.amazonaws.com:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): LeetnoteApiService {
        return retrofit.create(LeetnoteApiService::class.java)
    }
}