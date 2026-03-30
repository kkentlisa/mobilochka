package com.example.mobilo4ka.algorithms.neural

class Layer(
    val weights: Array<FloatArray>,
    val bias: FloatArray
){
    fun layerPass(input: FloatArray): FloatArray{
        val output = FloatArray(bias.size)

        for (i in weights.indices){
            var sum = bias[i]
            for (j in input.indices){
                sum += weights[i][j] * input[j]
            }

            output[i] = relu(sum)
        }

        return output
    }
}