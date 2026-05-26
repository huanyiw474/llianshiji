package com.lianshiji.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lianshiji.app.data.local.dao.ExerciseDao
import com.lianshiji.app.data.local.dao.FoodDao
import com.lianshiji.app.data.local.dao.TrainingDao
import com.lianshiji.app.data.local.entity.ExerciseEntity
import com.lianshiji.app.data.local.entity.FoodEntryEntity
import com.lianshiji.app.data.local.entity.TrainingEntryEntity

@Database(
    entities = [
        FoodEntryEntity::class,
        TrainingEntryEntity::class,
        ExerciseEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class LianShiJiDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun trainingDao(): TrainingDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var instance: LianShiJiDatabase? = null

        fun getDatabase(context: Context): LianShiJiDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LianShiJiDatabase::class.java,
                    "lian_shi_ji.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
