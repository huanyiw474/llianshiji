# 练食记

练食记是一个面向健身人群的本地饮食 + 训练记录 Android App。它用于记录每日饮食、训练动作、营养摄入和训练状态，帮助用户长期稳定地管理健身生活。

当前版本是本地 MVP 迭代版，重点是先把日常记录闭环做顺：快速录入、清晰查看、当天反馈、目标进度和简单建议。

## 当前状态

- App 名称：练食记
- 包名：`com.lianshiji.app`
- 技术栈：Kotlin、Jetpack Compose、Room、MVVM
- 数据存储：本地 Room 数据库
- 当前阶段：MVP + 第二轮交互与视觉优化
- 不包含：登录、云同步、社交、联网 API、AI API

## 已实现功能

### 首页

- 今日完成度圆形进度。
- 完成度公式：

```text
(热量完成 + 蛋白完成 + 训练完成) / 3
```

- 动态激励文案：
  - 低于 30%：开始记录今天的第一步
  - 30%-70%：今天已经完成一半
  - 70% 以上：离目标很近
- 今日目标进度：
  - 热量
  - 蛋白质
  - 碳水
  - 脂肪
- 快捷入口：
  - 记饮食
  - 记训练
- 今日待完成提醒，例如：
  - 蛋白质 40g
  - 训练 1次
  - 热量 500kcal
- 今日建议最多展示 2 条，避免首页过长。

### 饮食模块

- 新增饮食记录。
- 编辑饮食记录。
- 删除饮食记录。
- 查看选中日期饮食列表。
- 饮食列表按餐别分组显示。
- 自动统计当天总热量、蛋白质、碳水、脂肪。
- 添加饮食时支持常用食物快捷选择。
- 支持复制昨天饮食。

饮食字段：

- 食物名称
- 餐别
- 重量 g
- 热量 kcal
- 蛋白质 g
- 碳水 g
- 脂肪 g
- 时间

餐别：

- 早餐
- 午餐
- 晚餐
- 加餐

### 训练模块

- 新增训练动作。
- 编辑训练动作。
- 删除训练动作。
- 按日期查看当天训练动作。
- 当天训练以卡片列表展示。
- 历史记录展示：
  - 训练日期
  - 训练部位
  - 动作数量
  - 总组数
- 添加训练时支持从动作库选择。
- 支持复制上次训练。

训练字段：

- 动作名称
- 部位
- 组数
- 次数
- 重量 kg
- 备注

### 动作库

- 内置基础训练动作。
- 支持搜索动作或目标肌群。
- 支持按部位筛选：
  - 胸
  - 背
  - 腿
  - 肩
  - 手臂
  - 核心
- 支持动作详情弹窗。

动作详情包含：

- 动作说明
- 目标肌群
- 常见错误
- 建议组数次数

当前内置动作：

- 卧推
- 深蹲
- 硬拉
- 引体向上
- 划船
- 肩推
- 弯举
- 腿举

### 我的页面

- 展示饮食记录数量。
- 展示训练动作数量。
- 展示连续记录天数。
- 支持用户目标设置：
  - 每日热量目标
  - 蛋白质目标
  - 碳水目标
  - 脂肪目标
  - 每周训练次数目标
- 首页会根据这里的目标计算进度。
- 支持清除本地数据。
- 清除数据前有二次确认。

### 视觉优化

- 保持绿色健身主题。
- 首页增加深绿色渐变背景。
- 顶部使用运动风标题区。
- 首页主卡使用黑绿到亮绿渐变。
- 今日完成度圆环放大展示。
- 大数字突出热量和完成度。
- 统一 16dp 卡片圆角。
- 卡片增加轻阴影。
- 使用胶囊标签。
- 增加图标辅助识别。
- 底部导航栏保持五栏结构，并增强选中状态。
- 增加动效：
  - 页面切换动画
  - 按钮点击缩放
  - 卡片出现动画

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
│   │   │   ├── TrainingDao.kt
│   │   │   └── UserGoalDao.kt
│   │   └── entity
│   │       ├── ExerciseEntity.kt
│   │       ├── FoodEntryEntity.kt
│   │       ├── TrainingEntryEntity.kt
│   │       └── UserGoalEntity.kt
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

应用级入口，负责初始化 Room 数据库和 `FitnessRepository`。

### `MainActivity.kt`

Activity 入口，创建 `MainViewModel` 并加载 Compose 页面。

### `LianShiJiDatabase.kt`

Room 数据库定义。当前数据库版本为 v2。

包含表：

- `food_entries`
- `training_entries`
- `exercises`
- `user_goals`

### `FoodEntryEntity.kt`

饮食记录实体。

### `TrainingEntryEntity.kt`

训练动作记录实体。

### `ExerciseEntity.kt`

动作库实体。

### `UserGoalEntity.kt`

用户目标实体，用于保存每日热量和宏量营养素目标，以及每周训练次数目标。

### DAO

