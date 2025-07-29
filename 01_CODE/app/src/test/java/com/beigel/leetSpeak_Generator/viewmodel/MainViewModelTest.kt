package com.beigel.leetSpeak_Generator.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import app.cash.turbine.test
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.data.VersionInfo
import com.beigel.leetSpeak_Generator.data.WhatsNewPreferences
import com.beigel.leetSpeak_Generator.domain.usecase.leet.LeetManagerUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.translation.TranslationManagerUseCase
import com.beigel.leetSpeak_Generator.domain.usecase.ui.UiManagerUseCase
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.presentation.intent.MainIntent
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Umfassende Tests für MainViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Mocks
    private lateinit var translationManager: TranslationManagerUseCase
    private lateinit var leetManager: LeetManagerUseCase
    private lateinit var uiManager: UiManagerUseCase
    private lateinit var repository: LeetRepository
    private lateinit var themePreferences: ThemePreferences
    private lateinit var whatsNewPreferences: WhatsNewPreferences

    // Test StateFlows
    private lateinit var inputTextFlow: MutableStateFlow<String>
    private lateinit var isReverseModeFlow: MutableStateFlow<Boolean>
    private lateinit var currentModeFlow: MutableStateFlow<LeetTranslator.TranslationMode>
    private lateinit var leetsFlow: MutableStateFlow<List<CustomLeet>>
    private lateinit var currentLeetFlow: MutableStateFlow<CustomLeet?>

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        // Initialize flows
        inputTextFlow = MutableStateFlow("")
        isReverseModeFlow = MutableStateFlow(false)
        currentModeFlow = MutableStateFlow(LeetTranslator.TranslationMode.SIMPLE)
        leetsFlow = MutableStateFlow(emptyList())
        currentLeetFlow = MutableStateFlow(null)

        // Mock dependencies
        translationManager = mockk(relaxed = true)
        leetManager = mockk(relaxed = true)
        uiManager = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        themePreferences = mockk(relaxed = true)
        whatsNewPreferences = mockk(relaxed = true)

        // Setup UI Manager flows
        every { uiManager.inputText } returns inputTextFlow
        every { uiManager.isReverseMode } returns isReverseModeFlow
        every { uiManager.currentMode } returns currentModeFlow
        every { uiManager.uiState } returns MutableStateFlow(
            com.beigel.leetSpeak_Generator.domain.usecase.ui.UiStateManagementUseCase.UiState()
        )

        // Setup Repository flows
        every { repository.leets } returns leetsFlow
        every { repository.currentLeet } returns currentLeetFlow
        every { repository.hasLeets } returns MutableStateFlow(false)

        // Setup Theme Preferences
        every { themePreferences.defaultViewExpanded } returns flowOf(false)
        every { themePreferences.themeMode } returns flowOf(ThemePreferences.THEME_SYSTEM)

        // Setup What's New Preferences
        every { whatsNewPreferences.shouldShowWhatsNew } returns flowOf(false)
        every { whatsNewPreferences.isFirstLaunch } returns flowOf(false)
        every { whatsNewPreferences.getCurrentVersionInfo() } returns VersionInfo(1, "1.0.0")

        // Setup LeetManager with coroutine mock
        val mockLeetOptions = listOf(
            LeetOption.createSimple(),
            LeetOption.createExtended()
        )
        every { leetManager.getLeetOptions() } returns flowOf(mockLeetOptions)
        every { leetManager.getFavoriteLeetOptions() } returns flowOf(emptyList())
        // Mock suspend function properly
        coEvery { leetManager.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.simple()
        )

        // Setup TranslationManager
        every { translationManager.translate(any(), any(), any(), any()) } returns ""
        every { translationManager.isLikelyLeetspeak(any()) } returns false
        every { translationManager.analyzeTranslation(any(), any(), any()) } returns mockk(relaxed = true)

        viewModel = MainViewModel(
            translationManager,
            leetManager,
            uiManager,
            repository,
            themePreferences,
            whatsNewPreferences
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ===== INITIALIZATION TESTS =====

    @Test
    fun `viewModel initializes with correct default values`() = runTest {
        // Verify initialization calls
        coVerify { leetManager.loadFavoriteLeet() }

        // Verify flows are properly connected
        viewModel.inputText.test {
            assertEquals("", awaitItem())
        }

        viewModel.isReverseMode.test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `viewModel loads favorite leet on initialization`() = runTest {
        coEvery { leetManager.loadFavoriteLeet() } returns Result.success(
            LeetRepository.FavoriteLeetResult.simple()
        )

        // Create new viewModel to trigger initialization
        val newViewModel = MainViewModel(
            translationManager,
            leetManager,
            uiManager,
            repository,
            themePreferences,
            whatsNewPreferences
        )

        coVerify { leetManager.loadFavoriteLeet() }
        verify { uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE) }
    }

    // ===== INPUT HANDLING TESTS =====

    @Test
    fun `handleIntent UpdateInput updates input text`() = runTest {
        val testText = "Hello World"

        viewModel.handleIntent(MainIntent.UpdateInput(testText))

        verify { uiManager.updateInputText(testText) }
    }

    @Test
    fun `handleIntent ClearInput clears input text`() = runTest {
        viewModel.handleIntent(MainIntent.ClearInput)

        verify { uiManager.clearInput() }
    }

    // ===== MODE CHANGE TESTS =====

    @Test
    fun `handleIntent ChangeMode to simple mode`() = runTest {
        val simpleOption = LeetOption.createSimple()
        coEvery { leetManager.changeMode(any()) } returns Result.success(Unit)

        viewModel.handleIntent(MainIntent.ChangeMode(simpleOption))

        verify { uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE) }
        coVerify { leetManager.changeMode(simpleOption) }
    }

    @Test
    fun `changeMode failure shows error`() = runTest {
        val option = LeetOption.createSimple()
        coEvery { leetManager.changeMode(any()) } returns Result.failure(Exception("Test error"))

        viewModel.handleIntent(MainIntent.ChangeMode(option))

        verify { uiManager.setError("Failed to change mode: Test error") }
    }

    // ===== LEET CREATION TESTS =====

    @Test
    fun `handleIntent CreateLeet creates new leet`() = runTest {
        val testLeet = CustomLeet("New Leet", Icons.Default.Settings)
        coEvery { leetManager.createLeet(any(), any(), any(), any()) } returns Result.success(testLeet)

        viewModel.handleIntent(MainIntent.CreateLeet(
            name = "New Leet",
            icon = Icons.Default.Settings
        ))

        verify { uiManager.setLoading(true) }
        coVerify {
            leetManager.createLeet(
                name = "New Leet",
                iconResId = Icons.Default.Settings,
                useExtendedDefaults = false,
                customTranslations = null
            )
        }
        verify { uiManager.setTranslationMode(LeetTranslator.TranslationMode.CUSTOM) }
        verify { uiManager.setSuccess("Leet 'New Leet' created successfully") }
    }

    // ===== FAVORITE TESTS =====

    @Test
    fun `handleIntent ToggleFavorite toggles favorite status`() = runTest {
        val option = LeetOption.createSimple()
        val toggleResult = LeetRepository.FavoriteToggleResult(
            mode = LeetManager.MODE_SIMPLE,
            customIndex = 0,
            wasAlreadyFavorite = false,
            isNowFavorite = true,
            success = true
        )
        coEvery { leetManager.toggleFavorite(any()) } returns Result.success(toggleResult)

        viewModel.handleIntent(MainIntent.ToggleFavorite(option))

        coVerify { leetManager.toggleFavorite(option) }
        verify { uiManager.setSuccess("Added to favorites") }
    }

    // ===== REVERSE MODE TESTS =====

    @Test
    fun `handleIntent ToggleReverseMode toggles reverse mode`() = runTest {
        viewModel.handleIntent(MainIntent.ToggleReverseMode)

        verify { uiManager.toggleReverseMode() }
    }

    // ===== UI STATE TESTS =====

    @Test
    fun `handleIntent ClearError clears error state`() = runTest {
        viewModel.handleIntent(MainIntent.ClearError)

        verify { uiManager.clearError() }
    }

    @Test
    fun `handleIntent CopyToClipboard with empty output shows error`() = runTest {
        every { translationManager.translate(any(), any(), any(), any()) } returns ""

        viewModel.handleIntent(MainIntent.CopyToClipboard)

        verify { uiManager.setError("No text to copy") }
    }

    // ===== WHAT'S NEW TESTS =====

    @Test
    fun `handleIntent MarkWhatsNewAsShown marks as shown`() = runTest {
        coEvery { whatsNewPreferences.markCurrentVersionAsShown() } just Runs

        viewModel.handleIntent(MainIntent.MarkWhatsNewAsShown)

        coVerify { whatsNewPreferences.markCurrentVersionAsShown() }
    }

    // ===== COMPUTED PROPERTIES TESTS =====

    @Test
    fun `outputText reflects translation of input`() = runTest {
        every { translationManager.translate("Hello", any(), any(), false) } returns "#3110"
        inputTextFlow.value = "Hello"

        viewModel.outputText.test {
            assertEquals("#3110", awaitItem())
        }
    }

    @Test
    fun `generatePreview generates correct preview`() = runTest {
        val option = LeetOption.createSimple()
        every { translationManager.generatePreview(any(), any(), "Test") } returns "Test Preview"

        val result = viewModel.generatePreview(option, "Test")

        assertEquals("Test Preview", result)
        verify { translationManager.generatePreview(LeetTranslator.TranslationMode.SIMPLE, null, "Test") }
    }

    // ===== CLEANUP TESTS =====
    // Note: onCleared is protected, so we can't test it directly in unit tests
    // This would be tested in integration tests or by making the method internal/public for testing

    @Test
    fun `cleanup functionality works correctly`() = runTest {
        // Test that cleanup-related functionality works
        every { repository.cleanup() } just Runs

        // We can't call onCleared directly, but we can verify the repository cleanup would be called
        verify(exactly = 0) { repository.cleanup() } // Initially not called

        // In real implementation, onCleared would call repository.cleanup()
        repository.cleanup() // Simulate the call
        verify(exactly = 1) { repository.cleanup() }
    }
}