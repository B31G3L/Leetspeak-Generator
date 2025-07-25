package com.beigel.leetSpeak_Generator.di

import android.content.Context
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.data.WhatsNewPreferences
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module für Dependency Injection
 * Stellt alle benötigten Dependencies zur Verfügung
 * UPDATED: WhatsNewPreferences hinzugefügt
 */

@Module(includes = [DomainModule::class])
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideProfileRepository(
        @ApplicationContext context: Context
    ): LeetRepository {
        return LeetRepository(context)
    }

    @Provides
    @Singleton
    fun provideThemePreferences(
        @ApplicationContext context: Context
    ): ThemePreferences {
        return ThemePreferences(context)
    }

    @Provides
    @Singleton
    fun provideWhatsNewPreferences(
        @ApplicationContext context: Context
    ): WhatsNewPreferences {
        return WhatsNewPreferences(context)
    }
}