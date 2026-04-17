package com.example.mobilo4ka.algorithms.neural

class NeuralNetwork(
    w1: Array<FloatArray>,
    b1: FloatArray,
    w2: Array<FloatArray>,
    b2: FloatArray,
    w3: Array<FloatArray>,
    b3: FloatArray,
) {
    val layer1 = Layer(w1, b1)
    val layer2 = Layer(w2, b2)
    val layer3 = Layer(w3, b3, false)

    fun recognize(input: FloatArray): FloatArray {
        val output1 = layer1.forward(input)
        val output2 = layer2.forward(output1)
        val output3 = layer3.forward(output2)
        return softmax(output3)
    }

    fun train(input: FloatArray, targetDigit: Int, learningRate: Float) {
        val prob = recognize(input)

        val outputError = FloatArray(10)
        for (i in 0 until 10) {
            outputError[i] = prob[i] - (if (i == targetDigit) 1f else 0f)
        }

        val grad2 = layer3.backward(outputError, learningRate)
        val grad1 = layer2.backward(grad2, learningRate)
        layer1.backward(grad1, learningRate)
    }

    companion object {
        fun createEmpty(): NeuralNetwork {
            fun random2D(rows: Int, cols: Int): Array<FloatArray> {
                val scale = 1.0f / cols
                return Array(rows) {
                    FloatArray(cols) { (Math.random().toFloat() * 2f - 1f) * scale }
                }
            }

            fun zeros1D(size: Int): FloatArray = FloatArray(size) { 0f }

            return NeuralNetwork(
                w1 = random2D(128, 2500),
                b1 = zeros1D(128),
                w2 = random2D(64, 128),
                b2 = zeros1D(64),
                w3 = random2D(10, 64),
                b3 = zeros1D(10)
            )
        }
    }
}