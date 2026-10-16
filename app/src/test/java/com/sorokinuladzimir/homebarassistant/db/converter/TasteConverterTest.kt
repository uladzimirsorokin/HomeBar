package com.sorokinuladzimir.homebarassistant.db.converter

import com.sorokinuladzimir.homebarassistant.db.entity.Taste
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TasteConverterTest {

    @Test
    fun `fromList serializes and toList deserializes round trip`() {
        val tastes = arrayListOf(Taste("sweet").apply { id = "1" }, Taste("sour"))

        val json = TasteConverter.fromList(tastes)
        val restored = TasteConverter.toList(json)

        assertEquals("sweet", restored!![0].text)
        assertEquals("1", restored[0].id)
        assertEquals("sour", restored[1].text)
    }

    @Test
    fun `null list yields null string`() {
        assertNull(TasteConverter.fromList(null))
    }

    @Test
    fun `null string yields null list`() {
        assertNull(TasteConverter.toList(null))
    }
}
