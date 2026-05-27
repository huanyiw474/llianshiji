package com.lianshiji.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lianshiji.app.data.local.entity.ExerciseEntity
import com.lianshiji.app.data.local.entity.FoodEntryEntity
import com.lianshiji.app.data.local.entity.TrainingEntryEntity
import com.lianshiji.app.util.DateTimeUtils
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LianShiJiApp(viewModel: MainViewModel) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Dashboard) }

    val dashboard by viewModel.dashboardState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedFoods by viewModel.selectedFoods.collectAsState()
    val selectedTrainings by viewModel.selectedTrainings.collectAsState()
    val selectedTotals by viewModel.selectedNutritionTotals.collectAsState()
    val allTrainings by viewModel.allTrainings.collectAsState()
    val allFoods by viewModel.allFoods.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val foodForm by viewModel.foodForm.collectAsState()
    val trainingForm by viewModel.trainingForm.collectAsState()
    val goalForm by viewModel.goalForm.collectAsState()
    val isFoodDialogOpen by viewModel.isFoodDialogOpen.collectAsState()
    val isTrainingDialogOpen by viewModel.isTrainingDialogOpen.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            when (currentDestination) {
                AppDestination.Food -> {
                    FloatingActionButton(onClick = viewModel::openNewFoodForm) {
                        Icon(Icons.Outlined.Add, contentDescription = "添加饮食")
                    }
                }
                AppDestination.Training -> {
                    FloatingActionButton(onClick = viewModel::openNewTrainingForm) {
                        Icon(Icons.Outlined.Add, contentDescription = "添加训练")
                    }
                }
                else -> Unit
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF102116),
                tonalElevation = 8.dp
            ) {
                AppDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0E2418),
                            selectedTextColor = Color(0xFFDFF5D4),
                            unselectedIconColor = Color.White.copy(alpha = 0.58f),
                            unselectedTextColor = Color.White.copy(alpha = 0.58f),
                            indicatorColor = Color(0xFFDFF5D4)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    (fadeIn(tween(180)) + slideInHorizontally { it / 10 }) togetherWith
                        (fadeOut(tween(120)) + slideOutHorizontally { -it / 12 })
                },
                label = "page_switch"
            ) { destination ->
                when (destination) {
                    AppDestination.Dashboard -> DashboardPage(
                        state = dashboard,
                        onAddFood = {
                            viewModel.selectToday()
                            viewModel.openNewFoodForm()
                        },
                        onAddTraining = {
                            viewModel.selectToday()
                            viewModel.openNewTrainingForm()
                        }
                    )
                    AppDestination.Food -> FoodPage(
                        selectedDate = selectedDate,
                        totals = selectedTotals,
                        foods = selectedFoods,
                        onPreviousDay = viewModel::selectPreviousDay,
                        onNextDay = viewModel::selectNextDay,
                        onToday = viewModel::selectToday,
                        onAdd = viewModel::openNewFoodForm,
                        onCopyYesterday = viewModel::copyYesterdayFoods,
                        onEdit = viewModel::openEditFoodForm,
                        onDelete = viewModel::deleteFood
                    )
                    AppDestination.Training -> TrainingPage(
                        selectedDate = selectedDate,
                        selectedTrainings = selectedTrainings,
                        allTrainings = allTrainings,
                        onPreviousDay = viewModel::selectPreviousDay,
                        onNextDay = viewModel::selectNextDay,
                        onToday = viewModel::selectToday,
                        onAdd = viewModel::openNewTrainingForm,
                        onCopyLast = viewModel::copyLastTraining,
                        onEdit = viewModel::openEditTrainingForm,
                        onDelete = viewModel::deleteTraining
                    )
                    AppDestination.Exercises -> ExerciseLibraryPage(exercises)
                    AppDestination.Me -> MinePage(
                        dashboard = dashboard,
                        foodCount = allFoods.size,
                        trainingCount = allTrainings.size,
                        goalForm = goalForm,
                        onGoalFormChange = viewModel::updateGoalForm,
                        onSaveGoals = viewModel::saveGoals,
                        onClearUserData = viewModel::clearUserData
                    )
                }
            }
        }
    }

    if (isFoodDialogOpen) {
        FoodEntryDialog(
            form = foodForm,
            onFormChange = viewModel::updateFoodForm,
            onSelectCommonFood = viewModel::applyCommonFood,
            onDismiss = viewModel::dismissFoodForm,
            onSave = viewModel::saveFood
        )
    }

    if (isTrainingDialogOpen) {
        TrainingEntryDialog(
            form = trainingForm,
            exercises = exercises,
            onFormChange = viewModel::updateTrainingForm,
            onSelectExercise = viewModel::applyExerciseToTrainingForm,
            onDismiss = viewModel::dismissTrainingForm,
            onSave = viewModel::saveTraining
        )
    }
}

