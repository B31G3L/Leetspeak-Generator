package com.beigel.leetSpeak_Generator.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a custom leetspeak translation leet
 * FIXED: Icon storage changed from ImageVector to String for Gson compatibility
 */
data class CustomLeet(
    var name: String,
    var iconName: String = "Settings", // FIXED: Default-Wert sicherstellen
    private val _translations: MutableMap<String, String> = mutableMapOf()
) {

    init {
        // FIXED: Stelle sicher, dass iconName niemals null oder leer ist
        if (iconName.isBlank()) {
            iconName = "Settings"
        }
    }

    /**
     * FIXED: Transient computed property - wird nicht serialisiert
     * Wandelt iconName zur Laufzeit in ImageVector um
     */
    @delegate:Transient
    val iconImageVector: ImageVector by lazy {
        IconMapper.getIconByName(iconName)
    }

    /**
     * Immutable view of translations
     */
    val translations: Map<String, String>
        get() = _translations.toMap()

    /**
     * Sets a translation mapping from plain character to leet character
     */
    fun setTranslation(plainChar: String, leetChar: String) {
        _translations[plainChar] = leetChar
    }

    /**
     * Gets the leet translation for a plain character
     * Returns the original character if no translation exists
     */
    fun getTranslation(plainChar: String): String =
        _translations[plainChar] ?: plainChar

    /**
     * Sets multiple translations at once
     */
    fun setTranslations(newTranslations: Map<String, String>) {
        _translations.clear()
        _translations.putAll(newTranslations)
    }

    /**
     * Checks if a translation exists for the given character
     */
    fun hasTranslation(plainChar: String): Boolean =
        _translations.containsKey(plainChar)

    /**
     * Removes a translation
     */
    fun removeTranslation(plainChar: String) {
        _translations.remove(plainChar)
    }

    /**
     * Clears all translations
     */
    fun clearTranslations() {
        _translations.clear()
    }

    /**
     * Creates a copy of this leet with a new name
     */
    fun copy(newName: String = this.name): CustomLeet =
        CustomLeet(newName, iconName, _translations.toMutableMap())

    companion object {
        /**
         * Creates a CustomLeet with default Simple Leet translations
         * FIXED: Expliziter Default für iconName
         */
        fun createWithSimpleDefaults(name: String, iconName: String = "Settings"): CustomLeet {
            val leet = CustomLeet(name, iconName)

            // Initialize with Simple Leet mappings
            val simpleMap = mapOf(
                "A" to "4", "B" to "8", "C" to "C", "D" to "D", "E" to "3",
                "F" to "F", "G" to "6", "H" to "#", "I" to "1", "J" to "J",
                "K" to "K", "L" to "L", "M" to "M", "N" to "N", "O" to "0",
                "P" to "P", "Q" to "Q", "R" to "R", "S" to "5", "T" to "7",
                "U" to "U", "V" to "V", "W" to "W", "X" to "X", "Y" to "Y",
                "Z" to "2"
            )

            leet.setTranslations(simpleMap)
            return leet
        }

        /**
         * Creates a CustomLeet with Extended Leet translations
         * FIXED: Expliziter Default für iconName
         */
        fun createWithExtendedDefaults(name: String, iconName: String = "Settings"): CustomLeet {
            val leet = CustomLeet(name, iconName)

            // Initialize with Extended Leet mappings
            val extendedMap = mapOf(
                "A" to "4", "B" to "8", "C" to "(", "D" to "|)", "E" to "3",
                "F" to "|=", "G" to "6", "H" to "#", "I" to "!", "J" to "_|",
                "K" to "|<", "L" to "1", "M" to "/\\/\\", "N" to "|\\|", "O" to "0",
                "P" to "9", "Q" to "0_", "R" to "2", "S" to "5", "T" to "7",
                "U" to "|_|", "V" to "\\/", "W" to "\\/\\/", "X" to "><", "Y" to "`/",
                "Z" to "Z"
            )

            leet.setTranslations(extendedMap)
            return leet
        }
    }
}

/**
 * FIXED: Icon Mapper für Bidirektionale Konvertierung zwischen String und ImageVector
 * Zentralisiert alle Icon-Mappings für bessere Wartbarkeit
 */
object IconMapper {

