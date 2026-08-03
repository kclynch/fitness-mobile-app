package com.kclynch.fitness90.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetUpdater {
    suspend fun refresh(context: Context) {
        ChallengeWidget().updateAll(context)
    }
}
