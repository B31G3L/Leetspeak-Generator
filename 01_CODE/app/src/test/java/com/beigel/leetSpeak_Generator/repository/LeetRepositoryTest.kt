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

    // Weitere Tests hier...
    // (Die anderen Tests bleiben unverändert)
}