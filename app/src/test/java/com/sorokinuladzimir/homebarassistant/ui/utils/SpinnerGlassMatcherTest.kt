package com.sorokinuladzimir.homebarassistant.ui.utils

import com.sorokinuladzimir.homebarassistant.db.entity.Glass
import org.junit.Assert.assertEquals
import org.junit.Test

class SpinnerGlassMatcherTest {

    private val glasses = arrayOf("Любой", "Highball", "Rocks")

    @Test
    fun `matches glass name to index`() {
        assertEquals(1, SpinnerGlassMatcher.matchGlass(glasses, Glass().apply { glassName = "Highball" }))
    }

    @Test
    fun `returns zero for null glass`() {
        assertEquals(0, SpinnerGlassMatcher.matchGlass(glasses, null))
    }

    @Test
    fun `returns minus one for unknown glass`() {
        assertEquals(-1, SpinnerGlassMatcher.matchGlass(glasses, Glass().apply { glassName = "Nope" }))
    }
}
