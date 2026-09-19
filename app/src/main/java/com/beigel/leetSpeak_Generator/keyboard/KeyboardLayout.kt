package com.beigel.leetSpeak_Generator.keyboard

import com.beigel.leetSpeak_Generator.R

/**
 * Buchstaben-Anordnung der Leet-Tastatur. Die Reihen enthalten bewusst nur
 * Buchstaben — Ziffern und Symbole liegen in [LeetKeyboardService] auf einer
 * eigenen Ebene und sind für alle Layouts identisch.
 *
 * Reihen dürfen unterschiedlich lang sein (z. B. AZERTY mit 10/10/6, Dvorak mit
 * 7/10/9). [maxRowLength] dient dem Service als gemeinsames Raster, damit die
 * Tasten aller Reihen gleich breit bleiben und kürzere Reihen mittig
 * eingerückt werden, statt sich über die volle Breite zu strecken.
 *
 * [key] wird persistiert und darf sich nicht mehr ändern, sonst fällt die
 * Tastatur bei bestehenden Installationen auf [DEFAULT] zurück.
 */
enum class KeyboardLayout(
    val key: String,
    val displayNameRes: Int,
    val descriptionRes: Int,
    val row1: String,
    val row2: String,
    val row3: String
) {
    QWERTZ(
        key = "qwertz",
        displayNameRes = R.string.keyboard_layout_qwertz,
        descriptionRes = R.string.keyboard_layout_qwertz_desc,
        row1 = "qwertzuiop",
        row2 = "asdfghjkl",
        row3 = "yxcvbnm"
    ),
    QWERTY(
        key = "qwerty",
        displayNameRes = R.string.keyboard_layout_qwerty,
        descriptionRes = R.string.keyboard_layout_qwerty_desc,
        row1 = "qwertyuiop",
        row2 = "asdfghjkl",
        row3 = "zxcvbnm"
    ),
    QWERTY_ES(
        key = "qwerty_es",
        displayNameRes = R.string.keyboard_layout_qwerty_es,
        descriptionRes = R.string.keyboard_layout_qwerty_es_desc,
        row1 = "qwertyuiop",
        row2 = "asdfghjklñ",
        row3 = "zxcvbnm"
    ),
    AZERTY(
        key = "azerty",
        displayNameRes = R.string.keyboard_layout_azerty,
        descriptionRes = R.string.keyboard_layout_azerty_desc,
        row1 = "azertyuiop",
        row2 = "qsdfghjklm",
        row3 = "wxcvbn"
    ),
    DVORAK(
        key = "dvorak",
        displayNameRes = R.string.keyboard_layout_dvorak,
        descriptionRes = R.string.keyboard_layout_dvorak_desc,
        row1 = "pyfgcrl",
        row2 = "aoeuidhtns",
        row3 = "qjkxbmwvz"
    ),
    COLEMAK(
        key = "colemak",
        displayNameRes = R.string.keyboard_layout_colemak,
        descriptionRes = R.string.keyboard_layout_colemak_desc,
        row1 = "qwfpgjluy",
        row2 = "arstdhneio",
        row3 = "zxcvbkm"
    );

    /** Breitestes Raster des Layouts — Grundlage für gleich breite Tasten. */
    val maxRowLength: Int
        get() = maxOf(row1.length, row2.length, row3.length)

    companion object {
        val DEFAULT = QWERTZ

        fun fromKey(key: String?): KeyboardLayout =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}
