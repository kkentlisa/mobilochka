package com.example.mobilo4ka.ui.screens.tree

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobilo4ka.algorithms.tree.TreeAlgorithm
import com.example.mobilo4ka.algorithms.tree.TreeNode
import com.example.mobilo4ka.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeStructureScreen(onBack: () -> Unit) {
    val rootNode = remember { TreeAlgorithm.getRoot() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Интерактивная структура", color = SurfaceWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = SurfaceWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TsuBlue
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                if (rootNode != null) {
                    TreeNodeItem(node = rootNode, label = "Корень дерева")
                } else {
                    Text("Дерево не найдено", color = TextDark, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun TreeNodeItem(node: TreeNode, label: String, depth: Int = 0) {
    var isExpanded by remember { mutableStateOf(depth < 1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 12).dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { isExpanded = !isExpanded },
            color = if (node is TreeNode.Leaf) MapGrass else SurfaceWhite,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (node is TreeNode.Decision) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = TsuBlue
                    )
                }

                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Line
                    )
                    Text(
                        text = when (node) {
                            is TreeNode.Decision -> "Вопрос: ${node.question.text}"
                            is TreeNode.Leaf -> "Результаты: ${node.results.joinToString { it.first }}"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextDark
                    )
                }
            }
        }

        if (node is TreeNode.Decision) {
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    node.children.forEach { (option, child) ->
                        TreeNodeItem(
                            node = child,
                            label = "Если ответ: $option",
                            depth = depth + 1
                        )
                    }
                }
            }
        }
    }
}