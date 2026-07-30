---
name: android-test-gen
description: >
  Android 测试用例生成器。当 android-dev-workflow 执行 Step 5 时由编排器调用，
  或在用户明确说「生成测试用例」「写测试代码」「生成 test plan」时独立触发。
  读取 02-plan.md 中的 Task 列表，为每个 Task 生成对应的测试代码文件
  （JUnit 4/5 + Mockito + Espresso，遵循项目已有测试风格），
  并生成 03-test-plan.md 文档记录 Task-测试映射关系。
  所有测试初始状态必须为 FAIL（红阶段），以符合 TDD 流程。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-30'
  keywords:
  - android
  - testing
  - tdd
  - junit
  - mockito
  - espresso
  - test-generation
---

# Android 测试用例生成

## 前置条件

以下文件必须存在：
- `docs/features/<feature-name>/02-plan.md` — 任务拆分文档

若不存在，提示用户先完成 Step 1-4（brainstorming）。

## 工作流

### Phase 1：分析 Task 列表

1. 读取 `02-plan.md`，提取所有 Task
2. 对每个 Task 提取：
   - Task 编号和名称
   - 涉及文件（Create/Modify）
   - 描述（实现目标）
3. 确定测试目录位置：
   - Unit Test：`src/test/java/<package>/`
   - Instrumented Test：`src/androidTest/java/<package>/`
   - 检查项目已有测试目录结构，保持一致

### Phase 2：确定测试框架

不与项目现有测试风格冲突。检查条件：
- 如果项目中已有 `.kt` 测试文件 → 使用 Kotlin + JUnit
- 如果项目中已有 `.java` 测试文件 → 使用 Java + JUnit
- 检查 build.gradle 中已有的测试依赖（junit、mockito、espresso、robolectric）
- 如果项目中不存在任何测试 → 默认使用 Kotlin + JUnit 4 + Mockito-Kotlin + Espresso

### Phase 3：逐 Task 生成测试

对每个 Task 生成 **三类测试覆盖**：

#### 覆盖类型

| 类型 | 说明 | 示例 |
|------|------|------|
| **正常路径** | 给定正确输入，验证预期输出 | 登录成功 → Token 已保存，跳转首页 |
| **边界条件** | null、空字符串、空集合、极值 | 用户名长度=0、密码=null |
| **异常路径** | 非法输入、网络失败、权限拒绝 | 网络超时、服务端返回 500 |

#### 测试类命名

- 单元测试：`<源文件名>Test.kt`（如 `LoginViewModelTest.kt`）
- UI 测试：`<源文件名>InstrumentedTest.kt`（如 `LoginActivityInstrumentedTest.kt`）

#### 测试代码风格

严格遵循 `android-code-style` skill 中的编码规范。

**每个测试类的基本结构：**

```kotlin
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var analyticsTracker: AnalyticsTracker

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(userRepository, analyticsTracker)
    }

    @Test
    fun `login with valid credentials emits success state`() {
        // Given
        // When
        // Then
        TODO("Test not yet implemented")
    }

    @Test
    fun `login with empty password emits error state`() {
        TODO("Test not yet implemented")
    }

    @Test
    fun `login when network fails emits network error state`() {
        TODO("Test not yet implemented")
    }
}
```

关键规则：
- 测试方法名使用反引号英文描述（`` `login with valid credentials emits success state` ``）
- Given/When/Then 注释标注三段结构
- `TODO("Test not yet implemented")` 保证初始状态 FAIL
- 使用 `@Mock` 标注 mock 依赖，`private lateinit var` 声明被测对象
- 使用 `InstantTaskExecutorRule` 处理 LiveData

### Phase 4：生成 03-test-plan.md

文档路径：`docs/features/<feature-name>/03-test-plan.md`

**文档模板：**

```markdown
# 测试计划 — <feature-name>

## 概览

| 指标 | 数值 |
|------|------|
| Task 总数 | N |
| 测试类总数 | M |
| 测试用例总数 | K |
| 正常路径覆盖 | X |
| 边界条件覆盖 | Y |
| 异常路径覆盖 | Z |

## Task-测试映射

| Task | 测试类 | 覆盖类型 | 用例数 | 状态 |
|------|-------|---------|--------|------|
| Task 1: <名称> | <TestClassName> | 正常/边界/异常 | 3 | FAIL |
| Task 2: <名称> | <TestClassName> | 正常/边界/异常 | 2 | FAIL |
| ... | ... | ... | ... | FAIL |

## 测试文件清单

- `src/test/java/<package>/<TestClass1>.kt` — <简要说明>
- `src/test/java/<package>/<TestClass2>.kt` — <简要说明>
- `src/androidTest/java/<package>/<UITestClass>.kt` — <简要说明>

## 依赖

- JUnit 4 / 5
- Mockito-Kotlin / Mockito
- Espresso（UI 测试）
- InstantTaskExecutorRule（LiveData）
```

### Phase 5：验证初始 FAIL 状态

在创建测试文件后，运行测试确认初始状态：

```bash
./gradlew testDebugUnitTest --tests "<package>.<TestClass>*"
```

预期：**全部 FAIL**（因为实现代码尚未编写，TODO() 抛出异常）。

若某个测试意外 PASS，说明实现代码已经存在，标注 `⚠️ 已存在实现` 并保留测试。

---

## 固定输出

```
## Step 5: 测试用例编写

📄 产物:
  docs/features/<feature-name>/03-test-plan.md
  src/test/java/<package>/<TestClass1>.kt
  src/test/java/<package>/<TestClass2>.kt
  ...

📊 测试统计:
  Task 总数: N
  测试类: M
  测试用例: K
  → 正常路径: X, 边界条件: Y, 异常路径: Z
  → 初始状态: 全部 FAIL ✅

✅ Step 5 完成
```

---

## 边界

| 场景 | 行为 |
|------|------|
| 02-plan.md 不存在 | 提示用户先完成 Step 1-4 |
| plan 中无 Task | 硬阻塞，流程终止 |
| Task 只有描述无具体文件路径 | 推断文件名，标注 `⚠️ 推测路径` |
| 同名测试文件已存在 | 追加 `_new` 后缀到新文件名，警告用户合并 |
| 项目无任何测试依赖 | 终止，输出缺少的依赖清单（junit、mockito 等），要求用户先配置 |
| Task 涉及纯 UI 布局变化（XML） | 生成简单的 Activity/Fragment 启动测试，确保无崩溃 |
| 测试目录不存在 | 自动创建目录 |

---

## 特殊场景处理

### ViewModel 测试

```kotlin
@RunWith(MockitoJUnitRunner::class)
class XxxViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()  // 若项目有协程 Rule

    @Mock
    private lateinit var repository: XxxRepository

    private lateinit var viewModel: XxxViewModel

    @Before
    fun setUp() {
        viewModel = XxxViewModel(repository)
    }
}
```

### Repository/数据层测试

```kotlin
@RunWith(MockitoJUnitRunner::class)
class XxxRepositoryTest {
    @Mock
    private lateinit var apiService: XxxApiService

    @Mock
    private lateinit var localDataSource: XxxLocalDataSource

    private lateinit var repository: XxxRepository

    @Before
    fun setUp() {
        repository = XxxRepository(apiService, localDataSource)
    }
}
```

### Espresso UI 测试

```kotlin
@RunWith(AndroidJUnit4::class)
class XxxActivityInstrumentedTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(XxxActivity::class.java)

    @Test
    fun `initial UI state shows expected elements`() {
        TODO("Test not yet implemented")
    }
}
```
