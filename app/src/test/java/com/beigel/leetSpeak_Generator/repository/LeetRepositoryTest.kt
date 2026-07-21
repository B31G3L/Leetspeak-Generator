package com.beigel.leetSpeak_Generator.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Testet LeetRepository gegen einen echten (Robolectric-)Android-Context.
 * SharedPreferences läuft dabei in-memory und wird pro Test frisch isoliert,
 * dadurch lässt sich hier auf Mocking von LeetManager verzichtet werden —
 * das Repository wird so getestet, wie es tatsächlich läuft.
 */
@RunWith(RobolectricTestRunner::class)
class LeetRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: LeetRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Jeder Test bekommt eine frische, leere "LeetSpeakProfiles"-Prefs-Datei,
        // damit Tests sich nicht gegenseitig beeinflussen.
        context.getSharedPreferences("LeetSpeakProfiles", Context.MODE_PRIVATE)
            .edit().clear().commit()
        repository = LeetRepository(context)

        // LeetManager lädt seinen Anfangszustand aus den SharedPreferences asynchron
        // auf einem echten Dispatchers.IO-Hintergrundthread (in init{}, nicht über den
        // Test-Dispatcher gesteuert). Ohne dieses "Warm-up" kann dieser Ladevorgang
        // NACH den Mutationen eines Tests (z.B. createLeet) fertig werden und den
        // In-Memory-Zustand mit dem (leeren) Prefs-Inhalt überschreiben — ein reales
        // Race. loadFavoriteLeet() wartet intern auf den Ladevorgang (ensureLoaded()),
        // ein einmaliger Blocking-Aufruf hier stellt sicher, dass er vor jedem Test
        // abgeschlossen ist.
        runBlocking { repository.loadFavoriteLeet() }
    }

    @Test
    fun `starts with no custom leets`() = runBlocking {
        assertThat(repository.getLeetsValue()).isEmpty()
        assertThat(repository.hasLeets()).isFalse()
    }

    @Test
    fun `createLeet adds a leet and updates the leets flow`() = runBlocking {
        val result = repository.createLeet(
            LeetRepository.LeetCreationRequest(name = "Gaming", translations = mapOf("A" to "4"))
        )

        assertThat(result.isSuccess).isTrue()
        val created = result.getOrThrow()
        assertThat(created.leet.name).isEqualTo("Gaming")
        assertThat(created.index).isEqualTo(0)

        assertThat(repository.getLeetsValue()).hasSize(1)
        assertThat(repository.getLeetsValue().first().name).isEqualTo("Gaming")
    }

    @Test
    fun `createLeet sets the new leet as current`() = runBlocking {
        repository.createLeet(LeetRepository.LeetCreationRequest("First", emptyMap()))
        repository.createLeet(LeetRepository.LeetCreationRequest("Second", emptyMap()))

        assertThat(repository.getCurrentLeetIndexValue()).isEqualTo(1)
        assertThat(repository.getCurrentLeetValue()?.name).isEqualTo("Second")
    }

    @Test
    fun `deleteLeet removes the leet and reports it was the last one`() = runBlocking {
        repository.createLeet(LeetRepository.LeetCreationRequest("Solo", emptyMap()))

        val result = repository.deleteLeet(0)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().wasLastLeet).isTrue()
        assertThat(repository.getLeetsValue()).isEmpty()
    }

    @Test
    fun `deleteLeet with invalid index fails`() = runBlocking {
        val result = repository.deleteLeet(0)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `toggleFavorite marks simple mode as favorite and back`() = runBlocking {
        assertThat(repository.isFavorite(LeetManager.MODE_SIMPLE)).isFalse()

        val toggleOn = repository.toggleFavorite(LeetManager.MODE_SIMPLE)
        assertThat(toggleOn.getOrThrow().isNowFavorite).isTrue()
        assertThat(repository.isFavorite(LeetManager.MODE_SIMPLE)).isTrue()

        val toggleOff = repository.toggleFavorite(LeetManager.MODE_SIMPLE)
        assertThat(toggleOff.getOrThrow().isNowFavorite).isFalse()
        assertThat(repository.isFavorite(LeetManager.MODE_SIMPLE)).isFalse()
    }

    @Test
    fun `toggleFavorite for a custom leet uses its index`() = runBlocking {
        repository.createLeet(LeetRepository.LeetCreationRequest("Custom", emptyMap()))

        repository.toggleFavorite(LeetManager.MODE_CUSTOM, customIndex = 0)

        assertThat(repository.isFavorite(LeetManager.MODE_CUSTOM, customIndex = 0)).isTrue()
    }

    @Test
    fun `setCurrentLeetIndex updates the current index`() = runBlocking {
        repository.createLeet(LeetRepository.LeetCreationRequest("First", emptyMap()))
        repository.createLeet(LeetRepository.LeetCreationRequest("Second", emptyMap()))

        val result = repository.setCurrentLeetIndex(0)

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.getCurrentLeetIndexValue()).isEqualTo(0)
    }

    @Test
    fun `setCurrentLeetIndex with out-of-range index fails`() = runBlocking {
        val result = repository.setCurrentLeetIndex(5)
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `getLeetOptions always includes Simple and Extended`() = runBlocking {
        val options = repository.getLeetOptions().first()

        assertThat(options.map { it.mode }).contains(LeetManager.MODE_SIMPLE)
        assertThat(options.map { it.mode }).contains(LeetManager.MODE_EXTENDED)
    }

    @Test
    fun `getLeetOptions includes newly created custom leets`() = runBlocking {
        repository.createLeet(LeetRepository.LeetCreationRequest("Gaming", emptyMap()))

        val options = repository.getLeetOptions().first()

        assertThat(options.any { it.isCustom && it.customIndex == 0 }).isTrue()
    }

    @Test
    fun `getFavoriteLeetOptions only returns favorited options`() = runBlocking {
        var favorites = repository.getFavoriteLeetOptions().first()
        assertThat(favorites).isEmpty()

        repository.toggleFavorite(LeetManager.MODE_SIMPLE)

        favorites = repository.getFavoriteLeetOptions().first()
        assertThat(favorites).hasSize(1)
        assertThat(favorites.first().mode).isEqualTo(LeetManager.MODE_SIMPLE)
    }

    @Test
    fun `loadFavoriteLeet returns Simple result when no favorite is set`() = runBlocking {
        val result = repository.loadFavoriteLeet()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isInstanceOf(LeetRepository.FavoriteLeetResult.Simple::class.java)
    }

    @Test
    fun `loadFavoriteLeet returns the custom leet when it's the favorite`() = runBlocking {
        repository.createLeet(LeetRepository.LeetCreationRequest("Gaming", emptyMap()))
        repository.toggleFavorite(LeetManager.MODE_CUSTOM, customIndex = 0)

        val result = repository.loadFavoriteLeet().getOrThrow()

        assertThat(result).isInstanceOf(LeetRepository.FavoriteLeetResult.Custom::class.java)
        assertThat((result as LeetRepository.FavoriteLeetResult.Custom).leet.name).isEqualTo("Gaming")
    }

    @Test
    fun `addCustomLeet stores a fully constructed CustomLeet as-is`() = runBlocking {
        val leet = CustomLeet("Prebuilt").apply { setTranslation("A", "@") }

        val result = repository.addCustomLeet(leet)

        assertThat(result.isSuccess).isTrue()
        assertThat(repository.getLeetsValue()).contains(leet)
    }
}
