package com.example.mobilo4ka.algorithms.neural

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ModelLoader {
    fun load(context: Context): NeuralNetwork{
        val jsonString = context.assets.open("neural/weights.json")
            .bufferedReader()
            .use { it.readText() }

        val json = JSONObject(jsonString)

        val w1 = parse2D(json.getJSONArray("fc1.weight"))
        val b1 = parse1D(json.getJSONArray("fc1.bias"))

        val w2 = parse2D(json.getJSONArray("fc2.weight"))
        val b2 = parse1D(json.getJSONArray("fc2.bias"))

        val w3 = parse2D(json.getJSONArray("fc3.weight"))
        val b3 = parse1D(json.getJSONArray("fc3.bias"))

        return NeuralNetwork(w1, b1, w2, b2, w3, b3)
    }

    fun parse1D(array: JSONArray): FloatArray {
        return FloatArray(array.length()) { i ->
            array.getDouble(i).toFloat()}
    }

    fun parse2D(array: JSONArray): Array<FloatArray> {
        return Array(array.length()) { i ->
            val row = array.getJSONArray(i)
            FloatArray(row.length()) { j ->
                row.getDouble(j).toFloat()
            }
        }
    }
}