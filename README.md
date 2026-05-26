# 练食记

练食记是一个面向健身人群的本地饮食 + 训练记录 Android App。当前版本是 2026-05-26 完成的 MVP，目标是先跑通日常记录闭环：记录吃了什么、练了什么，并在首页看到当天摄入、训练状态和简单建议。

## 当前版本

- 版本阶段：MVP
- App 名称：练食记
- 包名：`com.lianshiji.app`
- 技术栈：Kotlin、Jetpack Compose、Room、MVVM
- 数据策略：本地 Room 数据库优先
- 暂不包含：登录、云同步、社交、AI API

## 已实现功能

### 首页 Dashboard

- 显示今日摄入热量。
- 显示今日蛋白质、碳水、脂肪。
- 显示今日训练状态。
- 显示连续记录天数。
- 根据饮食和训练记录给出简单规则建议。

### 饮食记录

- 新增一餐。
- 编辑饮食记录。
- 删除饮食记录。
- 查看选中日期的饮食列表，默认用于今日列表。
- 自动统计当天总热量、蛋白质、碳水、脂肪。
- 字段包括：
  - 食物名称
  - 重量 g
  - 热量 kcal
  - 蛋白质 g
  - 碳水 g
  - 脂肪 g
  - 餐别
  - 时间

### 训练记录

- 新增训练动作。
- 编辑训练动作。
- 删除训练动作。
- 按日期查看当天训练详情。
- 查看历史训练记录。
- 字段包括：
  - 训练日期
  - 训练部位
  - 动作名称
  - 组数
  - 次数
  - 重量 kg
  - 备注

### 动作库

已内置基础训练动作：

- 卧推
- 深蹲
- 硬拉
- 引体向上
- 划船
- 肩推
- 弯举
- 腿举

每个动作包含：

- 名称
- 目标肌群
- 动作说明
- 常见错误
- 建议组数次数

### 建议功能

当前建议使用本地规则逻辑，不接入 AI API。

已实现的规则包括：

- 今日没有饮食记录时提醒补充记录。
- 蛋白质不足时提醒增加蛋白质来源。
- 训练日碳水偏低时提醒补充碳水。
- 脂肪偏高时提醒下一餐更清淡。
- 今日没有训练记录时提醒安排短训练。
- 最近 7 天没有腿部训练时提醒安排腿部训练。

## 页面结构

底部导航包含 5 个页面：

- 首页
- 饮食
- 训练
- 动作库
- 我的

## 项目结构

```text
app/src/main/java/com/lianshiji/app
├── LianShiJiApplication.kt
├── MainActivity.kt
├── data
│   ├── local
│   │   ├── LianShiJiDatabase.kt
│   │   ├── dao
│   │   │   ├── ExerciseDao.kt
│   │   │   ├── FoodDao.kt
│   │   │   └── TrainingDao.kt
│   │   └── entity
│   │       ├── ExerciseEntity.kt
│   │       ├── FoodEntryEntity.kt
│   │       └── TrainingEntryEntity.kt
│   └── repository
│       └── FitnessRepository.kt
├── ui
│   ├── LianShiJiApp.kt
│   ├── MainViewModel.kt
│   └── theme
│       └── Theme.kt
└── util
    └── DateTimeUtils.kt
```

## 核心文件说明

### `LianShiJiApplication.kt`

应用入口级别的依赖初始化。当前负责创建 Room 数据库实例，并把 `FitnessRepository` 暴露给 `MainActivity` 使用。

### `MainActivity.kt`

Android Activity 入口。通过 `viewModels` 创建 `MainViewModel`，并加载 Compose 根页面 `LianShiJiApp`。

### `LianShiJiDatabase.kt`

Room 数据库定义，当前包含三张表：

- `food_entries`
- `training_entries`
- `exercises`

### `FoodEntryEntity.kt`

饮食记录表实体，对应一条饮食记录。

### `TrainingEntryEntity.kt`

训练动作记录表实体，对应一次训练中的一个动作记录。

### `ExerciseEntity.kt`

动作库实体，当前作为内置基础动作数据。

### `FoodDao.kt`

饮食记录 DAO，提供：

