package com.lianshiji.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val isFoodDialogOpen by viewModel.isFoodDialogOpen.collectAsState()
    val isTrainingDialogOpen by viewModel.isTrainingDialogOpen.collectAsState()

    Scaffold(
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
            NavigationBar {
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
                        label = { Text(destination.label, maxLines = 1) }
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
            when (currentDestination) {
                AppDestination.Dashboard -> DashboardPage(dashboard)
                AppDestination.Food -> FoodPage(
                    selectedDate = selectedDate,
                    totals = selectedTotals,
                    foods = selectedFoods,
                    onPreviousDay = viewModel::selectPreviousDay,
                    onNextDay = viewModel::selectNextDay,
                    onToday = viewModel::selectToday,
                    onAdd = viewModel::openNewFoodForm,
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
                    onEdit = viewModel::openEditTrainingForm,
                    onDelete = viewModel::deleteTraining
                )
                AppDestination.Exercises -> ExerciseLibraryPage(exercises)
                AppDestination.Me -> MinePage(
                    dashboard = dashboard,
                    foodCount = allFoods.size,
                    trainingCount = allTrainings.size
                )
            }
        }
    }

    if (isFoodDialogOpen) {
        FoodEntryDialog(
            form = foodForm,
            onFormChange = viewModel::updateFoodForm,
            onDismiss = viewModel::dismissFoodForm,
            onSave = viewModel::saveFood
        )
    }

    if (isTrainingDialogOpen) {
        TrainingEntryDialog(
            form = trainingForm,
            onFormChange = viewModel::updateTrainingForm,
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

@Composable
private fun DashboardPage(state: DashboardUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PageHeader(
                title = "练食记",
                subtitle = "${state.todayLabel} · 饮食与训练记录"
            )
        }
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "今日摄入",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${state.calories} kcal",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = state.trainingStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroCard(
                    label = "蛋白质",
                    value = "${state.protein.trimmedString()} g",
                    color = Color(0xFF2F6B4F),
                    modifier = Modifier.weight(1f)
                )
                MacroCard(
                    label = "碳水",
                    value = "${state.carbs.trimmedString()} g",
                    color = Color(0xFF2F5F8F),
                    modifier = Modifier.weight(1f)
                )
                MacroCard(
                    label = "脂肪",
                    value = "${state.fat.trimmedString()} g",
                    color = Color(0xFF8A5D19),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            OutlinedCard(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("连续记录", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "包含饮食或训练任一记录",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${state.streakDays} 天",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        item {
            SectionTitle("今日建议")
        }
        items(state.suggestions) { suggestion ->
            SuggestionRow(text = suggestion)
        }
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
    onEdit: (FoodEntryEntity) -> Unit,
    onDelete: (FoodEntryEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("添加一餐")
            }
        }
        if (foods.isEmpty()) {
            item {
                EmptyState(text = "这一天还没有饮食记录")
            }
        } else {
            items(foods, key = { it.id }) { food ->
                FoodEntryCard(
                    food = food,
                    onEdit = { onEdit(food) },
                    onDelete = { onDelete(food) }
                )
            }
        }
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
    onEdit: (TrainingEntryEntity) -> Unit,
    onDelete: (TrainingEntryEntity) -> Unit
) {
    val groupedHistory = allTrainings.groupBy { DateTimeUtils.formatDate(it.performedAt) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("添加训练动作")
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
                TrainingEntryCard(
                    training = training,
                    onEdit = { onEdit(training) },
                    onDelete = { onDelete(training) }
                )
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
                HistoryTrainingCard(date = entry.key, trainings = entry.value)
            }
        }
    }
}

@Composable
private fun ExerciseLibraryPage(exercises: List<ExerciseEntity>) {
    val grouped = exercises.groupBy { it.targetMuscle }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(title = "动作库", subtitle = "内置基础训练动作")
        }
        if (grouped.isEmpty()) {
            item {
                EmptyState(text = "动作库正在初始化")
            }
        } else {
            grouped.forEach { (target, exerciseItems) ->
                item {
                    SectionTitle(target)
                }
                items(exerciseItems, key = { it.name }) { exercise ->
                    ExerciseCard(exercise)
                }
            }
        }
    }
}

@Composable
private fun MinePage(
    dashboard: DashboardUiState,
    foodCount: Int,
    trainingCount: Int
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageHeader(title = "我的", subtitle = "本地记录，不含登录和云同步")
        }
        item {
            OutlinedCard(
                shape = RoundedCornerShape(8.dp),
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
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("MVP 范围", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "当前版本只使用本地 Room 数据库，先专注饮食和训练记录闭环。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
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
    OutlinedCard(
        shape = RoundedCornerShape(8.dp),
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
    OutlinedCard(
        shape = RoundedCornerShape(8.dp),
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
        shape = RoundedCornerShape(8.dp),
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
    OutlinedCard(
        shape = RoundedCornerShape(8.dp),
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
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = food.mealType,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
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
        shape = RoundedCornerShape(8.dp),
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
                Text(
                    text = training.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
    OutlinedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$date · ${trainings.size} 个动作",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
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
private fun ExerciseCard(exercise: ExerciseEntity) {
    OutlinedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
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
private fun SuggestionRow(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
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
        shape = RoundedCornerShape(8.dp),
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
    onFormChange: (TrainingFormState) -> Unit,
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
