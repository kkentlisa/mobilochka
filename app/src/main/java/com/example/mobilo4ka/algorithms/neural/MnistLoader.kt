package com.example.mobilo4ka.algorithms.neural

import android.content.Context
import java.io.DataInputStream

object MnistLoader {
    fun loadImages(context: Context, fileName: String): List<FloatArray> {
        val inputStream = context.assets.open("mnist/$fileName")
        val dataInputStream = DataInputStream(inputStream)

        val magicNumber = dataInputStream.readInt()
        val numberOfImages = dataInputStream.readInt()

        val rows = dataInputStream.readInt()
        val cols = dataInputStream.readInt()

        val images = mutableListOf<FloatArray>()

        for (i in 0 until numberOfImages) {
            val image = FloatArray(rows * cols)
            for (j in 0 until rows * cols){
                image[j] = (dataInputStream.readUnsignedByte().toFloat() / 255)
            }
            images.add(image)
        }

        dataInputStream.close()
        return images
    }

    fun loadLabels(context: Context, fileName: String): IntArray {
        val inputStream = context.assets.open("mnist/$fileName")
        val dataInputStream = DataInputStream(inputStream)

        val magicNumber = dataInputStream.readInt()
        val numberOfLabels = dataInputStream.readInt()

        val labels = IntArray(numberOfLabels)
        for (i in 0 until numberOfLabels) {
            labels[i] = dataInputStream.readUnsignedByte()
        }

        dataInputStream.close()
        return labels
    }
}