package com.lianshiji.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lianshiji.app.data.local.entity.ExerciseEntity
import com.lianshiji.app.data.local.entity.FoodEntryEntity
import com.lianshiji.app.data.local.entity.TrainingEntryEntity
import com.lianshiji.app.data.local.entity.UserGoalEntity
import com.lianshiji.app.data.repository.FitnessRepository
import com.lianshiji.app.util.DateTimeUtils
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt

class MainViewModel(
    private val repository: FitnessRepository
) : ViewModel() {
    private val _selectedDate = MutableStateFlow(DateTimeUtils.today())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val allFoods: StateFlow<List<FoodEntryEntity>> = repository.observeAllFoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTrainings: StateFlow<List<TrainingEntryEntity>> = repository.observeAllTrainings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val exercises = repository.observeExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val goals: StateFlow<UserGoalEntity> = repository.observeGoals()
        .map { it ?: UserGoalEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserGoalEntity())

    val selectedFoods: StateFlow<List<FoodEntryEntity>> = combine(
        allFoods,
        selectedDate
    ) { foods, date ->
        foods.filter { DateTimeUtils.toLocalDate(it.timestamp) == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedTrainings: StateFlow<List<TrainingEntryEntity>> = combine(
        allTrainings,
        selectedDate
    ) { trainings, date ->
        trainings.filter { DateTimeUtils.toLocalDate(it.performedAt) == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dashboardState: StateFlow<DashboardUiState> = combine(
        allFoods,
        allTrainings,
        goals
    ) { foods, trainings, goals ->
        val today = DateTimeUtils.today()
        val todayFoods = foods.filter { DateTimeUtils.toLocalDate(it.timestamp) == today }
        val todayTrainings = trainings.filter { DateTimeUtils.toLocalDate(it.performedAt) == today }
        val totals = NutritionTotals.from(todayFoods)
        val calorieCompletion = progressOf(totals.calories.toFloat(), goals.dailyCalories.toFloat())
        val proteinCompletion = progressOf(totals.protein, goals.proteinGrams)
        val trainingCompletion = if (todayTrainings.isEmpty()) 0f else 1f
        val overallCompletion = ((calorieCompletion + proteinCompletion + trainingCompletion) / 3f)
            .coerceIn(0f, 1f)

        DashboardUiState(
            todayLabel = DateTimeUtils.formatDate(today),
            calories = totals.calories,
            protein = totals.protein,
            carbs = totals.carbs,
            fat = totals.fat,
            calorieTarget = goals.dailyCalories,
            proteinTarget = goals.proteinGrams,
            carbsTarget = goals.carbsGrams,
            fatTarget = goals.fatGrams,
            overallCompletion = overallCompletion,
            motivationText = motivationText(overallCompletion),
            remainingItems = buildRemainingItems(totals, todayTrainings, goals),
            trainingStatus = if (todayTrainings.isEmpty()) "今日未训练" else "已记录 ${todayTrainings.size} 个动作",
            streakDays = calculateStreak(foods, trainings),
            suggestions = buildSuggestions(totals, todayFoods, todayTrainings, trainings, goals).take(3)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    val selectedNutritionTotals: StateFlow<NutritionTotals> = selectedFoods
        .combine(selectedDate) { foods, _ -> NutritionTotals.from(foods) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NutritionTotals())

    private val _foodForm = MutableStateFlow(FoodFormState())
    val foodForm: StateFlow<FoodFormState> = _foodForm.asStateFlow()

    private val _isFoodDialogOpen = MutableStateFlow(false)
    val isFoodDialogOpen: StateFlow<Boolean> = _isFoodDialogOpen.asStateFlow()

    private val _trainingForm = MutableStateFlow(TrainingFormState())
    val trainingForm: StateFlow<TrainingFormState> = _trainingForm.asStateFlow()

    private val _isTrainingDialogOpen = MutableStateFlow(false)
    val isTrainingDialogOpen: StateFlow<Boolean> = _isTrainingDialogOpen.asStateFlow()

    private val _goalForm = MutableStateFlow(GoalFormState.from(UserGoalEntity()))
    val goalForm: StateFlow<GoalFormState> = _goalForm.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDefaultExercisesIfNeeded()
        }
        viewModelScope.launch {
            goals.collect {
                _goalForm.value = GoalFormState.from(it)
            }
        }
    }

    fun selectPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun selectNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun selectToday() {
        _selectedDate.value = DateTimeUtils.today()
    }

    fun updateFoodForm(form: FoodFormState) {
        _foodForm.value = form.copy(error = null)
    }

    fun openNewFoodForm() {
        _foodForm.value = FoodFormState(
            mealType = mealTypeForNow(),
            time = DateTimeUtils.formatTime(System.currentTimeMillis())
        )
        _isFoodDialogOpen.value = true
    }

    fun openEditFoodForm(food: FoodEntryEntity) {
        _foodForm.value = FoodFormState(
            id = food.id,
            name = food.name,
            weightGrams = food.weightGrams.trimmedString(),
            calories = food.calories.toString(),
            proteinGrams = food.proteinGrams.trimmedString(),
            carbsGrams = food.carbsGrams.trimmedString(),
            fatGrams = food.fatGrams.trimmedString(),
            mealType = food.mealType,
            time = DateTimeUtils.formatTime(food.timestamp)
        )
        _selectedDate.value = DateTimeUtils.toLocalDate(food.timestamp)
        _isFoodDialogOpen.value = true
    }

    fun dismissFoodForm() {
        _isFoodDialogOpen.value = false
    }

    fun saveFood() {
        val form = foodForm.value
        val entity = form.toEntity(selectedDate.value)
        if (entity == null) {
            _foodForm.value = form.copy(error = "请填写食物名称，并确认重量、热量和营养素是有效数字。")
            return
        }

        viewModelScope.launch {
            repository.saveFood(entity)
            _isFoodDialogOpen.value = false
        }
    }

    fun deleteFood(food: FoodEntryEntity) {
        viewModelScope.launch {
            repository.deleteFood(food)
        }
    }

    fun applyCommonFood(food: CommonFoodOption) {
        val form = foodForm.value
        _foodForm.value = form.copy(
            name = food.name,
            weightGrams = food.weightGrams.trimmedString(),
            calories = food.calories.toString(),
            proteinGrams = food.proteinGrams.trimmedString(),
            carbsGrams = food.carbsGrams.trimmedString(),
            fatGrams = food.fatGrams.trimmedString(),
            error = null
        )
    }

    fun copyYesterdayFoods() {
        viewModelScope.launch {
            repository.copyFoodsFromPreviousDay(selectedDate.value)
        }
    }

    fun updateTrainingForm(form: TrainingFormState) {
        _trainingForm.value = form.copy(error = null)
    }

    fun openNewTrainingForm() {
        _trainingForm.value = TrainingFormState(
            date = DateTimeUtils.formatDate(selectedDate.value)
        )
        _isTrainingDialogOpen.value = true
    }

    fun openEditTrainingForm(training: TrainingEntryEntity) {
        _trainingForm.value = TrainingFormState(
            id = training.id,
            date = DateTimeUtils.formatDate(training.performedAt),
            bodyPart = training.bodyPart,
            exerciseName = training.exerciseName,
            sets = training.sets.toString(),
            reps = training.reps.toString(),
            weightKg = training.weightKg.trimmedString(),
            note = training.note
        )
        _selectedDate.value = DateTimeUtils.toLocalDate(training.performedAt)
        _isTrainingDialogOpen.value = true
    }

    fun dismissTrainingForm() {
        _isTrainingDialogOpen.value = false
    }

    fun saveTraining() {
        val form = trainingForm.value
        val entity = form.toEntity()
        if (entity == null) {
            _trainingForm.value = form.copy(error = "请填写日期、训练部位、动作名称，并确认组数、次数和重量是有效数字。")
            return
        }

        viewModelScope.launch {
            repository.saveTraining(entity)
            _selectedDate.value = DateTimeUtils.toLocalDate(entity.performedAt)
            _isTrainingDialogOpen.value = false
        }
    }

    fun deleteTraining(training: TrainingEntryEntity) {
        viewModelScope.launch {
            repository.deleteTraining(training)
        }
    }

    fun applyExerciseToTrainingForm(exercise: ExerciseEntity) {
        val bodyPart = normalizeBodyPart(exercise.targetMuscle)
        _trainingForm.value = trainingForm.value.copy(
            exerciseName = exercise.name,
            bodyPart = bodyPart,
            error = null
        )
    }

    fun copyLastTraining() {
        viewModelScope.launch {
            repository.copyLastTrainingToDate(selectedDate.value)
        }
    }

    fun updateGoalForm(form: GoalFormState) {
        _goalForm.value = form.copy(error = null)
    }

    fun saveGoals() {
        val form = goalForm.value
        val entity = form.toEntity()
        if (entity == null) {
            _goalForm.value = form.copy(error = "请确认所有目标都是有效的正数。")
            return
        }
        viewModelScope.launch {
            repository.saveGoals(entity)
        }
    }

    fun clearUserData() {
        viewModelScope.launch {
            repository.clearUserData()
            _selectedDate.value = DateTimeUtils.today()
        }
    }

    private fun calculateStreak(
        foods: List<FoodEntryEntity>,
        trainings: List<TrainingEntryEntity>
    ): Int {
        val recordedDays = buildSet {
            foods.forEach { add(DateTimeUtils.toLocalDate(it.timestamp)) }
            trainings.forEach { add(DateTimeUtils.toLocalDate(it.performedAt)) }
        }

        var cursor = DateTimeUtils.today()
        var count = 0
        while (recordedDays.contains(cursor)) {
            count += 1
            cursor = cursor.minusDays(1)
        }
        return count
    }

    private fun progressOf(current: Float, target: Float): Float {
        return if (target <= 0f) 0f else (current / target).coerceIn(0f, 1f)
    }

    private fun motivationText(completion: Float): String {
        return when {
            completion < 0.3f -> "开始记录今天的第一步"
            completion < 0.7f -> "今天已经完成一半"
            else -> "离目标很近"
        }
    }

    private fun buildRemainingItems(
        totals: NutritionTotals,
        todayTrainings: List<TrainingEntryEntity>,
        goals: UserGoalEntity
    ): List<String> {
        val remaining = mutableListOf<String>()
        val proteinLeft = (goals.proteinGrams - totals.protein).coerceAtLeast(0f)
        val calorieLeft = (goals.dailyCalories - totals.calories).coerceAtLeast(0)

        if (proteinLeft > 0.5f) {
            remaining += "蛋白质 ${ceil(proteinLeft.toDouble()).toInt()}g"
        }
        if (todayTrainings.isEmpty()) {
            remaining += "训练 1次"
        }
        if (calorieLeft > 0 && remaining.size < 3) {
            remaining += "热量 ${calorieLeft}kcal"
        }

        return remaining
    }

    private fun buildSuggestions(
        totals: NutritionTotals,
        todayFoods: List<FoodEntryEntity>,
        todayTrainings: List<TrainingEntryEntity>,
        allTrainings: List<TrainingEntryEntity>,
        goals: UserGoalEntity
    ): List<String> {
        val suggestions = mutableListOf<String>()

        if (todayFoods.isEmpty()) {
            suggestions += "今天还没有饮食记录，先补一餐基础记录。"
        } else {
            if (totals.protein < goals.proteinGrams * 0.8f) {
                suggestions += "今日蛋白质偏低，可以增加鸡蛋、鱼肉、牛肉、豆制品或乳清。"
            }
            if (totals.carbs < goals.carbsGrams * 0.65f && todayTrainings.isNotEmpty()) {
                suggestions += "训练日碳水偏少，可以在训练前后补一些米饭、面、土豆或水果。"
            }
            if (totals.fat > goals.fatGrams * 1.15f) {
                suggestions += "今日脂肪偏高，下一餐可以选择更清淡的蛋白质来源。"
            }
            if (totals.calories > goals.dailyCalories) {
                suggestions += "今日热量已经超过目标，剩余餐次尽量选择低脂高蛋白。"
            }
        }

        if (todayTrainings.isEmpty()) {
            suggestions += "今天还没有训练记录，若身体状态允许，可以安排一次短训练。"
        }

        val sevenDaysAgo = DateTimeUtils.today().minusDays(6)
        val hasRecentLegTraining = allTrainings.any {
            val date = DateTimeUtils.toLocalDate(it.performedAt)
            date >= sevenDaysAgo && (it.bodyPart.contains("腿") || it.bodyPart.contains("下肢"))
        }
        if (!hasRecentLegTraining) {
            suggestions += "最近 7 天没有腿部训练，可以安排深蹲、腿举或硬拉相关训练。"
        }

        if (suggestions.isEmpty()) {
            suggestions += "今日饮食和训练记录节奏不错，继续保持稳定。"
        }

        return suggestions
    }

    private fun normalizeBodyPart(targetMuscle: String): String {
        return when {
            targetMuscle.contains("胸") -> "胸部"
            targetMuscle.contains("背") -> "背部"
            targetMuscle.contains("腿") || targetMuscle.contains("后链") -> "腿部"
            targetMuscle.contains("肩") -> "肩部"
            targetMuscle.contains("手臂") || targetMuscle.contains("二头") -> "手臂"
            targetMuscle.contains("核心") -> "核心"
            else -> BodyPartOptions.first()
        }
    }

    private fun FoodFormState.toEntity(date: LocalDate): FoodEntryEntity? {
        val cleanName = name.trim()
        val parsedWeight = weightGrams.toPositiveFloatOrNull()
        val parsedCalories = calories.toNonNegativeIntOrNull()
        val parsedProtein = proteinGrams.toNonNegativeFloatOrNull()
        val parsedCarbs = carbsGrams.toNonNegativeFloatOrNull()
        val parsedFat = fatGrams.toNonNegativeFloatOrNull()
        val parsedTime = DateTimeUtils.parseTime(time)

        if (
            cleanName.isBlank() ||
            parsedWeight == null ||
            parsedCalories == null ||
            parsedProtein == null ||
            parsedCarbs == null ||
            parsedFat == null ||
            parsedTime == null
        ) {
            return null
        }

        return FoodEntryEntity(
            id = id ?: 0L,
            name = cleanName,
            weightGrams = parsedWeight,
            calories = parsedCalories,
            proteinGrams = parsedProtein,
            carbsGrams = parsedCarbs,
            fatGrams = parsedFat,
            mealType = mealType,
            timestamp = DateTimeUtils.dateTimeMillis(date, time)
        )
    }

    private fun TrainingFormState.toEntity(): TrainingEntryEntity? {
        val parsedDate = DateTimeUtils.parseDate(date)
        val parsedSets = sets.toNonNegativeIntOrNull()
        val parsedReps = reps.toNonNegativeIntOrNull()
        val parsedWeight = weightKg.toNonNegativeFloatOrNull()

        if (
            parsedDate == null ||
            bodyPart.isBlank() ||
            exerciseName.isBlank() ||
            parsedSets == null ||
            parsedSets <= 0 ||
            parsedReps == null ||
            parsedReps <= 0 ||
            parsedWeight == null
        ) {
            return null
        }

        return TrainingEntryEntity(
            id = id ?: 0L,
            performedAt = DateTimeUtils.dateAtNoonMillis(parsedDate),
            bodyPart = bodyPart.trim(),
            exerciseName = exerciseName.trim(),
            sets = parsedSets,
            reps = parsedReps,
            weightKg = parsedWeight,
            note = note.trim()
        )
    }

    private fun GoalFormState.toEntity(): UserGoalEntity? {
        val parsedCalories = dailyCalories.toNonNegativeIntOrNull()
        val parsedProtein = proteinGrams.toNonNegativeFloatOrNull()
        val parsedCarbs = carbsGrams.toNonNegativeFloatOrNull()
        val parsedFat = fatGrams.toNonNegativeFloatOrNull()
        val parsedWeeklyTraining = weeklyTrainingCount.toNonNegativeIntOrNull()

        if (
            parsedCalories == null ||
            parsedCalories <= 0 ||
            parsedProtein == null ||
            parsedProtein <= 0f ||
            parsedCarbs == null ||
            parsedCarbs <= 0f ||
            parsedFat == null ||
            parsedFat <= 0f ||
            parsedWeeklyTraining == null ||
            parsedWeeklyTraining <= 0
        ) {
            return null
        }

        return UserGoalEntity(
            dailyCalories = parsedCalories,
            proteinGrams = parsedProtein,
            carbsGrams = parsedCarbs,
            fatGrams = parsedFat,
            weeklyTrainingCount = parsedWeeklyTraining
        )
    }

    private fun mealTypeForNow(): String {
        return when (java.time.LocalTime.now().hour) {
            in 5..10 -> MealType.Breakfast.label
            in 11..14 -> MealType.Lunch.label
            in 17..20 -> MealType.Dinner.label
            else -> MealType.Snack.label
        }
    }
}

class MainViewModelFactory(
    private val repository: FitnessRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

data class DashboardUiState(
    val todayLabel: String = DateTimeUtils.formatDate(DateTimeUtils.today()),
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val calorieTarget: Int = 2200,
    val proteinTarget: Float = 120f,
    val carbsTarget: Float = 250f,
    val fatTarget: Float = 70f,
    val overallCompletion: Float = 0f,
    val motivationText: String = "开始记录今天的第一步",
    val remainingItems: List<String> = listOf("蛋白质 120g", "训练 1次"),
    val trainingStatus: String = "今日未训练",
    val streakDays: Int = 0,
    val suggestions: List<String> = listOf("开始记录第一餐和第一组训练。")
)

data class NutritionTotals(
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f
) {
    companion object {
        fun from(foods: List<FoodEntryEntity>): NutritionTotals {
            return NutritionTotals(
                calories = foods.sumOf { it.calories },
                protein = foods.sumOf { it.proteinGrams.toDouble() }.toFloat(),
                carbs = foods.sumOf { it.carbsGrams.toDouble() }.toFloat(),
                fat = foods.sumOf { it.fatGrams.toDouble() }.toFloat()
            )
        }
    }
}

data class FoodFormState(
    val id: Long? = null,
    val name: String = "",
    val weightGrams: String = "",
    val calories: String = "",
    val proteinGrams: String = "",
    val carbsGrams: String = "",
    val fatGrams: String = "",
    val mealType: String = MealType.Lunch.label,
    val time: String = "12:00",
    val error: String? = null
)

data class TrainingFormState(
    val id: Long? = null,
    val date: String = DateTimeUtils.formatDate(DateTimeUtils.today()),
    val bodyPart: String = "胸部",
    val exerciseName: String = "",
    val sets: String = "",
    val reps: String = "",
    val weightKg: String = "",
    val note: String = "",
    val error: String? = null
)

data class GoalFormState(
    val dailyCalories: String = "2200",
    val proteinGrams: String = "120",
    val carbsGrams: String = "250",
    val fatGrams: String = "70",
    val weeklyTrainingCount: String = "4",
    val error: String? = null
) {
    companion object {
        fun from(goals: UserGoalEntity): GoalFormState {
            return GoalFormState(
                dailyCalories = goals.dailyCalories.toString(),
                proteinGrams = goals.proteinGrams.trimmedString(),
                carbsGrams = goals.carbsGrams.trimmedString(),
                fatGrams = goals.fatGrams.trimmedString(),
                weeklyTrainingCount = goals.weeklyTrainingCount.toString()
            )
        }
    }
}

data class CommonFoodOption(
    val name: String,
    val weightGrams: Float,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float
)

enum class MealType(val label: String) {
    Breakfast("早餐"),
    Lunch("午餐"),
    Dinner("晚餐"),
    Snack("加餐")
}

val BodyPartOptions = listOf("胸部", "背部", "腿部", "肩部", "手臂", "核心", "有氧")

val CommonFoodOptions = listOf(
    CommonFoodOption("鸡胸肉", 100f, 165, 31f, 0f, 3.6f),
    CommonFoodOption("鸡蛋", 50f, 72, 6.3f, 0.4f, 4.8f),
    CommonFoodOption("米饭", 150f, 174, 3.9f, 38.1f, 0.5f),
    CommonFoodOption("燕麦", 50f, 190, 6.5f, 33f, 3.5f),
    CommonFoodOption("香蕉", 120f, 107, 1.3f, 27.4f, 0.4f),
    CommonFoodOption("牛奶", 250f, 135, 8f, 12f, 7.5f),
    CommonFoodOption("牛肉", 100f, 250, 26f, 0f, 15f),
    CommonFoodOption("西兰花", 100f, 34, 2.8f, 6.6f, 0.4f)
)

fun Float.trimmedString(): String {
    return if (this % 1f == 0f) {
        this.roundToInt().toString()
    } else {
        String.format("%.1f", this)
    }
}

private fun String.normalizedNumber(): String = trim().replace(",", ".")

private fun String.toPositiveFloatOrNull(): Float? {
    val value = normalizedNumber().toFloatOrNull() ?: return null
    return value.takeIf { it > 0f }
}

private fun String.toNonNegativeFloatOrNull(): Float? {
    val value = normalizedNumber().toFloatOrNull() ?: return null
    return value.takeIf { it >= 0f }
}

private fun String.toNonNegativeIntOrNull(): Int? {
    val direct = trim().toIntOrNull()
    if (direct != null) return direct.takeIf { it >= 0 }

    val rounded = normalizedNumber().toFloatOrNull()?.roundToInt() ?: return null
    return rounded.takeIf { it >= 0 }
}