private enum class AppDestination(
    val label: String,
    val icon: ImageVector
) {
    Dashboard("首页", Icons.Outlined.Home),
    Food("饮食", Icons.Outlined.Restaurant),
    Training("训练", Icons.Outlined.FitnessCenter),
    Exercises("动作库", Icons.AutoMirrored.Outlined.MenuBook),
    Me("我的", Icons.Outlined.Person)
}

private val AppCardShape = RoundedCornerShape(16.dp)
private val AppPillShape = RoundedCornerShape(999.dp)

@Composable
private fun cardShadow() = CardDefaults.cardElevation(defaultElevation = 9.dp)

@Composable
private fun CardEntrance(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 8 },
        modifier = modifier
    ) {
        content()
    }
}

@Composable
private fun AppButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "button_press_scale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(54.dp)
            .scale(scale),
        shape = AppPillShape,
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AppOutlinedButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "outlined_button_press_scale"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(54.dp)
            .scale(scale),
        shape = AppPillShape,
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PillTag(
    text: String,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(
        shape = AppPillShape,
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            icon?.let {
                Icon(it, contentDescription = null, modifier = Modifier.size(14.dp), tint = contentColor)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun DashboardPage(
    state: DashboardUiState,
    onAddFood: () -> Unit,
    onAddTraining: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF102116),
                        Color(0xFF1F4D33),
                        Color(0xFFF4F8F1)
                    )
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DashboardHeader(date = state.todayLabel)
        }
        item {
            CardEntrance {
                GoalProgressCard(state)
            }
        }
        item {
            CardEntrance {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppButton(
                        text = "记饮食",
                        icon = Icons.Outlined.Restaurant,
                        onClick = onAddFood,
                        modifier = Modifier.weight(1f)
                    )
                    AppOutlinedButton(
                        text = "记训练",
                        icon = Icons.Outlined.FitnessCenter,
                        onClick = onAddTraining,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            CardEntrance {
                TodayRemainingCard(state.remainingItems)
            }
        }
        item {
            SectionTitle("建议")
        }
        items(state.suggestions.take(2)) { suggestion ->
            SuggestionRow(text = suggestion)
        }
    }
}

@Composable
private fun DashboardHeader(date: String) {
    Column(
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PillTag(
            text = date,
            icon = Icons.Outlined.Home,
            containerColor = Color.White.copy(alpha = 0.14f),
            contentColor = Color.White
        )
        Text(
            text = "练食记",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Text(
            text = "Eat clean. Train steady.",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun GoalProgressCard(state: DashboardUiState) {
    Card(
        shape = AppCardShape,
        elevation = cardShadow(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0B1C13),
                            Color(0xFF245D3C),
                            Color(0xFF8CCB6E)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompletionCircle(progress = state.overallCompletion)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PillTag(
                            text = "TODAY SCORE",
                            icon = Icons.Outlined.FitnessCenter,
                            containerColor = Color.White.copy(alpha = 0.22f),
                            contentColor = Color.White
                        )
                        Text(
                            text = state.motivationText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "连续 ${state.streakDays} 天 · ${state.trainingStatus}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.86f)
                        )
                        Text(
                            text = "${state.calories}/${state.calorieTarget} kcal",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                ProgressLine(
                    label = "热量",
                    current = state.calories.toFloat(),
                    target = state.calorieTarget.toFloat(),
                    unit = "kcal",
                    color = Color.White
                )
                ProgressLine(
                    label = "蛋白",
                    current = state.protein,
                    target = state.proteinTarget,
                    unit = "g",
                    color = Color(0xFFE7F7D9)
                )
                ProgressLine(
                    label = "碳水",
                    current = state.carbs,
                    target = state.carbsTarget,
                    unit = "g",
                    color = Color(0xFFD5EAFE)
                )
                ProgressLine(
                    label = "脂肪",
                    current = state.fat,
                    target = state.fatTarget,
                    unit = "g",
                    color = Color(0xFFFFE0A6)
                )
            }
        }
    }
}

@Composable
private fun CompletionCircle(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(650),
        label = "completion_progress"
    )
    Box(
        modifier = Modifier.size(142.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 12.dp,
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.22f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).roundToInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "完成",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun TodayRemainingCard(items: List<String>) {
    Card(
        shape = AppCardShape,
        elevation = cardShadow(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "还差",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (items.isEmpty()) {
                Text(
                    text = "今日目标已完成",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        PillTag(
                            text = item,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressLine(
    label: String,
    current: Float,
    target: Float,
    unit: String,
    color: Color
) {
    val progress = if (target <= 0f) 0f else (current / target).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "${current.trimmedString()} / ${target.trimmedString()} $unit",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = Color.White.copy(alpha = 0.24f)
        )
    }
}

@Composable
private fun FoodPage(
    selectedDate: LocalDate,
    totals: NutritionTotals,
    foods: List<FoodEntryEntity>,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onAdd: () -> Unit,
    onCopyYesterday: () -> Unit,
    onEdit: (FoodEntryEntity) -> Unit,
    onDelete: (FoodEntryEntity) -> Unit
) {
    val groupedFoods = MealType.entries
        .map { it.label to foods.filter { food -> food.mealType == it.label } }
        .filter { it.second.isNotEmpty() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PageHeader(title = "饮食", subtitle = "记录每一餐和三大营养素")
        }
        item {
            DateSelector(
                date = selectedDate,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onToday = onToday
            )
        }
        item {
            NutritionSummary(totals = totals)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppButton(
                    text = "添加",
                    icon = Icons.Outlined.Add,
                    onClick = onAdd,
                    modifier = Modifier.weight(1f)
                )
                AppOutlinedButton(
                    text = "复制昨天",
                    icon = Icons.Outlined.ContentCopy,
                    onClick = onCopyYesterday,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (foods.isEmpty()) {
            item {
                EmptyState(text = "这一天还没有饮食记录")
            }
        } else {
            groupedFoods.forEach { (mealType, mealFoods) ->
                item {
                    MealGroupHeader(mealType, NutritionTotals.from(mealFoods))
                }
                items(mealFoods, key = { it.id }) { food ->
                    CardEntrance {
                        FoodEntryCard(
                            food = food,
                            onEdit = { onEdit(food) },
                            onDelete = { onDelete(food) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealGroupHeader(mealType: String, totals: NutritionTotals) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mealType,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "${totals.calories} kcal · 蛋白 ${totals.protein.trimmedString()}g",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TrainingPage(
    selectedDate: LocalDate,
    selectedTrainings: List<TrainingEntryEntity>,
    allTrainings: List<TrainingEntryEntity>,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onAdd: () -> Unit,
    onCopyLast: () -> Unit,
    onEdit: (TrainingEntryEntity) -> Unit,
    onDelete: (TrainingEntryEntity) -> Unit
) {
    val groupedHistory = allTrainings.groupBy { DateTimeUtils.formatDate(it.performedAt) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PageHeader(title = "训练", subtitle = "按日期记录动作、组数、次数和重量")
        }
        item {
            DateSelector(
                date = selectedDate,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onToday = onToday
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppButton(
                    text = "添加",
                    icon = Icons.Outlined.Add,
                    onClick = onAdd,
                    modifier = Modifier.weight(1f)
                )
                AppOutlinedButton(
                    text = "复制上次",
                    icon = Icons.Outlined.ContentCopy,
                    onClick = onCopyLast,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            SectionTitle("当天详情")
        }
        if (selectedTrainings.isEmpty()) {
            item {
                EmptyState(text = "这一天还没有训练动作")
            }
        } else {
            items(selectedTrainings, key = { it.id }) { training ->
                CardEntrance {
                    TrainingEntryCard(
                        training = training,
                        onEdit = { onEdit(training) },
                        onDelete = { onDelete(training) }
                    )
                }
            }
        }
        item {
            SectionTitle("历史记录")
        }
        if (groupedHistory.isEmpty()) {
            item {
                EmptyState(text = "还没有历史训练记录")
            }
        } else {
            items(groupedHistory.entries.toList().take(20), key = { it.key }) { entry ->
                CardEntrance {
                    HistoryTrainingCard(date = entry.key, trainings = entry.value)
                }
            }
        }
    }
}

@Composable
private fun ExerciseLibraryPage(exercises: List<ExerciseEntity>) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedPart by rememberSaveable { mutableStateOf("全部") }
    var selectedExercise by remember { mutableStateOf<ExerciseEntity?>(null) }

    val partOptions = listOf("全部", "胸", "背", "腿", "肩", "手臂", "核心")
    val filteredExercises = exercises.filter { exercise ->
        val matchesQuery = query.isBlank() ||
            exercise.name.contains(query.trim(), ignoreCase = true) ||
            exercise.targetMuscle.contains(query.trim(), ignoreCase = true)
        val matchesPart = selectedPart == "全部" ||
            exercise.targetMuscle.contains(selectedPart) ||
            (selectedPart == "腿" && exercise.targetMuscle.contains("后链"))
        matchesQuery && matchesPart
    }
    val grouped = filteredExercises.groupBy { it.targetMuscle }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PageHeader(title = "动作库", subtitle = "内置基础训练动作")
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("搜索动作或肌群") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                partOptions.forEach { part ->
                    FilterChip(
                        selected = selectedPart == part,
                        onClick = { selectedPart = part },
                        label = { Text(part) }
                    )
                }
            }
        }
        if (grouped.isEmpty()) {
            item {
                EmptyState(text = if (exercises.isEmpty()) "动作库正在初始化" else "没有匹配的动作")
            }
        } else {
            grouped.forEach { (target, exerciseItems) ->
                item {
                    SectionTitle(target)
                }
                items(exerciseItems, key = { it.name }) { exercise ->
                    CardEntrance {
                        ExerciseCard(
                            exercise = exercise,
                            onClick = { selectedExercise = exercise }
                        )
                    }
                }
            }
        }
    }

    selectedExercise?.let { exercise ->
        ExerciseDetailDialog(
            exercise = exercise,
            onDismiss = { selectedExercise = null }
        )
    }
}

@Composable
private fun MinePage(
    dashboard: DashboardUiState,
    foodCount: Int,
    trainingCount: Int,
    goalForm: GoalFormState,
    onGoalFormChange: (GoalFormState) -> Unit,
    onSaveGoals: () -> Unit,
    onClearUserData: () -> Unit
) {
    var showClearConfirm by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PageHeader(title = "我的", subtitle = "本地记录，不含登录和云同步")
        }
        item {
            OutlinedCard(
                shape = AppCardShape,
                elevation = cardShadow(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MineStatRow("饮食记录", "$foodCount 条")
                    HorizontalDivider()
                    MineStatRow("训练动作", "$trainingCount 条")
                    HorizontalDivider()
                    MineStatRow("连续记录", "${dashboard.streakDays} 天")
                }
            }
        }
        item {
            OutlinedCard(
                shape = AppCardShape,
                elevation = cardShadow(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("每日目标", style = MaterialTheme.typography.titleMedium)
                    goalForm.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GoalNumberField(
                            value = goalForm.dailyCalories,
                            label = "热量",
                            onValueChange = { onGoalFormChange(goalForm.copy(dailyCalories = it)) },
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Number
                        )
                        GoalNumberField(
                            value = goalForm.proteinGrams,
                            label = "蛋白 g",
                            onValueChange = { onGoalFormChange(goalForm.copy(proteinGrams = it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GoalNumberField(
                            value = goalForm.carbsGrams,
                            label = "碳水 g",
                            onValueChange = { onGoalFormChange(goalForm.copy(carbsGrams = it)) },
                            modifier = Modifier.weight(1f)
                        )
                        GoalNumberField(
                            value = goalForm.fatGrams,
                            label = "脂肪 g",
                            onValueChange = { onGoalFormChange(goalForm.copy(fatGrams = it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    GoalNumberField(
                        value = goalForm.weeklyTrainingCount,
                        label = "每周训练次数",
                        onValueChange = { onGoalFormChange(goalForm.copy(weeklyTrainingCount = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardType = KeyboardType.Number
                    )
                    AppButton(
                        text = "保存目标",
                        icon = Icons.Outlined.FitnessCenter,
                        onClick = onSaveGoals,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            OutlinedCard(
                shape = AppCardShape,
                elevation = cardShadow(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("数据管理", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "清除会删除饮食和训练记录，并把目标恢复默认值。动作库会保留。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppOutlinedButton(
                        text = "清除本地数据",
                        icon = Icons.Outlined.RestartAlt,
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("确认清除数据？") },
            text = { Text("此操作会删除所有饮食和训练记录，并重置目标设置。") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearUserData()
                        showClearConfirm = false
                    }
                ) {
                    Text("确认清除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun GoalNumberField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
    )
}

@Composable
private fun PageHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DateSelector(
    date: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit
) {
    Card(
        shape = AppCardShape,
        elevation = cardShadow(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "前一天")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = DateTimeUtils.formatDate(date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onToday) {
                    Text("今天")
                }
            }
            IconButton(onClick = onNextDay) {
                Icon(Icons.Outlined.ChevronRight, contentDescription = "后一天")
            }
        }
    }
}

@Composable
private fun NutritionSummary(totals: NutritionTotals) {
    Card(
        shape = AppCardShape,
        elevation = cardShadow(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("当天合计", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${totals.calories} kcal",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = {}, label = { Text("蛋白 ${totals.protein.trimmedString()}g") })
                AssistChip(onClick = {}, label = { Text("碳水 ${totals.carbs.trimmedString()}g") })
                AssistChip(onClick = {}, label = { Text("脂肪 ${totals.fat.trimmedString()}g") })
            }
        }
    }
}

@Composable
private fun MacroCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = AppCardShape,
        elevation = cardShadow(),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier.heightIn(min = 92.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FoodEntryCard(
    food: FoodEntryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = AppCardShape,
        elevation = cardShadow(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    PillTag(
                        text = food.mealType,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "${food.weightGrams.trimmedString()}g · ${food.calories} kcal · ${DateTimeUtils.formatTime(food.timestamp)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "蛋白 ${food.proteinGrams.trimmedString()}g  碳水 ${food.carbsGrams.trimmedString()}g  脂肪 ${food.fatGrams.trimmedString()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "编辑饮食")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "删除饮食")
            }
        }
    }
}

@Composable
private fun TrainingEntryCard(
    training: TrainingEntryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        shape = AppCardShape,
        elevation = cardShadow(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = training.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${training.bodyPart} · ${training.sets} 组 x ${training.reps} 次 · ${training.weightKg.trimmedString()} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (training.note.isNotBlank()) {
                    Text(
                        text = training.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "编辑训练")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "删除训练")
            }
        }
    }
}

@Composable
private fun HistoryTrainingCard(
    date: String,
    trainings: List<TrainingEntryEntity>
) {
    val bodyParts = trainings.map { it.bodyPart }.distinct().joinToString(" / ")
    val totalSets = trainings.sumOf { it.sets }

    OutlinedCard(
        shape = AppCardShape,
        elevation = cardShadow(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$bodyParts · ${trainings.size} 个动作 · $totalSets 组",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = trainings.joinToString("、") { it.exerciseName },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseEntity,
    onClick: () -> Unit
) {
    OutlinedCard(
        shape = AppCardShape,
        elevation = cardShadow(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = exercise.recommendedSetsReps,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            PillTag(
                text = exercise.targetMuscle,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(exercise.instruction, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "常见错误：${exercise.commonMistakes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExerciseDetailDialog(
    exercise: ExerciseEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(exercise.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailBlock("目标肌群", exercise.targetMuscle)
                DetailBlock("动作说明", exercise.instruction)
                DetailBlock("常见错误", exercise.commonMistakes)
                DetailBlock("建议组数次数", exercise.recommendedSetsReps)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun DetailBlock(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SuggestionRow(text: String) {
    Card(
        shape = AppCardShape,
        elevation = cardShadow(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun EmptyState(text: String) {
    OutlinedCard(
        shape = AppCardShape,
        elevation = cardShadow(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MineStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodEntryDialog(
    form: FoodFormState,
    onFormChange: (FoodFormState) -> Unit,
    onSelectCommonFood: (CommonFoodOption) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (form.id == null) "添加一餐" else "编辑饮食") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                form.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "常用食物",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CommonFoodOptions.forEach { food ->
                        AssistChip(
                            onClick = { onSelectCommonFood(food) },
                            label = { Text(food.name) }
                        )
                    }
                }
                OutlinedTextField(
                    value = form.name,
                    onValueChange = { onFormChange(form.copy(name = it)) },
                    label = { Text("食物名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = form.weightGrams,
                        onValueChange = { onFormChange(form.copy(weightGrams = it)) },
                        label = { Text("重量 g") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = form.calories,
                        onValueChange = { onFormChange(form.copy(calories = it)) },
                        label = { Text("热量") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MacroTextField(
                        value = form.proteinGrams,
                        label = "蛋白",
                        onValueChange = { onFormChange(form.copy(proteinGrams = it)) },
                        modifier = Modifier.weight(1f)
                    )
                    MacroTextField(
                        value = form.carbsGrams,
                        label = "碳水",
                        onValueChange = { onFormChange(form.copy(carbsGrams = it)) },
                        modifier = Modifier.weight(1f)
                    )
                    MacroTextField(
                        value = form.fatGrams,
                        label = "脂肪",
                        onValueChange = { onFormChange(form.copy(fatGrams = it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "餐别",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealType.entries.forEach { meal ->
                        FilterChip(
                            selected = form.mealType == meal.label,
                            onClick = { onFormChange(form.copy(mealType = meal.label)) },
                            label = { Text(meal.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = form.time,
                    onValueChange = { onFormChange(form.copy(time = it)) },
                    label = { Text("时间 HH:mm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingEntryDialog(
    form: TrainingFormState,
    exercises: List<ExerciseEntity>,
    onFormChange: (TrainingFormState) -> Unit,
    onSelectExercise: (ExerciseEntity) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (form.id == null) "添加训练动作" else "编辑训练动作") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                form.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedTextField(
                    value = form.date,
                    onValueChange = { onFormChange(form.copy(date = it)) },
                    label = { Text("训练日期 yyyy-MM-dd") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (exercises.isNotEmpty()) {
                    Text(
                        text = "从动作库选择",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        exercises.forEach { exercise ->
                            AssistChip(
                                onClick = { onSelectExercise(exercise) },
                                label = { Text(exercise.name) }
                            )
                        }
                    }
                }
                Text(
                    text = "训练部位",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BodyPartOptions.forEach { part ->
                        FilterChip(
                            selected = form.bodyPart == part,
                            onClick = { onFormChange(form.copy(bodyPart = part)) },
                            label = { Text(part) }
                        )
                    }
                }
                OutlinedTextField(
                    value = form.exerciseName,
                    onValueChange = { onFormChange(form.copy(exerciseName = it)) },
                    label = { Text("动作名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = form.sets,
                        onValueChange = { onFormChange(form.copy(sets = it)) },
                        label = { Text("组数") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = form.reps,
                        onValueChange = { onFormChange(form.copy(reps = it)) },
                        label = { Text("次数") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = form.weightKg,
                        onValueChange = { onFormChange(form.copy(weightKg = it)) },
                        label = { Text("重量 kg") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = form.note,
                    onValueChange = { onFormChange(form.copy(note = it)) },
                    label = { Text("备注") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun MacroTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}
