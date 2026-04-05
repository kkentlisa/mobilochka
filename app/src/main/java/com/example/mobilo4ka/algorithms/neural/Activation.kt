package com.example.mobilo4ka.algorithms.neural

import kotlin.math.exp

fun relu(input : Float): Float {
    if (input > 0) return input
    return 0f
}

fun softmax(input: FloatArray): FloatArray {
    val output = FloatArray(input.size)
    var sum = 0f

    for (i in input.indices){
        output[i] = exp(input[i])
        sum += output[i]
    }

    for (i in input.indices){
        output[i] /= sum
    }

    return output
}