    /**
     * Konvertiert String-Name zu ImageVector
     */
    fun getIconByName(name: String): ImageVector {
        return when (name) {
            // Basis Icons
            "Settings" -> Icons.Default.Settings
            "Build" -> Icons.Default.Build
            "Code" -> Icons.Default.Code
            "Computer" -> Icons.Default.Computer

            // Gaming & Entertainment
            "Games" -> Icons.Default.Games
            "SportsEsports" -> Icons.Default.SportsEsports
            "Gamepad" -> Icons.Default.Gamepad
            "Sports" -> Icons.Default.Sports
            "Movie" -> Icons.Default.Movie
            "Videocam" -> Icons.Default.Videocam
            "PhotoCamera" -> Icons.Default.PhotoCamera

            // Tech & Science
            "Science" -> Icons.Default.Science
            "Memory" -> Icons.Default.Memory
            "Storage" -> Icons.Default.Storage
            "Psychology" -> Icons.Default.Psychology
            "AutoAwesome" -> Icons.Default.AutoAwesome
            "Widgets" -> Icons.Default.Widgets
            "Extension" -> Icons.Default.Extension

            // Creative & Design
            "Palette" -> Icons.Default.Palette
            "Brush" -> Icons.Default.Brush
            "Create" -> Icons.Default.Create
            "Diamond" -> Icons.Default.Diamond
            "Star" -> Icons.Default.Star
            "Favorite" -> Icons.Default.Favorite

            // Professional
            "Work" -> Icons.Default.Work
            "Business" -> Icons.Default.Business
            "School" -> Icons.Default.School
            "Book" -> Icons.Default.Book
            "Assignment" -> Icons.AutoMirrored.Filled.Assignment

            // Lifestyle
            "Home" -> Icons.Default.Home
            "Flight" -> Icons.Default.Flight
            "Restaurant" -> Icons.Default.Restaurant
            "Nature" -> Icons.Default.Nature
            "Pets" -> Icons.Default.Pets
            "Face" -> Icons.Default.Face
            "Group" -> Icons.Default.Group

            // Audio & Music
            "Headphones" -> Icons.Default.Headphones
            "Mic" -> Icons.Default.Mic
            "MusicNote" -> Icons.Default.MusicNote
            "VolumeUp" -> Icons.AutoMirrored.Filled.VolumeUp

            // Security & Tools
            "Security" -> Icons.Default.Security
            "Shield" -> Icons.Default.Shield
            "Lock" -> Icons.Default.Lock
            "Key" -> Icons.Default.Key
            "VpnKey" -> Icons.Default.VpnKey

            // Special Effects
            "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
            "Bolt" -> Icons.Default.Bolt
            "FlashOn" -> Icons.Default.FlashOn
            "Celebration" -> Icons.Default.Celebration
            "EmojiEvents" -> Icons.Default.EmojiEvents

            // Cloud & Storage
            "Cloud" -> Icons.Default.Cloud
            "CloudDownload" -> Icons.Default.CloudDownload
            "CloudUpload" -> Icons.Default.CloudUpload
            "Backup" -> Icons.Default.Backup

            // Communication
            "Chat" -> Icons.AutoMirrored.Filled.Chat
            "Message" -> Icons.AutoMirrored.Filled.Message
            "Email" -> Icons.Default.Email
            "Phone" -> Icons.Default.Phone

            // Transport
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "Train" -> Icons.Default.Train
            "Motorcycle" -> Icons.Default.Motorcycle
            "RocketLaunch" -> Icons.Default.RocketLaunch

            // Fallback
            else -> Icons.Default.Settings
        }
    }

