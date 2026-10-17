package com.sorokinuladzimir.homebarassistant.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MeasureParserTest {

    private val allowedUnits = listOf(
        " ", "ml", "cl", "oz", "part", "dash", "splash", "drop", "half", "quarter",
        "third", "cube", "leaf", "peel", "slice", "scoop", "sprig", "teaspoon",
        "twist", "wedge", "whole"
    )

    @Test
    fun `splits mixed fraction and ounce`() {
        val parsed = MeasureParser.parse("1 1/2 oz", allowedUnits)

        assertEquals("1.5", parsed.amount)
        assertEquals("oz", parsed.unit)
    }

    @Test
    fun `splits simple fraction`() {
        val parsed = MeasureParser.parse("1/2 oz", allowedUnits)

        assertEquals("0.5", parsed.amount)
        assertEquals("oz", parsed.unit)
    }

    @Test
    fun `splits decimal centilitres`() {
        val parsed = MeasureParser.parse("4.5 cl", allowedUnits)

        assertEquals("4.5", parsed.amount)
        assertEquals("cl", parsed.unit)
    }

    @Test
    fun `handles unicode fraction`() {
        val parsed = MeasureParser.parse("1½ oz", allowedUnits)

        assertEquals("1.5", parsed.amount)
        assertEquals("oz", parsed.unit)
    }

    @Test
    fun `converts shots to ounces`() {
        val parsed = MeasureParser.parse("2 shots", allowedUnits)

        assertEquals("3", parsed.amount)
        assertEquals("oz", parsed.unit)
    }

    @Test
    fun `converts cups to millilitres`() {
        val parsed = MeasureParser.parse("1 cup", allowedUnits)

        assertEquals("240", parsed.amount)
        assertEquals("ml", parsed.unit)
    }

    @Test
    fun `maps teaspoon synonyms`() {
        val parsed = MeasureParser.parse("2 tsp", allowedUnits)

        assertEquals("2", parsed.amount)
        assertEquals("teaspoon", parsed.unit)
    }

    @Test
    fun `ignores trailing descriptor after unit`() {
        val cup = MeasureParser.parse("1 cup fruit", allowedUnits)
        assertEquals("240", cup.amount)
        assertEquals("ml", cup.unit)

        val ounce = MeasureParser.parse("4 oz frozen", allowedUnits)
        assertEquals("4", ounce.amount)
        assertEquals("oz", ounce.unit)
    }

    @Test
    fun `unknown unit keeps original measure`() {
        val parsed = MeasureParser.parse("2 spoons", allowedUnits)

        assertEquals("2 spoons", parsed.amount)
        assertNull(parsed.unit)
    }

    @Test
    fun `blank measure yields nulls`() {
        val parsed = MeasureParser.parse("  ", allowedUnits)

        assertNull(parsed.amount)
        assertNull(parsed.unit)
    }

    @Test
    fun `unit not allowed by app falls back to original`() {
        val parsed = MeasureParser.parse("1 gallon", allowedUnits)

        assertEquals("1 gallon", parsed.amount)
        assertNull(parsed.unit)
    }
}
