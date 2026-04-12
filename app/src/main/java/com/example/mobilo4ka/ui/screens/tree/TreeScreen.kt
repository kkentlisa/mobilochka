package com.example.mobilo4ka.ui.screens.tree

import android.content.Context
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
import androidx.compose.ui.unit.dp
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
import com.example.mobilo4ka.ui.theme.Dimens
import com.example.mobilo4ka.ui.theme.Dimens.cardWeight

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun TreeScreen() {
    Mobilo4kaTheme {
        TreeScreenContent()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TreeScreenContent() {
    SetStatusBarColor(false)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var showOptions by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var firstStepCompleted by remember { mutableStateOf(false) }
    var userCsvUploaded by remember { mutableStateOf(false) }
    var showCsvBlock by remember { mutableStateOf(false) }

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
                    messages.add(ChatMessage(context.getString(R.string.error_file) + "${e.message}", isUser = false))
                }
            }
        }
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    LaunchedEffect(Unit) {
        messages.add(ChatMessage(context.getString(R.string.type_table), isUser = false))
        showOptions = true
    }

    fun handleAnswer(answer: String) {
        if (isSearching) return
        messages.add(ChatMessage(answer, isUser = true))

        if (!firstStepCompleted) {
            when (answer) {
                context.getString(R.string.base_table) -> {
                    TreeAlgorithm.loadPlaces(context)
                    TreeAlgorithm.reset()
                    firstStepCompleted = true
                }
                context.getString(R.string.input_table) -> {
                    showCsvBlock = true
                    messages.add(ChatMessage(context.getString(R.string.download_file), isUser = false))
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
                    appendLine(context.getString(R.string.recommended_places))
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
                appendLine(context.getString(R.string.output_tree))
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
                                contentDescription = context.getString(R.string.input_table),
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
                            AnswerButton(context.getString(R.string.base_table))
                            { handleAnswer(context.getString(R.string.base_table)) }
                        }
                        item {
                            AnswerButton(context.getString(R.string.input_table))
                            { handleAnswer(context.getString(R.string.input_table)) }
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
                            AnswerButton(context.getString(R.string.base_table)) {
                                handleAnswer(context.getString(R.string.base_table))
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
                            messages.add(ChatMessage(context.getString(R.string.type_table), isUser = false))
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