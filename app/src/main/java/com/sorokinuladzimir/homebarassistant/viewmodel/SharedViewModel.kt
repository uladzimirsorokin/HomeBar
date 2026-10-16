package com.sorokinuladzimir.homebarassistant.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {

    private val selectedIds = MediatorLiveData<List<Long>>()

    fun getSelectedIds(): LiveData<List<Long>> = selectedIds

    fun selectIds(ingredients: List<Long>?) {
        selectedIds.value = ingredients
    }
}
