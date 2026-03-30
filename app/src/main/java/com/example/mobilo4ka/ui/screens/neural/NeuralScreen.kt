package com.example.mobilo4ka.ui.screens.neural

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.mobilo4ka.R
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.Dimens

@Composable
fun NeuralScreen(){
    SetStatusBarColor(false)
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
                    painter = painterResource(id = R.drawable.ic_neural),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.logoSize)
                )
                Text(
                    text = stringResource(R.string.algo_neural),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(Dimens.paddingSmall)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingLarge),
                elevation = CardDefaults.cardElevation(Dimens.shadowHeight),
                shape = RoundedCornerShape(Dimens.cardCornerRadius),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.paddingLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
                ) {
                    ActionButton(
                        titleRes = R.string.neural_button_recognize,
                        onClick = {}
                    )
                    ActionButton(
                        titleRes = R.string.neural_button_clear,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    titleRes: Int,
    onClick : () -> Unit
){
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground),
        shape = RoundedCornerShape(Dimens.buttonRadius)
    ) {
        Text(
            text = stringResource(titleRes),
            modifier = Modifier.padding(vertical = Dimens.paddingSmall),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}