package com.example.mobilo4ka.algorithms.genetic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobilo4ka.data.models.Building

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSelector(
    buildings: List<Building>?,
    onProductsSelected: (List<String>) -> Unit
) {
    val safeBuildings = buildings ?: emptyList()

    val allProducts = remember(safeBuildings) {
        try {
            safeBuildings
                .filter { it.menu != null }
                .flatMap { it.menu }
                .filter { !it.isNullOrBlank() }
                .distinct()
                .sorted()
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    var selectedItems by remember { mutableStateOf(setOf<String>()) }

    if (allProducts.isEmpty()) {
        Text("Загрузка товаров...", modifier = Modifier.padding(16.dp))
    } else {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allProducts) { product ->
                FilterChip(
                    selected = selectedItems.contains(product),
                    onClick = {
                        val newSelection = if (selectedItems.contains(product)) {
                            selectedItems - product
                        } else {
                            selectedItems + product
                        }
                        selectedItems = newSelection
                        onProductsSelected(newSelection.toList())
                    },
                    label = { Text(product) }
                )
            }
        }
    }
}