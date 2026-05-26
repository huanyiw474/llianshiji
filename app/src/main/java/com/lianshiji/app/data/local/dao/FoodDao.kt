package com.lianshiji.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lianshiji.app.data.local.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<FoodEntryEntity>>

    @Query(
        """
        SELECT * FROM food_entries
        WHERE timestamp >= :startMillis AND timestamp < :endMillis
        ORDER BY timestamp DESC
        """
    )
    fun observeByTimeRange(startMillis: Long, endMillis: Long): Flow<List<FoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: FoodEntryEntity): Long

    @Update
    suspend fun update(food: FoodEntryEntity)

    @Delete
    suspend fun delete(food: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
