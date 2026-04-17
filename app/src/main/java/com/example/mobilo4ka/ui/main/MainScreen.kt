package com.example.mobilo4ka.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
    onCloseMenu: () -> Unit,
    onToggleLanguage: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(state.isMenuOpen) {
        if (state.isMenuOpen) drawerState.open() else drawerState.close()
    }

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.isClosed && state.isMenuOpen) {
            onCloseMenu()
        }
    }

    SetStatusBarColor(isLight = false)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.primary,
                drawerShape = RoundedCornerShape(Dimens.zeroCornerRadius),
                modifier = Modifier.width(Dimens.menuWidth)
            ) {
                Spacer(Modifier.height(Dimens.paddingExtraLarge))

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(R.string.language_button),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    selected = false,
                    onClick = onToggleLanguage,
                    shape = RoundedCornerShape(Dimens.zeroCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Dimens.paddingLarge)
                )
            }
        }
    ) {
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
                    IconButton(
                        onClick = onToggleMenu
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.menu_open),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(Dimens.iconSize)
                        )
                    }

                    Text(
                        text = stringResource(R.string.header_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(Dimens.paddingSmall)
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge),
                contentPadding = PaddingValues(
                    top = Dimens.paddingLarge,
                    bottom = Dimens.paddingLarge
                )
            ) {
                items(buttons) { button ->
                    AlgorithmButton(
                        titleRes = button.titleRes,
                        iconRes = button.iconRes,
                        onClick = { onNavigate(button.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlgorithmButton(
    titleRes: Int,
    iconRes: Int,
    onClick: () -> Unit
) {
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