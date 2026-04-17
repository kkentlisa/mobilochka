package com.example.mobilo4ka.ui.screens.neural

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilo4ka.R
import com.example.mobilo4ka.algorithms.neural.ModelLoader
import com.example.mobilo4ka.algorithms.neural.NeuralNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NeuralUIState(
    val cellStates: List<Boolean> = List(2500) { false },
    val resultText: String = "",
    val predictedDigit: Int = -1
)

class NeuralViewModel(context: Context) : ViewModel() {
    private val _state = MutableStateFlow(
        NeuralUIState(
            resultText = context.getString(R.string.draw_number)
        )
    )
    val state: StateFlow<NeuralUIState> = _state

    private var network: NeuralNetwork? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            network = ModelLoader.load(context)
        }
    }

    fun onCellsTouched(indices: List<Int>) {
        val newList = _state.value.cellStates.toMutableList()
        var changed = false
        indices.forEach { index ->
            if (index in newList.indices && !newList[index]) {
                newList[index] = true
                changed = true
            }
        }
        if (changed) {
            _state.value = _state.value.copy(cellStates = newList)
        }
    }

    fun clear(context: Context) {
        _state.value = _state.value.copy(
            cellStates = List(2500) { false },
            resultText = context.getString(R.string.draw_number),
            predictedDigit = -1
        )
    }

    fun recognize(context: Context) {
        val currentNetwork = network
        if (currentNetwork == null) return
        val currentCells = _state.value.cellStates
        val pointsCount = currentCells.count { it }
        if (pointsCount < 35) {
            _state.value = _state.value.copy(
                resultText = context.getString(R.string.draw_number)
            )
            return
        }

        val input = centerImage(currentCells, 50)
        val result = currentNetwork.recognize(input)
        val predictDigit = result.indices.maxByOrNull { result[it] } ?: -1

        _state.value = _state.value.copy(
            resultText = context.getString(R.string.rating, predictDigit),
            predictedDigit = predictDigit
        )
    }
}