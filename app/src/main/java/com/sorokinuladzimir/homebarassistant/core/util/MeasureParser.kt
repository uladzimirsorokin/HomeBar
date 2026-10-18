package com.sorokinuladzimir.homebarassistant.core.util

import kotlin.math.roundToInt

/**
 * Splits a free-form TheCocktailDB measure (e.g. "1 1/2 oz", "4.5 cl", "2 shots")
 * into an amount and a unit from the app's allowed unit list.
 *
 * When the unit is not recognized the original measure is kept in the amount field
 * and the unit is left empty, so no data is lost.
 */
object MeasureParser {

    data class ParsedMeasure(val amount: String?, val unit: String?)

    private val quantityRegex = Regex(
        """^([0-9]+/[0-9]+|[0-9]+\s*[-–—]\s*[0-9]+|[0-9]+(?:[.,][0-9]+)?(?:\s+[0-9]+/[0-9]+)?)\s*(.*)$"""
    )

    private val fractions = mapOf(
        '½' to " 1/2", '⅓' to " 1/3", '⅔' to " 2/3",
        '¼' to " 1/4", '¾' to " 3/4", '⅛' to " 1/8"
    )

    private val aliases = mapOf(
        "ml" to "ml", "milliliter" to "ml", "milliliters" to "ml",
        "millilitre" to "ml", "millilitres" to "ml",
        "cl" to "cl", "centiliter" to "cl", "centiliters" to "cl",
        "centilitre" to "cl", "centilitres" to "cl",
        "oz" to "oz", "ounce" to "oz", "ounces" to "oz", "fl oz" to "oz", "fl. oz." to "oz",
        "part" to "part", "parts" to "part",
        "dash" to "dash", "dashes" to "dash",
        "splash" to "splash", "splashes" to "splash",
        "drop" to "drop", "drops" to "drop",
        "half" to "half", "quarter" to "quarter", "quarters" to "quarter",
        "third" to "third", "thirds" to "third",
        "cube" to "cube", "cubes" to "cube",
        "leaf" to "leaf", "leaves" to "leaf",
        "peel" to "peel", "peels" to "peel",
        "slice" to "slice", "slices" to "slice",
        "scoop" to "scoop", "scoops" to "scoop",
        "sprig" to "sprig", "sprigs" to "sprig",
        "teaspoon" to "teaspoon", "teaspoons" to "teaspoon", "tsp" to "teaspoon", "tsps" to "teaspoon",
        "twist" to "twist", "twists" to "twist",
        "wedge" to "wedge", "wedges" to "wedge",
        "whole" to "whole"
    )

    private data class UnitMapping(val unit: String, val factor: Double = 1.0)

    private fun mappingFor(unitText: String): UnitMapping? {
        val cleaned = unitText.lowercase().trim().trimEnd('.').replace(Regex("\\s+"), " ")
        if (cleaned.isBlank()) return null
        directMapping(cleaned)?.let { return it }
        // Measure often carries a descriptor after the unit ("4 oz frozen", "1 cup fruit").
        // Match the leading recognized unit and ignore the trailing words.
        val tokens = cleaned.split(" ")
        if (tokens.size >= 2) {
            directMapping(tokens.take(2).joinToString(" "))?.let { return it }
        }
        return directMapping(tokens.first())
    }

    private fun directMapping(unit: String): UnitMapping? = when (unit) {
        "shot", "shots" -> UnitMapping("oz", 1.5)
        "cup", "cups" -> UnitMapping("ml", 240.0)
        "tablespoon", "tablespoons", "tbsp" -> UnitMapping("ml", 15.0)
        "pint", "pints" -> UnitMapping("ml", 473.0)
        "quart", "quarts" -> UnitMapping("ml", 946.0)
        else -> aliases[unit]?.let { UnitMapping(it) }
    }

    fun parse(measure: String?, allowedUnits: List<String>): ParsedMeasure {
        if (measure.isNullOrBlank()) return ParsedMeasure(null, null)
        val original = measure.trim()
        val normalized = normalizeFractions(original)
        val match = quantityRegex.find(normalized)
            ?: return ParsedMeasure(original, null)

        val amountRaw = match.groupValues[1]
        val unitRaw = match.groupValues[2].trim()
        val mapping = mappingFor(unitRaw)
        val allowed = allowedUnits.map { it.trim().lowercase() }.filter { it.isNotEmpty() }

        if (mapping == null || mapping.unit !in allowed) {
            return ParsedMeasure(original, null)
        }
        val amount = scaleAmount(normalizeAmount(amountRaw), mapping.factor)
            ?: return ParsedMeasure(original, null)
        return ParsedMeasure(amount, mapping.unit)
    }

    private fun normalizeFractions(value: String): String {
        var result = value
        fractions.forEach { (symbol, replacement) ->
            result = result.replace(symbol.toString(), replacement)
        }
        return result.replace(Regex("\\s+"), " ").trim()
    }

    private fun normalizeAmount(raw: String): String {
        val cleaned = raw.replace(Regex("\\s+"), " ").trim()
        if (cleaned.contains('-') || cleaned.contains('–')) return cleaned
        val parts = cleaned.split(" ")
        return when {
            parts.size == 2 && parts[1].contains('/') -> {
                val whole = parts[0].toDoubleOrNull() ?: return cleaned
                val fraction = parseFraction(parts[1]) ?: return cleaned
                format(whole + fraction)
            }
            cleaned.contains('/') -> parseFraction(cleaned)?.let(::format) ?: cleaned
            else -> cleaned
        }
    }

    private fun parseFraction(value: String): Double? {
        val parts = value.split("/")
        if (parts.size != 2) return null
        val numerator = parts[0].trim().toDoubleOrNull() ?: return null
        val denominator = parts[1].trim().toDoubleOrNull() ?: return null
        if (denominator == 0.0) return null
        return numerator / denominator
    }

    private fun scaleAmount(amount: String, factor: Double): String? {
        if (factor == 1.0) return amount
        val base = amount.replace(',', '.').toDoubleOrNull() ?: return null
        return format(base * factor)
    }

    private fun format(value: Double): String {
        val rounded = (value * 100).roundToInt() / 100.0
        return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
    }
}
