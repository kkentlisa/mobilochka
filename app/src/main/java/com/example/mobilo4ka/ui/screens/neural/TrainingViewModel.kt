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
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class TrainingViewModel : ViewModel() {
    fun startTraining(context: Context, network: NeuralNetwork) {
        viewModelScope.launch(Dispatchers.Default) {
            val trainImages = MnistLoader.loadImages(context, "train-images.idx3-ubyte")
            val trainLabels = MnistLoader.loadLabels(context, "train-labels.idx1-ubyte")
            val testImages = MnistLoader.loadImages(context, "t10k-images.idx3-ubyte")
            val testLabels = MnistLoader.loadLabels(context, "t10k-labels.idx1-ubyte")

            var learningRate = 0.005f
            var bestAccuracy = 0f
            var badEpochsCount = 0
            val maxBadEpochs = 3

            val indices = trainImages.indices.toMutableList()

            repeat(50) { epoch ->
                indices.shuffle()
                var trainCorrect = 0

                for ((counter, i) in indices.withIndex()) {
                    var input = trainImages[i]
                    val target = trainLabels[i]

                    val angle = (-15..15).random().toFloat()
                    val shiftX = (-3..3).random().toFloat()
                    val shiftY = (-3..3).random().toFloat()
                    val zoom = 0.9f + (Math.random().toFloat() * 0.2f)

                    input = mnist28to50(input)
                    input = transformImage(input, 50, angle, shiftX, shiftY, zoom)

                    val prediction = network.recognize(input)
                    val predictedDigit = prediction.indices.maxByOrNull { prediction[it] } ?: -1

                    if (predictedDigit == target)
                        trainCorrect++

                    network.train(input, target, learningRate)

                    if (counter % 10000 == 0 && counter > 0) {
                        val currentAcc = (trainCorrect.toFloat() / counter) * 100
                        println("Эпоха: $epoch | Обработано: $counter/60000 | Точность: ${String.format(
                            Locale.US, "%.2f", currentAcc)}%")
                    }
                }
                var testCorrect = 0
                for (i in testImages.indices) {
                    val input = mnist28to50(testImages[i])
                    val result = network.recognize(input)
                    val pred = result.indices.maxByOrNull { result[it] } ?: -1
                    if (pred == testLabels[i]) testCorrect++
                }

                val testAcc = (testCorrect.toFloat() / testImages.size) * 100
                println("Эпоха $epoch завершена")
                println(" - точность обучения: ${String.format(Locale.US, "%.2f", (trainCorrect.toFloat() / trainImages.size) * 100)}%")
                println(" - точность на тесте: ${String.format(Locale.US, "%.2f", testAcc)}%")

                if (testAcc > bestAccuracy) {
                    bestAccuracy = testAcc
                    badEpochsCount = 0
                    saveWeightsToJson(context, network)
                } else {
                    badEpochsCount++
                }

                if (badEpochsCount >= maxBadEpochs) {
                    println("Переобучение. Обучение завершено")
                    return@launch
                }

                learningRate *= 0.9f
            }

            println("Обучение завершено")
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

    private fun mnist28to50(input: FloatArray): FloatArray {
        val oldSize = 28
        val newSize = 50
        val output = FloatArray(newSize * newSize)
        val ratio = oldSize.toFloat() / newSize

        for (y in 0 until newSize) {
            for (x in 0 until newSize) {
                val oldX = x * ratio
                val oldY = y * ratio

                val xL = oldX.toInt()
                val yL = oldY.toInt()
                val xH = if (xL >= oldSize - 1) xL else xL + 1
                val yH = if (yL >= oldSize - 1) yL else yL + 1

                val xWeight = oldX - xL
                val yWeight = oldY - yL

                val a = input[yL * oldSize + xL]
                val b = input[yL * oldSize + xH]
                val c = input[yH * oldSize + xL]
                val d = input[yH * oldSize + xH]

                output[y * newSize + x] = a * (1 - xWeight) * (1 - yWeight) +
                        b * xWeight * (1 - yWeight) +
                        c * (1 - xWeight) * yWeight +
                        d * xWeight * yWeight
            }
        }
        return output
    }

    private fun transformImage(
        input: FloatArray, size: Int,
        angle: Float, sx: Float,
        sy: Float, zoom: Float)
    : FloatArray {
        val output = FloatArray(size * size)
        val rad = angle * (PI.toFloat() / 180f)
        val sin = sin(rad)
        val cos = cos(rad)
        val center = size / 2f

        for (y in 0 until size) {
            for (x in 0 until size) {
                val px = (x - center) / zoom
                val py = (y - center) / zoom

                val sourceX = (px * cos - py * sin) + center - sx
                val sourceY = (px * sin + py * cos) + center - sy

                val iX = sourceX.toInt()
                val iY = sourceY.toInt()

                if (iX in 0 until size - 1 && iY in 0 until size - 1) {
                    val xWeight = sourceX - iX
                    val yWeight = sourceY - iY

                    val a = input[iY * size + iX]
                    val b = input[iY * size + (iX + 1)]
                    val c = input[(iY + 1) * size + iX]
                    val d = input[(iY + 1) * size + (iX + 1)]

                    output[y * size + x] = a * (1 - xWeight) * (1 - yWeight) +
                            b * xWeight * (1 - yWeight) +
                            c * (1 - xWeight) * yWeight +
                            d * xWeight * yWeight
                }
            }
        }
        return output
    }

}