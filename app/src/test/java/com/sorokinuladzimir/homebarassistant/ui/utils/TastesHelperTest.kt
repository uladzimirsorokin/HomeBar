package com.sorokinuladzimir.homebarassistant.ui.utils

import com.sorokinuladzimir.homebarassistant.db.entity.Taste
import org.junit.Assert.assertEquals
import org.junit.Test

class TastesHelperTest {

    private val array = arrayOf("any", "sweet", "sour", "bitter")

    @Test
    fun `toTastesList maps indexes to names`() {
        val tastes = TastesHelper.toTastesList(listOf(1, 2), array)

        assertEquals(listOf("sweet", "sour"), tastes.map { it.text })
    }

    @Test
    fun `getAsIntegerList maps names back to indexes`() {
        val result = TastesHelper.getAsIntegerList(
            listOf(Taste("sour"), Taste("bitter")),
            array
        )

        assertEquals(listOf(2, 3), result)
    }

    @Test
    fun `getAsIntegerList ignores unknown names`() {
        val result = TastesHelper.getAsIntegerList(listOf(Taste("unknown")), array)

        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun `getAsIntegerList handles null`() {
        assertEquals(emptyList<Int>(), TastesHelper.getAsIntegerList(null, array))
    }

    @Test
    fun `tastesToString joins with comma`() {
        assertEquals("sweet, sour", TastesHelper.tastesToString(listOf(Taste("sweet"), Taste("sour"))))
        assertEquals("", TastesHelper.tastesToString(null))
    }
}
