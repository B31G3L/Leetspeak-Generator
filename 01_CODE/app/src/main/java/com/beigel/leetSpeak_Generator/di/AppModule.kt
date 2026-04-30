package com.beigel.leetSpeak_Generator.di

import android.content.Context
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.data.WhatsNewPreferences
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import com.beigel.leetSpeak_Generator.review.InAppReviewManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideProfileRepository(
        @ApplicationContext context: Context
    ): LeetRepository = LeetRepository(context)

    @Provides
    @Singleton
    fun provideThemePreferences(
        @ApplicationContext context: Context
    ): ThemePreferences = ThemePreferences(context)

    @Provides
    @Singleton
    fun provideWhatsNewPreferences(
        @ApplicationContext context: Context
    ): WhatsNewPreferences = WhatsNewPreferences(context)

    @Provides
    @Singleton
    fun provideInAppReviewManager(
        @ApplicationContext context: Context
    ): InAppReviewManager = InAppReviewManager(context)
}