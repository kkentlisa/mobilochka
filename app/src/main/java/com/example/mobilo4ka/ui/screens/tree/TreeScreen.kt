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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalLayoutApi::class)
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
    val ouputTreeStr = stringResource(R.string.output_tree)

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            TreeAlgorithm.loadPlaces(context)
            TreeAlgorithm.reset()
            messages.add(ChatMessage(typeTableStr, isUser = false))
            showOptions = true
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
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
                        TreeAlgorithm.getResults().forEach { (name, address) ->
                            appendLine("\n$name\n$address")
                        }
                    } else {
                        appendLine("${TreeAlgorithm.getResult()}\n${TreeAlgorithm.getAddress()}")
                    }
                    appendLine("\n$ouputTreeStr")
                    path.forEachIndexed { index, step ->
                        val prefix = if (index == path.lastIndex) "└─ " else "├─ "
                        appendLine("$prefix $step")
                    }
                }
                messages.add(ChatMessage(resultMessage, isUser = false))
                isSearching = false
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TsuBlue)
                        .statusBarsPadding()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_tree),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = stringResource(R.string.algo_tree),
                            color = SurfaceWhite,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    if (showCsvBlock && !userCsvUploaded) {
                        IconButton(onClick = { csvLauncher.launch("text/csv") }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SurfaceWhite)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundLight)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { msg -> MessageBubble(msg) }
                }

                if (!firstStepCompleted && !showCsvBlock) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { AnswerButton(baseTableStr) { handleAnswer(baseTableStr) } }
                        item { AnswerButton(inputTableStr) { handleAnswer(inputTableStr) } }
                    }
                }

                if (showOptions && firstStepCompleted && !isSearching) {
                    TreeAlgorithm.getCurrentQuestion()?.let { question ->
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .padding(16.dp)
                                .heightIn(max = 250.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(question.options) { option -> AnswerButton(option) { handleAnswer(option) } }
                        }
                    }
                }

                if (!showOptions && !isSearching && firstStepCompleted) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Button(
                            onClick = { isShowingStructure = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MapBuilding,
                                contentColor = TextDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Посмотреть структуру дерева")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                messages.clear()
                                TreeAlgorithm.reset()
                                firstStepCompleted = false
                                userCsvUploaded = false
                                showCsvBlock = false
                                messages.add(ChatMessage(typeTableStr, isUser = false))
                                showOptions = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TsuBlue,
                                contentColor = SurfaceWhite
                            ),
                            shape = RoundedCornerShape(8.dp)
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(horizontal = 8.dp),
            color = if (message.isUser) MapWater else SurfaceWhite,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = TextDark,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AnswerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceWhite,
            contentColor = TsuBlue
        ),
        elevation = ButtonDefaults.buttonElevation(2.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}