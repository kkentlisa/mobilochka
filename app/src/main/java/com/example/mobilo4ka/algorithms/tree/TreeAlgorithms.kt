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
        val result: String,
        val address: String
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
        questionsList = questions
        buildDecisionTree()
    }

    private fun buildDecisionTree() {
        if (allPlaces.isEmpty() || questionsList.isEmpty()) return

        val data = allPlaces.flatMap { place ->
            val attributeColumns = questionsList.map { it.columnName }
            val valueCombinations = attributeColumns.map { column ->
                place.attributes[column]
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()
            }

            if (valueCombinations.all { it.isNotEmpty() }) {
                cartesianProduct(valueCombinations).map { combination ->
                    val values = mutableMapOf<String, String>()
                    attributeColumns.forEachIndexed { index, column ->
                        values[column] = combination.getOrNull(index) ?: ""
                    }
                    DataRow(values, place.result, place.address)
                }
            } else {
                listOf(
                    DataRow(
                        attributeColumns.associateWith { place.attributes[it] ?: "" },
                        place.result,
                        place.address
                    )
                )
            }
        }

        val attributes = questionsList.map { it.columnName }
        rootNode = buildTree(data, attributes)
        currentNode = rootNode
    }

    private fun cartesianProduct(lists: List<List<String>>): List<List<String>> {
        if (lists.isEmpty()) return emptyList()
        var result = lists.first().map { listOf(it) }
        for (i in 1 until lists.size) {
            result = result.flatMap { existing ->
                lists[i].map { existing + it }
            }
        }
        return result
    }

    private fun buildTree(
        data: List<DataRow>,
        attributes: List<String>
    ): TreeNode {
        val results = data.map { it.result }

        if (results.distinct().size == 1) {
            val first = data.first()
            return TreeNode.Leaf(first.result, first.address)
        }

        if (attributes.isEmpty()) {
            val counts = results.groupingBy { it }.eachCount()
            val mostCommon = counts.maxByOrNull { it.value }?.key ?: ""
            val row = data.firstOrNull { it.result == mostCommon }
            return TreeNode.Leaf(mostCommon, row?.address ?: "")
        }

        val bestAttr = selectBestAttribute(data, attributes)
        val question = questionsList.first { it.columnName == bestAttr }

        val grouped = data.groupBy { it.values[bestAttr] }
        val remainingAttrs = attributes.filter { it != bestAttr }

        val children: Map<String, TreeNode> = grouped
            .filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { (_, subset) ->
                buildTree(subset, remainingAttrs)
            }

        return TreeNode.Decision(question, children)
    }

    private fun selectBestAttribute(
        data: List<DataRow>,
        attributes: List<String>
    ): String {
        val totalEntropy = calculateEntropy(data)
        return attributes.maxByOrNull {
            calculateInformationGain(data, it, totalEntropy)
        } ?: attributes.first()
    }

    private fun calculateEntropy(data: List<DataRow>): Double {
        val counts = data.map { it.address }.groupingBy { it }.eachCount()
        var entropy = 0.0
        for (count in counts.values) {
            val p = count.toDouble() / data.size
            if (p > 0) entropy -= p * log2(p)
        }
        return entropy
    }

    private fun calculateInformationGain(
        data: List<DataRow>,
        attribute: String,
        totalEntropy: Double
    ): Double {
        val grouped = data.groupBy { it.values[attribute] }
        var weightedEntropy = 0.0
        for ((_, subset) in grouped) {
            val weight = subset.size.toDouble() / data.size
            weightedEntropy += weight * calculateEntropy(subset)
        }
        return totalEntropy - weightedEntropy
    }

    private fun log2(x: Double): Double = ln(x) / ln(2.0)

    fun getCurrentQuestion(): Question? =
        (currentNode as? TreeNode.Decision)?.question

    fun getCurrentOptions(): List<String> =
        (currentNode as? TreeNode.Decision)?.children?.keys?.toList() ?: emptyList()

    fun answerSelected(answer: String): Boolean {
        val node = currentNode as? TreeNode.Decision ?: return false
        val child = node.children[answer]
            ?: node.children.values.firstOrNull()
            ?: return false

        path.add(node.question.text to answer)
        currentNode = child
        return true
    }

    fun getResult(): String? =
        (currentNode as? TreeNode.Leaf)?.result

    fun getAddress(): String =
        (currentNode as? TreeNode.Leaf)?.address ?: ""

    fun getPath(): List<String> =
        path.map { "${it.first} → ${it.second}" }

    fun isFinished(): Boolean =
        currentNode is TreeNode.Leaf

    fun reset() {
        currentNode = rootNode ?: return
        path.clear()
    }

    private fun parsePlacesWithQuestions(context: Context, userCsvPath: String? = null): Pair<List<Place>, List<Question>> {
        val places = mutableListOf<Place>()
        val questions = mutableListOf<Question>()

        val inputStream = if (userCsvPath != null) {
            context.openFileInput(userCsvPath)
        } else {
            context.assets.open("table/version-ru/places.csv")
        }

        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val headers = reader.readLine()?.split(";")?.map { it.trim() } ?: return Pair(emptyList(), emptyList())

        val resultColumnIndex = headers.size - 2
        val addressColumnIndex = headers.size - 1

        for (i in 0 until resultColumnIndex) {
            val columnName = headers[i]
            questions.add(Question(columnName, emptyList(), columnName))
        }

        reader.forEachLine { line ->
            val columns = line.split(";").map { it.trim() }
            if (columns.size >= headers.size) {
                val attributes = mutableMapOf<String, String>()
                for (i in 0 until resultColumnIndex) {
                    attributes[headers[i]] = columns[i]
                }
                places.add(
                    Place(
                        attributes,
                        columns[resultColumnIndex],
                        columns[addressColumnIndex]
                    )
                )
            }
        }

        val updatedQuestions = questions.map { q ->
            val options = places
                .mapNotNull { it.attributes[q.columnName] }
                .flatMap { it.split(",").map { v -> v.trim() } }
                .filter { it.isNotBlank() }
                .distinct()
            q.copy(options = options)
        }

        return Pair(places, updatedQuestions)
    }

    fun saveUserCsv(context: Context, csvContent: String) {
        val filename = "user_places.csv"
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { output ->
            output.write(csvContent.toByteArray(Charsets.UTF_8))
        }
    }
}