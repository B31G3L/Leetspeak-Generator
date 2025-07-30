package com.beigel.leetSpeak_Generator.repository

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.test.core.app.ApplicationProvider
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
import org.robolectric.RobolectricTestRunner
import app.cash.turbine.test

/**
 * COMPLETED: Umfassende Tests für LeetRepository
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
        // Clear existing preferences
        context.getSharedPreferences("LeetSpeakProfiles", Context.MODE_PRIVATE)
            .edit().clear().commit()
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
        Assert.assertEquals("4", creationResult.leet.getTranslation("A"))
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
    fun `createLeet with empty translations succeeds`() = runTest {
        val request = LeetRepository.LeetCreationRequest(
            name = "Empty Translations",
            iconImageVector = Icons.Default.Settings,
            translations = emptyMap()
        )

        val result = repository.createLeet(request)

        Assert.assertTrue(result.isSuccess)
        val leet = result.getOrNull()!!.leet
        Assert.assertTrue(leet.translations.isEmpty())
    }

    // ===== UPDATE TESTS =====

    @Test
    fun `updateLeet modifies existing leet`() = runTest {
        // First create a leet
        val createRequest = LeetRepository.LeetCreationRequest(
            name = "Original",
            iconImageVector = Icons.Default.Settings,
            translations = mapOf("A" to "4")
        )
        repository.createLeet(createRequest)

        // Then update it
        val updatedLeet = CustomLeet("Updated Name", Icons.Default.Star)
        updatedLeet.setTranslation("A", "@")
        updatedLeet.setTranslation("E", "€")

        val result = repository.updateLeet(0, updatedLeet)

        Assert.assertTrue(result.isSuccess)
        val updateResult = result.getOrNull()!!
        Assert.assertEquals("Updated Name", updateResult.leet.name)
        Assert.assertEquals(0, updateResult.index)
        Assert.assertTrue(updateResult.success)

        // Verify the leet was actually updated in the repository
        val leets = repository.leets.first()
        Assert.assertEquals("Updated Name", leets[0].name)
        Assert.assertEquals("@", leets[0].getTranslation("A"))
        Assert.assertEquals("€", leets[0].getTranslation("E"))
    }

    @Test
    fun `updateLeet with invalid index fails`() = runTest {
        val leet = CustomLeet("Test", Icons.Default.Settings)

        val result = repository.updateLeet(999, leet)

        Assert.assertTrue(result.isFailure)
    }

    // ===== DELETION TESTS =====

    @Test
    fun `deleteLeet removes leet from repository`() = runTest {
        // Create two leets
        val request1 = LeetRepository.LeetCreationRequest(
            name = "First",
            iconImageVector = Icons.Default.Settings,
            translations = mapOf("A" to "4")
        )
        val request2 = LeetRepository.LeetCreationRequest(
            name = "Second",
            iconImageVector = Icons.Default.Star,
            translations = mapOf("E" to "3")
        )
        repository.createLeet(request1)
        repository.createLeet(request2)

        // Delete the first one
        val result = repository.deleteLeet(0)

        Assert.assertTrue(result.isSuccess)
        val deletionResult = result.getOrNull()!!
        Assert.assertEquals("First", deletionResult.deletedLeet.name)
        Assert.assertFalse(deletionResult.wasFavorite)
        Assert.assertFalse(deletionResult.wasLastLeet)
        Assert.assertTrue(deletionResult.success)

        // Verify only second leet remains
        val remainingLeets = repository.leets.first()
        Assert.assertEquals(1, remainingLeets.size)
        Assert.assertEquals("Second", remainingLeets[0].name)
    }

    @Test
    fun `deleteLeet of last leet works correctly`() = runTest {
        // Create and set as favorite
        val request = LeetRepository.LeetCreationRequest(
            name = "Only Leet",
            iconImageVector = Icons.Default.Settings,
            translations = mapOf("A" to "4")
        )
        repository.createLeet(request)
        repository.toggleFavorite(LeetManager.MODE_CUSTOM, 0)

        val result = repository.deleteLeet(0)

        Assert.assertTrue(result.isSuccess)
        val deletionResult = result.getOrNull()!!
        Assert.assertEquals("Only Leet", deletionResult.deletedLeet.name)
        Assert.assertTrue(deletionResult.wasFavorite)
        Assert.assertTrue(deletionResult.wasLastLeet)

        // Verify repository is now empty
        val leets = repository.leets.first()
        Assert.assertTrue(leets.isEmpty())
    }

    @Test
    fun `deleteLeet with invalid index fails`() = runTest {
        val result = repository.deleteLeet(999)

        Assert.assertTrue(result.isFailure)
    }

    // ===== FAVORITE MANAGEMENT TESTS =====

    @Test
    fun `toggleFavorite simple mode works`() = runTest {
        val result = repository.toggleFavorite(LeetManager.MODE_SIMPLE)

        Assert.assertTrue(result.isSuccess)
        val toggleResult = result.getOrNull()!!
        Assert.assertEquals(LeetManager.MODE_SIMPLE, toggleResult.mode)
        Assert.assertFalse(toggleResult.wasAlreadyFavorite)
        Assert.assertTrue(toggleResult.isNowFavorite)
        Assert.assertTrue(toggleResult.success)
    }

    @Test
    fun `toggleFavorite custom mode requires valid index`() = runTest {
        // Without any custom leets, this should fail
        val result = repository.toggleFavorite(LeetManager.MODE_CUSTOM, 0)

        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `toggleFavorite custom mode with valid leet works`() = runTest {
        // Create a custom leet first
        val request = LeetRepository.LeetCreationRequest(
            name = "Custom Favorite",
            iconImageVector = Icons.Default.Star,
            translations = mapOf("A" to "@")
        )
        repository.createLeet(request)

        val result = repository.toggleFavorite(LeetManager.MODE_CUSTOM, 0)

        Assert.assertTrue(result.isSuccess)
        val toggleResult = result.getOrNull()!!
        Assert.assertEquals(LeetManager.MODE_CUSTOM, toggleResult.mode)
        Assert.assertEquals(0, toggleResult.customIndex)
        Assert.assertTrue(toggleResult.isNowFavorite)
    }

    // ===== LOAD FAVORITE TESTS =====

    @Test
    fun `loadFavoriteLeet returns simple by default`() = runTest {
        val result = repository.loadFavoriteLeet()

        Assert.assertTrue(result.isSuccess)
        val favoriteResult = result.getOrNull()!!
        Assert.assertTrue(favoriteResult is LeetRepository.FavoriteLeetResult.Simple)
        Assert.assertEquals(LeetManager.MODE_SIMPLE, favoriteResult.mode)
    }

    @Test
    fun `loadFavoriteLeet returns extended when set`() = runTest {
        repository.toggleFavorite(LeetManager.MODE_EXTENDED)

        val result = repository.loadFavoriteLeet()

        Assert.assertTrue(result.isSuccess)
        val favoriteResult = result.getOrNull()!!
        Assert.assertTrue(favoriteResult is LeetRepository.FavoriteLeetResult.Extended)
        Assert.assertEquals(LeetManager.MODE_EXTENDED, favoriteResult.mode)
    }

    @Test
    fun `loadFavoriteLeet returns custom when set`() = runTest {
        // Create custom leet and set as favorite
        val request = LeetRepository.LeetCreationRequest(
            name = "Favorite Custom",
            iconImageVector = Icons.Default.Star,
            translations = mapOf("A" to "★")
        )
        repository.createLeet(request)
        repository.toggleFavorite(LeetManager.MODE_CUSTOM, 0)

        val result = repository.loadFavoriteLeet()

        Assert.assertTrue(result.isSuccess)
        val favoriteResult = result.getOrNull()!!
        Assert.assertTrue(favoriteResult is LeetRepository.FavoriteLeetResult.Custom)

        val customResult = favoriteResult as LeetRepository.FavoriteLeetResult.Custom
        Assert.assertEquals(LeetManager.MODE_CUSTOM, customResult.mode)
        Assert.assertEquals(0, customResult.customIndex)
        Assert.assertEquals("Favorite Custom", customResult.leet.name)
        Assert.assertEquals("★", customResult.leet.getTranslation("A"))
    }

    // ===== FACTORY METHODS TESTS =====

    @Test
    fun `createLeetWithSimpleDefaults creates and adds leet`() = runTest {
        val result = repository.createLeetWithSimpleDefaults("Simple Factory")

        Assert.assertTrue(result.isSuccess)
        val leet = result.getOrNull()!!
        Assert.assertEquals("Simple Factory", leet.name)
        Assert.assertEquals("4", leet.getTranslation("A"))
        Assert.assertEquals("3", leet.getTranslation("E"))

        // Verify it was added to repository
        val leets = repository.leets.first()
        Assert.assertEquals(1, leets.size)
        Assert.assertEquals("Simple Factory", leets[0].name)
    }

    @Test
    fun `createLeetWithExtendedDefaults creates and adds leet`() = runTest {
        val result = repository.createLeetWithExtendedDefaults("Extended Factory")

        Assert.assertTrue(result.isSuccess)
        val leet = result.getOrNull()!!
        Assert.assertEquals("Extended Factory", leet.name)
        Assert.assertEquals("/\\/\\", leet.getTranslation("M"))
        Assert.assertEquals("|\\|", leet.getTranslation("N"))

        // Verify it was added to repository
        val leets = repository.leets.first()
        Assert.assertEquals(1, leets.size)
        Assert.assertEquals("Extended Factory", leets[0].name)
    }

    @Test
    fun `addCustomLeet adds pre-configured leet`() = runTest {
        val customLeet = CustomLeet("Pre-configured", Icons.Default.Settings)
        customLeet.setTranslation("X", "×")
        customLeet.setTranslation("Y", "¥")

        val result = repository.addCustomLeet(customLeet)

        Assert.assertTrue(result.isSuccess)
        val addedLeet = result.getOrNull()!!
        Assert.assertEquals("Pre-configured", addedLeet.name)

        // Verify translations were preserved
        val leets = repository.leets.first()
        Assert.assertEquals("×", leets[0].getTranslation("X"))
        Assert.assertEquals("¥", leets[0].getTranslation("Y"))
    }

    // ===== LEET OPTIONS FLOW TESTS =====

    @Test
    fun `getLeetOptions returns built-in options initially`() = runTest {
        repository.getLeetOptions().test {
            val options = awaitItem()

            Assert.assertEquals(2, options.size) // Simple + Extended

            val simple = options.find { it.mode == LeetManager.MODE_SIMPLE }
            val extended = options.find { it.mode == LeetManager.MODE_EXTENDED }

            Assert.assertNotNull(simple)
            Assert.assertNotNull(extended)
            Assert.assertEquals("Simple Leet", simple!!.name)
            Assert.assertEquals("Extended Leet", extended!!.name)
        }
    }

    @Test
    fun `getLeetOptions includes custom leets`() = runTest {
        // Create a custom leet
        val request = LeetRepository.LeetCreationRequest(
            name = "Custom Option",
            iconImageVector = Icons.Default.Star,
            translations = mapOf("Z" to "Ω")
        )
        repository.createLeet(request)

        repository.getLeetOptions().test {
            val options = awaitItem()

            Assert.assertEquals(3, options.size) // Simple + Extended + Custom

            val custom = options.find { it.mode == LeetManager.MODE_CUSTOM }
            Assert.assertNotNull(custom)
            Assert.assertEquals("Custom Option", custom!!.name)
            Assert.assertTrue(custom.isCustom)
            Assert.assertEquals(0, custom.customIndex)
        }
    }

    @Test
    fun `getFavoriteLeetOptions returns only favorites`() = runTest {
        // Set extended as favorite
        repository.toggleFavorite(LeetManager.MODE_EXTENDED)

        repository.getFavoriteLeetOptions().test {
            val favoriteOptions = awaitItem()

            Assert.assertEquals(1, favoriteOptions.size)
            Assert.assertEquals(LeetManager.MODE_EXTENDED, favoriteOptions[0].mode)
            Assert.assertTrue(favoriteOptions[0].isFavorite)
        }
    }

    // ===== INDEX MANAGEMENT TESTS =====

    @Test
    fun `setCurrentLeetIndex updates current index`() = runTest {
        // Create multiple leets
        repository.createLeetWithSimpleDefaults("First")
        repository.createLeetWithSimpleDefaults("Second")

        val result = repository.setCurrentLeetIndex(1)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(1, repository.getCurrentLeetIndex())
    }

    @Test
    fun `setCurrentLeetIndex with invalid index fails`() = runTest {
        val result = repository.setCurrentLeetIndex(999)

        Assert.assertTrue(result.isFailure)
    }

    // ===== SYNCHRONOUS ACCESS TESTS =====

    @Test
    fun `getCurrentLeet returns current leet synchronously`() = runTest {
        repository.createLeetWithSimpleDefaults("Current")

        val currentLeet = repository.getCurrentLeet()

        Assert.assertNotNull(currentLeet)
        Assert.assertEquals("Current", currentLeet!!.name)
    }

    @Test
    fun `hasLeets returns correct status synchronously`() = runTest {
        Assert.assertFalse(repository.hasLeets())

        repository.createLeetWithSimpleDefaults("Test")

        Assert.assertTrue(repository.hasLeets())
    }

    @Test
    fun `getLeets returns current leet list synchronously`() = runTest {
        repository.createLeetWithSimpleDefaults("Sync Test")

        val leets = repository.getLeets()

        Assert.assertEquals(1, leets.size)
        Assert.assertEquals("Sync Test", leets[0].name)
    }

    // ===== PERSISTENCE TESTS =====

    @Test
    fun `repository state persists across instances`() = runTest {
        // Create and configure leets
        repository.createLeetWithSimpleDefaults("Persistent")
        repository.toggleFavorite(LeetManager.MODE_SIMPLE)

        // Create new repository instance
        val newRepository = LeetRepository(context)

        // Verify data persisted
        val leets = newRepository.leets.first()
        Assert.assertEquals(1, leets.size)
        Assert.assertEquals("Persistent", leets[0].name)

        val favoriteResult = newRepository.loadFavoriteLeet().getOrNull()!!
        Assert.assertTrue(favoriteResult is LeetRepository.FavoriteLeetResult.Simple)

        newRepository.cleanup()
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    fun `repository handles invalid operations gracefully`() = runTest {
        // Test various invalid operations
        Assert.assertTrue(repository.updateLeet(-1, CustomLeet("Invalid", Icons.Default.Settings)).isFailure)
        Assert.assertTrue(repository.deleteLeet(-1).isFailure)
        Assert.assertTrue(repository.setCurrentLeetIndex(-1).isFailure)
        Assert.assertTrue(repository.toggleFavorite(999).isFailure)
    }

    @Test
    fun `cleanup doesn't affect functionality`() = runTest {
        repository.createLeetWithSimpleDefaults("Before Cleanup")

        repository.cleanup()

        // Repository should still work after cleanup
        val result = repository.createLeetWithSimpleDefaults("After Cleanup")
        Assert.assertTrue(result.isSuccess)
    }
}