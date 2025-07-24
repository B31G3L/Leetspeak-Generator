package com.beigel.leetSpeak_Generator.domain.usecase.leet

import androidx.compose.ui.graphics.vector.ImageVector
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.LeetOption
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kombinierter Use Case für alle Leet-Management Operationen
 * Vereinfacht das ViewModel und bietet eine saubere API
 */
@Singleton
class LeetManagerUseCase @Inject constructor(
    private val getLeetOptionsUseCase: GetLeetOptionsUseCase,
    private val getFavoriteLeetOptionsUseCase: GetFavoriteLeetOptionsUseCase,
    private val createLeetUseCase: CreateLeetUseCase,
    private val updateLeetUseCase: UpdateLeetUseCase,
    private val deleteLeetUseCase: DeleteLeetUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val loadFavoriteLeetUseCase: LoadFavoriteLeetUseCase,
    private val setCurrentLeetIndexUseCase: SetCurrentLeetIndexUseCase
) {

    /**
     * Lädt alle verfügbaren Leet-Optionen
     */
    fun getLeetOptions(): Flow<List<LeetOption>> {
        return getLeetOptionsUseCase()
    }

    /**
     * Lädt nur die favorisierten Leet-Optionen
     */
    fun getFavoriteLeetOptions(): Flow<List<LeetOption>> {
        return getFavoriteLeetOptionsUseCase()
    }

    /**
     * Erstellt ein neues Leet
     */
    suspend fun createLeet(
        name: String,
        iconResId: ImageVector,
        useExtendedDefaults: Boolean = false
    ): Result<CustomLeet> {
        return createLeetUseCase(name, iconResId, useExtendedDefaults)
    }

    /**
     * Aktualisiert ein bestehendes Leet
     */
    suspend fun updateLeet(
        index: Int,
        leet: CustomLeet
    ): Result<LeetRepository.LeetUpdateResult> {
        return updateLeetUseCase(index, leet)
    }

    /**
     * Löscht ein Leet
     */
    suspend fun deleteLeet(
        index: Int
    ): Result<LeetRepository.LeetDeletionResult> {
        return deleteLeetUseCase(index)
    }

    /**
     * Schaltet Favoriten-Status um
     */
    suspend fun toggleFavorite(
        leetOption: LeetOption
    ): Result<LeetRepository.FavoriteToggleResult> {
        return toggleFavoriteUseCase(leetOption.mode, leetOption.customIndex)
    }

    /**
     * Lädt das Favoriten-Leet beim App-Start
     */
    suspend fun loadFavoriteLeet(): Result<LeetRepository.FavoriteLeetResult> {
        return loadFavoriteLeetUseCase()
    }

    /**
     * Ändert den aktuellen Leet-Modus
     */
    suspend fun changeMode(leetOption: LeetOption): Result<Unit> {
        return if (leetOption.isCustom && leetOption.customIndex >= 0) {
            setCurrentLeetIndexUseCase(leetOption.customIndex)
        } else {
            Result.success(Unit) // Für Built-in Modi ist kein Index nötig
        }
    }
}