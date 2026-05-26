package com.lianshiji.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lianshiji.app.data.local.entity.FoodEntryEntity
import com.lianshiji.app.data.local.entity.TrainingEntryEntity
import com.lianshiji.app.data.repository.FitnessRepository
import com.lianshiji.app.util.DateTimeUtils
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
        allTrainings
    ) { foods, trainings ->
        val today = DateTimeUtils.today()
        val todayFoods = foods.filter { DateTimeUtils.toLocalDate(it.timestamp) == today }
        val todayTrainings = trainings.filter { DateTimeUtils.toLocalDate(it.performedAt) == today }
        val totals = NutritionTotals.from(todayFoods)

        DashboardUiState(
            todayLabel = DateTimeUtils.formatDate(today),
            calories = totals.calories,
            protein = totals.protein,
            carbs = totals.carbs,
            fat = totals.fat,
            trainingStatus = if (todayTrainings.isEmpty()) "今日未训练" else "已记录 ${todayTrainings.size} 个动作",
            streakDays = calculateStreak(foods, trainings),
            suggestions = buildSuggestions(totals, todayFoods, todayTrainings, trainings)
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

    init {
        viewModelScope.launch {
            repository.seedDefaultExercisesIfNeeded()
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

    private fun buildSuggestions(
        totals: NutritionTotals,
        todayFoods: List<FoodEntryEntity>,
        todayTrainings: List<TrainingEntryEntity>,
        allTrainings: List<TrainingEntryEntity>
    ): List<String> {
        val suggestions = mutableListOf<String>()

        if (todayFoods.isEmpty()) {
            suggestions += "今天还没有饮食记录，先补一餐基础记录。"
        } else {
            if (totals.protein < 100f) {
                suggestions += "今日蛋白质偏低，可以增加鸡蛋、鱼肉、牛肉、豆制品或乳清。"
            }
            if (totals.carbs < 150f && todayTrainings.isNotEmpty()) {
                suggestions += "训练日碳水偏少，可以在训练前后补一些米饭、面、土豆或水果。"
            }
            if (totals.fat > 80f) {
                suggestions += "今日脂肪偏高，下一餐可以选择更清淡的蛋白质来源。"
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

enum class MealType(val label: String) {
    Breakfast("早餐"),
    Lunch("午餐"),
    Dinner("晚餐"),
    Snack("加餐")
}

val BodyPartOptions = listOf("胸部", "背部", "腿部", "肩部", "手臂", "核心", "有氧")

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
