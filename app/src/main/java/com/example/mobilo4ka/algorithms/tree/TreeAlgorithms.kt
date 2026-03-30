package com.example.mobilo4ka.algorithms.tree

import android.content.Context
import com.example.mobilo4ka.R
import java.io.BufferedReader
import java.io.InputStreamReader

data class Place(
    val location: String,
    val foodType: List<String>,
    val timeAvailable: List<String>,
    val budget: List<String>,
    val queueTolerance: List<String>,
    val weather: List<String>,
    val recommendedPlace: String,
    val address: String
)

data class Question(
    val text: String,
    val options: List<String>,
    val typeAnswer: String
)

object TreeAlgorithm {

    private var allPlaces: List<Place> = emptyList()
    private var currentPlaces: List<Place> = emptyList()

    fun loadPlaces(context: Context) {
        try {
            allPlaces = parsePlaces(context)
            currentPlaces = allPlaces
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getQuestions(context: Context, places: List<Place> = currentPlaces): List<Question> {
        if (places.isEmpty()) return emptyList()

        val allQuestions = listOf(
            Question(
                text = context.getString(R.string.question_location),
                options = places.map { it.location }.distinct(),
                typeAnswer = "location"
            ),
            Question(
                text = context.getString(R.string.question_food),
                options = places.flatMap { it.foodType }.distinct(),
                typeAnswer = "foodType"
            ),
            Question(
                text = context.getString(R.string.question_time),
                options = places.flatMap { it.timeAvailable }.distinct(),
                typeAnswer = "timeAvailable"
            ),
            Question(
                text = context.getString(R.string.question_budget),
                options = places.flatMap { it.budget }.distinct(),
                typeAnswer = "budget"
            ),
            Question(
                text = context.getString(R.string.question_queue),
                options = places.flatMap { it.queueTolerance }.distinct(),
                typeAnswer = "queueTolerance"
            ),
            Question(
                text = context.getString(R.string.question_weather),
                options = places.flatMap { it.weather }.distinct(),
                typeAnswer = "weather"
            )
        )

        return allQuestions
    }

    fun filterByAnswer(typeAnswer: String, answer: String) {
        currentPlaces = when (typeAnswer) {
            "location" -> currentPlaces.filter { it.location.contains(answer) }
            "foodType" -> currentPlaces.filter { it.foodType.contains(answer) }
            "timeAvailable" -> currentPlaces.filter { it.timeAvailable.contains(answer) }
            "budget" -> currentPlaces.filter { it.budget.contains(answer) }
            "queueTolerance" -> currentPlaces.filter { it.queueTolerance.contains(answer) }
            "weather" -> currentPlaces.filter { it.weather.contains(answer) }
            else -> currentPlaces
        }.distinctBy { it.recommendedPlace }
    }


    fun getFinalPlaces(): List<Place> = currentPlaces

    fun reset() {
        currentPlaces = allPlaces
    }

    private fun parsePlaces(context: Context): List<Place> {
        val places = mutableListOf<Place>()

        try {
            val inputStream = context.assets.open("table/places.csv")
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))

            reader.readLine()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    val columns = it.split(";")
                    if (columns.size >= 8) {
                        val place = Place(
                            location = columns[0].trim(),
                            foodType = parseList(columns[1]),
                            timeAvailable = parseList(columns[2]),
                            budget = parseList(columns[3]),
                            queueTolerance = parseList(columns[4]),
                            weather = parseList(columns[5]),
                            recommendedPlace = columns[6].trim(),
                            address = columns[7].trim()
                        )
                        places.add(place)
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return places
    }

    private fun parseList(value: String): List<String> {
        return value.split(",").map { it.trim() }
    }
}
