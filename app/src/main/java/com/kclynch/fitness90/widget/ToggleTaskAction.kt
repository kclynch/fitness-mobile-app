package com.kclynch.fitness90.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.kclynch.fitness90.data.AppDatabase
import com.kclynch.fitness90.data.ChallengeRepository

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[taskIdKey] ?: return
        val dayNumber = parameters[dayNumberKey] ?: return

        val repository = ChallengeRepository(AppDatabase.getInstance(context))
        repository.toggleChecked(taskId, dayNumber)

        // Refresh this specific instance rather than updateAll() — list-item
        // actions need to target the glanceId they were triggered from for
        // the widget's own LazyColumn to reliably redraw after the tap.
        ChallengeWidget().update(context, glanceId)
    }
}
