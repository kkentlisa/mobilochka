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
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.Dimens
import com.example.mobilo4ka.ui.theme.Mobilo4kaTheme
import com.example.mobilo4ka.ui.theme.Typography

data class ChatMessage(
    val text: String?,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class Question(
    val text: String,
    val options: List<String?>
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

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var showOptions by remember { mutableStateOf(true) }

    val questions = listOf(
        Question(context.getString(R.string.question_location), context.resources.getStringArray(R.array.options_location).toList()),
        Question(context.getString(R.string.question_budget), context.resources.getStringArray(R.array.options_budget).toList()),
        Question(context.getString(R.string.question_time), context.resources.getStringArray(R.array.options_time).toList()),
        Question(context.getString(R.string.question_food), context.resources.getStringArray(R.array.options_food).toList()),
        Question(context.getString(R.string.question_queue), context.resources.getStringArray(R.array.options_queue).toList()),
        Question(context.getString(R.string.question_weather), context.resources.getStringArray(R.array.options_weather).toList()),

    )

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages = messages + ChatMessage( questions[0].text, isUser = false)
        }
    }

    fun handleAnswer(answer: String?) {
        messages = messages + ChatMessage(answer, isUser = true)

        if (currentQuestionIndex + 1 < questions.size) {
            currentQuestionIndex++
            messages = messages + ChatMessage(questions[currentQuestionIndex].text, isUser = false)
        } else {
            showOptions = false
            messages = messages + ChatMessage(context.getString(R.string.answer_find), isUser = false)
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message = message)
                    }
                }

                if (showOptions && currentQuestionIndex < questions.size) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.paddingLarge)
                            .heightIn(max = 300.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(questions[currentQuestionIndex].options) { option ->
                                AnswerButton(
                                    text = option,
                                    onClick = { handleAnswer(option) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
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
                .widthIn(max = 280.dp)
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
            message.text?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AnswerButton(
    text: String?,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (text != null) {
            Text(
                text = text,
                modifier = Modifier.padding(vertical = 8.dp),
                style = Typography.bodyMedium
            )
        }
    }
}