    /**
     * Konvertiert ImageVector zu String-Name
     * Wird für die Serialisierung benötigt
     */
    fun getNameByIcon(icon: ImageVector): String {
        return when (icon) {
            // Basis Icons
            Icons.Default.Settings -> "Settings"
            Icons.Default.Build -> "Build"
            Icons.Default.Code -> "Code"
            Icons.Default.Computer -> "Computer"

            // Gaming & Entertainment
            Icons.Default.Games -> "Games"
            Icons.Default.SportsEsports -> "SportsEsports"
            Icons.Default.Gamepad -> "Gamepad"
            Icons.Default.Sports -> "Sports"
            Icons.Default.Movie -> "Movie"
            Icons.Default.Videocam -> "Videocam"
            Icons.Default.PhotoCamera -> "PhotoCamera"

            // Tech & Science
            Icons.Default.Science -> "Science"
            Icons.Default.Memory -> "Memory"
            Icons.Default.Storage -> "Storage"
            Icons.Default.Psychology -> "Psychology"
            Icons.Default.AutoAwesome -> "AutoAwesome"
            Icons.Default.Widgets -> "Widgets"
            Icons.Default.Extension -> "Extension"

            // Creative & Design
            Icons.Default.Palette -> "Palette"
            Icons.Default.Brush -> "Brush"
            Icons.Default.Create -> "Create"
            Icons.Default.Diamond -> "Diamond"
            Icons.Default.Star -> "Star"
            Icons.Default.Favorite -> "Favorite"

            // Professional
            Icons.Default.Work -> "Work"
            Icons.Default.Business -> "Business"
            Icons.Default.School -> "School"
            Icons.Default.Book -> "Book"
            Icons.AutoMirrored.Filled.Assignment -> "Assignment"

            // Lifestyle
            Icons.Default.Home -> "Home"
            Icons.Default.Flight -> "Flight"
            Icons.Default.Restaurant -> "Restaurant"
            Icons.Default.Nature -> "Nature"
            Icons.Default.Pets -> "Pets"
            Icons.Default.Face -> "Face"
            Icons.Default.Group -> "Group"

            // Audio & Music
            Icons.Default.Headphones -> "Headphones"
            Icons.Default.Mic -> "Mic"
            Icons.Default.MusicNote -> "MusicNote"
            Icons.AutoMirrored.Filled.VolumeUp -> "VolumeUp"

            // Security & Tools
            Icons.Default.Security -> "Security"
            Icons.Default.Shield -> "Shield"
            Icons.Default.Lock -> "Lock"
            Icons.Default.Key -> "Key"
            Icons.Default.VpnKey -> "VpnKey"

            // Special Effects
            Icons.Default.LocalFireDepartment -> "LocalFireDepartment"
            Icons.Default.Bolt -> "Bolt"
            Icons.Default.FlashOn -> "FlashOn"
            Icons.Default.Celebration -> "Celebration"
            Icons.Default.EmojiEvents -> "EmojiEvents"

            // Cloud & Storage
            Icons.Default.Cloud -> "Cloud"
            Icons.Default.CloudDownload -> "CloudDownload"
            Icons.Default.CloudUpload -> "CloudUpload"
            Icons.Default.Backup -> "Backup"

            // Communication
            Icons.AutoMirrored.Filled.Chat -> "Chat"
            Icons.AutoMirrored.Filled.Message -> "Message"
            Icons.Default.Email -> "Email"
            Icons.Default.Phone -> "Phone"

            // Transport
            Icons.Default.DirectionsCar -> "DirectionsCar"
            Icons.Default.Train -> "Train"
            Icons.Default.Motorcycle -> "Motorcycle"
            Icons.Default.RocketLaunch -> "RocketLaunch"

            // Fallback
            else -> "Settings"
        }
    }

    /**
     * Liste aller verfügbaren Icon-Namen
     */
    fun getAllIconNames(): List<String> {
        return listOf(
            "Settings", "Build", "Code", "Computer",
            "Games", "SportsEsports", "Gamepad", "Sports", "Movie", "Videocam", "PhotoCamera",
            "Science", "Memory", "Storage", "Psychology", "AutoAwesome", "Widgets", "Extension",
            "Palette", "Brush", "Create", "Diamond", "Star", "Favorite",
            "Work", "Business", "School", "Book", "Assignment",
            "Home", "Flight", "Restaurant", "Nature", "Pets", "Face", "Group",
            "Headphones", "Mic", "MusicNote", "VolumeUp",
            "Security", "Shield", "Lock", "Key", "VpnKey",
            "LocalFireDepartment", "Bolt", "FlashOn", "Celebration", "EmojiEvents",
            "Cloud", "CloudDownload", "CloudUpload", "Backup",
            "Chat", "Message", "Email", "Phone",
            "DirectionsCar", "Train", "Motorcycle", "RocketLaunch"
        )
    }
}