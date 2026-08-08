package com.kclynch.fitness90.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kclynch.fitness90.data.DayCompletionCategory

private val PerfectGreen = Color(0xFF2E7D32)
private val MissedOneGreen = Color(0xFF2E7D32)
private val MissedYellow = Color(0xFFFBC02D)
private val MissedRed = Color(0xFFE53935)

fun DayCompletionCategory.color(): Color = when (this) {
    DayCompletionCategory.PERFECT -> PerfectGreen
    DayCompletionCategory.GREEN -> MissedOneGreen
    DayCompletionCategory.YELLOW -> MissedYellow
    DayCompletionCategory.RED -> MissedRed
}

@Composable
fun DayCategorySummary(counts: Map<DayCompletionCategory, Int>, modifier: Modifier = Modifier) {
    val total = remember(counts) { counts.values.sum() }
    Column(modifier = modifier) {
        Text(
            text = "Your track record ($total ${if (total == 1) "day" else "days"} scored)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.size(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryStat(
                label = "Perfect",
                count = counts[DayCompletionCategory.PERFECT] ?: 0,
                color = DayCompletionCategory.PERFECT.color(),
                showStar = true
            )
            CategoryStat(
                label = "Missed 1",
                count = counts[DayCompletionCategory.GREEN] ?: 0,
                color = DayCompletionCategory.GREEN.color()
            )
            CategoryStat(
                label = "Missed 2-3",
                count = counts[DayCompletionCategory.YELLOW] ?: 0,
                color = DayCompletionCategory.YELLOW.color()
            )
            CategoryStat(
                label = "Missed 4+",
                count = counts[DayCompletionCategory.RED] ?: 0,
                color = DayCompletionCategory.RED.color()
            )
        }
    }
}

@Composable
private fun CategoryStat(label: String, count: Int, color: Color, showStar: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (showStar) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(4.dp))
        Text(text = "$count", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
