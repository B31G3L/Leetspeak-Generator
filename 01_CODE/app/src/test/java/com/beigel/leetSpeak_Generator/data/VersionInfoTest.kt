package com.beigel.leetSpeak_Generator.data

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests für VersionInfo Data Class
 */
class VersionInfoTest {

    @Test
    fun `constructor sets correct values`() {
        val versionInfo = VersionInfo(42, "1.2.3")

        assertEquals(42, versionInfo.versionCode)
        assertEquals("1.2.3", versionInfo.versionName)
    }

    @Test
    fun `displayVersion formats correctly`() {
        val versionInfo = VersionInfo(100, "2.0.1-beta")

        assertEquals("Version 2.0.1-beta", versionInfo.displayVersion)
    }

    @Test
    fun `isNewerThan compares version codes correctly`() {
        val version1 = VersionInfo(10, "1.0.0")
        val version2 = VersionInfo(20, "2.0.0")
        val version3 = VersionInfo(15, "1.5.0")

        assertTrue(version2.isNewerThan(version1))
        assertFalse(version1.isNewerThan(version2))
        assertTrue(version3.isNewerThan(version1))
        assertFalse(version3.isNewerThan(version2))
    }

    @Test
    fun `isNewerThan with null returns true`() {
        val version = VersionInfo(1, "1.0.0")

        assertTrue(version.isNewerThan(null))
    }

    @Test
    fun `isNewerThan with same version code returns false`() {
        val version1 = VersionInfo(10, "1.0.0")
        val version2 = VersionInfo(10, "1.0.1") // Different name, same code

        assertFalse(version1.isNewerThan(version2))
        assertFalse(version2.isNewerThan(version1))
    }

    @Test
    fun `equals and hashCode work correctly`() {
        val version1 = VersionInfo(10, "1.0.0")
        val version2 = VersionInfo(10, "1.0.0")
        val version3 = VersionInfo(10, "1.0.1")
        val version4 = VersionInfo(11, "1.0.0")

        assertEquals(version1, version2)
        assertNotEquals(version1, version3) // Different name
        assertNotEquals(version1, version4) // Different code

        assertEquals(version1.hashCode(), version2.hashCode())
    }

    @Test
    fun `toString contains version information`() {
        val version = VersionInfo(42, "1.2.3-alpha")
        val toString = version.toString()

        assertTrue(toString.contains("42"))
        assertTrue(toString.contains("1.2.3-alpha"))
    }

    @Test
    fun `displayVersion handles empty version name`() {
        val version = VersionInfo(1, "")

        assertEquals("Version ", version.displayVersion)
    }

    @Test
    fun `displayVersion handles special characters`() {
        val version = VersionInfo(1, "1.0.0-beta.1+build.123")

        assertEquals("Version 1.0.0-beta.1+build.123", version.displayVersion)
    }

    @Test
    fun `version code can be zero`() {
        val version = VersionInfo(0, "0.0.1")

        assertEquals(0, version.versionCode)
        assertEquals("0.0.1", version.versionName)
    }

    @Test
    fun `version code can be negative`() {
        val version = VersionInfo(-1, "debug")

        assertEquals(-1, version.versionCode)
        assertEquals("debug", version.versionName)
    }

    @Test
    fun `comparison edge cases`() {
        val versionZero = VersionInfo(0, "0.0.0")
        val versionNegative = VersionInfo(-1, "debug")
        val versionLarge = VersionInfo(Int.MAX_VALUE, "max")

        assertTrue(versionZero.isNewerThan(versionNegative))
        assertFalse(versionNegative.isNewerThan(versionZero))
        assertTrue(versionLarge.isNewerThan(versionZero))
        assertTrue(versionLarge.isNewerThan(versionNegative))
    }
}