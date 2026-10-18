package com.sorokinuladzimir.homebarassistant.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorokinuladzimir.homebarassistant.ai.AiSearchPlanner
import com.sorokinuladzimir.homebarassistant.net.ApiResult
import com.sorokinuladzimir.homebarassistant.net.SearchConditions
import com.sorokinuladzimir.homebarassistant.net.SearchRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AiSearchUiState {
    data object Idle : AiSearchUiState
    data object Loading : AiSearchUiState
    data class Success(val request: SearchRequest) : AiSearchUiState
    data class Error(val message: String) : AiSearchUiState
}

@HiltViewModel
class AiSearchViewModel @Inject constructor(
    private val planner: AiSearchPlanner
) : ViewModel() {

    private val _state = MutableLiveData<AiSearchUiState>(AiSearchUiState.Idle)
    val state: LiveData<AiSearchUiState> = _state

    fun plan(description: String, conditions: SearchConditions) {
        if (_state.value is AiSearchUiState.Loading) return
        _state.value = AiSearchUiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = planner.plan(description, conditions)) {
                is ApiResult.Success -> AiSearchUiState.Success(result.data)
                is ApiResult.Error -> AiSearchUiState.Error(result.message)
            }
        }
    }

    fun consume() {
        _state.value = AiSearchUiState.Idle
    }
}
