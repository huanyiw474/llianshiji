package com.lianshiji.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val name: String,
    val targetMuscle: String,
    val instruction: String,
    val commonMistakes: String,
    val recommendedSetsReps: String
)
