package com.sorokinuladzimir.homebarassistant

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.DrinkIngredientJoin
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity

interface BarData {

    fun getLocalDrinks(): LiveData<List<Drink>>

    fun getRemoteDrinks(
        requestConditions: String?,
        queryType: BarDataRepository.QueryType,
        clearList: Boolean
    )

    fun getDrink(drinkId: Long?): LiveData<Drink?>

    fun getDrinksByName(searchQuery: String): LiveData<List<Drink>>

    fun getDrinksByIngredient(ingredientId: Long?): LiveData<List<Drink>>

    fun saveRemoteDrink(drinkEntity: DrinkEntity, bitmap: Bitmap?)

    fun saveDrink(drink: Drink, drinkIngredients: List<DrinkIngredientJoin>)

    fun deleteDrink(drinkId: Long?)

    fun getIngredients(): LiveData<List<Ingredient>>

    fun getIngredient(ingredientId: Long?): LiveData<Ingredient?>

    fun getIngredientsByName(searchQuery: String): LiveData<List<Ingredient>>

    fun deleteIngredient(ingredientId: Long?): Int

    fun getDrinkIngredients(drinkId: Long?): LiveData<List<WholeCocktail>>

    fun getRemoteDrinkIngredients(drinkId: String)

    fun addIngredient(ingredient: Ingredient)

    fun getListIngredientsNames(ingredientIds: List<Long>): LiveData<List<WholeCocktail>>

    fun saveImageToAlbum(
        imageUri: Uri,
        sizeForScale: Int,
        deleteSource: Boolean,
        ingredientImage: Boolean
    )

    fun createImageFile(): Uri

    fun deleteImage(imagePath: String?)

    fun resetIngredientImagePath()

    fun resetDrinkImagePath()

    fun getObservableDrinkImagePath(): LiveData<String?>

    fun getObservableIngredientImagePath(): LiveData<String?>
}
