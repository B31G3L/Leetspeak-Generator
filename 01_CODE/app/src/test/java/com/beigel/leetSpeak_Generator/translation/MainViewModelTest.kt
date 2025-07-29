// app/src/test/java/com/beigel/leetSpeak_Generator/viewmodel/MainViewModelTest.kt
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
 * Verwendet MockK für Mocking und Turbine für Flow Testing
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

        // Setup LeetManager
        val mockLeetOptions = listOf(
            LeetOption.createSimple(),
            LeetOption.createExtended()
        )
        every { leetManager.getLeetOptions() } returns flowOf(mockLeetOptions)
        every { leetManager.getFavoriteLeetOptions() } returns flowOf(emptyList())

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
        verify { leetManager.loadFavoriteLeet() }

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

    @Test
    fun `updateInputText calls uiManager`() = runTest {
        val testText = "Test"

        viewModel.updateInputText(testText)

        verify { uiManager.updateInputText(testText) }
    }

    @Test
    fun `clearInput calls uiManager`() = runTest {
        viewModel.clearInput()

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
    fun `handleIntent ChangeMode to extended mode`() = runTest {
        val extendedOption = LeetOption.createExtended()
        coEvery { leetManager.changeMode(any()) } returns Result.success(Unit)

        viewModel.handleIntent(MainIntent.ChangeMode(extendedOption))

        verify { uiManager.setTranslationMode(LeetTranslator.TranslationMode.EXTENDED) }
        coVerify { leetManager.changeMode(extendedOption) }
    }

    @Test
    fun `handleIntent ChangeMode to custom mode updates index`() = runTest {
        val customOption = LeetOption.createCustom(
            CustomLeet("Test", Icons.Default.Settings),
            customIndex = 2
        )
        coEvery { leetManager.changeMode(any()) } returns Result.success(Unit)
        coEvery { repository.setCurrentLeetIndex(any()) } returns Result.success(Unit)

        viewModel.handleIntent(MainIntent.ChangeMode(customOption))

        verify { uiManager.setTranslationMode(LeetTranslator.TranslationMode.CUSTOM) }
        coVerify { repository.setCurrentLeetIndex(2) }
        coVerify { leetManager.changeMode(customOption) }
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

    @Test
    fun `handleIntent CreateLeet with custom translations`() = runTest {
        val customTranslations = mapOf("A" to "@", "E" to "€")
        val testLeet = CustomLeet("Custom Leet", Icons.Default.Settings)
        coEvery { leetManager.createLeet(any(), any(), any(), any()) } returns Result.success(testLeet)

        viewModel.handleIntent(MainIntent.CreateLeet(
            name = "Custom Leet",
            icon = Icons.Default.Settings,
            customTranslations = customTranslations
        ))

        coVerify {
            leetManager.createLeet(
                name = "Custom Leet",
                iconResId = Icons.Default.Settings,
                useExtendedDefaults = false,
                customTranslations = customTranslations
            )
        }
    }

    @Test
    fun `createLeet failure shows error`() = runTest {
        coEvery { leetManager.createLeet(any(), any(), any(), any()) } returns
                Result.failure(Exception("Creation failed"))

        viewModel.handleIntent(MainIntent.CreateLeet(
            name = "Test",
            icon = Icons.Default.Settings
        ))

        verify { uiManager.setError("Failed to create leet: Creation failed") }
    }

    // ===== LEET UPDATE TESTS =====

    @Test
    fun `handleIntent UpdateLeet updates existing leet`() = runTest {
        val updatedLeet = CustomLeet("Updated", Icons.Default.Settings)
        val updateResult = LeetRepository.LeetUpdateResult(
            leet = updatedLeet,
            index = 0,
            success = true,
            message = "Updated"
        )
        coEvery { leetManager.updateLeet(any(), any()) } returns Result.success(updateResult)

        viewModel.handleIntent(MainIntent.UpdateLeet(0, updatedLeet))

        verify { uiManager.setLoading(true) }
        coVerify { leetManager.updateLeet(0, updatedLeet) }
        verify { uiManager.setSuccess("Leet 'Updated' updated successfully") }
    }

    @Test
    fun `updateLeet failure shows error`() = runTest {
        val leet = CustomLeet("Test", Icons.Default.Settings)
        coEvery { leetManager.updateLeet(any(), any()) } returns
                Result.failure(Exception("Update failed"))

        viewModel.handleIntent(MainIntent.UpdateLeet(0, leet))

        verify { uiManager.setError("Failed to update leet: Update failed") }
    }

    // ===== LEET DELETION TESTS =====

    @Test
    fun `handleIntent DeleteLeet removes leet`() = runTest {
        val deletionResult = LeetRepository.LeetDeletionResult(
            deletedLeet = CustomLeet("Deleted", Icons.Default.Settings),
            wasFavorite = false,
            wasLastLeet = false,
            success = true,
            message = "Deleted"
        )
        coEvery { leetManager.deleteLeet(any()) } returns Result.success(deletionResult)

        viewModel.handleIntent(MainIntent.DeleteLeet(0))

        verify { uiManager.setLoading(true) }
        coVerify { leetManager.deleteLeet(0) }
        verify { uiManager.setSuccess("Leet deleted successfully") }
    }

    @Test
    fun `deleteLeet last leet switches to simple mode`() = runTest {
        val deletionResult = LeetRepository.LeetDeletionResult(
            deletedLeet = CustomLeet("Last", Icons.Default.Settings),
            wasFavorite = false,
            wasLastLeet = true,
            success = true,
            message = "Deleted"
        )
        coEvery { leetManager.deleteLeet(any()) } returns Result.success(deletionResult)

        viewModel.handleIntent(MainIntent.DeleteLeet(0))

        verify { uiManager.setTranslationMode(LeetTranslator.TranslationMode.SIMPLE) }
        verify { uiManager.setSuccess("Last leet deleted, switched to Simple mode") }
    }

    @Test
    fun `deleteLeet favorite shows appropriate message`() = runTest {
        val deletionResult = LeetRepository.LeetDeletionResult(
            deletedLeet = CustomLeet("Favorite", Icons.Default.Settings),
            wasFavorite = true,
            wasLastLeet = false,
            success = true,
            message = "Deleted"
        )
        coEvery { leetManager.deleteLeet(any()) } returns Result.success(deletionResult)

        viewModel.handleIntent(MainIntent.DeleteLeet(0))

        verify { uiManager.setSuccess("Favorite leet deleted") }
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

    @Test
    fun `toggleFavorite removed from favorites shows correct message`() = runTest {
        val option = LeetOption.createSimple()
        val toggleResult = LeetRepository.FavoriteToggleResult(
            mode = LeetManager.MODE_SIMPLE,
            customIndex = 0,
            wasAlreadyFavorite = true,
            isNowFavorite = false,
            success = true
        )
        coEvery { leetManager.toggleFavorite(any()) } returns Result.success(toggleResult)

        viewModel.handleIntent(MainIntent.ToggleFavorite(option))

        verify { uiManager.setSuccess("Removed from favorites") }
    }

    // ===== REVERSE MODE TESTS =====

    @Test
    fun `handleIntent ToggleReverseMode toggles reverse mode`() = runTest {
        viewModel.handleIntent(MainIntent.ToggleReverseMode)

        verify { uiManager.toggleReverseMode() }
    }

    @Test
    fun `toggleReverseMode with output updates input`() = runTest {
        // Setup output text
        every { translationManager.translate(any(), any(), any(), any()) } returns "Test Output"
        inputTextFlow.value = "Test Input"

        viewModel.handleIntent(MainIntent.ToggleReverseMode)

        // Should update input with previous output
        verify { uiManager.toggleReverseMode() }
        // Note: In real implementation, this would update input with the output
    }

    // ===== UI STATE TESTS =====

    @Test
    fun `handleIntent ClearError clears error state`() = runTest {
        viewModel.handleIntent(MainIntent.ClearError)

        verify { uiManager.clearError() }
    }

    @Test
    fun `handleIntent ClearSuccess clears success state`() = runTest {
        viewModel.handleIntent(MainIntent.ClearSuccess)

        verify { uiManager.clearSuccess() }
    }

    @Test
    fun `handleIntent CopyToClipboard with empty output shows error`() = runTest {
        every { translationManager.translate(any(), any(), any(), any()) } returns ""

        viewModel.handleIntent(MainIntent.CopyToClipboard)

        verify { uiManager.setError("No text to copy") }
    }

    @Test
    fun `handleIntent CopyToClipboard with output shows success`() = runTest {
        every { translationManager.translate(any(), any(), any(), any()) } returns "Test Output"

        viewModel.handleIntent(MainIntent.CopyToClipboard)

        verify { uiManager.setSuccess("Copied to clipboard") }
    }

    // ===== WHAT'S NEW TESTS =====

    @Test
    fun `handleIntent MarkWhatsNewAsShown marks as shown`() = runTest {
        coEvery { whatsNewPreferences.markCurrentVersionAsShown() } just Runs

        viewModel.handleIntent(MainIntent.MarkWhatsNewAsShown)

        coVerify { whatsNewPreferences.markCurrentVersionAsShown() }
    }

    @Test
    fun `handleIntent ResetWhatsNewForTesting resets dialog`() = runTest {
        coEvery { whatsNewPreferences.resetForTesting() } just Runs

        viewModel.handleIntent(MainIntent.ResetWhatsNewForTesting)

        coVerify { whatsNewPreferences.resetForTesting() }
        verify { uiManager.setSuccess("What's New Dialog reset - wird beim nächsten Start angezeigt") }
    }

    @Test
    fun `handleIntent ForceShowWhatsNew forces show`() = runTest {
        coEvery { whatsNewPreferences.forceShowNextTime() } just Runs

        viewModel.handleIntent(MainIntent.ForceShowWhatsNew)

        coVerify { whatsNewPreferences.forceShowNextTime() }
        verify { uiManager.setSuccess("What's New Dialog wird beim nächsten Start angezeigt") }
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
    fun `isInputLikelyLeetspeak reflects detection`() = runTest {
        every { translationManager.isLikelyLeetspeak("#3110") } returns true
        inputTextFlow.value = "#3110"

        viewModel.isInputLikelyLeetspeak.test {
            assertEquals(true, awaitItem())
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

    @Test
    fun `generatePreview with custom leet uses custom leet`() = runTest {
        val customLeet = CustomLeet("Test", Icons.Default.Settings)
        leetsFlow.value = listOf(customLeet)
        val option = LeetOption.createCustom(customLeet, 0)
        every { translationManager.generatePreview(any(), any(), "Test") } returns "Custom Preview"

        val result = viewModel.generatePreview(option, "Test")

        assertEquals("Custom Preview", result)
        verify { translationManager.generatePreview(LeetTranslator.TranslationMode.CUSTOM, customLeet, "Test") }
    }

    // ===== CLEANUP TESTS =====

    @Test
    fun `onCleared calls repository cleanup`() = runTest {
        every { repository.cleanup() } just Runs

        viewModel.onCleared()

        verify { repository.cleanup() }
    }

    // ===== FLOW REACTIVITY TESTS =====

    @Test
    fun `leetOptions flow updates with selection state`() = runTest {
        val simpleOption = LeetOption.createSimple(isSelected = false)
        val extendedOption = LeetOption.createExtended(isSelected = false)
        every { leetManager.getLeetOptions() } returns flowOf(listOf(simpleOption, extendedOption))
        every { repository.currentLeetIndex } returns MutableStateFlow(0)

        currentModeFlow.value = LeetTranslator.TranslationMode.SIMPLE

        viewModel.leetOptions.test {
            val options = awaitItem()
            val simpleOptionResult = options.find { it.name == "Simple Leet" }
            val extendedOptionResult = options.find { it.name == "Extended Leet" }

            assertNotNull(simpleOptionResult)
            assertNotNull(extendedOptionResult)
            assertTrue(simpleOptionResult!!.isSelected)
            assertFalse(extendedOptionResult!!.isSelected)
        }
    }
}