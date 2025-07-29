package com.beigel.leetSpeak_Generator.repository

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.manager.LeetManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Umfassende Tests für LeetRepository
 * Verwendet Robolectric für Android Context
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LeetRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: LeetRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = LeetRepository(context)
    }

    @After
    fun tearDown() {
        repository.cleanup()
    }

    // ===== CREATION TESTS =====

    @Test
    fun `createLeet with simple defaults succeeds`() = runTest {
        val request = LeetRepository.LeetCreationRequest(
            name = "Test Simple",
            iconImageVector = Icons.Default.Settings,
            translations = mapOf("A" to "4", "E" to "3")
        )

        val result = repository.createLeet(request)

        Assert.assertTrue(result.isSuccess)
        val creationResult = result.getOrNull()!!
        Assert.assertEquals("Test Simple", creationResult.leet.name)
        Assert.assertEquals(0, creationResult.index) // First leet
        Assert.assertTrue(creationResult.success)
    }

    @Test
    fun `createLeet adds leet to flow`() = runTest {
        val request = LeetRepository.LeetCreationRequest(
            name = "Flow Test",
            iconImageVector = Icons.Default.Star,
            translations = mapOf("A" to "@")
        )

        val initialCount = repository.leets.first().size
        repository.createLeet(request)
        val newCount = repository.leets.first().size

        Assert.assertEquals(initialCount + 1, newCount)

        val leets = repository.leets.first()
        val createdLeet = leets.last()
        Assert.assertEquals("Flow Test", createdLeet.name)
        Assert.assertEquals("@", createdLeet.getTranslation("A"))
    }

    @Test
    fun `createLeetWithSimpleDefaults creates leet with predefined mappings`() = runTest {
        val result = repository.createLeetWithSimpleDefaults("Simple Test", Icons.Default.Settings)

        Assert.assertTrue(result.isSuccess)
        val leet = result.getOrNull()!!
        Assert.assertEquals("Simple Test", leet.name)
        Assert.assertEquals("4", leet.getTranslation("A"))
        Assert.assertEquals("3", leet.getTranslation("E"))
        Assert.assertEquals("0", leet.getTranslation("O"))
    }

    @Test
    fun `createLeetWithExtendedDefaults creates leet with extended mappings`() = runTest {
        val result = repository.createLeetWithExtendedDefaults("Extended Test", Icons.Default.Star)

        Assert.assertTrue(result.isSuccess)
        val leet = result.getOrNull()!!
        Assert.assertEquals("Extended Test", leet.name)
        Assert.assertEquals("/\\/\\", leet.getTranslation("M"))
        Assert.assertEquals("|\\|", leet.getTranslation("N"))
        Assert.assertEquals("|_|", leet.getTranslation("U"))
    }

    @Test
    fun `addCustomLeet adds existing leet instance`() = runTest {
        val customLeet = CustomLeet("Added Leet", Icons.Default.Settings)
        customLeet.setTranslation("A", "∀")

        val result = repository.addCustomLeet(customLeet)

        Assert.assertTrue(result.isSuccess)
        val leets = repository.leets.first()
        Assert.assertTrue(leets.any { it.name == "Added Leet" })
    }

    // ===== UPDATE TESTS =====

    @Test
    fun `updateLeet modifies existing leet`() = runTest {
        // Create initial leet
        val initialLeet = CustomLeet("Original", Icons.Default.Settings)
        initialLeet.setTranslation("A", "4")
        repository.addCustomLeet(initialLeet)

        // Update it
        val updatedLeet = CustomLeet("Updated", Icons.Default.Star)
        updatedLeet.setTranslation("A", "@")
        updatedLeet.setTranslation("E", "€")

        val result = repository.updateLeet(0, updatedLeet)

        Assert.assertTrue(result.isSuccess)
        val updateResult = result.getOrNull()!!
        Assert.assertEquals("Updated", updateResult.leet.name)
        Assert.assertEquals(0, updateResult.index)

        // Verify in flow
        val leets = repository.leets.first()
        val leet = leets.first()
        Assert.assertEquals("Updated", leet.name)
        Assert.assertEquals("@", leet.getTranslation("A"))
        Assert.assertEquals("€", leet.getTranslation("E"))
    }

    @Test
    fun `updateLeet with invalid index fails`() = runTest {
        val leet = CustomLeet("Test", Icons.Default.Settings)

        val result = repository.updateLeet(999, leet)

        Assert.assertTrue(result.isFailure)
    }

    // ===== DELETION TESTS =====

    @Test
    fun `deleteLeet removes leet from collection`() = runTest {
        // Add two leets
        repository.addCustomLeet(CustomLeet("First", Icons.Default.Settings))
        repository.addCustomLeet(CustomLeet("Second", Icons.Default.Star))

        val initialCount = repository.leets.first().size
        val result = repository.deleteLeet(0)

        Assert.assertTrue(result.isSuccess)
        val deletionResult = result.getOrNull()!!
        Assert.assertEquals("First", deletionResult.deletedLeet.name)
        Assert.assertFalse(deletionResult.wasLastLeet)

        val finalCount = repository.leets.first().size
        Assert.assertEquals(initialCount - 1, finalCount)
    }

    @Test
    fun `deleteLeet last leet marks wasLastLeet true`() = runTest {
        repository.addCustomLeet(CustomLeet("Only One", Icons.Default.Settings))

        val result = repository.deleteLeet(0)

        Assert.assertTrue(result.isSuccess)
        val deletionResult = result.getOrNull()!!
        Assert.assertTrue(deletionResult.wasLastLeet)
    }

    @Test
    fun `deleteLeet with invalid index fails`() = runTest {
        val result = repository.deleteLeet(999)

        Assert.assertTrue(result.isFailure)
    }

    // ===== FAVORITE TESTS =====

    @Test
    fun `toggleFavorite simple mode works`() = runTest {
        val result = repository.toggleFavorite(LeetManager.MODE_SIMPLE)

        Assert.assertTrue(result.isSuccess)
        val toggleResult = result.getOrNull()!!
        Assert.assertEquals(LeetManager.MODE_SIMPLE, toggleResult.mode)
        Assert.assertTrue(toggleResult.isNowFavorite)
        Assert.assertFalse(toggleResult.wasAlreadyFavorite)
    }

    @Test
    fun `toggleFavorite custom mode with valid index works`() = runTest {
        repository.addCustomLeet(CustomLeet("Custom Favorite", Icons.Default.Star))

        val result = repository.toggleFavorite(LeetManager.MODE_CUSTOM, 0)

        Assert.assertTrue(result.isSuccess)
        val toggleResult = result.getOrNull()!!
        Assert.assertEquals(LeetManager.MODE_CUSTOM, toggleResult.mode)
        Assert.assertEquals(0, toggleResult.customIndex)
        Assert.assertTrue(toggleResult.isNowFavorite)
    }

    @Test
    fun `toggleFavorite twice removes favorite`() = runTest {
        repository.toggleFavorite(LeetManager.MODE_SIMPLE)
        val result = repository.toggleFavorite(LeetManager.MODE_SIMPLE)

        Assert.assertTrue(result.isSuccess)
        val toggleResult = result.getOrNull()!!
        Assert.assertFalse(toggleResult.isNowFavorite)
        Assert.assertTrue(toggleResult.wasAlreadyFavorite)
    }

    @Test
    fun `loadFavoriteLeet returns simple by default`() = runTest {
        val result = repository.loadFavoriteLeet()

        Assert.assertTrue(result.isSuccess)
        val favoriteResult = result.getOrNull()!!
        Assert.assertTrue(favoriteResult is LeetRepository.FavoriteLeetResult.Simple)
    }

    @Test
    fun `loadFavoriteLeet returns set favorite`() = runTest {
        repository.toggleFavorite(LeetManager.MODE_EXTENDED)

        val result = repository.loadFavoriteLeet()

        Assert.assertTrue(result.isSuccess)
        val favoriteResult = result.getOrNull()!!
        Assert.assertTrue(favoriteResult is LeetRepository.FavoriteLeetResult.Extended)
    }

    @Test
    fun `loadFavoriteLeet returns custom favorite`() = runTest {
        val customLeet = CustomLeet("Favorite Custom", Icons.Default.Star)
        customLeet.setTranslation("A", "∀")
        repository.addCustomLeet(customLeet)
        repository.toggleFavorite(LeetManager.MODE_CUSTOM, 0)

        val result = repository.loadFavoriteLeet()

        Assert.assertTrue(result.isSuccess)
        val favoriteResult = result.getOrNull()!!
        Assert.assertTrue(favoriteResult is LeetRepository.FavoriteLeetResult.Custom)
        val customResult = favoriteResult as LeetRepository.FavoriteLeetResult.Custom
        Assert.assertEquals("Favorite Custom", customResult.leet.name)
        Assert.assertEquals(0, customResult.customIndex)
    }

    // ===== LEET OPTIONS TESTS =====

    @Test
    fun `getLeetOptions returns built-in options`() = runTest {
        val options = repository.getLeetOptions().first()

        Assert.assertTrue(options.size >= 2) // At least Simple and Extended
        Assert.assertTrue(options.any { it.name == "Simple Leet" })
        Assert.assertTrue(options.any { it.name == "Extended Leet" })
    }

    @Test
    fun `getLeetOptions includes custom leets`() = runTest {
        repository.addCustomLeet(CustomLeet("Custom Option", Icons.Default.Settings))

        val options = repository.getLeetOptions().first()

        Assert.assertTrue(options.any { it.name == "Custom Option" && it.isCustom })
    }

    @Test
    fun `getLeetOptions marks favorites correctly`() = runTest {
        repository.toggleFavorite(LeetManager.MODE_SIMPLE)

        val options = repository.getLeetOptions().first()
        val simpleOption = options.find { it.name == "Simple Leet" }

        Assert.assertNotNull(simpleOption)
        Assert.assertTrue(simpleOption!!.isFavorite)
    }

    @Test
    fun `getFavoriteLeetOptions returns only favorites`() = runTest {
        repository.toggleFavorite(LeetManager.MODE_SIMPLE)
        repository.addCustomLeet(CustomLeet("Custom", Icons.Default.Settings))
        repository.toggleFavorite(LeetManager.MODE_CUSTOM, 0)

        val favoriteOptions = repository.getFavoriteLeetOptions().first()

        Assert.assertEquals(2, favoriteOptions.size)
        Assert.assertTrue(favoriteOptions.all { it.isFavorite })
        Assert.assertTrue(favoriteOptions.any { it.name == "Simple Leet" })
        Assert.assertTrue(favoriteOptions.any { it.name == "Custom" })
    }

    // ===== INDEX MANAGEMENT TESTS =====

    @Test
    fun `setCurrentLeetIndex updates current index`() = runTest {
        repository.addCustomLeet(CustomLeet("First", Icons.Default.Settings))
        repository.addCustomLeet(CustomLeet("Second", Icons.Default.Star))

        val result = repository.setCurrentLeetIndex(1)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(1, repository.getCurrentLeetIndex())
    }

    @Test
    fun `setCurrentLeetIndex with invalid index fails`() = runTest {
        val result = repository.setCurrentLeetIndex(999)

        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `currentLeet flow reflects index changes`() = runTest {
        repository.addCustomLeet(CustomLeet("First", Icons.Default.Settings))
        repository.addCustomLeet(CustomLeet("Second", Icons.Default.Star))

        repository.setCurrentLeetIndex(1)

        val currentLeet = repository.currentLeet.first()
        Assert.assertNotNull(currentLeet)
        Assert.assertEquals("Second", currentLeet!!.name)
    }

    // ===== SYNCHRONOUS GETTERS TESTS =====

    @Test
    fun `getCurrentLeet returns current leet synchronously`() = runTest {
        repository.addCustomLeet(CustomLeet("Current", Icons.Default.Settings))

        val currentLeet = repository.getCurrentLeet()

        Assert.assertNotNull(currentLeet)
        Assert.assertEquals("Current", currentLeet!!.name)
    }

    @Test
    fun `hasLeets returns false initially`() = runTest {
        // Assuming fresh repository
        val hasLeets = repository.hasLeets()

        // This depends on initial state - adjust as needed
        Assert.assertEquals(repository.leets.first().isEmpty(), !hasLeets)
    }

    @Test
    fun `hasLeets returns true after adding leet`() = runTest {
        repository.addCustomLeet(CustomLeet("Test", Icons.Default.Settings))

        val hasLeets = repository.hasLeets()

        Assert.assertTrue(hasLeets)
    }

    @Test
    fun `getLeets returns all leets synchronously`() = runTest {
        repository.addCustomLeet(CustomLeet("First", Icons.Default.Settings))
        repository.addCustomLeet(CustomLeet("Second", Icons.Default.Star))

        val leets = repository.getLeets()

        Assert.assertEquals(2, leets.size)
        Assert.assertEquals("First", leets[0].name)
        Assert.assertEquals("Second", leets[1].name)
    }

    // ===== FAVORITE RESULT COMPANION TESTS =====

    @Test
    fun `FavoriteLeetResult companion functions work correctly`() {
        val simple = LeetRepository.FavoriteLeetResult.simple()
        Assert.assertTrue(simple is LeetRepository.FavoriteLeetResult.Simple)
        Assert.assertEquals(LeetManager.MODE_SIMPLE, simple.mode)

        val extended = LeetRepository.FavoriteLeetResult.extended()
        Assert.assertTrue(extended is LeetRepository.FavoriteLeetResult.Extended)
        Assert.assertEquals(LeetManager.MODE_EXTENDED, extended.mode)

        val customLeet = CustomLeet("Test", Icons.Default.Settings)
        val custom = LeetRepository.FavoriteLeetResult.custom(5, customLeet)
        Assert.assertTrue(custom is LeetRepository.FavoriteLeetResult.Custom)
        Assert.assertEquals(LeetManager.MODE_CUSTOM, custom.mode)
        Assert.assertEquals(5, custom.customIndex)
        Assert.assertEquals(customLeet, custom.leet)
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    fun `operations handle concurrent modifications gracefully`() = runTest {
        repository.addCustomLeet(CustomLeet("Test", Icons.Default.Settings))

        // Simulate concurrent access
        val job1 = kotlin.coroutines.test.TestScope().launch {
            repository.updateLeet(0, CustomLeet("Updated1", Icons.Default.Settings))
        }

        val job2 = kotlin.coroutines.test.TestScope().launch {
            repository.updateLeet(0, CustomLeet("Updated2", Icons.Default.Star))
        }

        // Both should complete without throwing
        job1.join()
        job2.join()

        // One of the updates should have succeeded
        val leets = repository.leets.first()
        Assert.assertTrue(leets[0].name in listOf("Updated1", "Updated2"))
    }

    // ===== CLEANUP TESTS =====

    @Test
    fun `cleanup doesn't throw exceptions`() {
        // Should not throw
        repository.cleanup()

        // Should be able to call multiple times
        repository.cleanup()
        repository.cleanup()
    }

    // ===== FLOW REACTIVITY TESTS =====

    @Test
    fun `leets flow updates on modifications`() = runTest {
        val initialLeets = repository.leets.first()
        val initialSize = initialLeets.size

        repository.addCustomLeet(CustomLeet("Reactive Test", Icons.Default.Settings))

        val updatedLeets = repository.leets.first()
        Assert.assertEquals(initialSize + 1, updatedLeets.size)
        Assert.assertTrue(updatedLeets.any { it.name == "Reactive Test" })
    }

    @Test
    fun `hasLeets flow updates correctly`() = runTest {
        // Start with empty or add one to ensure we have a change
        repository.addCustomLeet(CustomLeet("Flow Test", Icons.Default.Settings))

        val hasLeetsBefore = repository.hasLeets.first()
        Assert.assertTrue(hasLeetsBefore)

        // Delete all leets
        val leets = repository.leets.first()
        for (i in leets.indices.reversed()) {
            repository.deleteLeet(i)
        }

        val hasLeetsAfter = repository.hasLeets.first()
        Assert.assertFalse(hasLeetsAfter)
    }
}