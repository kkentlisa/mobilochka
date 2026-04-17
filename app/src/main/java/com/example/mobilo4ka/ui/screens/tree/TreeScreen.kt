@file:OptIn(ExperimentalLayoutApi::class)

package com.example.mobilo4ka.ui.screens.tree

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.mobilo4ka.R
import com.example.mobilo4ka.algorithms.tree.TreeAlgorithm
import com.example.mobilo4ka.ui.main.Language
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun TreeScreen(currentLanguage: Language) {
    TreeScreenContent(currentLanguage)
}

@Composable
fun TreeScreenContent(currentLanguage: Language) {

    SetStatusBarColor(false)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var isShowingStructure by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<ChatMessage>() }

    var showOptions by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var firstStepCompleted by remember { mutableStateOf(false) }
    var userCsvUploaded by remember { mutableStateOf(false) }
    var showCsvBlock by remember { mutableStateOf(false) }

    val typeTableStr = stringResource(R.string.type_table)
    val baseTableStr = stringResource(R.string.base_table)
    val inputTableStr = stringResource(R.string.input_table)
    val downloadFileStr = stringResource(R.string.download_file)
    val errorFileStr = stringResource(R.string.error_file)
    val recommendedPlacesStr = stringResource(R.string.recommended_places)
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            TreeAlgorithm.loadPlaces(context)
            TreeAlgorithm.reset()
            messages.add(ChatMessage(typeTableStr, false))
            showOptions = true
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                val csvContent = it.readText()
                TreeAlgorithm.saveUserCsv(context, csvContent)
                TreeAlgorithm.loadPlaces(context, "user_places.csv")
                TreeAlgorithm.reset()

                TreeAlgorithm.getCurrentQuestion()?.let { first ->
                    messages.add(ChatMessage(first.text, false))
                }

                userCsvUploaded = true
                showCsvBlock = false
                firstStepCompleted = true
            }
        } catch (e: Exception) {
            messages.add(ChatMessage("$errorFileStr ${e.message}", false))
        }
    }

    fun handleAnswer(answer: String) {
        if (isSearching) return

        messages.add(ChatMessage(answer, true))

        if (!firstStepCompleted) {
            when (answer) {
                baseTableStr -> {
                    TreeAlgorithm.loadPlaces(context)
                    TreeAlgorithm.reset()
                    firstStepCompleted = true
                }
                inputTableStr -> {
                    showCsvBlock = true
                    messages.add(ChatMessage(downloadFileStr, false))
                    return
                }
            }

            TreeAlgorithm.getCurrentQuestion()?.let {
                messages.add(ChatMessage(it.text, false))
                showOptions = true
            }
            return
        }

        TreeAlgorithm.answerSelected(answer)

        showOptions = false
        showOptions = true

        if (!TreeAlgorithm.isFinished()) {
            TreeAlgorithm.getCurrentQuestion()?.let {
                messages.add(ChatMessage(it.text, false))
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
                        TreeAlgorithm.getResults().forEach { (name, address) ->
                            appendLine("\n$name\n$address")
                        }
                    } else {
                        appendLine(recommendedPlacesStr)
                        appendLine()
                        appendLine(TreeAlgorithm.getResult())
                        appendLine(TreeAlgorithm.getAddress())
                    }
                }

                messages.add(ChatMessage(resultMessage, false))
                isSearching = false
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {

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
                            painter = painterResource(R.drawable.ic_tree),
                            contentDescription = null,
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
                        IconButton(onClick = { csvLauncher.launch("text/csv") }) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface)
            ) {

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.paddingLarge),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                    contentPadding = PaddingValues(vertical = Dimens.paddingLarge)
                ) {
                    items(messages) { MessageBubble(it) }
                }

                if (!firstStepCompleted && !showCsvBlock) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.padding(Dimens.paddingLarge),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                    ) {
                        item { AnswerButton(baseTableStr) { handleAnswer(baseTableStr) } }
                        item { AnswerButton(inputTableStr) { handleAnswer(inputTableStr) } }
                    }
                }

                if (showOptions && firstStepCompleted && !isSearching) {
                    TreeAlgorithm.getCurrentQuestion()?.let { q ->
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .padding(Dimens.paddingLarge)
                                .heightIn(max = Dimens.cardWeight),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                        ) {
                            items(q.options) {
                                AnswerButton(it) { handleAnswer(it) }
                            }
                        }
                    }
                }

                if (!showOptions && !isSearching && firstStepCompleted) {
                    Column(Modifier.padding(Dimens.paddingLarge)) {

                        Button(
                            onClick = { isShowingStructure = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Dimens.paddingMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(context.getString(R.string.tree_structure))
                        }

                        Spacer(Modifier.height(Dimens.paddingSmall))

                        Button(
                            onClick = {
                                messages.clear()
                                TreeAlgorithm.reset()
                                firstStepCompleted = false
                                userCsvUploaded = false
                                showCsvBlock = false

                                messages.add(ChatMessage(typeTableStr, false))
                                showOptions = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Dimens.paddingMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(R.string.again_button))
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isShowingStructure,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            TreeStructureScreen(onBack = { isShowingStructure = false })
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {

    val shape = if (message.isUser) {
        RoundedCornerShape(
            topStart = Dimens.paddingLarge,
            topEnd = Dimens.paddingLarge,
            bottomStart = Dimens.paddingLarge,
            bottomEnd = Dimens.buttonSmall
        )
    } else {
        RoundedCornerShape(Dimens.paddingLarge)
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {

        Surface(
            modifier = Modifier.widthIn(max = Dimens.cardWeight),
            shape = shape,
            color = if (message.isUser)
                MaterialTheme.colorScheme.primary.copy(alpha = AppAlpha.USER_MESSAGE)
            else
                MaterialTheme.colorScheme.surface,
            shadowElevation = Dimens.paddingDefault
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(Dimens.paddingMedium),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AnswerButton(text: String, onClick: () -> Unit) {

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.buttonQuestions),
        shape = RoundedCornerShape(Dimens.paddingMedium),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = Dimens.paddingDefault),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
