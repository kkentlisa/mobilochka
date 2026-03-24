package com.example.mobilo4ka.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.mobilo4ka.R
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.ButtonLarge
import com.example.mobilo4ka.ui.theme.Dimens


data class ButtonData(
    val titleRes: Int,
    val iconRes: Int,
    val route: String
)

private val buttons = listOf(
    ButtonData(R.string.algo_astar, R.drawable.ic_astar, "astar"),
    ButtonData(R.string.algo_clustering, R.drawable.ic_clustering, "clustering"),
    ButtonData(R.string.algo_genetic, R.drawable.ic_genetic, "genetic"),
    ButtonData(R.string.algo_ants, R.drawable.ic_ants, "ants"),
    ButtonData(R.string.algo_tree, R.drawable.ic_tree, "tree"),
    ButtonData(R.string.algo_neural, R.drawable.ic_neural, "neural")
)

@Composable
fun MainScreen(
    state: MainUiState,
    onToggleMenu: () -> Unit,
    onNavigate: (String) -> Unit
){
    SetStatusBarColor(isLight = false)

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
                    painter = painterResource(id = R.drawable.logo_tsu_white),
                    contentDescription = stringResource(R.string.tsu_logo),
                    modifier = Modifier.size(Dimens.logoSize)
                )
                Text(
                    text = stringResource(R.string.header_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(Dimens.paddingSmall)
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onToggleMenu
                ) {
                    Icon(
                        imageVector = if (state.isMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (state.isMenuExpanded) stringResource(R.string.menu_close)
                        else stringResource(R.string.menu_open),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    ) { paddingValues ->
        Box (modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge),
                contentPadding = PaddingValues(
                    top = Dimens.paddingLarge,
                    bottom = Dimens.paddingLarge
                ),
                userScrollEnabled = !state.isMenuExpanded
            ) {
                items(buttons) { button ->
                    AlgorithmButton(
                        titleRes = button.titleRes,
                        iconRes = button.iconRes,
                        onClick = { onNavigate(button.route) }
                    )
                }
            }

            if (state.isMenuExpanded){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                        .clickable (
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onToggleMenu() }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(top = Dimens.topBarHeight)
                        .padding(Dimens.paddingLarge)
                        .align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {}
                            .padding(Dimens.paddingMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = stringResource(R.string.language_button),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                    }
                }
            }
        }
    }
}

@Composable
private fun AlgorithmButton(
    titleRes: Int,
    iconRes: Int,
    onClick : () -> Unit
){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingLarge)
            .height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = Dimens.shadowHeight)

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSize),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.width(Dimens.paddingMedium))

            Text(
                text = stringResource(titleRes),
                style = ButtonLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Left
            )
        }
    }
}