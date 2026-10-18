package com.sorokinuladzimir.homebarassistant.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sorokinuladzimir.homebarassistant.BarData
import com.sorokinuladzimir.homebarassistant.db.entity.Drink
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DrinkListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val application = mockk<Application>(relaxed = true)
    private val repository = mockk<BarData>()
    private val observer = mockk<Observer<List<Drink>>>(relaxed = true)

    private val initial: MutableLiveData<List<Drink>> = MutableLiveData()

    private fun createViewModel(): DrinkListViewModel {
        every { repository.getLocalDrinks() } returns initial
        return DrinkListViewModel(application, repository)
    }

    @Test
    fun `emits local drinks from repository`() {
        val drink = Drink().apply { id = 1; name = "Mojito" }
        val viewModel = createViewModel()
        viewModel.getDrinks().observeForever(observer)

        initial.value = listOf(drink)

        assertEquals(listOf(drink), viewModel.getDrinks().value)
    }

    @Test
    fun `searchDrinks switches to search source`() {
        val searchResult = Drink().apply { id = 2; name = "Daiquiri" }
        val searchLive = MutableLiveData<List<Drink>>()
        every { repository.getDrinksByName("dai") } returns searchLive

        val viewModel = createViewModel()
        viewModel.getDrinks().observeForever(observer)

        viewModel.searchDrinks("dai")
        searchLive.value = listOf(searchResult)

        assertEquals(listOf(searchResult), viewModel.getDrinks().value)
    }

    @Test
    fun `restoreSources returns to local drinks`() {
        val local = Drink().apply { id = 1; name = "Mojito" }
        val searchLive = MutableLiveData<List<Drink>>()
        every { repository.getDrinksByName(any()) } returns searchLive

        val viewModel = createViewModel()
        viewModel.getDrinks().observeForever(observer)

        viewModel.searchDrinks("x")
        viewModel.restoreSources()
        initial.value = listOf(local)

        assertEquals(listOf(local), viewModel.getDrinks().value)
    }
}
