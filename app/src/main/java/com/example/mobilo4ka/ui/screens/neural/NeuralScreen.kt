package com.example.mobilo4ka.ui.screens.neural

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.mobilo4ka.R
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.Dimens
import androidx.compose.runtime.getValue

@Composable
fun NeuralScreen(viewModel: NeuralViewModel){
    SetStatusBarColor(false)

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    /*
    val trainingViewModel: TrainingViewModel = viewModel()
    val networkState = remember { mutableStateOf(NeuralNetwork.createEmpty()) }
    */

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
                    .padding(Dimens.paddingLarge),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_neural),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.logoSize)
                )
                Text(
                    text = stringResource(R.string.algo_neural),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(Dimens.paddingSmall)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            DrawingGrid(
                gridSize = 50,
                cellStates = state.cellStates,
                onCellsTouched = { indices -> viewModel.onCellsTouched(indices) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = state.resultText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingLarge),
                elevation = CardDefaults.cardElevation(Dimens.shadowHeight),
                shape = RoundedCornerShape(Dimens.cardCornerRadius),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.paddingLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
                ) {
                    /*
                    ActionButton(
                        titleRes = R.string.neural_button_train,
                        onClick = {
                            trainingViewModel.startTraining(context, networkState.value)
                        }
                    )
                     */
                    ActionButton(
                        titleRes = R.string.neural_button_recognize,
                        onClick = { viewModel.recognize(context) }
                    )
                    ActionButton(
                        titleRes = R.string.neural_button_clear,
                        onClick = { viewModel.clear(context) }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingGrid(
    gridSize: Int,
    cellStates: List<Boolean>,
    onCellsTouched: (List<Int>) -> Unit
) {
    val drawColor = MaterialTheme.colorScheme.onSurface
    val gridLineColor = MaterialTheme.colorScheme.outline

    Box (
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.paddingLarge)
            .aspectRatio(1f)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(Dimens.cardCornerRadius)
            )
            .border(
                width = Dimens.borderSize,
                color = MaterialTheme.colorScheme.primary
            )
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val cellWidth = size.width / gridSize
                    val cellHeight = size.height / gridSize
                    val steps = 15
                    val touchedIndices = mutableListOf<Int>()

                    for (i in 0..steps) {
                        val t = i / steps.toFloat()
                        val interpX = change.previousPosition.x + (change.position.x - change.previousPosition.x) * t
                        val interpY = change.previousPosition.y + (change.position.y - change.previousPosition.y) * t

                        val x = (interpX / cellWidth).toInt().coerceIn(0, gridSize - 1)
                        val y = (interpY / cellHeight).toInt().coerceIn(0, gridSize - 1)

                        val brushSize = 1

                        for (dy in -brushSize..brushSize) {
                            for (dx in -brushSize..brushSize) {
                                if (dx * dx + dy * dy <= brushSize * brushSize) {
                                    val nx = (x + dx).coerceIn(0, gridSize - 1)
                                    val ny = (y + dy).coerceIn(0, gridSize - 1)
                                    touchedIndices.add(ny * gridSize + nx)
                                }
                            }
                        }
                    }
                    onCellsTouched(touchedIndices)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()){
            val cellWidth = size.width / gridSize
            val cellHeight = size.height / gridSize

            for (y in 0 until gridSize){
                for (x in 0 until gridSize){
                    val index = y * gridSize + x
                    if (cellStates[index]) {
                        drawRect(
                            color = drawColor,
                            topLeft = Offset(x * cellWidth, y * cellHeight),
                            size = Size(cellWidth, cellHeight)
                        )
                    }
                }
            }

            for (i in 0..gridSize){
                val posX = i * cellWidth
                val posY = i * cellHeight
                drawLine(gridLineColor, Offset(posX, 0f), Offset(posX, size.height), strokeWidth = 0.5f)
                drawLine(gridLineColor, Offset(0f, posY), Offset(size.width, posY), strokeWidth = 0.5f)
            }
        }
    }
}

@Composable
fun ActionButton(
    titleRes: Int,
    onClick : () -> Unit
){
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground),
        shape = RoundedCornerShape(Dimens.buttonRadius)
    ) {
        Text(
            text = stringResource(titleRes),
            modifier = Modifier.padding(vertical = Dimens.paddingSmall),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun centerImage(cellStates: List<Boolean>, gridSize: Int): FloatArray {
    val input = FloatArray(gridSize * gridSize) { 0f }

    var minX = gridSize
    var maxX = -1
    var minY = gridSize
    var maxY = -1
    var hasPixels = false

    for (y in 0 until gridSize) {
        for (x in 0 until gridSize) {
            if (cellStates[y * gridSize + x]) {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
                hasPixels = true
            }
        }
    }

    if (!hasPixels) return input

    val centerX = (minX + maxX) / 2
    val centerY = (minY + maxY) / 2

    val shiftX = (gridSize / 2) - centerX
    val shiftY = (gridSize / 2) - centerY

    for (y in minY..maxY) {
        for (x in minX..maxX) {
            if (cellStates[y * gridSize + x]) {
                val newX = x + shiftX
                val newY = y + shiftY

                if (newX in 0 until gridSize && newY in 0 until gridSize)
                    input[newY * gridSize + newX] = 1f
            }
        }
    }
    return input
}