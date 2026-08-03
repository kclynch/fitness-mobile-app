package com.kclynch.fitness90.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kclynch.fitness90.MainActivity
import com.kclynch.fitness90.data.AppDatabase
import com.kclynch.fitness90.data.ChallengeDates
import com.kclynch.fitness90.data.ChallengeRepository
import com.kclynch.fitness90.data.ChecklistTask
import kotlinx.coroutines.flow.first

val taskIdKey = ActionParameters.Key<Long>("taskId")
val dayNumberKey = ActionParameters.Key<Int>("dayNumber")

private val GreenPrimary = Color(0xFF2E7D32)
private val GreenContainer = Color(0xFFDCEDC8)
private val White = Color(0xFFFFFFFF)

class ChallengeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = ChallengeRepository(AppDatabase.getInstance(context))
        val challenge = repository.challenge.first()

        if (challenge == null) {
            provideContent { NotStartedContent() }
            return
        }

        val status = ChallengeDates.status(challenge)
        val tasks = repository.tasks.first()
        val checks = repository.checksForDay(status.todayDayNumber).first()
        val checkedIds = checks.filter { it.isChecked }.map { it.taskId }.toSet()

        provideContent {
            ChallengeContent(
                dayNumber = status.todayDayNumber,
                totalDays = challenge.totalDays,
                daysRemaining = status.daysRemaining,
                isFinished = status.isFinished,
                tasks = tasks,
                checkedIds = checkedIds
            )
        }
    }
}

@Composable
private fun NotStartedContent() {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GreenContainer)
            .cornerRadius(16.dp)
            .padding(16.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "90 Day Challenge",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ColorProvider(GreenPrimary)
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "Tap to set your start date",
            style = TextStyle(fontSize = 13.sp, color = ColorProvider(GreenPrimary))
        )
    }
}

@Composable
private fun ChallengeContent(
    dayNumber: Int,
    totalDays: Int,
    daysRemaining: Int,
    isFinished: Boolean,
    tasks: List<ChecklistTask>,
    checkedIds: Set<Long>
) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(White)
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "Day $dayNumber of $totalDays",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ColorProvider(GreenPrimary)
                    )
                )
                Text(
                    text = if (isFinished) "Challenge complete!" else "$daysRemaining days left",
                    style = TextStyle(fontSize = 12.sp, color = ColorProvider(GreenPrimary))
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (tasks.isEmpty()) {
            Text(
                text = "Open the app to add checklist items.",
                style = TextStyle(fontSize = 13.sp, color = ColorProvider(GreenPrimary))
            )
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                items(tasks, itemId = { it.id }) { task ->
                    val isChecked = checkedIds.contains(task.id)
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable(
                                actionRunCallback<ToggleTaskAction>(
                                    actionParametersOf(
                                        taskIdKey to task.id,
                                        dayNumberKey to dayNumber
                                    )
                                )
                            ),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = if (isChecked) "☑" else "☐",
                            style = TextStyle(fontSize = 18.sp, color = ColorProvider(GreenPrimary))
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = task.title,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = ColorProvider(GreenPrimary),
                                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }
                }
            }
        }
    }
}
