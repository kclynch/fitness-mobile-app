package com.kclynch.fitness90.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class WeightRepository(private val db: WeightDatabase) {

    val allEntries: Flow<List<WeightEntry>> = db.weightDao().observeAll()

    suspend fun logWeight(date: LocalDate, weightLbs: Float) {
        val existing = db.weightDao().findByDate(date.toEpochDay())
        db.weightDao().upsert(
            WeightEntry(id = existing?.id ?: 0, epochDay = date.toEpochDay(), weightLbs = weightLbs)
        )
    }
}
