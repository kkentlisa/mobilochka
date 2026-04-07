package com.example.mobilo4ka.algorithms.neural

class Layer(
    val weights: Array<FloatArray>,
    val bias: FloatArray,
    private val useRelu: Boolean = true
){
    private var lastInput = FloatArray(0)
    private var lastZ = FloatArray(0)
    fun forward(input: FloatArray): FloatArray{
        lastInput = input.copyOf()
        val output = FloatArray(bias.size)
        lastZ = FloatArray(bias.size)

        for (i in weights.indices){
            var sum = bias[i]
            for (j in input.indices){
                sum += weights[i][j] * input[j]
            }
            lastZ[i] = sum
            output[i] = if (useRelu) relu(sum) else sum
        }
        return output
    }

    fun backward(gradientOutput: FloatArray, learningRate: Float): FloatArray {
        val gradientInput = FloatArray(lastInput.size)

        for (i in weights.indices){
            val derivative = if (useRelu) reluDerivative(lastZ[i]) else 1f
            val delta = gradientOutput[i] * derivative

            for (j in lastInput.indices) {
                gradientInput[j] += weights[i][j] * delta
            }

            for (j in lastInput.indices) {
                weights[i][j] -= learningRate * delta * lastInput[j]
            }

            bias[i] -= learningRate * delta
        }
        return gradientInput
    }
}