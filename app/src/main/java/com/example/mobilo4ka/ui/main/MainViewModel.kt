package com.example.mobilo4ka.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

enum class Language { RU, EN }

data class MainUiState(
    val isMenuOpen: Boolean = false,
    val currentLanguage: Language = Language.RU
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state

    fun toggleMenu() {
        _state.update { it.copy(isMenuOpen = !it.isMenuOpen) }
    }

    fun toggleLanguage() {
        _state.update {
            it.copy(
                currentLanguage = if (it.currentLanguage == Language.RU) Language.EN else Language.RU,
                isMenuOpen = false
            )
        }
    }
}