- `FoodDao.kt`：饮食记录增删改查、按日期查询、批量插入、清空。
- `TrainingDao.kt`：训练记录增删改查、按日期查询、查询上次训练、批量插入、清空。
- `ExerciseDao.kt`：动作库查询和默认动作写入。
- `UserGoalDao.kt`：用户目标读取和保存。

### `FitnessRepository.kt`

Repository 层，统一封装本地数据访问。

当前负责：

- 饮食记录读写
- 训练记录读写
- 动作库读取
- 默认动作库初始化
- 用户目标保存
- 复制昨天饮食
- 复制上次训练
- 清除本地用户数据

### `MainViewModel.kt`

ViewModel 层，负责页面状态和业务逻辑。

当前负责：

- 选中日期
- 今日饮食列表
- 今日训练列表
- 首页目标进度
- 今日完成度
- 今日待完成提醒
- 动态激励文案
- 简单建议规则
- 饮食表单状态
- 训练表单状态
- 目标设置表单状态
- 新增、编辑、删除、复制等操作

### `LianShiJiApp.kt`

主要 Compose UI 文件。

当前包含：

- 底部导航
- 首页
- 饮食页
- 训练页
- 动作库页
- 我的页
- 饮食新增/编辑弹窗
- 训练新增/编辑弹窗
- 动作详情弹窗
- 数据清除确认弹窗
- 视觉组件和动效组件

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
| `timestamp` | Long | 时间戳 |

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

### `user_goals`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | Int | 固定为 1 |
| `dailyCalories` | Int | 每日热量目标 |
| `proteinGrams` | Float | 蛋白质目标 |
| `carbsGrams` | Float | 碳水目标 |
| `fatGrams` | Float | 脂肪目标 |
| `weeklyTrainingCount` | Int | 每周训练次数目标 |

## 构建与运行

### 命令行构建

```powershell
cd C:\Users\SB\Documents\Codex\2026-05-26\android-app-app-mvp-kotlin-jetpack
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

Debug APK 路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 安装到设备

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

如果看不到最新 UI，先卸载旧版本或确认 Android Studio 运行的是当前项目的 `app` 配置。

## 开发记录

### 2026-05-26

完成初版 MVP：

- 创建 Android 项目结构。
- 配置 Kotlin、Jetpack Compose、Room、KSP。
- 建立 Room 数据库、Entity、DAO、Repository。
- 实现首页、饮食、训练、动作库、我的页面。
- 实现饮食 CRUD。
- 实现训练 CRUD。
- 实现默认动作库。
- 实现简单规则建议。
- 完成 Debug 构建验证。

### 2026-05-27

完成第二轮优化：

- 首页增加今日目标进度。
- 首页增加今日完成度圆形进度。
- 首页增加动态激励文案。
- 首页增加今日待完成提醒。
- 饮食增加常用食物快捷选择。
- 饮食列表按餐别分组。
- 饮食增加复制昨天功能。
- 训练添加时支持从动作库选择。
- 训练历史增加部位、动作数、总组数展示。
- 训练增加复制上次训练功能。
- 动作库增加搜索。
- 动作库增加按部位筛选。
- 动作库增加详情弹窗。
- 我的页面增加用户目标设置。
- 我的页面增加清除数据入口和二次确认。
- 数据库升级到 v2，新增 `user_goals` 表。
- 视觉升级：
  - 深绿色渐变首页
  - 顶部渐变卡片
  - 大圆形完成度
  - 胶囊标签
  - 图标
  - 卡片阴影
  - 16dp 圆角
  - 页面切换动画
  - 按钮点击动画
  - 卡片出现动画

验证命令：

```powershell
.\gradlew.bat assembleDebug --stacktrace --no-daemon
```

验证结果：

```text
BUILD SUCCESSFUL
```

## GitHub 推送说明

当前项目使用 `.upload-git` 作为临时 Git 元数据目录。

常规推送命令：

```powershell
git --git-dir=.upload-git --work-tree=. push https://github.com/huanyiw474/llianshiji.git main:main
```

如果遇到 Windows Git 的 `schannel` 报错：

```text
schannel: server closed abruptly (missing close_notify)
```

可以尝试使用 OpenSSL 后端和 HTTP/1.1：

```powershell
git --git-dir=.upload-git --work-tree=. -c http.sslBackend=openssl -c http.version=HTTP/1.1 push https://github.com/huanyiw474/llianshiji.git main:main
```

如果仍然失败，可以先重试一次，或者换网络环境后再推送。这个错误通常是 HTTPS/TLS 连接被中途关闭，不代表代码或提交有问题。

## 后续可迭代方向

- 增加食物模板和自定义常用食物。
- 增加训练计划模板。
- 增加体重、围度、体脂记录。
- 增加周/月统计图表。
- 增加训练容量统计。
- 增加动作库更多动作。
- 增加数据导出。
- 增加更细的建议规则。
- 后续再考虑登录、云同步和 AI 建议。
