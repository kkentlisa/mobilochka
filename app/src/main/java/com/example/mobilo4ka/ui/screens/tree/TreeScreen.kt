package com.example.mobilo4ka.ui.screens.tree

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobilo4ka.R
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.ButtonLarge
import com.example.mobilo4ka.ui.theme.Dimens
import com.example.mobilo4ka.ui.theme.Mobilo4kaTheme

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class Question(
    val text: String,
    val options: List<String>
)


@Composable
fun TreeScreen() {
    Mobilo4kaTheme {
        TreeScreenContent()
    }
}

@Composable
fun TreeScreenContent() {
    SetStatusBarColor(true)

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var showOptions by remember { mutableStateOf(true) }

    val questions = listOf(
        Question("Где вы находитесь?",
            listOf("Главный корпус", "Кампусная территория ",
                "Московский тракт", "Автобусная остановка ТГУ", "Общежитие 5, 6", "Горсад")),
        Question("Какой у вас бюджет?",
            listOf("Маленький", "Средний", "Большой")),
        Question("Сколько времени есть?",
            listOf("Меньше 5 мин", "5-15 мин", "Более 15 мин")),
        Question("Что вам нужно?",
            listOf("Полноценный прием пищи", "Продукты", "Блины", "Выпечка",
                "Кофе", "Напитки", "Перекус", "Мороженное")),
        Question("Готовы ждать очередь?",
            listOf("Да", "Нет")),
        Question("Какая погода?",
            listOf("Хорошая", "Плохая"))
    )

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages = messages + ChatMessage(questions[0].text, isUser = false)
        }
    }

    fun handleAnswer(answer: String) {
        messages = messages + ChatMessage(answer, isUser = true)

        if (currentQuestionIndex + 1 < questions.size) {
            currentQuestionIndex++
            messages = messages + ChatMessage(questions[currentQuestionIndex].text, isUser = false)
        } else {
            showOptions = false
            messages = messages + ChatMessage("Спасибо за ответы! Ищу подходящее место...", isUser = false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(id = com.example.mobilo4ka.R.drawable.ic_tree),
                        contentDescription = stringResource(R.string.algo_tree),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Помощник выбора места",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

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
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        )
                    ) {
                        items(questions[currentQuestionIndex].options) { option ->
                            AnswerButton(
                                text = option,
                                onClick = { handleAnswer(option) }
                            )
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
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AnswerButton(
    text: String,
    onClick: () -> Unit
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
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 8.dp),
            style = ButtonLarge
        )
    }
}