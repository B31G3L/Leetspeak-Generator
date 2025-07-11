package com.beigel.leetSpeak_Generator.di

import android.content.Context
import com.beigel.leetSpeak_Generator.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module für Dependency Injection
 * Stellt alle benötigten Dependencies zur Verfügung
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideProfileRepository(
        @ApplicationContext context: Context
    ): ProfileRepository {
        return ProfileRepository(context)
    }

    // Falls später weitere Dependencies benötigt werden:

    /*
    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("leetspeak_prefs", Context.MODE_PRIVATE)
    }
    */
}