- 查询全部饮食记录
- 按时间范围查询饮食记录
- 新增
- 更新
- 删除

### `TrainingDao.kt`

训练记录 DAO，提供：

- 查询全部训练记录
- 按时间范围查询训练记录
- 新增
- 更新
- 删除

### `ExerciseDao.kt`

动作库 DAO，提供：

- 查询全部动作
- 查询动作数量
- 批量插入内置动作

### `FitnessRepository.kt`

Repository 层，统一封装 DAO 调用。当前负责：

- 饮食记录读写
- 训练记录读写
- 动作库读取
- 首次启动时写入默认动作库

### `MainViewModel.kt`

MVVM 中的 ViewModel 层。当前负责：

- 维护选中日期
- 暴露饮食列表
- 暴露训练列表
- 计算今日 Dashboard 状态
- 计算营养合计
- 处理饮食新增、编辑、删除
- 处理训练新增、编辑、删除
- 生成简单规则建议

### `LianShiJiApp.kt`

Compose UI 主文件。当前包含：

- 底部导航
- 首页
- 饮食页
- 训练页
- 动作库页
- 我的页
- 饮食新增/编辑弹窗
- 训练新增/编辑弹窗
- 列表卡片和统计卡片

### `DateTimeUtils.kt`

日期时间工具类，负责：

- 获取今日日期
- 日期与毫秒时间戳转换
- 格式化日期
- 格式化时间
- 解析日期和时间输入

## 数据库设计

### `food_entries`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | Long | 主键，自增 |
| `name` | String | 食物名称 |
| `weightGrams` | Float | 重量 |
| `calories` | Int | 热量 |
| `proteinGrams` | Float | 蛋白质 |
| `carbsGrams` | Float | 碳水 |
| `fatGrams` | Float | 脂肪 |
| `mealType` | String | 餐别 |
| `timestamp` | Long | 记录时间 |

### `training_entries`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | Long | 主键，自增 |
| `performedAt` | Long | 训练日期时间 |
| `bodyPart` | String | 训练部位 |
| `exerciseName` | String | 动作名称 |
| `sets` | Int | 组数 |
| `reps` | Int | 次数 |
| `weightKg` | Float | 重量 |
| `note` | String | 备注 |

### `exercises`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `name` | String | 主键，动作名称 |
| `targetMuscle` | String | 目标肌群 |
| `instruction` | String | 动作说明 |
| `commonMistakes` | String | 常见错误 |
| `recommendedSetsReps` | String | 建议组数次数 |

## 构建与运行

### 命令行构建

```powershell
cd C:\Users\SB\Documents\Codex\2026-05-26\android-app-app-mvp-kotlin-jetpack
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

构建成功后，Debug APK 会生成在：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Android Studio 运行

1. 使用 Android Studio 打开项目根目录。
2. 等待 Gradle Sync 完成。
3. 选择 `app` 配置。
4. 连接模拟器或真机。
5. 点击 Run。

## 2026-05-26 开发记录

今天完成了从空目录到可运行 MVP 的搭建：

- 创建 Android Gradle 项目结构。
- 配置 Kotlin、Jetpack Compose、Room、KSP。
- 补齐 Gradle Wrapper。
- 配置本地 Android SDK 构建环境。
- 建立 Room 数据库、Entity、DAO、Repository。
- 实现 MVVM 状态管理。
- 实现首页 Dashboard。
- 实现饮食记录完整 CRUD。
- 实现训练记录完整 CRUD。
- 实现动作库页面和默认动作数据。
- 实现规则建议功能。
- 实现底部五栏导航。
- 完成 Debug 构建验证。

验证命令：

```powershell
.\gradlew.bat assembleDebug --stacktrace --no-daemon
```

验证结果：

```text
BUILD SUCCESSFUL
```

## 后续可迭代方向

- 增加饮食模板和常吃食物。
- 增加训练计划模板。
- 增加体重、围度、体脂记录。
- 增加目标热量和目标宏量营养素设置。
- 增加按周/月统计图表。
- 增加数据导出。
- 增加动作库搜索和筛选。
- 增加训练容量统计。
- 增加更细的训练建议规则。
- 后续再考虑登录、云同步和 AI 建议。
