package com.sorokinuladzimir.homebarassistant.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SharedViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val viewModel = SharedViewModel()

    @Test
    fun `selected ids are null initially`() {
        assertNull(viewModel.getSelectedIds().value)
    }

    @Test
    fun `selectIds publishes new selection`() {
        val observer = mockk<Observer<List<Long>>>(relaxed = true)
        viewModel.getSelectedIds().observeForever(observer)

        viewModel.selectIds(listOf(1L, 2L))

        assertEquals(listOf(1L, 2L), viewModel.getSelectedIds().value)
    }

    @Test
    fun `selectIds accepts null to clear selection`() {
        viewModel.selectIds(listOf(1L))
        viewModel.selectIds(null)

        assertNull(viewModel.getSelectedIds().value)
    }
}
