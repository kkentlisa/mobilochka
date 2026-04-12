package com.example.mobilo4ka.algorithms.tree

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.ln

data class Place(
    val attributes: Map<String, String>,
    val result: String,
    val address: String
)

data class Question(
    val text: String,
    val options: List<String>,
    val columnName: String
)

data class DataRow(
    val values: Map<String, String>,
    val result: String,
    val address: String
)

sealed class TreeNode {
    data class Decision(
        val question: Question,
        val children: Map<String, TreeNode>
    ) : TreeNode()

    data class Leaf(
        val results: List<Pair<String, String>>
    ) : TreeNode()
}

object TreeAlgorithm {

    private var allPlaces: List<Place> = emptyList()
    private var questionsList: List<Question> = emptyList()
    private var rootNode: TreeNode? = null
    private var currentNode: TreeNode? = null
    private val path = mutableListOf<Pair<String, String>>()

    fun loadPlaces(context: Context, userCsvPath: String? = null) {
        val (places, questions) = parsePlacesWithQuestions(context, userCsvPath)
        allPlaces = places
        questionsList = questions.filter { it.options.size > 1 }
        buildDecisionTree()
        reset()
    }

    private fun buildDecisionTree() {
        if (allPlaces.isEmpty() || questionsList.isEmpty()) {
            rootNode = null
            return
        }

        val data = allPlaces.map { place ->
            DataRow(
                values = place.attributes,
                result = place.result,
                address = place.address
            )
        }

        val attributes = questionsList.map { it.columnName }
        rootNode = buildTree(data, attributes)
    }

    private fun buildTree(
        data: List<DataRow>,
        attributes: List<String>
    ): TreeNode {
        val results = data.map { it.result }
        val uniqueResults = results.distinct()

        if (uniqueResults.size == 1) {
            val first = data.first()
            return TreeNode.Leaf(listOf(first.result to first.address))
        }

        if (attributes.isEmpty()) {
            val places = data.map { it.result to it.address }.distinctBy { it.first }
            return TreeNode.Leaf(places)
        }

        val bestAttr = selectBestAttribute(data, attributes)
        val question = questionsList.first { it.columnName == bestAttr }
        val remainingAttrs = attributes.filter { it != bestAttr }

        val allValues = data.flatMap { row ->
            row.values[bestAttr]?.split(",")?.map { it.trim() } ?: emptyList()
        }.distinct()

        val children = mutableMapOf<String, TreeNode>()

        for (value in allValues) {
            val subset = data.filter { row ->
                val values = row.values[bestAttr]?.split(",")?.map { it.trim() } ?: emptyList()
                values.contains(value)
            }
            if (subset.isNotEmpty()) {
                children[value] = buildTree(subset, remainingAttrs)
            }
        }

        return TreeNode.Decision(question, children)
    }

    private fun selectBestAttribute(
        data: List<DataRow>,
        attributes: List<String>
    ): String {
        val totalEntropy = calculateEntropy(data.map { it.result })
        var bestAttr = attributes.first()
        var bestGain = -1.0

        for (attr in attributes) {
            val gain = calculateInformationGain(data, attr, totalEntropy)
            if (gain > bestGain) {
                bestGain = gain
                bestAttr = attr
            }
        }
        return bestAttr
    }

    private fun calculateEntropy(results: List<String>): Double {
        val counts = results.groupingBy { it }.eachCount()
        var entropy = 0.0
        val total = results.size.toDouble()

        for (count in counts.values) {
            val p = count / total
            entropy -= p * log2(p)
        }
        return entropy
    }

    private fun calculateInformationGain(
        data: List<DataRow>,
        attribute: String,
        totalEntropy: Double
    ): Double {
        val allValues = data.flatMap { row ->
            row.values[attribute]?.split(",")?.map { it.trim() } ?: emptyList()
        }.distinct()

        var weightedEntropy = 0.0

        for (value in allValues) {
            val subset = data.filter { row ->
                val values = row.values[attribute]?.split(",")?.map { it.trim() } ?: emptyList()
                values.contains(value)
            }

            if (subset.isNotEmpty()) {
                val weight = subset.size.toDouble() / data.size
                val subsetEntropy = calculateEntropy(subset.map { it.result })
                weightedEntropy += weight * subsetEntropy
            }
        }

        return totalEntropy - weightedEntropy
    }

