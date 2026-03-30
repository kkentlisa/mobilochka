package com.example.mobilo4ka.ui.screens.tree

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobilo4ka.R
import com.example.mobilo4ka.algorithms.tree.TreeAlgorithm
import com.example.mobilo4ka.algorithms.tree.Question
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.AppAlpha
import com.example.mobilo4ka.ui.theme.Dimens
import com.example.mobilo4ka.ui.theme.Mobilo4kaTheme
import com.example.mobilo4ka.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String?,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val id: Long = System.currentTimeMillis()
)

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

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var showOptions by remember { mutableStateOf(true) }
    var currentQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        TreeAlgorithm.loadPlaces(context)
        currentQuestions = TreeAlgorithm.getQuestions(context)
        if (currentQuestions.isNotEmpty()) {
            messages.add(ChatMessage(currentQuestions[0].text, isUser = false))
        }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun showSearchingMessage() {
        val searchingMessage = ChatMessage(context.getString(R.string.answer_find), isUser = false)
        messages.add(searchingMessage)

        scope.launch {
            delay(1000)
            messages.remove(searchingMessage)
        }
    }

    fun handleAnswer(answer: String?) {
        if (answer == null || currentQuestions.isEmpty() || isSearching) return

        val currentQuestion = currentQuestions[currentQuestionIndex]

        messages.add(ChatMessage(answer, isUser = true))

        TreeAlgorithm.filterByAnswer(currentQuestion.typeAnswer, answer)

        val updatedQuestions = TreeAlgorithm.getQuestions(context)

        var nextIndex = currentQuestionIndex + 1
        while (nextIndex < updatedQuestions.size && updatedQuestions[nextIndex].options.size <= 1) {
            val autoQuestion = updatedQuestions[nextIndex]
            TreeAlgorithm.filterByAnswer(autoQuestion.typeAnswer, autoQuestion.options.first())
            nextIndex++
        }

        if (nextIndex < updatedQuestions.size) {
            currentQuestions = updatedQuestions
            currentQuestionIndex = nextIndex
            messages.add(ChatMessage(currentQuestions[currentQuestionIndex].text, isUser = false))
        } else {
            showSearchingMessage()
            isSearching = true

            scope.launch {
                delay(1000)

                val finalPlaces = TreeAlgorithm.getFinalPlaces()
                showOptions = false
                isSearching = false

                val resultMessage = buildString {
                    if (finalPlaces.isNotEmpty()) {
                        appendLine("${context.getString(R.string.count_answer)}${finalPlaces.size}")
                        appendLine()
                        finalPlaces.forEach { place ->
                            appendLine("   ${place.recommendedPlace}")
                            appendLine("   ${place.address}")
                            appendLine()
                        }
                    }
                }
                messages.add(ChatMessage(resultMessage, isUser = false))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .statusBarsPadding()
                        .padding(Dimens.paddingLarge),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_tree),
                        contentDescription = stringResource(R.string.algo_tree),
                        modifier = Modifier.size(Dimens.logoSize)
                    )
                    Text(
                        text = context.getString(R.string.algo_tree),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(start = Dimens.paddingSmall)
                    )
                }
            }) { paddingValues ->
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
                    items(messages) { message ->
                        MessageBubble(message = message)
                    }
                }

                if (showOptions && currentQuestions.isNotEmpty() && currentQuestionIndex < currentQuestions.size && !isSearching) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.paddingLarge)
                            .heightIn(max = 400.dp),
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
                                .padding(Dimens.paddingLarge),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                        ) {
                            items(currentQuestions[currentQuestionIndex].options) { option ->
                                AnswerButton(
                                    text = option,
                                    onClick = { handleAnswer(option) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                if (!showOptions && !isSearching) {
                    Button(
                        onClick = {
                            messages.clear()
                            currentQuestionIndex = 0
                            showOptions = true
                            currentQuestions = TreeAlgorithm.getQuestions(context)
                            TreeAlgorithm.reset()
                            currentQuestions = TreeAlgorithm.getQuestions(context)
                            if (currentQuestions.isNotEmpty()) {
                                messages.add(ChatMessage(currentQuestions[0].text, isUser = false))
                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = Dimens.paddingLarge, vertical = Dimens.paddingSmall
                            ), colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(context.getString(R.string.again_button))
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
                else MaterialTheme.colorScheme.surface
            ), shape = RoundedCornerShape(
                topStart = Dimens.paddingLarge,
                topEnd = Dimens.paddingLarge,
                bottomStart = if (message.isUser) Dimens.paddingLarge else Dimens.buttonSmall,
                bottomEnd = if (message.isUser) Dimens.buttonSmall else Dimens.paddingLarge
            ), elevation = CardDefaults.cardElevation(defaultElevation = Dimens.paddingDefault)
        ) {
            message.text?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(Dimens.paddingMedium),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AnswerButton(
    text: String?, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick, modifier = modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ), shape = RoundedCornerShape(Dimens.paddingMedium)
    ) {
        if (text != null) {
            Text(
                text = text,
                modifier = Modifier.padding(vertical = Dimens.paddingMedium),
                style = Typography.bodyMedium
            )
        }
    }
}