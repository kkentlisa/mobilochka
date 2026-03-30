package com.example.mobilo4ka.algorithms.neural

class NeuralNetwork(
    w1: Array<FloatArray>,
    b1: FloatArray,
    w2: Array<FloatArray>,
    b2: FloatArray,
    w3: Array<FloatArray>,
    b3: FloatArray,
){
    private val layer1 = Layer(w1, b1)
    private val layer2 = Layer(w2, b2)
    private val layer3 = Layer(w3, b3)
    fun recognize(input: FloatArray): FloatArray{
        val output1 = layer1.layerPass(input)
        val output2 = layer2.layerPass(output1)
        val output3 = layer3.layerPass(output2)

        return softmax(output3)
    }
}