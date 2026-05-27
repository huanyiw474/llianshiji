package com.lianshiji.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lianshiji.app.data.local.dao.ExerciseDao
import com.lianshiji.app.data.local.dao.FoodDao
import com.lianshiji.app.data.local.dao.TrainingDao
import com.lianshiji.app.data.local.dao.UserGoalDao
import com.lianshiji.app.data.local.entity.ExerciseEntity
import com.lianshiji.app.data.local.entity.FoodEntryEntity
import com.lianshiji.app.data.local.entity.TrainingEntryEntity
import com.lianshiji.app.data.local.entity.UserGoalEntity

@Database(
    entities = [
        FoodEntryEntity::class,
        TrainingEntryEntity::class,
        ExerciseEntity::class,
        UserGoalEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class LianShiJiDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun trainingDao(): TrainingDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun userGoalDao(): UserGoalDao

    companion object {
        @Volatile
        private var instance: LianShiJiDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_goals (
                        id INTEGER NOT NULL PRIMARY KEY,
                        dailyCalories INTEGER NOT NULL,
                        proteinGrams REAL NOT NULL,
                        carbsGrams REAL NOT NULL,
                        fatGrams REAL NOT NULL,
                        weeklyTrainingCount INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO user_goals (
                        id,
                        dailyCalories,
                        proteinGrams,
                        carbsGrams,
                        fatGrams,
                        weeklyTrainingCount
                    ) VALUES (1, 2200, 120.0, 250.0, 70.0, 4)
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): LianShiJiDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LianShiJiDatabase::class.java,
                    "lian_shi_ji.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
