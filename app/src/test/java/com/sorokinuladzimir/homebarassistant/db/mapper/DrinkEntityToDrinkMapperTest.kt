package com.sorokinuladzimir.homebarassistant.db.mapper

import com.sorokinuladzimir.homebarassistant.Constants
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import com.sorokinuladzimir.homebarassistant.net.entity.Glass
import com.sorokinuladzimir.homebarassistant.net.entity.TasteDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DrinkEntityToDrinkMapperTest {

    private val mapper = DrinkEntityToDrinkMapper.getInstance()

    @Test
    fun `reverseMap copies simple fields`() {
        val entity = DrinkEntity(
            id = "123",
            name = "Mojito",
            description = "Rum, mint and lime",
            rating = 5,
            isAlcoholic = true,
            isCarbonated = true
        )

        val drink = mapper.reverseMap(entity)

        assertEquals("Mojito", drink.name)
        assertEquals("Rum, mint and lime", drink.description)
        assertEquals(5, drink.rating)
        assertEquals(true, drink.isAlcoholic)
        assertEquals(true, drink.isCarbonated)
    }

    @Test
    fun `reverseMap builds image url from id`() {
        val drink = mapper.reverseMap(DrinkEntity(id = "42"))

        assertEquals(Constants.Uri.ABSOLUT_DRINKS_IMAGE_ROOT + "42.png", drink.image)
    }

    @Test
    fun `reverseMap maps servedIn glass name`() {
        val drink = mapper.reverseMap(DrinkEntity(servedIn = Glass(text = "highball-glass")))

        assertEquals("highball-glass", drink.glass?.glassName)
    }

    @Test
    fun `reverseMap maps tastes`() {
        val drink = mapper.reverseMap(
            DrinkEntity(tastes = listOf(TasteDto(id = "t1", text = "sweet")))
        )

        assertEquals(1, drink.tastes?.size)
        assertEquals("sweet", drink.tastes?.get(0)?.text)
        assertEquals("t1", drink.tastes?.get(0)?.id)
    }

    @Test
    fun `reverseMap with no tastes yields empty list`() {
        val drink = mapper.reverseMap(DrinkEntity())

        assertEquals(0, drink.tastes?.size)
        assertNull(drink.glass?.glassName)
    }
}
