# Section E 注释检查重构：grep → detekt + Checkstyle

## 背景

`android-code-review` Section E 当前使用纯 bash（grep + sed）扫描 KDoc/Javadoc 缺失，判定逻辑为检查声明前 5 行是否含 `/**`。存在以下局限：

1. **精度不足**：正则匹配无法理解语义，容易误报/漏报（如 KDoc 写在别处、多行声明的识别等）
2. **不可复用**：纯 bash 脚本只存在于 opencode skill 中，CI 或 IDE 无法直接调用
3. **维护成本**：正则表达式随 Kotlin/Java 语法演进需不断修补

## 目标

用 **detekt**（Kotlin）和 **Checkstyle**（Java）替代 grep+sed 作为 Section E 的检测引擎，必要时**引导项目安装**这两个工具。

## 设计

### 总体流程

```
android-code-review Section E 入口
  ↓
① 前置检查：项目是否已配置 detekt + Checkstyle？
  ├─ 未配置 → 引导安装
  │   ├─ 展示将要添加的内容（版本号、插件、配置文件）
  │   ├─ 用户确认 → 自动写入以下文件：
  │   │   ├─ gradle/libs.versions.toml（版本号）
  │   │   ├─ Basic/build.gradle（插件 + 依赖 + 自定义 task）
  │   │   ├─ .detekt.yml（Kotlin 注释检查规则）
  │   │   └─ checkstyle.xml（Java Javadoc 检查规则）
  │   └─ 安装完成 → 继续
  └─ 已配置 → 继续
  ↓
② skill 根据 scope 参数收集文件列表
  - full: 全项目 .kt / .java 文件（排除 test/）
  - staged: git diff --cached 中的 .kt / .java 文件
  ↓
③ 调用 Gradle task 执行检测
  ./gradlew :Basic:checkComments -Pscope=full|staged
  ↓
④ 解析 JSON 输出 → Section E 命中列表（E1/E2/E3）
  ↓
⑤ AI 一次性补全所有缺失 KDoc/Javadoc
    按文件批量生成，逐项展示 diff
  ↓
⑥ 逐项确认（y/n）
  - y → 写入注释
  - n → 该项回退硬阻塞，用户自行补全
```

### 组件职责

#### 1. `android-code-review` skill（编排层）

- 入口：接收 scope 参数（full | staged，默认 full）
- 前置检查：检测 `Basic/build.gradle` 是否含 detekt/Checkstyle 插件、`.detekt.yml`/`checkstyle.xml` 是否存在
- 未配置时引导安装：生成配置内容预览，用户确认后写入文件
- scope→文件列表：`find` 或 `git diff --cached --name-only` 收集目标文件
- 调用 Gradle task 并解析输出
- 命中列表移交 opencode AI 补全

#### 2. Gradle 自定义 task（执行层）

位于 `Basic/build.gradle`，task 名 `checkComments`：

```groovy
tasks.register('checkComments') {
    doLast {
        // 1. 根据 scope 参数收集文件列表
        // 2. 分别调用 detekt（Kotlin）和 Checkstyle（Java）
        // 3. 合并结果输出 JSON
    }
}
```

#### 3. detekt（Kotlin 检测引擎）

- 启用规则：
  - `UndocumentedPublicClass` → 对应 E1
  - `UndocumentedPublicFunction` → 对应 E2
  - `UndocumentedPublicProperty` → 对应 E3
- 排除：`protected`、`override`、`companion object`、`test/` 目录
- 输出格式：XML 或 SARIF，Gradle task 统一转为 JSON

#### 4. Checkstyle（Java 检测引擎）

- 启用规则：
  - `JavadocType` → 对应 E1
  - `JavadocMethod` → 对应 E2
- 排除：`protected`、`test/` 目录
- 输出格式：XML，Gradle task 统一转为 JSON

### 需要生成的配置文件

#### `.detekt.yml`（项目根目录）

仅启用注释相关规则，其余关闭：

