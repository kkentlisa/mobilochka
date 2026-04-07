package com.example.mobilo4ka.ui.screens.neural

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilo4ka.algorithms.neural.MnistLoader
import com.example.mobilo4ka.algorithms.neural.NeuralNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class TrainingViewModel : ViewModel() {
    fun startTraining(context: Context, network: NeuralNetwork) {
        viewModelScope.launch(Dispatchers.Default) {
            val images = MnistLoader.loadImages(context, "train-images.idx3-ubyte")
            val labels = MnistLoader.loadLabels(context, "train-labels.idx1-ubyte")

            val learningRate = 0.005f
            val epochs = 12

            repeat(epochs) { epoch ->
                var correctPredictions = 0
                for (i in images.indices) {
                    val input = images[i]
                    val target = labels[i]

                    val prediction = network.recognize(input)
                    val predictedDigit = prediction.indices.maxByOrNull { prediction[it] } ?: -1

                    if (predictedDigit == target)
                        correctPredictions++

                    network.train(input, target, learningRate)

                    if (i % 10000 == 0 && i > 0) {
                        val currentAcc = (correctPredictions.toFloat() / i) * 100
                        println("Эпоха: $epoch | Обработано: $i/60000 | Текущая точность: ${String.format("%.2f", currentAcc)}%")
                    }
                }
                val finalEpochAcc = (correctPredictions.toFloat() / images.size) * 100
                println("Эпоха $epoch завершена. Точность: ${String.format("%.2f", finalEpochAcc)}%")
            }
            println("Обучение завершено")
            saveWeightsToJson(context, network)
        }
    }

    private fun saveWeightsToJson(context: Context, network: NeuralNetwork) {
        try {
            val root = JSONObject()

            fun array1DToJson(array: FloatArray) = JSONArray().apply {
                array.forEach { put(it.toDouble())}
            }

            fun array2DToJson(array: Array<FloatArray>) = JSONArray().apply {
                array.forEach { row -> put(array1DToJson(row))}
            }

            root.put("fc1.weight", array2DToJson(network.layer1.weights))
            root.put("fc1.bias", array1DToJson(network.layer1.bias))

            root.put("fc2.weight", array2DToJson(network.layer2.weights))
            root.put("fc2.bias", array1DToJson(network.layer2.bias))

            root.put("fc3.weight", array2DToJson(network.layer3.weights))
            root.put("fc3.bias", array1DToJson(network.layer3.bias))

            val fileName = "weights.json"
            val file = File(context.filesDir, fileName)

            file.writeText(root.toString())
            println("Веса сохранены")
        } catch (e: Exception) {
            println("Ошибка при сохранении: ${e.message}")
            e.printStackTrace()
        }
    }
}