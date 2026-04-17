package com.example.mobilo4ka.ui.screens.tree

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import com.example.mobilo4ka.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.times
import com.example.mobilo4ka.algorithms.tree.TreeAlgorithm
import com.example.mobilo4ka.algorithms.tree.TreeNode
import com.example.mobilo4ka.algorithms.tree.TreeOptimizer
import com.example.mobilo4ka.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeStructureScreen(onBack: () -> Unit) {

    val rootNode = remember {
        TreeAlgorithm.getRoot()?.let { TreeOptimizer.optimize(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.structure),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(Dimens.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
        ) {

            item {
                if (rootNode != null) {
                    TreeNodeItem(
                        node = rootNode,
                        label = stringResource(R.string.root_tree),
                        depth = 0
                    )
                } else {
                    Text(
                        text = stringResource(R.string.not_found_tree),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(Dimens.paddingMedium)
                    )
                }
            }
        }
    }
}

@Composable
fun TreeNodeItem(
    node: TreeNode,
    label: String,
    depth: Int = 0
) {
    var isExpanded by remember { mutableStateOf(depth < 1) }

    val shape = RoundedCornerShape(Dimens.paddingMedium)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * Dimens.paddingMedium))
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            shape = shape,
            color = if (node is TreeNode.Leaf) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            shadowElevation = Dimens.paddingDefault,
            border = BorderStroke(
                width = Dimens.dividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        ) {

            Row(
                modifier = Modifier.padding(Dimens.paddingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (node is TreeNode.Decision) {
                    Icon(
                        imageVector = if (isExpanded)
                            Icons.Default.KeyboardArrowDown
                        else
                            Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.logoSize)
                    )
                }

                Spacer(Modifier.width(Dimens.paddingSmall))

                Column {

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = when (node) {
                            is TreeNode.Decision ->
                                "${stringResource(R.string.question)} ${node.question.text}"

                            is TreeNode.Leaf ->
                                "${stringResource(R.string.result)} ${node.results.joinToString { it.first }}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (node is TreeNode.Decision) {
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = Dimens.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                ) {
                    node.children.forEach { (option, child) ->
                        TreeNodeItem(
                            node = child,
                            label = "${stringResource(R.string.answer)} $option",
                            depth = depth + 1
                        )
                    }
                }
            }
        }
    }
}