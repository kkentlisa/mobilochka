package com.example.mobilo4ka.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.mobilo4ka.R
import com.example.mobilo4ka.ui.theme.Dimens

@Composable
fun MainScreen(){
    var isMenuExpanded by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf("ru") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.paddingLarge),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Image(
                        painter = painterResource(id = R.drawable.logo_tsu_white),
                        contentDescription = stringResource(R.string.tsu_logo),
                        modifier = Modifier.size(Dimens.logoSize)
                    )

                    Text(
                        text = stringResource(R.string.header_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(start = Dimens.paddingSmall)
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = {isMenuExpanded = !isMenuExpanded}
                    ) {
                        Icon(
                            imageVector = if (isMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isMenuExpanded) stringResource(R.string.menu_close)
                                else stringResource(R.string.menu_open),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }

                if (isMenuExpanded){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.paddingLarge)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentLanguage = if (currentLanguage == "ru") "en" else "ru"
                                }
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .clickable {
                    if (isMenuExpanded) {
                        isMenuExpanded = false
                    }
                }
        )
    }
}