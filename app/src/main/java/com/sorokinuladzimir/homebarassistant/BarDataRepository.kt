package com.sorokinuladzimir.homebarassistant

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.sorokinuladzimir.homebarassistant.core.util.MeasureParser
import com.sorokinuladzimir.homebarassistant.db.CocktailsDatabase
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.DrinkIngredientJoin
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import com.sorokinuladzimir.homebarassistant.db.mapper.DrinkEntityToDrinkMapper
import com.sorokinuladzimir.homebarassistant.net.ApiResult
import com.sorokinuladzimir.homebarassistant.net.CocktailDbApi
import com.sorokinuladzimir.homebarassistant.net.CocktailDbImages
import com.sorokinuladzimir.homebarassistant.net.NoConnectivityException
import com.sorokinuladzimir.homebarassistant.net.SearchConditions
import com.sorokinuladzimir.homebarassistant.net.entity.CocktailDbDrinkMapper
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import com.sorokinuladzimir.homebarassistant.net.entity.IngredientDto
import com.sorokinuladzimir.homebarassistant.ui.utils.ImageHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarDataRepository @Inject constructor(
    private val mDatabase: CocktailsDatabase,
    private val mApi: CocktailDbApi,
    @ApplicationContext private val context: Context,
    private val imageHandler: ImageHandler
) : BarData {

    private val mScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val mObservableRemoteDrinks = MutableLiveData<List<DrinkEntity>>()
    private val mObservableRemoteDrink = MutableLiveData<DrinkEntity?>()
    private val mObservableDrinkImagePath = MediatorLiveData<String?>()
    private val mObservableIngredientImagePath = MediatorLiveData<String?>()

    override fun getLocalDrinks(): LiveData<List<Drink>> =
        mDatabase.getDrinkDao().loadAllDrinks().asLiveData()

    override fun getDrink(drinkId: Long?): LiveData<Drink?> =
        mDatabase.getDrinkDao().loadDrinkById(drinkId).asLiveData()

    override fun getDrinksByName(searchQuery: String): LiveData<List<Drink>> =
        mDatabase.getDrinkDao().searchDrinksByName("%$searchQuery%").asLiveData()

    override fun getDrinksByIngredient(ingredientId: Long?): LiveData<List<Drink>> =
        mDatabase.getCocktailDao().getDrinksWithIngredient(ingredientId).asLiveData()

    override fun getIngredients(): LiveData<List<Ingredient>> =
        mDatabase.getIngredientDao().loadIngredients().asLiveData()

    override fun getIngredient(ingredientId: Long?): LiveData<Ingredient?> =
        mDatabase.getIngredientDao().loadIngredient(ingredientId).asLiveData()

    override fun getIngredientsByName(searchQuery: String): LiveData<List<Ingredient>> =
        mDatabase.getIngredientDao().searchIngredientsByName("%$searchQuery%").asLiveData()

    override fun getDrinkIngredients(drinkId: Long?): LiveData<List<WholeCocktail>> =
        mDatabase.getCocktailDao().findAllIngredientsByDrinkId(drinkId).asLiveData()

    override fun getListIngredientsNames(ingredientIds: List<Long>): LiveData<List<WholeCocktail>> =
        mDatabase.getIngredientDao().loadCocktailIngredients(ingredientIds).asLiveData()

    override fun saveDrink(drink: Drink, drinkIngredients: List<DrinkIngredientJoin>) {
        mScope.launch {
            drink.id?.let { mDatabase.getCocktailDao().deleteDrinkIngredientsById(it) }
            val drinkId = mDatabase.getDrinkDao().insertDrink(drink)
            drinkIngredients.forEach { it.drinkId = drinkId }
            mDatabase.getCocktailDao().insertDrinkIngredients(drinkIngredients)
        }
    }

    override fun deleteDrink(drinkId: Long?) {
        mScope.launch {
            drinkId?.let {
                mDatabase.getCocktailDao().deleteDrinkIngredientsById(it)
                mDatabase.getDrinkDao().deleteDrinkById(it)
            }
        }
    }

    override fun addIngredient(ingredient: Ingredient) {
        mScope.launch {
            mDatabase.getIngredientDao().insertOrReplaceIngredient(ingredient)
        }
    }

    override fun deleteIngredient(ingredientId: Long?): Int = runBlocking {
        if (ingredientId == null) {
            return@runBlocking 0
        }
        val count = mDatabase.getCocktailDao().countCocktailsWithIngridient(ingredientId)
        if (count <= 0) {
            mDatabase.getIngredientDao().deleteIngredientById(ingredientId)
        }
        count
    }

    override fun saveImageToAlbum(
        imageUri: Uri,
        sizeForScale: Int,
        deleteSource: Boolean,
        ingredientImage: Boolean
    ) {
        mScope.launch {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            try {
                val bitmap = imageHandler.getBitmapFromUri(context, imageUri, sizeForScale)
                if (deleteSource) imageHandler.deleteImage(context, imageUri)
                if (ingredientImage) {
                    mObservableIngredientImagePath.postValue(imageHandler.saveImage(context, bitmap))
                } else {
                    mObservableDrinkImagePath.postValue(imageHandler.saveImage(context, bitmap))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun createImageFile(): Uri? = imageHandler.createImageFile(context)

    override fun deleteImage(imagePath: String?) {
        mScope.launch {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            if (imagePath != null) {
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    val photoUri = FileProvider.getUriForFile(
                        context,
                        Constants.Strings.AUTHORITY,
                        imageFile
                    )
                    imageHandler.deleteImage(context, photoUri)
                }
            }
        }
    }

    override fun resetIngredientImagePath() {
        mObservableIngredientImagePath.value = null
    }

    override fun resetDrinkImagePath() {
        mObservableDrinkImagePath.value = null
    }

    override fun getObservableDrinkImagePath(): LiveData<String?> = mObservableDrinkImagePath

    override fun getObservableIngredientImagePath(): LiveData<String?> = mObservableIngredientImagePath

    override fun getRemoteDrinks(conditionsJson: String?) {
        mScope.launch {
            val conditions = SearchConditions.fromJson(conditionsJson)
            Log.d(TAG, "Remote search conditions: $conditions")
            when (val result = safeApiCall { requestDrinks(conditions) }) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Remote search returned ${result.data.size} drinks")
                    mObservableRemoteDrinks.postValue(result.data)
                }
                is ApiResult.Error -> {
                    Log.w(TAG, "Remote search failed: ${result.message}")
                    mObservableRemoteDrinks.postValue(emptyList())
                    showToast(result.message)
                }
            }
        }
    }

    private suspend fun requestDrinks(conditions: SearchConditions): List<DrinkEntity> {
        val response = when {
            !conditions.name.isNullOrBlank() -> mApi.searchByName(conditions.name)
            !conditions.ingredient.isNullOrBlank() -> mApi.filterByIngredient(conditions.ingredient)
            !conditions.glass.isNullOrBlank() -> mApi.filterByGlass(conditions.glass)
            !conditions.category.isNullOrBlank() -> mApi.filterByCategory(conditions.category)
            conditions.alcoholic != null ->
                mApi.filterByAlcoholic(if (conditions.alcoholic) "Alcoholic" else "Non_Alcoholic")
            else -> return emptyList()
        }
        return response.drinks.orEmpty().map(CocktailDbDrinkMapper::map)
    }

    fun getRemoteDrink(id: String): LiveData<DrinkEntity?> {
        mObservableRemoteDrink.value = null
        mScope.launch {
            when (val result = safeApiCall { mApi.lookup(id) }) {
                is ApiResult.Success -> mObservableRemoteDrink.postValue(
                    result.data.drinks?.firstOrNull()?.let(CocktailDbDrinkMapper::map)
                )
                is ApiResult.Error -> showToast(result.message)
            }
        }
        return mObservableRemoteDrink
    }

    fun getObservableRemoteDrinks(): LiveData<List<DrinkEntity>> = mObservableRemoteDrinks

    override fun saveRemoteDrink(drinkEntity: DrinkEntity, bitmap: Bitmap?) {
        mScope.launch {
            val drink = DrinkEntityToDrinkMapper.getInstance().reverseMap(drinkEntity)
            drink.image = if (bitmap != null) {
                try {
                    imageHandler.saveImage(context, bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                    drinkEntity.imageUrl
                }
            } else {
                drinkEntity.imageUrl
            }
            val drinkId = mDatabase.getDrinkDao().insertDrink(drink)
            mDatabase.getCocktailDao().insertDrinkIngredients(
                getDrinkIngredientsJoin(drinkEntity.ingredients.orEmpty(), drinkId)
            )
        }
    }

    private suspend fun getDrinkIngredientsJoin(
        ingredients: List<IngredientDto>,
        drinkId: Long
    ): List<DrinkIngredientJoin> {
        val allowedUnits = context.resources.getStringArray(R.array.ingredient_unit).toList()
        val joinList = ArrayList<DrinkIngredientJoin>()
        for (dto in ingredients) {
            val name = dto.name ?: continue
            val image = CocktailDbImages.ingredient(name)
            val parsedMeasure = MeasureParser.parse(dto.measure, allowedUnits)
            val drinkIngredientJoin = DrinkIngredientJoin()
            drinkIngredientJoin.drinkId = drinkId
            drinkIngredientJoin.amount = parsedMeasure.amount
            drinkIngredientJoin.unit = parsedMeasure.unit
            val dbIngredient = mDatabase.getIngredientDao().getIngredientByName(name)
            val ingredientId: Long? = if (dbIngredient != null) {
                if (dbIngredient.image.isNullOrEmpty()) {
                    dbIngredient.image = image
                    mDatabase.getIngredientDao().insertOrReplaceIngredient(dbIngredient)
                } else {
                    dbIngredient.id
                }
            } else {
                mDatabase.getIngredientDao()
                    .insertOrReplaceIngredient(
                        Ingredient().apply {
                            this.name = name
                            this.image = image
                        }
                    )
            }
            drinkIngredientJoin.ingredientId = ingredientId
            joinList.add(drinkIngredientJoin)
        }
        return joinList
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> =
        try {
            ApiResult.Success(block())
        } catch (e: NoConnectivityException) {
            ApiResult.Error("no internet", e)
        } catch (e: HttpException) {
            ApiResult.Error("server returned error", e)
        } catch (e: IOException) {
            ApiResult.Error("server error, try again later", e)
        }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private companion object {
        private const val TAG = "HomeBar-CocktailDB"
    }
}
