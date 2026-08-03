package com.kclynch.fitness90.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.kclynch.fitness90.MainActivity
import com.kclynch.fitness90.R
import com.kclynch.fitness90.data.AppDatabase
import com.kclynch.fitness90.data.ChallengeDates
import com.kclynch.fitness90.data.ChallengeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val ACTION_TOGGLE_TASK = "com.kclynch.fitness90.widget.ACTION_TOGGLE_TASK"
private const val EXTRA_TASK_ID = "extra_task_id"
private const val EXTRA_DAY_NUMBER = "extra_day_number"

private val ITEM_VIEW_IDS = intArrayOf(
    R.id.widget_item_1,
    R.id.widget_item_2,
    R.id.widget_item_3,
    R.id.widget_item_4,
    R.id.widget_item_5,
    R.id.widget_item_6,
    R.id.widget_item_7,
    R.id.widget_item_8
)

/**
 * A plain AppWidgetProvider + hand-built RemoteViews, deliberately avoiding
 * any composable/list-adapter abstraction: every tap directly writes to
 * Room and directly pushes a freshly built RemoteViews tree.
 */
class ChallengeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                pushUpdate(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_TOGGLE_TASK) return

        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val dayNumber = intent.getIntExtra(EXTRA_DAY_NUMBER, -1)
        if (taskId == -1L || dayNumber == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = ChallengeRepository(AppDatabase.getInstance(context))
                repository.toggleChecked(taskId, dayNumber)
                pushUpdate(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        suspend fun pushUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ChallengeWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val views = buildRemoteViews(context)
            manager.updateAppWidget(ids, views)
        }

        private suspend fun buildRemoteViews(context: Context): RemoteViews {
            val repository = ChallengeRepository(AppDatabase.getInstance(context))
            val views = RemoteViews(context.packageName, R.layout.widget_challenge)

            val openAppIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_day_text, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_subtitle_text, openAppIntent)

            val challenge = repository.challenge.first()
            if (challenge == null) {
                views.setTextViewText(R.id.widget_day_text, "90 Day Challenge")
                views.setTextViewText(R.id.widget_subtitle_text, "Tap to set your start date")
                ITEM_VIEW_IDS.forEach { views.setViewVisibility(it, View.GONE) }
                views.setViewVisibility(R.id.widget_more_text, View.GONE)
                return views
            }

            val status = ChallengeDates.status(challenge)
            views.setTextViewText(R.id.widget_day_text, "Day ${status.todayDayNumber} of ${challenge.totalDays}")
            views.setTextViewText(
                R.id.widget_subtitle_text,
                if (status.isFinished) "Challenge complete!" else "${status.daysRemaining} days left"
            )

            val tasks = repository.tasks.first()
            val checks = repository.checksForDay(status.todayDayNumber).first()
            val checkedIds = checks.filter { it.isChecked }.map { it.taskId }.toSet()

            ITEM_VIEW_IDS.forEachIndexed { index, viewId ->
                val task = tasks.getOrNull(index)
                if (task == null) {
                    views.setViewVisibility(viewId, View.GONE)
                    return@forEachIndexed
                }

                views.setViewVisibility(viewId, View.VISIBLE)
                val isChecked = checkedIds.contains(task.id)
                views.setTextViewText(viewId, (if (isChecked) "☑ " else "☐ ") + task.title)

                val toggleIntent = Intent(context, ChallengeWidgetProvider::class.java).apply {
                    action = ACTION_TOGGLE_TASK
                    data = Uri.parse("widget://task/${task.id}")
                    putExtra(EXTRA_TASK_ID, task.id)
                    putExtra(EXTRA_DAY_NUMBER, status.todayDayNumber)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.id.toInt(),
                    toggleIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(viewId, pendingIntent)
            }

            if (tasks.size > ITEM_VIEW_IDS.size) {
                views.setViewVisibility(R.id.widget_more_text, View.VISIBLE)
                views.setTextViewText(R.id.widget_more_text, "+${tasks.size - ITEM_VIEW_IDS.size} more in app")
            } else {
                views.setViewVisibility(R.id.widget_more_text, View.GONE)
            }

            return views
        }
    }
}
