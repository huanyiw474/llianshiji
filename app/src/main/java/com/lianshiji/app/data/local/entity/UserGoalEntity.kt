package com.lianshiji.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_goals")
data class UserGoalEntity(
    @PrimaryKey val id: Int = 1,
    val dailyCalories: Int = 2200,
    val proteinGrams: Float = 120f,
    val carbsGrams: Float = 250f,
    val fatGrams: Float = 70f,
    val weeklyTrainingCount: Int = 4
)
