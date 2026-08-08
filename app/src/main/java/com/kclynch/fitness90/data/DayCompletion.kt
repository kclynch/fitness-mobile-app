package com.kclynch.fitness90.data

/**
 * How a *past* day (one that's already over) shook out, based on how many
 * checklist items got missed. Only meaningful once a day has ended — you
 * can't know what was "missed" while it's still in progress.
 */
enum class DayCompletionCategory {
    PERFECT,
    GREEN,
    YELLOW,
    RED
}

/**
 * Null when there were no tasks to complete at all (nothing to score).
 */
fun completionCategory(completed: Int, total: Int): DayCompletionCategory? {
    if (total <= 0) return null
    val missed = total - completed
    return when {
        missed <= 0 -> DayCompletionCategory.PERFECT
        missed == 1 -> DayCompletionCategory.GREEN
        missed <= 3 -> DayCompletionCategory.YELLOW
        else -> DayCompletionCategory.RED
    }
}
