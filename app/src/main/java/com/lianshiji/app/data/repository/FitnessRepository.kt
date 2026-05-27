package com.lianshiji.app.data.repository

import com.lianshiji.app.data.local.LianShiJiDatabase
import com.lianshiji.app.data.local.entity.ExerciseEntity
import com.lianshiji.app.data.local.entity.FoodEntryEntity
import com.lianshiji.app.data.local.entity.TrainingEntryEntity
import com.lianshiji.app.data.local.entity.UserGoalEntity
import com.lianshiji.app.util.DateTimeUtils
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class FitnessRepository(
    database: LianShiJiDatabase
) {
    private val foodDao = database.foodDao()
    private val trainingDao = database.trainingDao()
    private val exerciseDao = database.exerciseDao()
    private val userGoalDao = database.userGoalDao()

    fun observeAllFoods(): Flow<List<FoodEntryEntity>> = foodDao.observeAll()

    fun observeGoals(): Flow<UserGoalEntity?> = userGoalDao.observeGoals()

    fun observeFoodsForDate(date: LocalDate): Flow<List<FoodEntryEntity>> {
        return foodDao.observeByTimeRange(
            startMillis = DateTimeUtils.dayStartMillis(date),
            endMillis = DateTimeUtils.nextDayStartMillis(date)
        )
    }

    fun observeAllTrainings(): Flow<List<TrainingEntryEntity>> = trainingDao.observeAll()

    fun observeTrainingsForDate(date: LocalDate): Flow<List<TrainingEntryEntity>> {
        return trainingDao.observeByTimeRange(
            startMillis = DateTimeUtils.dayStartMillis(date),
            endMillis = DateTimeUtils.nextDayStartMillis(date)
        )
    }

    fun observeExercises(): Flow<List<ExerciseEntity>> = exerciseDao.observeAll()

    suspend fun seedDefaultExercisesIfNeeded() {
        if (exerciseDao.count() == 0) {
            exerciseDao.insertAll(DefaultExerciseCatalog.items)
        }
        userGoalDao.insertIfAbsent(UserGoalEntity())
    }

    suspend fun saveFood(food: FoodEntryEntity): Long {
        return if (food.id == 0L) {
            foodDao.insert(food)
        } else {
            foodDao.update(food)
            food.id
        }
    }

    suspend fun deleteFood(food: FoodEntryEntity) {
        foodDao.delete(food)
    }

    suspend fun copyFoodsFromPreviousDay(targetDate: LocalDate): Int {
        val sourceDate = targetDate.minusDays(1)
        val sourceFoods = foodDao.listByTimeRange(
            DateTimeUtils.dayStartMillis(sourceDate),
            DateTimeUtils.nextDayStartMillis(sourceDate)
        )
        if (sourceFoods.isEmpty()) return 0

        val copiedFoods = sourceFoods.map { food ->
            food.copy(
                id = 0,
                timestamp = DateTimeUtils.dateTimeMillis(
                    targetDate,
                    DateTimeUtils.formatTime(food.timestamp)
                )
            )
        }
        foodDao.insertAll(copiedFoods)
        return copiedFoods.size
    }

    suspend fun saveTraining(training: TrainingEntryEntity): Long {
        return if (training.id == 0L) {
            trainingDao.insert(training)
        } else {
            trainingDao.update(training)
            training.id
        }
    }

    suspend fun deleteTraining(training: TrainingEntryEntity) {
        trainingDao.delete(training)
    }

    suspend fun copyLastTrainingToDate(targetDate: LocalDate): Int {
        val beforeTargetDay = DateTimeUtils.dayStartMillis(targetDate)
        val previousTrainings = trainingDao.listBefore(beforeTargetDay)
        val latestTrainingDay = previousTrainings.firstOrNull()?.let {
            DateTimeUtils.toLocalDate(it.performedAt)
        } ?: return 0

        val sourceTrainings = trainingDao.listByTimeRange(
            DateTimeUtils.dayStartMillis(latestTrainingDay),
            DateTimeUtils.nextDayStartMillis(latestTrainingDay)
        )
        if (sourceTrainings.isEmpty()) return 0

        val copiedTrainings = sourceTrainings.map { training ->
            training.copy(
                id = 0,
                performedAt = DateTimeUtils.dateAtNoonMillis(targetDate)
            )
        }
        trainingDao.insertAll(copiedTrainings)
        return copiedTrainings.size
    }

    suspend fun saveGoals(goals: UserGoalEntity) {
        userGoalDao.upsert(goals.copy(id = 1))
    }

    suspend fun clearUserData() {
        foodDao.deleteAll()
        trainingDao.deleteAll()
        userGoalDao.upsert(UserGoalEntity())
    }
}

private object DefaultExerciseCatalog {
    val items = listOf(
        ExerciseEntity(
            name = "卧推",
            targetMuscle = "胸部",
            instruction = "肩胛后缩下沉，杠铃落到胸中下部，推起时保持手腕稳定。",
            commonMistakes = "肘外展过大、臀部离凳、借力弹胸。",
            recommendedSetsReps = "4 组 x 6-10 次"
        ),
        ExerciseEntity(
            name = "深蹲",
            targetMuscle = "腿部",
            instruction = "脚掌踩稳，膝盖跟随脚尖方向，下蹲到稳定深度后发力站起。",
            commonMistakes = "膝内扣、塌腰、重心过度前移。",
            recommendedSetsReps = "4 组 x 5-8 次"
        ),
        ExerciseEntity(
            name = "硬拉",
            targetMuscle = "后链",
            instruction = "杠铃贴近小腿，背部保持中立，髋膝同时伸展完成拉起。",
            commonMistakes = "弓背、杠铃远离身体、顶端过度后仰。",
            recommendedSetsReps = "3 组 x 3-6 次"
        ),
        ExerciseEntity(
            name = "引体向上",
            targetMuscle = "背部",
            instruction = "先下沉肩胛，再用背部发力上拉到下巴过杠。",
            commonMistakes = "耸肩、摆动借力、下降不受控。",
            recommendedSetsReps = "4 组 x 6-12 次"
        ),
        ExerciseEntity(
            name = "划船",
            targetMuscle = "背部",
            instruction = "躯干稳定，肘部向后拉，顶峰短暂停顿感受背部收缩。",
            commonMistakes = "腰背晃动、用手臂硬拽、肩膀前探。",
            recommendedSetsReps = "4 组 x 8-12 次"
        ),
        ExerciseEntity(
            name = "肩推",
            targetMuscle = "肩部",
            instruction = "核心收紧，前臂接近垂直，把重量稳定推到头顶。",
            commonMistakes = "腰部过度反弓、路径前后飘、锁定时耸肩。",
            recommendedSetsReps = "3 组 x 6-10 次"
        ),
        ExerciseEntity(
            name = "弯举",
            targetMuscle = "手臂",
            instruction = "上臂稳定贴近身体，控制离心，顶端挤压肱二头肌。",
            commonMistakes = "身体后仰借力、甩动重量、下降过快。",
            recommendedSetsReps = "3 组 x 10-15 次"
        ),
        ExerciseEntity(
            name = "腿举",
            targetMuscle = "腿部",
            instruction = "腰背贴紧靠垫，膝盖对准脚尖，控制下降再推起。",
            commonMistakes = "膝盖内扣、下放过深导致骨盆卷起、膝盖锁死。",
            recommendedSetsReps = "4 组 x 10-15 次"
        )
    )
}
