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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.mobilo4ka.algorithms.neural.ModelLoader
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.Dimens

@Composable
fun NeuralScreen(){
    SetStatusBarColor(false)

    val context = LocalContext.current
    /*
    val trainingViewModel: TrainingViewModel = viewModel()
    val networkState = remember { mutableStateOf(NeuralNetwork.createEmpty()) }
    */

    val networkState = remember { mutableStateOf(ModelLoader.load(context)) }
    val resultText = remember {mutableStateOf(context.getString(R.string.draw_number))}

    val gridSize = 50
    val cellStates = remember { mutableStateListOf<Boolean>().apply {repeat(gridSize * gridSize) { add(false)} } }

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

            DrawingGrid(gridSize, cellStates)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = resultText.value,
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
                        onClick = {
                            val scaleInput = scaleGrid(cellStates, 50, 28)
                            val result = networkState.value.recognize(scaleInput)

                            val predictDigit = result.indices.maxByOrNull { result[it] } ?: -1
                            resultText.value = context.getString(R.string.rating, predictDigit)
                        }
                    )
                    ActionButton(
                        titleRes = R.string.neural_button_clear,
                        onClick = {
                            cellStates.replaceAll { false }
                            resultText.value = context.getString(R.string.draw_number)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawingGrid(
    gridSize: Int,
    cellStates: MutableList<Boolean>
) {
    val fillColor = MaterialTheme.colorScheme.surface
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
                                    val index = ny * gridSize + nx
                                    cellStates[index] = true
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()){
            val cellWidth = size.width / gridSize
            val cellHeight = size.height / gridSize

            for (y in 0 until gridSize){
                for (x in 0 until gridSize){
                    val index = y * gridSize + x
                    drawRect(
                        color = if (cellStates[index]) drawColor else fillColor,
                        topLeft = Offset(x * cellWidth, y * cellHeight),
                        size = Size(cellWidth, cellHeight)
                    )
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
private fun ActionButton(
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

fun scaleGrid(input: List<Boolean>, oldSize: Int, newSize: Int): FloatArray {
    val output = FloatArray(newSize * newSize)
    val step = oldSize.toFloat() / newSize

    for (y in 0 until newSize) {
        for (x in 0 until newSize) {
            val startX = (x * step).toInt()
            val endX = ((x + 1) * step).toInt().coerceAtMost(oldSize)
            val startY = (y * step).toInt()
            val endY = ((y + 1) * step).toInt().coerceAtMost(oldSize)

            var filledCount = 0
            val totalPixels = (endX - startX) * (endY - startY)
            for (iy in startY until endY) {
                for (ix in startX until endX) {
                    if (input[iy * oldSize + ix]) {
                        filledCount++
                    }
                }
            }
            output[y * newSize + x] = filledCount.toFloat() / totalPixels
        }
    }
    return output
}