```yaml
comments:
  UndocumentedPublicClass:
    active: true
    excludes: ['**/test/**']
  UndocumentedPublicFunction:
    active: true
    excludes: ['**/test/**']
  UndocumentedPublicProperty:
    active: true
    excludes: ['**/test/**']
```

#### `checkstyle.xml`（项目根目录）

仅启用 Javadoc 规则：

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC ...>
<module name="Checker">
  <module name="TreeWalker">
    <module name="JavadocType">
      <property name="scope" value="public"/>
      <property name="excludeScope" value="protected"/>
    </module>
    <module name="JavadocMethod">
      <property name="scope" value="public"/>
      <property name="excludeScope" value="protected"/>
      <property name="allowMissingParamTags" value="false"/>
      <property name="allowMissingReturnTag" value="false"/>
    </module>
  </module>
</module>
```

#### `libs.versions.toml` 新增版本号

```toml
[versions]
detekt = "1.23.7"
checkstyle = "10.21.1"

[plugins]
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
checkstyle = { id = "com.github.spotbugs.snapshot", version = "6.0.26" }
```

具体插件选择以实际调研为准（Checkstyle Gradle 插件可用 `com.github.spotbugs.snapshot` 或手动配置）。

### Section E 流程变更

| 环节 | 旧（grep+sed） | 新（detekt + Checkstyle） |
|------|---------------|--------------------------|
| 检测引擎 | bash grep/sed 正则 | detekt CLI / Checkstyle CLI |
| 判定逻辑 | 声明前 5 行查 `/**` | 官方规则语义分析 |
| 输出格式 | 文本行 `[E1] 缺少 KDoc: file:line` | JSON 结构化输出 |
| 误报率 | 中等（注释在非标准位置会误报） | 低（规则经过社区验证） |
| 可复用性 | 仅 opencode skill | Gradle task → CI / IDE 可直接调用 |
| AI 补全流程 | 按文件批量，一次确认 | **不变**：先一次性补全，逐项展示 diff，逐项确认 |
| 排除规则 | grep -v（手动维护） | detekt/Checkstyle 配置声明式 |
| 前置依赖 | 无（纯 bash） | **新增**：未安装时引导安装 |

### 风险与边界

| 风险 | 缓解 |
|------|------|
| 引导安装写入失败（权限/格式冲突） | 安装前展示 diff 预览，用户确认后写入，写入后验证编译通过 |
| detekt/Checkstyle 版本与 AGP 版本兼容性 | 选择与 AGP 8.8.x 兼容的稳定版本 |
| Basic 模块是库模块，添加插件影响发布 | 插件仅用于检测，不打包进 AAR；自定义 task 不参与 `assembleRelease` |
| `.detekt.yml` / `checkstyle.xml` 规则可能与其他项目规范冲突 | 仅启用注释相关规则，其余关闭，不干扰现有规范 |
| Java 源文件可能很少或没有 | Checkstyle 找不到 Java 文件时 task 正常完成（空结果），不报错 |
| 项目使用者无意中触发检测 | 自定义 task 非自动绑定，仅 Section E 入口调用 |

### 成功标准

1. `android-code-review` Section E 的检测能力 ≥ 旧 grep 方案（相同文件集，命中数不减少）
2. 误报数 ≤ 旧方案（语义分析不应引入新误报）
3. 引导安装流程在未配置项目中一次完成（用户确认 → 自动写入 → 编译通过）
4. Gradle task 可独立运行：`./gradlew :Basic:checkComments -Pscope=full`
5. AI 补全 + 逐项确认流程无回退

## 变更范围

| 文件 | 操作 | 说明 |
|------|------|------|
| `gradle/libs.versions.toml` | 修改 | 新增 detekt/Checkstyle 版本号 |
| `Basic/build.gradle` | 修改 | 新增插件、依赖、自定义 task |
| `.detekt.yml` | 新增 | Kotlin 注释检查规则 |
| `checkstyle.xml` | 新增 | Java Javadoc 检查规则 |
| `.opencode/skill/android-code-review/SKILL.md` | 修改 | Section E 替换为基于 Gradle task 的流程 |
