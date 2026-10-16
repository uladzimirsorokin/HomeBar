package com.sorokinuladzimir.homebarassistant

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Process
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.sorokinuladzimir.homebarassistant.db.CocktailsDatabase
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import com.sorokinuladzimir.homebarassistant.db.entity.DrinkIngredientJoin
import com.sorokinuladzimir.homebarassistant.db.entity.Ingredient
import com.sorokinuladzimir.homebarassistant.db.entity.WholeCocktail
import com.sorokinuladzimir.homebarassistant.db.mapper.DrinkEntityToDrinkMapper
import com.sorokinuladzimir.homebarassistant.net.AbsolutDrinksApi
import com.sorokinuladzimir.homebarassistant.net.AbsolutDrinksResult
import com.sorokinuladzimir.homebarassistant.net.NoConnectivityException
import com.sorokinuladzimir.homebarassistant.net.entity.DrinkEntity
import com.sorokinuladzimir.homebarassistant.net.entity.Preparation
import com.sorokinuladzimir.homebarassistant.net.entity.PreparationStep
import com.sorokinuladzimir.homebarassistant.ui.utils.ImageHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.NumberFormat
import java.util.ArrayList
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarDataRepository @Inject constructor(
    private val mDatabase: CocktailsDatabase,
    private val mApi: AbsolutDrinksApi,
    @ApplicationContext private val context: Context,
    private val imageHandler: ImageHandler
) : BarData {

    private val mScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val mObservableRemoteDrinks = MediatorLiveData<List<DrinkEntity>>()
    private val mRemoteDrinkIngredients = MediatorLiveData<List<WholeCocktail>>()
    private val mObservableDrinkImagePath = MediatorLiveData<String?>()
    private val mObservableIngredientImagePath = MediatorLiveData<String?>()

    private var mRemoteIngredients: ArrayList<Ingredient> = ArrayList()
    private var mTotalResult = 0
    private var mNextLink: String? = null
    private var mCurrentSearchType: QueryType? = null

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
                    mObservableIngredientImagePath.postValue(imageHandler.saveImage(bitmap))
                } else {
                    mObservableDrinkImagePath.postValue(imageHandler.saveImage(bitmap))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun createImageFile(): Uri = imageHandler.createImageFile(context)

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

    override fun getRemoteDrinks(
        requestConditions: String?,
        queryType: QueryType,
        clearList: Boolean
    ) {
        mScope.launch {
            if (clearList) clearRemoteDrinks()
            val start = mObservableRemoteDrinks.value?.size ?: 0
            val client = mApi
            val call = getDrinksCall(requestConditions, queryType, client, start)
            if (call != null) {
                if (!(queryType == QueryType.CURRENT && start >= mTotalResult)) {
                    loadDrinks(call)
                }
            } else {
                Toast.makeText(context, "something went wrong :(", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDrinksCall(
        requestConditions: String?,
        queryType: QueryType,
        client: AbsolutDrinksApi,
        start: Int
    ): Call<AbsolutDrinksResult>? {
        if (mCurrentSearchType == null && queryType != QueryType.CURRENT) {
            mCurrentSearchType = queryType
        }
        mCurrentSearchType ?: return null
        if (mCurrentSearchType != queryType && queryType != QueryType.CURRENT) {
            mCurrentSearchType = queryType
        }
        return when (mCurrentSearchType) {
            QueryType.SEARCH_BY_CONDITIONS ->
                client.getAllMatchedDrinks(requestConditions, start, Constants.Values.DEFAULT_ITEM_AMOUNT)
            QueryType.SEARCH_BY_NAME ->
                client.searchDrinks(requestConditions, start, Constants.Values.DEFAULT_ITEM_AMOUNT)
            else -> null
        }
    }

    private fun clearRemoteDrinks() {
        (mObservableRemoteDrinks.value as? MutableList<DrinkEntity>)?.clear()
    }

    fun getObservableRemoteDrinks(): LiveData<List<DrinkEntity>> = mObservableRemoteDrinks

    fun getRemoteDrinkIngredients(): LiveData<List<WholeCocktail>> = mRemoteDrinkIngredients

    override fun saveRemoteDrink(drinkEntity: DrinkEntity, bitmap: Bitmap?) {
        mScope.launch {
            val drink = DrinkEntityToDrinkMapper.getInstance().reverseMap(drinkEntity)
            try {
                drink.image = imageHandler.saveImage(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val drinkId = mDatabase.getDrinkDao().insertDrink(drink)
            val ingredients = ArrayList(mRemoteIngredients)
            val wholeCocktailList = mRemoteDrinkIngredients.value
            mDatabase.getCocktailDao().insertDrinkIngredients(
                getDrinkIngredientsJoin(ingredients, wholeCocktailList, drinkId)
            )
        }
    }

    private suspend fun getDrinkIngredientsJoin(
        ingredients: List<Ingredient>,
        wholeCocktailList: List<WholeCocktail>?,
        drinkId: Long
    ): List<DrinkIngredientJoin> {
        val joinList = ArrayList<DrinkIngredientJoin>()
        for (ingredient in ingredients) {
            val drinkIngredientJoin = DrinkIngredientJoin()
            drinkIngredientJoin.drinkId = drinkId
            val wholeCocktail = wholeCocktailList?.getOrNull(ingredients.indexOf(ingredient))
            if (wholeCocktail != null) {
                drinkIngredientJoin.amount = wholeCocktail.amount
                drinkIngredientJoin.unit = wholeCocktail.unit
            }
            val dbIngredient = mDatabase.getIngredientDao().getIngredientByName(ingredient.name)
            val ingredientId: Long?
            if (dbIngredient != null) {
                if (dbIngredient.notes.isNullOrEmpty()) {
                    dbIngredient.notes = ingredient.notes
                    ingredientId = mDatabase.getIngredientDao().insertOrReplaceIngredient(dbIngredient)
                } else {
                    ingredientId = dbIngredient.id
                }
            } else {
                ingredientId = mDatabase.getIngredientDao().insertOrReplaceIngredient(ingredient)
            }
            drinkIngredientJoin.ingredientId = ingredientId
            joinList.add(drinkIngredientJoin)
        }
        return joinList
    }

    override fun getRemoteDrinkIngredients(drinkId: String) {
        mScope.launch {
            (mRemoteDrinkIngredients.value as? MutableList<WholeCocktail>)?.clear()
            mRemoteIngredients.clear()
            val client = mApi
            val call = client.getPreparationSteps(drinkId)
            call.enqueue(object : Callback<Preparation> {
                override fun onResponse(call: Call<Preparation>, response: Response<Preparation>) {
                    if (response.isSuccessful) {
                        handlePreparation(response.body())
                    } else {
                        Toast.makeText(context, "server returned error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Preparation>, t: Throwable) {
                    if (t is NoConnectivityException) {
                        Toast.makeText(context, "no internet", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "server error, try again later", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun handlePreparation(preparation: Preparation?) {
        if (preparation != null) {
            val drinkIngredients = ArrayList<WholeCocktail>()
            mRemoteIngredients = ArrayList()
            for (step: PreparationStep in preparation.preparationSteps) {
                val wholeCocktail = WholeCocktail()
                val ingredient = Ingredient()
                if (step.amount != 0.0f || step.centilitresAmount != 0.0f) {
                    val map = matchAmountAndUnit(step.amount, step.centilitresAmount, step.unit)
                    wholeCocktail.amount = map[Constants.Extra.AMOUNT]
                    wholeCocktail.unit = map[Constants.Extra.UNIT]
                    wholeCocktail.ingredientName = step.ingredientName
                    wholeCocktail.image =
                        Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT + step.imagePath + "/" + step.imageName + ".png"
                    drinkIngredients.add(wholeCocktail)
                    ingredient.name = step.ingredientName
                    ingredient.notes = step.ingredientDescription
                    ingredient.image =
                        Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT + step.imagePath + "/" + step.imageName + ".png"
                    mRemoteIngredients.add(ingredient)
                } else if (step.imageName == "ice") {
                    wholeCocktail.ingredientName = step.ingredientName
                    wholeCocktail.image =
                        Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT + step.imagePath + "/" + step.imageName + ".png"
                    drinkIngredients.add(wholeCocktail)
                    ingredient.name = step.ingredientName
                    ingredient.image =
                        Constants.Uri.ABSOLUT_INGREDIENTS_IMAGE_ROOT + step.imagePath + "/" + step.imageName + ".png"
                    ingredient.notes = step.ingredientDescription
                    mRemoteIngredients.add(ingredient)
                }
            }
            mRemoteDrinkIngredients.postValue(drinkIngredients)
        }
    }

    private fun formatFloatNumber(number: Float): String {
        val formatter = NumberFormat.getInstance()
        formatter.maximumFractionDigits = 2
        return formatter.format(number.toDouble())
    }

    private fun matchAmountAndUnit(
        amount: Float,
        centilitresAmount: Float,
        unit: String?
    ): Map<String, String> {
        val result = HashMap<String, String>()
        if (amount != 0.0f && unit != null) {
            val unitPatterns = context.resources.getStringArray(R.array.ingredient_unit_ru)
            val unitMatches = context.resources.getStringArray(R.array.ingredient_unit_ru_matches)
            for (unitPattern in unitPatterns) {
                if (unit.lowercase().contains(unitPattern)) {
                    result[Constants.Extra.AMOUNT] = formatFloatNumber(amount)
                    result[Constants.Extra.UNIT] = unitMatches[unitPatterns.indexOf(unitPattern)]
                    return result
                }
            }
            result[Constants.Extra.AMOUNT] = formatFloatNumber(amount)
            result[Constants.Extra.UNIT] = ""
            return result
        } else if (centilitresAmount != 0.0f) {
            result[Constants.Extra.AMOUNT] = formatFloatNumber(centilitresAmount)
            result[Constants.Extra.UNIT] = "cl"
            return result
        }
        return result
    }

    private fun loadDrinks(call: Call<AbsolutDrinksResult>) {
        call.enqueue(object : Callback<AbsolutDrinksResult> {
            override fun onResponse(
                call: Call<AbsolutDrinksResult>,
                response: Response<AbsolutDrinksResult>
            ) {
                if (response.isSuccessful) {
                    setDrinksResult(response)
                } else {
                    Toast.makeText(context, "server returned error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AbsolutDrinksResult>, t: Throwable) {
                if (t is NoConnectivityException) {
                    Toast.makeText(context, "no internet", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "server error, try again later", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setDrinksResult(response: Response<AbsolutDrinksResult>) {
        val result = response.body() ?: return
        mTotalResult = result.totalResult
        val currentDrinksList = mObservableRemoteDrinks.value
        if (currentDrinksList != null && mNextLink != null && mNextLink != result.next) {
            (currentDrinksList as? MutableList<DrinkEntity>)?.addAll(result.result)
            mObservableRemoteDrinks.postValue(currentDrinksList)
        } else {
            mObservableRemoteDrinks.postValue(result.result)
        }
        mNextLink = result.next
    }

    enum class QueryType {
        SEARCH_BY_NAME, SEARCH_BY_CONDITIONS, CURRENT
    }
}
