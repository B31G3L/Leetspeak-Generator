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
 * Ersetzt die manuelle ViewModelFactory
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
}

/**
 * Hilt Module für ViewModels
 * ViewModels werden automatisch injiziert
 */
@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {

    // ViewModels werden automatisch von Hilt bereitgestellt
    // Kein manueller Code erforderlich dank @HiltViewModel Annotation
}