package com.example.mobilo4ka.algorithms.tree

object TreeOptimizer {

    fun optimize(node: TreeNode): TreeNode {
        return when (node) {

            is TreeNode.Leaf -> node

            is TreeNode.Decision -> {

                val optimizedChildren = node.children.mapValues { (_, child) ->
                    optimize(child)
                }

                if (optimizedChildren.size == 1) {
                    return optimizedChildren.values.first()
                }

                val leafResults = optimizedChildren.values
                    .flatMap { extractResults(it) }
                    .distinctBy { it.first }

                if (leafResults.size == 1) {
                    return TreeNode.Leaf(leafResults)
                }

                val branchResults = optimizedChildren.values.map {
                    extractResults(it).map { r -> r.first }.toSet()
                }.distinct()

                if (branchResults.size == 1) {
                    return optimizedChildren.values.first()
                }

                TreeNode.Decision(
                    question = node.question,
                    children = optimizedChildren
                )
            }
        }
    }

    private fun extractResults(node: TreeNode): List<Pair<String, String>> {
        return when (node) {
            is TreeNode.Leaf -> node.results
            is TreeNode.Decision ->
                node.children.values.flatMap { extractResults(it) }
        }
    }
}