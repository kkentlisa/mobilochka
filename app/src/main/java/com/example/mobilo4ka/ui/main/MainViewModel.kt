package com.example.mobilo4ka.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    val isMenuOpen: Boolean = false
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state

    fun toggleMenu() {
        _state.update { it.copy(isMenuOpen = !it.isMenuOpen) }
    }
}