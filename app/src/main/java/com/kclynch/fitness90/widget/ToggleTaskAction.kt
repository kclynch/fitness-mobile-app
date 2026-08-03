package com.kclynch.fitness90.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.kclynch.fitness90.data.AppDatabase
import com.kclynch.fitness90.data.ChallengeRepository

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[taskIdKey] ?: return
        val dayNumber = parameters[dayNumberKey] ?: return

        val repository = ChallengeRepository(AppDatabase.getInstance(context))
        repository.toggleChecked(taskId, dayNumber)

        ChallengeWidget().updateAll(context)
    }
}
