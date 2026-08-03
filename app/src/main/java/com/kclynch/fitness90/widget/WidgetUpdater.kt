package com.kclynch.fitness90.widget

import android.content.Context

object WidgetUpdater {
    suspend fun refresh(context: Context) {
        ChallengeWidgetProvider.pushUpdate(context)
    }
}
