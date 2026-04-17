package com.example.mobilo4ka.ui.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.mobilo4ka.R
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.ui.theme.AppAlpha
import com.example.mobilo4ka.ui.theme.Dimens
import com.example.mobilo4ka.ui.theme.lineHeight

@Composable
fun getDrawableByCategory(category: String?): Int {
    return when (category?.lowercase()?.trim()) {
        "apricot" -> R.drawable.apricot
        "rostics" -> R.drawable.rostics
        "yarche" -> R.drawable.yarche
        "coffee_machine" -> R.drawable.coffee_machine
        "baba_roma" -> R.drawable.baba_roma
        "bristol" -> R.drawable.bristol
        "crazy" -> R.drawable.crazy
        "dining_room_1" -> R.drawable.dining_room_1
        "penguins_33" -> R.drawable.penguins_33
        "point" -> R.drawable.point
        "siberian_pancakes" -> R.drawable.siberian_pancakes
        "starbooks" -> R.drawable.starbooks
        else -> R.drawable.download
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingBottomSheet(
    building: Building,
    rating: Float?,
    onDismiss: () -> Unit,
    onLeaveReviewClick: () -> Unit,
    placeStr: String = stringResource(R.string.building),
    workStr: String = stringResource(R.string.work),
    menuStr: String = stringResource(R.string.menu),
    rateStr: String = stringResource(R.string.rate_place),
    noRatingsStr: String = stringResource(R.string.no_ratings)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(
            topStart = Dimens.cornerExtraLarge,
            topEnd = Dimens.cornerExtraLarge
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingExtraLarge),
            contentPadding = PaddingValues(bottom = Dimens.paddingBottomScale)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.paddingSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(Dimens.cornerExtraLarge),
                        modifier = Modifier
                            .size(width = Dimens.bannerSize, height = Dimens.bannerSize),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.paddingExtraSmall)
                    ) {
                        Image(
                            painter = painterResource(id = getDrawableByCategory(building.category)),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.paddingExtraLarge))

                Text(
                    text = building.name ?: placeStr,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = lineHeight
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = Dimens.paddingSmall)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.btn_star_big_on),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(Dimens.paddingLarge)
                    )
                    Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                    Text(
                        text = if (rating != null) "$rating" else noRatingsStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onLeaveReviewClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.paddingMedium),
                    shape = RoundedCornerShape(Dimens.paddingSmall)
                ) {
                    Text(text = rateStr)
                }

                if (!building.openTime.isNullOrBlank()) {
                    Text(
                        text = "$workStr ${building.openTime} — ${building.closeTime}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = Dimens.paddingMedium)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Dimens.paddingSmall),
                    thickness = Dimens.dividerThickness,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            if (building.menu.isNotEmpty()) {
                item {
                    Text(
                        text = menuStr,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            top = Dimens.paddingSmall,
                            bottom = Dimens.paddingMedium
                        )
                    )
                }

                items(building.menu) { item ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.COLOR),
                        shape = RoundedCornerShape(Dimens.paddingSmall),
                        modifier = Modifier
                            .padding(vertical = Dimens.paddingExtraSmall)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = item.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(
                                horizontal = Dimens.paddingLarge,
                                vertical = Dimens.paddingMedium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}