    private fun log2(x: Double): Double = ln(x) / ln(2.0)

    fun getCurrentQuestion(): Question? {
        val node = currentNode as? TreeNode.Decision ?: return null
        val currentData = getCurrentData()
        val availableOptions = currentData
            .flatMap { row ->
                row.values[node.question.columnName]?.split(",")?.map { it.trim() } ?: emptyList()
            }
            .distinct()
            .sorted()

        return node.question.copy(options = availableOptions)
    }

    private fun getCurrentData(): List<DataRow> {
        var currentData = allPlaces.map { place ->
            DataRow(
                values = place.attributes,
                result = place.result,
                address = place.address
            )
        }

        for (step in path) {
            val question = questionsList.find { it.text == step.first }
            if (question != null) {
                currentData = currentData.filter { row ->
                    val values = row.values[question.columnName]?.split(",")?.map { it.trim() } ?: emptyList()
                    values.contains(step.second)
                }
            }
        }

        return currentData
    }

    fun answerSelected(answer: String): Boolean {
        val node = currentNode as? TreeNode.Decision ?: return false
        val child = node.children[answer] ?: return false

        path.add(node.question.text to answer)
        currentNode = child
        return true
    }

    fun getResults(): List<Pair<String, String>> {
        val leaf = currentNode as? TreeNode.Leaf
        if (leaf != null) {
            return leaf.results
        }

        val currentData = getCurrentData()
        return currentData.map { it.result to it.address }.distinctBy { it.first }
    }

    fun hasMultipleResults(): Boolean = getResults().size > 1

    fun getResult(): String? {
        val results = getResults()
        return if (results.size == 1) results.first().first else null
    }

    fun getAddress(): String {
        val results = getResults()
        return if (results.size == 1) results.first().second else ""
    }

    fun getPath(): List<String> = path.map { "${it.first} → ${it.second}" }

    fun isFinished(): Boolean = currentNode is TreeNode.Leaf

    fun reset() {
        currentNode = rootNode
        path.clear()
    }

    private fun parsePlacesWithQuestions(context: Context, userCsvPath: String? = null): Pair<List<Place>, List<Question>> {
        val places = mutableListOf<Place>()

        val inputStream = if (userCsvPath != null) {
            context.openFileInput(userCsvPath)
        } else {
            context.assets.open("table/version-ru/places.csv")
        }

        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val headers = reader.readLine()?.split(";")?.map { it.trim() } ?: return Pair(emptyList(), emptyList())

        val resultColumnIndex = headers.size - 2
        val addressColumnIndex = headers.size - 1

        reader.forEachLine { line ->
            val columns = line.split(";").map { it.trim() }
            if (columns.size >= headers.size) {
                val attributes = mutableMapOf<String, String>()
                for (i in 0 until resultColumnIndex) {
                    attributes[headers[i]] = columns[i]
                }
                places.add(
                    Place(
                        attributes = attributes,
                        result = columns[resultColumnIndex],
                        address = columns[addressColumnIndex]
                    )
                )
            }
        }
        reader.close()

        val questions = headers.take(resultColumnIndex).map { columnName ->
            val options = places
                .mapNotNull { it.attributes[columnName] }
                .flatMap { it.split(",").map { v -> v.trim() } }
                .filter { it.isNotBlank() }
                .distinct()
            Question(
                text = columnName,
                options = options,
                columnName = columnName
            )
        }

        return Pair(places, questions)
    }

    fun saveUserCsv(context: Context, csvContent: String) {
        val filename = "user_places.csv"
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { output ->
            output.write(csvContent.toByteArray(Charsets.UTF_8))
        }
    }
}