package com.lianshiji.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_entries")
data class TrainingEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val performedAt: Long,
    val bodyPart: String,
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Float,
    val note: String
)
