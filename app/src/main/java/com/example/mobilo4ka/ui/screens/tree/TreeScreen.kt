package com.example.mobilo4ka.ui.screens.tree

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.mobilo4ka.algorithms.tree.TreeAlgorithm
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.AppAlpha
import com.example.mobilo4ka.ui.theme.Mobilo4kaTheme
import com.example.mobilo4ka.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.mobilo4ka.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.mobilo4ka.ui.main.Language
import com.example.mobilo4ka.ui.theme.Dimens

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun TreeScreen(currentLanguage: Language) {
    Mobilo4kaTheme {
        TreeScreenContent(currentLanguage)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TreeScreenContent(currentLanguage: Language) {
    SetStatusBarColor(false)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val typeTableStr = stringResource(R.string.type_table)
    val baseTableStr = stringResource(R.string.base_table)
    val inputTableStr = stringResource(R.string.input_table)
    val downloadFileStr = stringResource(R.string.download_file)
    val errorFileStr = stringResource(R.string.error_file)
    val recommendedPlacesStr = stringResource(R.string.recommended_places)
    val ouputTreeStr = stringResource(R.string.output_tree)

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var showOptions by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var firstStepCompleted by remember { mutableStateOf(false) }
    var userCsvUploaded by remember { mutableStateOf(false) }
    var showCsvBlock by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        TreeAlgorithm.loadPlaces(context)
        TreeAlgorithm.reset()

        messages.add(ChatMessage(typeTableStr, isUser = false))
        showOptions = true
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                        val csvContent = reader.readText()
                        TreeAlgorithm.saveUserCsv(context, csvContent)
                        TreeAlgorithm.loadPlaces(context, "user_places.csv")
                        TreeAlgorithm.reset()
                        TreeAlgorithm.getCurrentQuestion()?.let { first ->
                            messages.add(ChatMessage(first.text, isUser = false))
                            showOptions = true
                        }
                        userCsvUploaded = true
                        showCsvBlock = false
                        firstStepCompleted = true
                    }
                } catch (e: Exception) {
                    messages.add(ChatMessage("$errorFileStr ${e.message}", isUser = false))
                }
            }
        }
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun handleAnswer(answer: String) {
        if (isSearching) return
        messages.add(ChatMessage(answer, isUser = true))

        if (!firstStepCompleted) {
            when (answer) {
                baseTableStr -> {
                    TreeAlgorithm.loadPlaces(context)
                    TreeAlgorithm.reset()
                    firstStepCompleted = true
                }
                inputTableStr -> {
                    showCsvBlock = true
                    messages.add(ChatMessage(downloadFileStr, isUser = false))
                    return
                }
            }

            TreeAlgorithm.getCurrentQuestion()?.let { first ->
                messages.add(ChatMessage(first.text, isUser = false))
                showOptions = true
            }
            return
        }

        TreeAlgorithm.answerSelected(answer)

        if (!TreeAlgorithm.isFinished()) {
            TreeAlgorithm.getCurrentQuestion()?.let { next ->
                messages.add(ChatMessage(next.text, isUser = false))
                showOptions = true
            }
        } else {
        showOptions = false
        isSearching = true
        scope.launch {
            delay(500)
            val path = TreeAlgorithm.getPath()

            val resultMessage = buildString {
                if (TreeAlgorithm.hasMultipleResults()) {
                    appendLine(recommendedPlacesStr)
                    appendLine()
                    val results = TreeAlgorithm.getResults()
                    results.forEach { (name, address) ->
                        appendLine("$name")
                        appendLine(address)
                        appendLine()
                    }
                } else {
                    val result = TreeAlgorithm.getResult()
                    val address = TreeAlgorithm.getAddress()
                    appendLine(result)
                    appendLine(address)
                    appendLine()
                }
                appendLine(ouputTreeStr)
                path.forEachIndexed { index, step ->
                    val prefix = if (index == path.lastIndex) "└─ " else "├─ "
                    appendLine("$prefix $step")
                }
            }
            messages.add(ChatMessage(resultMessage, isUser = false))
            isSearching = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .statusBarsPadding()
                        .padding(Dimens.paddingLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_tree),
                            contentDescription = stringResource(R.string.algo_tree),
                            modifier = Modifier.size(Dimens.logoSize)
                        )
                        Text(
                            text = stringResource(R.string.algo_tree),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = Dimens.paddingSmall)
                        )
                    }

                    if (showCsvBlock && !userCsvUploaded) {
                        IconButton(
                            onClick = { csvLauncher.launch("text/csv") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = inputTableStr,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = Dimens.paddingLarge),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                    contentPadding = PaddingValues(vertical = Dimens.paddingLarge)
                ) {
                    items(messages) { msg ->
                        MessageBubble(msg)
                    }
                }

                if (!firstStepCompleted && !showCsvBlock) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.paddingLarge),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                    ) {
                        item {
                            AnswerButton(baseTableStr)
                            { handleAnswer(baseTableStr) }
                        }
                        item {
                            AnswerButton(inputTableStr)
                            { handleAnswer(inputTableStr) }
                        }
                    }
                }

                if (showCsvBlock && !userCsvUploaded) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.paddingLarge),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                    ) {
                        item {
                            AnswerButton(baseTableStr) {
                                handleAnswer(baseTableStr)
                                showCsvBlock = false
                            }
                        }
                    }
                }

                if (showOptions && firstStepCompleted && !isSearching) {
                    TreeAlgorithm.getCurrentQuestion()?.let { question ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.paddingLarge)
                                .heightIn(max = Dimens.cardWeight),
                            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.paddingSmall),
                            shape = RoundedCornerShape(Dimens.paddingLarge),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.paddingLarge)
                                    .heightIn(max = Dimens.cardWeight),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                            ) {
                                items(question.options) { option ->
                                    AnswerButton(option) { handleAnswer(option) }
                                }
                            }
                        }
                    }
                }

                if (!showOptions && !isSearching && firstStepCompleted) {
                    Button(
                        onClick = {
                            messages.clear()
                            firstStepCompleted = false
                            userCsvUploaded = false
                            showCsvBlock = false
                            TreeAlgorithm.reset()
                            messages.add(ChatMessage(typeTableStr, isUser = false))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.paddingLarge, vertical = Dimens.paddingSmall)
                    ) {
                        Text(stringResource(R.string.again_button))
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = Dimens.cardWeight)
                .padding(horizontal = Dimens.paddingSmall), colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) MaterialTheme.colorScheme.primary.copy(alpha = AppAlpha.USER_MESSAGE)
                else
                    MaterialTheme.colorScheme.surface
            ), shape = RoundedCornerShape(
                topStart = Dimens.paddingLarge,
                topEnd = Dimens.paddingLarge,
                bottomStart = if (message.isUser) Dimens.paddingLarge else Dimens.buttonSmall,
                bottomEnd = if (message.isUser) Dimens.buttonSmall else Dimens.paddingLarge
            ), elevation = CardDefaults.cardElevation(defaultElevation = Dimens.paddingDefault)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(Dimens.paddingMedium),
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AnswerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.paddingMedium),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = Dimens.paddingMedium),
            style = Typography.bodyMedium
        )
    }
}