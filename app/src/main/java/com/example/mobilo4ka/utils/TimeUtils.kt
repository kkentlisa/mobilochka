package com.example.mobilo4ka.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import com.example.mobilo4ka.R
import java.time.LocalTime
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun parseTimeToMinutes(timeStr: String?): Int {
    if (timeStr.isNullOrBlank()) return 1440
    return try {
        val cleaned = timeStr.replace("\u00A0", " ").trim()
        val formatters = listOf(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm"),
            java.time.format.DateTimeFormatter.ofPattern("H:mm"),
            java.time.format.DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
            java.time.format.DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        )

        var time: LocalTime? = null
        for (formatter in formatters) {
            try {
                time = LocalTime.parse(cleaned, formatter)
                break
            } catch (_: Exception) {}
        }

        time?.let { it.hour * 60 + it.minute } ?: 1440
    } catch (e: Exception) {
        1440
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun formatToString(timeString: String?, format: String): String {
    if (timeString.isNullOrBlank()) return ""
    val totalMinutes = parseTimeToMinutes(timeString)
    return formatTimeToMinutes(totalMinutes, format)
}

@Composable
fun formatTimeToMinutes(totalMinutes: Int, format: String): String {
    val hoursTotal = totalMinutes / 60
    val hours24 = hoursTotal % 24
    val minutes = totalMinutes % 60

    val isExactTimeWithAmPm = format.contains("a", ignoreCase = true)

    return if (isExactTimeWithAmPm) {
        val isPm = hours24 >= 12
        val hours12 = when {
            hours24 == 0 -> 12
            hours24 > 12 -> hours24 - 12
            else -> hours24
        }
        val amPm = if (isPm) "PM" else "AM"

        format
            .replace("HH", String.format("%02d", hours12))
            .replace("mm", String.format("%02d", minutes))
            .replace("a", amPm)
    } else {
        format
            .replace("HH", String.format("%02d", hours24))
            .replace("H", hours24.toString())
            .replace("mm", String.format("%02d", minutes))
            .replace("totalH", hoursTotal.toString())
    }
}