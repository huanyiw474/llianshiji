package com.lianshiji.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lianshiji.app.data.local.entity.TrainingEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training_entries ORDER BY performedAt DESC, id DESC")
    fun observeAll(): Flow<List<TrainingEntryEntity>>

    @Query(
        """
        SELECT * FROM training_entries
        WHERE performedAt >= :startMillis AND performedAt < :endMillis
        ORDER BY performedAt DESC, id DESC
        """
    )
    fun observeByTimeRange(startMillis: Long, endMillis: Long): Flow<List<TrainingEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(training: TrainingEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trainings: List<TrainingEntryEntity>)

    @Update
    suspend fun update(training: TrainingEntryEntity)

    @Delete
    suspend fun delete(training: TrainingEntryEntity)

    @Query("DELETE FROM training_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT * FROM training_entries
        WHERE performedAt >= :startMillis AND performedAt < :endMillis
        ORDER BY performedAt ASC, id ASC
        """
    )
    suspend fun listByTimeRange(startMillis: Long, endMillis: Long): List<TrainingEntryEntity>

    @Query(
        """
        SELECT * FROM training_entries
        WHERE performedAt < :beforeMillis
        ORDER BY performedAt DESC, id ASC
        """
    )
    suspend fun listBefore(beforeMillis: Long): List<TrainingEntryEntity>

    @Query("DELETE FROM training_entries")
    suspend fun deleteAll()
}
