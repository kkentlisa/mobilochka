package com.example.mobilo4ka.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MainUiState(
    val isMenuExpanded : Boolean = false
)

class MainViewModel : ViewModel(){
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state

    fun toggleMenu() {
        _state.value = _state.value.copy(
            isMenuExpanded = !_state.value.isMenuExpanded
        )
    }
}