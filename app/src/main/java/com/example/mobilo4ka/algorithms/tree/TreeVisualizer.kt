package com.example.mobilo4ka.algorithms.tree


import java.lang.StringBuilder

object TreeVisualizer {

    fun getFullTreeVisual(rootNode: TreeNode?): String {
        if (rootNode == null) return "Дерево еще не построено"
        val sb = StringBuilder()
        generateTreeString(rootNode, "", true, sb)
        return sb.toString()
    }

    private fun generateTreeString(
        node: TreeNode,
        prefix: String,
        isLast: Boolean,
        sb: StringBuilder
    ) {
        sb.append(prefix)
        sb.append(if (isLast) "└── " else "├── ")

        when (node) {
            is TreeNode.Leaf -> {
                val results = node.results.joinToString(", ") { it.first }
                sb.append("Результат: $results\n")
            }
            is TreeNode.Decision -> {
                sb.append("Вопрос: ${node.question.text}\n")

                val children = node.children.entries.toList()
                val newPrefix = prefix + if (isLast) "    " else "│   "

                children.forEachIndexed { index, entry ->
                    val lastChild = index == children.size - 1

                    sb.append(newPrefix)
                        .append(if (lastChild) "└── " else "├── ")
                        .append("[Вариант: ${entry.key}]\n")

                    generateTreeString(
                        entry.value,
                        newPrefix + if (lastChild) "    " else "│   ",
                        true,
                        sb
                    )
                }
            }
        }
    }
}