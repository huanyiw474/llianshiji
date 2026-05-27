package com.lianshiji.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lianshiji.app.data.local.entity.UserGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGoalDao {
    @Query("SELECT * FROM user_goals WHERE id = 1")
    fun observeGoals(): Flow<UserGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(goals: UserGoalEntity)

    @Update
    suspend fun update(goals: UserGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goals: UserGoalEntity)
}
