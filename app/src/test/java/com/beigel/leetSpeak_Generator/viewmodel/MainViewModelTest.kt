package com.beigel.leetSpeak_Generator.viewmodel

import android.app.Application
import app.cash.turbine.test
import com.beigel.leetSpeak_Generator.data.HistoryEntry
import com.beigel.leetSpeak_Generator.data.HistoryPreferences
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.data.OnboardingPreferences
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.domain.usecase.leet.LeetManagerUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.translation.AnalyzeTranslationUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.translation.DetectLeetSpeakUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.translation.GeneratePreviewUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.translation.ReverseTranslateUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.translation.TranslateTextUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.translation.TranslationManagerUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.ui.DisplayNameUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.ui.InputTextUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.ui.ReverseModeModeUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.ui.TranslationModeUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.ui.UiManagerUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.ui.UiStateManagementUseCase
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import com.beigel.leetSpeak_Generator.review.InAppReviewManager
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Testet MainViewModel mit **echten** Use-Case-Objekten für die reine
 * Übersetzungs-/UI-Logik (TranslationManagerUseCase, UiManagerUseCase — beide
 * ohne Android-Abhängigkeiten konstruierbar, siehe deren eigene Unit Tests)
 * und mockk-Mocks für alles, was echten Context/Persistenz bräuchte
 * (Repository, LeetManagerUseCase, *Preferences, InAppReviewManager,
 * Application). So wird tatsächliches ViewModel-Verhalten geprüft statt nur
 * Mock-Interaktionen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: LeetRepository
    private lateinit var leetManagerUseCase: LeetManagerUseCase
    private lateinit var themePreferences: ThemePreferences
    private lateinit var onboardingPreferences: OnboardingPreferences
    private lateinit var inAppReviewManager: InAppReviewManager
    private lateinit var historyPreferences: HistoryPreferences
    private lateinit var application: Application

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Echte, Android-freie Use Cases -> ViewModel-Tests prüfen tatsächliches
        // Übersetzungs-/UI-Verhalten statt nur, ob Methoden aufgerufen wurden.
        val uiManager = UiManagerUseCase(
            ReverseModeModeUseCase(), TranslationModeUseCase(), DisplayNameUseCase(),
            InputTextUseCase(), UiStateManagementUseCase()
        )
        val translationManager = TranslationManagerUseCase(
            TranslateTextUseCase(), ReverseTranslateUseCase(), DetectLeetSpeakUseCase(),
            AnalyzeTranslationUseCase(), GeneratePreviewUseCase()
        )

        repository = mockk(relaxed = true)
        leetManagerUseCase = mockk(relaxed = true)
        themePreferences = mockk(relaxed = true)
        onboardingPreferences = mockk(relaxed = true)
        inAppReviewManager = mockk(relaxed = true)
        historyPreferences = mockk(relaxed = true)
        application = mockk(relaxed = true)

        every { repository.leets } returns MutableStateFlow(emptyList())
        every { repository.currentLeet } returns MutableStateFlow<com.beigel.leetSpeak_Generator.data.CustomLeet?>(null)
        every { repository.currentLeetIndex } returns MutableStateFlow(0)
        every { repository.hasLeets } returns MutableStateFlow(false)
        coEvery { repository.setCurrentLeetIndex(any()) } returns Result.success(Unit)

        every { themePreferences.defaultViewExpanded } returns MutableStateFlow(false)
        every { themePreferences.themeMode } returns MutableStateFlow(ThemePreferences.THEME_SYSTEM)
        every { themePreferences.clearInputAfterCopy } returns MutableStateFlow(false)
        every { themePreferences.askBeforeClear } returns MutableStateFlow(true)
        every { themePreferences.hapticFeedbackEnabled } returns MutableStateFlow(true)

        every { onboardingPreferences.isOnboardingCompleted } returns MutableStateFlow(true)

        every { leetManagerUseCase.getLeetOptions() } returns MutableStateFlow(emptyList())
        every { leetManagerUseCase.getFavoriteLeetOptions() } returns MutableStateFlow(emptyList())
        coEvery { leetManagerUseCase.loadFavoriteLeet() } returns
            Result.success(LeetRepository.FavoriteLeetResult.simple())

        every { inAppReviewManager.getReviewStats() } returns
            MutableStateFlow(InAppReviewManager.ReviewStats(0, 0, 0, 0))
        coEvery { inAppReviewManager.incrementAppStartCount() } just Runs
        coEvery { inAppReviewManager.shouldShowReview() } returns false

        every { historyPreferences.history } returns MutableStateFlow(emptyList())
        coEvery { historyPreferences.addEntry(any()) } just Runs
        coEvery { historyPreferences.removeEntry(any()) } just Runs
        coEvery { historyPreferences.clear() } just Runs

        viewModel = MainViewModel(
            application, translationManager, leetManagerUseCase, uiManager, repository,
            themePreferences, onboardingPreferences, inAppReviewManager, historyPreferences
        )
        // Lässt die init{}-Coroutinen (initializeFavoriteLeet, trackAppStart) durchlaufen,
        // bevor die eigentlichen Tests starten.
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── Übersetzung ───────────────────────────────────────────────────────────

    @Test
    fun `updateInputText updates inputText and outputText reflects real translation`() = runTest(testDispatcher) {
        viewModel.outputText.test {
            awaitItem() // initialer Wert ""
            viewModel.handleIntent(MainIntent.UpdateInput("HELLO"))
            assertThat(awaitItem()).isEqualTo("#3LL0")
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(viewModel.inputText.value).isEqualTo("HELLO")
    }

    @Test
    fun `toggleReverseMode moves current output into input when output is not empty`() = runTest(testDispatcher) {
        var translatedOutput = ""
        viewModel.outputText.test {
            awaitItem()
            viewModel.handleIntent(MainIntent.UpdateInput("HELLO"))
            translatedOutput = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.handleIntent(MainIntent.ToggleReverseMode)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isReverseMode.value).isTrue()
        assertThat(viewModel.inputText.value).isEqualTo(translatedOutput)
    }

    @Test
    fun `toggleReverseMode with empty output leaves input untouched`() = runTest(testDispatcher) {
        viewModel.handleIntent(MainIntent.ToggleReverseMode)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isReverseMode.value).isTrue()
        assertThat(viewModel.inputText.value).isEmpty()
    }

    // ─── Kopieren & Teilen ─────────────────────────────────────────────────────

    @Test
    fun `copyToClipboard with empty output sets an error and saves no history entry`() = runTest(testDispatcher) {
        viewModel.handleIntent(MainIntent.CopyToClipboard)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        coVerify(exactly = 0) { historyPreferences.addEntry(any()) }
    }

    @Test
    fun `copyToClipboard with content sets success and saves a history entry`() = runTest(testDispatcher) {
        viewModel.outputText.test {
            awaitItem()
            viewModel.handleIntent(MainIntent.UpdateInput("HI"))
            awaitItem() // "#1"
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.handleIntent(MainIntent.CopyToClipboard)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.successMessage).isNotNull()
        coVerify(exactly = 1) { historyPreferences.addEntry(any()) }
    }

    @Test
    fun `shareOutput with content emits the output via shareEvent and saves history`() = runTest(testDispatcher) {
        var expectedOutput = ""
        viewModel.outputText.test {
            awaitItem()
            viewModel.handleIntent(MainIntent.UpdateInput("HI"))
            expectedOutput = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.shareEvent.test {
            viewModel.handleIntent(MainIntent.ShareOutput)
            assertThat(awaitItem()).isEqualTo(expectedOutput)
            cancelAndIgnoreRemainingEvents()
        }

        // shareOutput() speichert den Verlaufseintrag über einen eigenen
        // viewModelScope.launch{} — der läuft nicht zwingend schon vollständig
        // durch, nur weil das shareEvent bereits emittiert wurde.
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { historyPreferences.addEntry(any()) }
    }

    @Test
    fun `shareOutput with empty output sets an error and does not save history`() = runTest(testDispatcher) {
        viewModel.handleIntent(MainIntent.ShareOutput)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
        coVerify(exactly = 0) { historyPreferences.addEntry(any()) }
    }

    // ─── Automatisches Speichern (Debounce) ────────────────────────────────────

    @Test
    fun `auto-saves to history after a pause in typing, even without copy or share`() = runTest(testDispatcher) {
        viewModel.outputText.test {
            awaitItem()
            viewModel.handleIntent(MainIntent.UpdateInput("HI"))
            awaitItem() // "#1"
            cancelAndIgnoreRemainingEvents()
        }

        // 2 Sekunden Pause abwarten -> Debounce löst aus
        testDispatcher.scheduler.advanceTimeBy(2100)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { historyPreferences.addEntry(any()) }
    }

    @Test
    fun `does not auto-save before the debounce pause has elapsed`() = runTest(testDispatcher) {
        viewModel.outputText.test {
            awaitItem()
            viewModel.handleIntent(MainIntent.UpdateInput("HI"))
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        // Erst 500ms vergangen -> die 2-Sekunden-Pause ist noch nicht erreicht
        testDispatcher.scheduler.advanceTimeBy(500)
        testDispatcher.scheduler.runCurrent()

        coVerify(exactly = 0) { historyPreferences.addEntry(any()) }
    }

    // ─── Verlauf ────────────────────────────────────────────────────────────────

    @Test
    fun `useHistoryEntry restores input text and mode`() = runTest(testDispatcher) {
        val entry = HistoryEntry(
            inputText = "restored text",
            outputText = "some output",
            modeDisplayName = "Extended Leet",
            mode = LeetManager.MODE_EXTENDED
        )

        viewModel.handleIntent(MainIntent.UseHistoryEntry(entry))
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.inputText.value).isEqualTo("restored text")
        assertThat(viewModel.currentMode.value).isEqualTo(LeetTranslator.TranslationMode.EXTENDED)
    }

    @Test
    fun `deleteHistoryEntry delegates to HistoryPreferences with the given id`() = runTest(testDispatcher) {
        viewModel.handleIntent(MainIntent.DeleteHistoryEntry("abc-123"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { historyPreferences.removeEntry("abc-123") }
    }

    @Test
    fun `clearHistory delegates to HistoryPreferences`() = runTest(testDispatcher) {
        viewModel.handleIntent(MainIntent.ClearHistory)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { historyPreferences.clear() }
    }

    // ─── Vorschau (Modi-Selector) ──────────────────────────────────────────────

    @Test
    fun `generatePreview uses Simple translation mode for the Simple option`() {
        val simpleOption = LeetOption(
            mode = LeetManager.MODE_SIMPLE, name = "Simple Leet", description = "", isCustom = false
        )

        val preview = viewModel.generatePreview(simpleOption, sampleText = "A")

        assertThat(preview).isEqualTo("4")
    }
}
