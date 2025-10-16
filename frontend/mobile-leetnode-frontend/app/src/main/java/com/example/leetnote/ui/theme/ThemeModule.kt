package com.example.leetnote.ui.theme

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {
    
    @Provides
    @Singleton
    fun provideThemeManager(@ApplicationContext context: Context): ThemeManager {
        return ThemeManager(context)
    }
}