package com.beigel.leetSpeak_Generator.di

import com.beigel.leetSpeak_Generator.domain.usecase.leet.*
import com.beigel.leetSpeak_Generator.domain.usecase.translation.*
import com.beigel.leetSpeak_Generator.domain.usecase.ui.*
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module für Domain Layer Use Cases
 * Stellt alle Business Logic Use Cases zur Verfügung
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    // Translation Use Cases
    @Provides
    @Singleton
    fun provideTranslateTextUseCase(): TranslateTextUseCase {
        return TranslateTextUseCase()
    }

    @Provides
    @Singleton
    fun provideReverseTranslateUseCase(): ReverseTranslateUseCase {
        return ReverseTranslateUseCase()
    }

    @Provides
    @Singleton
    fun provideDetectLeetSpeakUseCase(): DetectLeetSpeakUseCase {
        return DetectLeetSpeakUseCase()
    }

    @Provides
    @Singleton
    fun provideAnalyzeTranslationUseCase(): AnalyzeTranslationUseCase {
        return AnalyzeTranslationUseCase()
    }

    @Provides
    @Singleton
    fun provideGeneratePreviewUseCase(): GeneratePreviewUseCase {
        return GeneratePreviewUseCase()
    }

    @Provides
    @Singleton
    fun provideTranslationManagerUseCase(
        translateTextUseCase: TranslateTextUseCase,
        reverseTranslateUseCase: ReverseTranslateUseCase,
        detectLeetSpeakUseCase: DetectLeetSpeakUseCase,
        analyzeTranslationUseCase: AnalyzeTranslationUseCase,
        generatePreviewUseCase: GeneratePreviewUseCase
    ): TranslationManagerUseCase {
        return TranslationManagerUseCase(
            translateTextUseCase,
            reverseTranslateUseCase,
            detectLeetSpeakUseCase,
            analyzeTranslationUseCase,
            generatePreviewUseCase
        )
    }

    // Leet Management Use Cases
    @Provides
    @Singleton
    fun provideGetLeetOptionsUseCase(
        repository: LeetRepository
    ): GetLeetOptionsUseCase {
        return GetLeetOptionsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetFavoriteLeetOptionsUseCase(
        repository: LeetRepository
    ): GetFavoriteLeetOptionsUseCase {
        return GetFavoriteLeetOptionsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreateLeetUseCase(
        repository: LeetRepository
    ): CreateLeetUseCase {
        return CreateLeetUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateLeetUseCase(
        repository: LeetRepository
    ): UpdateLeetUseCase {
        return UpdateLeetUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteLeetUseCase(
        repository: LeetRepository
    ): DeleteLeetUseCase {
        return DeleteLeetUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideToggleFavoriteUseCase(
        repository: LeetRepository
    ): ToggleFavoriteUseCase {
        return ToggleFavoriteUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLoadFavoriteLeetUseCase(
        repository: LeetRepository
    ): LoadFavoriteLeetUseCase {
        return LoadFavoriteLeetUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSetCurrentLeetIndexUseCase(
        repository: LeetRepository
    ): SetCurrentLeetIndexUseCase {
        return SetCurrentLeetIndexUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLeetManagerUseCase(
        getLeetOptionsUseCase: GetLeetOptionsUseCase,
        getFavoriteLeetOptionsUseCase: GetFavoriteLeetOptionsUseCase,
        createLeetUseCase: CreateLeetUseCase,
        updateLeetUseCase: UpdateLeetUseCase,
        deleteLeetUseCase: DeleteLeetUseCase,
        toggleFavoriteUseCase: ToggleFavoriteUseCase,
        loadFavoriteLeetUseCase: LoadFavoriteLeetUseCase,
        setCurrentLeetIndexUseCase: SetCurrentLeetIndexUseCase
    ): LeetManagerUseCase {
        return LeetManagerUseCase(
            getLeetOptionsUseCase,
            getFavoriteLeetOptionsUseCase,
            createLeetUseCase,
            updateLeetUseCase,
            deleteLeetUseCase,
            toggleFavoriteUseCase,
            loadFavoriteLeetUseCase,
            setCurrentLeetIndexUseCase
        )
    }

    // UI State Use Cases
    @Provides
    @Singleton
    fun provideReverseModeUseCase(): ReverseModeModeUseCase {
        return ReverseModeModeUseCase()
    }

    @Provides
    @Singleton
    fun provideTranslationModeUseCase(): TranslationModeUseCase {
        return TranslationModeUseCase()
    }

    @Provides
    @Singleton
    fun provideDisplayNameUseCase(): DisplayNameUseCase {
        return DisplayNameUseCase()
    }

    @Provides
    @Singleton
    fun provideInputTextUseCase(): InputTextUseCase {
        return InputTextUseCase()
    }

    @Provides
    @Singleton
    fun provideUiStateManagementUseCase(): UiStateManagementUseCase {
        return UiStateManagementUseCase()
    }

    @Provides
    @Singleton
    fun provideUiManagerUseCase(
        reverseModeUseCase: ReverseModeModeUseCase,
        translationModeUseCase: TranslationModeUseCase,
        displayNameUseCase: DisplayNameUseCase,
        inputTextUseCase: InputTextUseCase,
        uiStateManagementUseCase: UiStateManagementUseCase
    ): UiManagerUseCase {
        return UiManagerUseCase(
            reverseModeUseCase,
            translationModeUseCase,
            displayNameUseCase,
            inputTextUseCase,
            uiStateManagementUseCase
        )
    }
}