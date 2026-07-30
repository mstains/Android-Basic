---
name: android-drawable-xml
description: Android Drawable XML resources for buttons, backgrounds, shapes, icons, and visual effects. Use this skill whenever the user needs to create or modify any XML file under res/drawable/, or when they ask about making UI elements visually respond to touch (pressed/disabled states, ripple effects), adding shadows or rounded corners to views, creating gradient backgrounds, using vector icons, configuring app launcher icons, or handling visual compatibility across Android versions.
license: Apache-2.0
metadata:
  author: Android-Basic
  last-updated: 2026-07-26
  keywords: [drawable, xml, shape, selector, layer-list, ripple, vector, adaptive-icon, inset, transition]
---

# Android Drawable XML 开发指南

`res/drawable` 下的 XML 资源是构建高效、动态、自适应 UI 的基石。熟练掌握各种 Drawable 标签，不仅能减小 APK 体积，还能显著提升界面的交互体验。由于 Android 系统版本碎片化严重，版本适配是日常编写 Drawable XML 时必须考虑的核心环节。

---

## 1. 标签速查与 API 门槛

下表整理了常用 Drawable XML 标签的系统支持门槛。在低版本系统上使用高 API 标签会直接抛出 `InflateException` 导致崩溃：

| 标签 | 核心作用 | 最低 API | 低版本后果 |
|:---|:---|:---|:---|
| `<shape>` | 绘制基础几何图形（矩形、椭圆、线、环） | API 1 | 无兼容问题 |
| `<selector>` | 根据控件状态（按下、禁用等）切换图形 | API 1 | 无兼容问题 |
| `<layer-list>` | 将多个 Drawable 按顺序堆叠 | API 1 | 无兼容问题 |
| `<level-list>` | 根据 level 值（0-10000）切换图片 | API 1 | 无兼容问题 |
| `<clip>` | 对内部图形按比例裁剪（进度条效果） | API 1 | 无兼容问题 |
| `<scale>` | 对内部图形按比例缩放 | API 1 | 无兼容问题 |
| `<rotate>` | 对内部图形按角度旋转 | API 1 | 无兼容问题 |
| `<inset>` | 为内部 Drawable 添加边距（内缩效果） | API 1 | 无兼容问题 |
| `<transition>` | 两张 Drawable 之间的渐变切换 | API 1 | 无兼容问题 |
| `<vector>` | 矢量图形（Android 专用的 SVG 格式） | API 21 | API < 21 直接崩溃 |
| `<ripple>` | Material Design 点击水波纹效果 | API 21 | API < 21 直接崩溃 |
| `<animated-vector>` | 动态矢量图（让矢量路径动起来） | API 21 | API < 21 直接崩溃 |
| `<animated-selector>` | 带状态切换过渡动画的选择器 | API 21 | API < 21 直接崩溃 |
| `<adaptive-icon>` | 桌面自适应应用图标 | API 26 | API < 26 无法识别 |

---

## 2. 高频实战用例

### 2.1 `<shape>` + `<selector>` — 圆角点击变色按钮（全版本通用）

**文件名：`res/drawable/bg_button_clickable.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 按下状态：深蓝色背景 + 8dp 圆角 -->
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="#1A5276" />
            <corners android:radius="8dp" />
        </shape>
    </item>

    <!-- 禁用状态：灰色背景 + 8dp 圆角 -->
    <item android:state_enabled="false">
        <shape android:shape="rectangle">
            <solid android:color="#BDC3C7" />
            <corners android:radius="8dp" />
        </shape>
    </item>

    <!-- 默认状态：天蓝色背景 + 8dp 圆角（必须放在最底部） -->
    <item>
        <shape android:shape="rectangle">
            <solid android:color="#3498DB" />
            <corners android:radius="8dp" />
        </shape>
    </item>
</selector>
```

关键点：`<selector>` 的匹配是从上到下的，一旦匹配到某个 `<item>` 就不再继续。因此没有状态约束的默认项必须放在最底部，否则会拦截所有其他状态。

### 2.2 `<layer-list>` — 带单边阴影的卡片背景（全版本通用）

**文件名：`res/drawable/bg_card_shadow.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 底层：阴影层，整体向右下偏移 3dp -->
    <item android:left="3dp" android:top="3dp">
        <shape android:shape="rectangle">
            <solid android:color="#10000000" />
            <corners android:radius="6dp" />
        </shape>
    </item>

    <!-- 顶层：卡片主体，底部和右侧留 3dp 让阴影露出 -->
    <item android:right="3dp" android:bottom="3dp">
        <shape android:shape="rectangle">
            <solid android:color="#FFFFFF" />
            <stroke android:width="1dp" android:color="#E5E7E9" />
            <corners android:radius="6dp" />
        </shape>
    </item>
</layer-list>
```

原理：底层 `<item>` 向左上偏移（`android:left` / `android:top` 相当于在该方向留白），顶层 `<item>` 向右下偏移，从而在视觉上模拟光源从左上角照射的投影效果。

### 2.3 `<shape>` — 渐变背景（线性 / 径向 / 扫描渐变）

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <!-- angle: 渐变角度，0=左到右，90=下到上，180=右到左，270=上到下 -->
    <gradient
        android:angle="270"
        android:startColor="#3498DB"
        android:endColor="#1A5276"
        android:type="linear" />
    <corners android:radius="8dp" />
</shape>
```

`android:type` 可选值：
- `linear`：线性渐变（默认），配合 `android:angle` 控制方向
- `radial`：径向渐变，需配合 `android:gradientRadius` 使用
- `sweep`：扫描渐变（API 21+）

### 2.4 `<inset>` + `<shape>` — 带内边距的虚线边框

```xml
<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:inset="4dp">
    <shape android:shape="rectangle">
        <solid android:color="#FFFFFF" />
        <stroke
            android:width="2dp"
            android:color="#3498DB"
            android:dashWidth="6dp"
            android:dashGap="3dp" />
        <corners android:radius="8dp" />
    </shape>
</inset>
```

原理：`<inset>` 将内部 Drawable 等比例缩小指定边距。配合 `<shape>` 的 `stroke` 虚线属性（`dashWidth` + `dashGap`），可轻松实现带间距的虚线边框效果。常用于选中状态的卡片、图片占位框等场景。

`<inset>` 也支持四个方向不同的边距：

```xml
<inset
    android:insetLeft="8dp"
    android:insetTop="4dp"
    android:insetRight="8dp"
    android:insetBottom="4dp">
    <!-- 内部 drawable -->
</inset>
```

---

## 3. 版本适配方案

针对 API 21+ 引入的高阶特效标签（`<ripple>`、`<vector>`、`<animated-vector>`、`<animated-selector>`），行业常用的三种适配策略：

### 方案 A：资源目录隔离降级（最推荐，适用 `<ripple>`）

原理：Android 的资源加载机制会优先匹配当前 API 级别的限定符目录。在 `res/drawable-v21/` 放置高版本特效，在 `res/drawable/` 放置低版本降级方案，同名文件引用 `@drawable/xxx` 即可自动分发。

1. **在 `res/drawable-v21/` 中放入水波纹版本**（API 21+ 系统读取）：

   **文件名：`res/drawable-v21/bg_interactive_btn.xml`**
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <ripple xmlns:android="http://schemas.android.com/apk/res/android"
       android:color="#40FFFFFF">
       <item>
           <shape>
               <solid android:color="#3498DB" />
               <corners android:radius="4dp" />
           </shape>
       </item>
   </ripple>
   ```

2. **在 `res/drawable/` 中放入普通 selector 降级版**（低版本系统读取）：

   **文件名：`res/drawable/bg_interactive_btn.xml`**
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <selector xmlns:android="http://schemas.android.com/apk/res/android">
       <item android:state_pressed="true">
           <shape>
               <solid android:color="#1A5276" />
               <corners android:radius="4dp" />
           </shape>
       </item>
       <item>
           <shape>
               <solid android:color="#3498DB" />
               <corners android:radius="4dp" />
           </shape>
       </item>
   </selector>
   ```

   布局中统一引用 `@drawable/bg_interactive_btn`，系统自动按 API 级别分发。

### 方案 B：AppCompat 矢量图向后兼容（适用 `<vector>`）

如需在 Android 5.0 以下系统使用 `<vector>` 标签，必须启用 Jetpack AppCompat 的兼容模式：

1. **在 `build.gradle` 中开启兼容支持：**
   ```groovy
   android {
       defaultConfig {
           vectorDrawables.useSupportLibrary = true
       }
   }
   ```

2. **布局中引用矢量图时使用 `app:srcCompat`，而非 `android:src`：**
   ```xml
   <!-- android:src 由 framework 直接解析，不支持向下兼容；
        app:srcCompat 由 AppCompat 库代理解析，低版本自动将 vector 转为 PNG -->
   <ImageView
       android:layout_width="wrap_content"
       android:layout_height="wrap_content"
       app:srcCompat="@drawable/ic_my_vector_icon" />
   ```

3. **Button / TextView 的 Compound Drawables 同理：**
   使用 `app:drawableTopCompat`、`app:drawableStartCompat` 等 AppCompat 属性替代原生属性。

### 方案 C：系统预设属性（适用水波纹）

如果不需要自定义水波纹颜色或形状，只想给列表项或按钮增加标准的水波纹点击反馈，可以直接引用系统内置属性——系统内部已处理好版本兼容：

```xml
<!-- 有界水波纹（5.0+ 自动水波纹，5.0 以下降级为灰色高亮） -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="?android:attr/selectableItemBackground"
    android:clickable="true"
    android:focusable="true">
</LinearLayout>

<!-- 无视边界的水波纹（常用于圆形小图标按钮） -->
<!-- android:background="?android:attr/selectableItemBackgroundBorderless" -->
```

---

## 4. 常见陷阱速查

| 陷阱 | 后果 | 对应章节 |
|------|------|----------|
| `<selector>` 默认项不在最末位 | 默认项拦截所有状态，按下/禁用等状态不生效 | 2.1 |
| vector 在低版本用 `android:src` 引用 | InflateException 崩溃 | 3-B |
| `<ripple>` 未放在 `drawable-v21/` 目录 | 低版本 InflateException 崩溃 | 3-A |
| XML 命名空间写为 `http://android.com` | 属性解析失败，无明显报错但效果不生效 | 2.1 |
| `<gradient>` 放在 `<solid>` 之后 | 渐变被纯色覆盖，无法显示 | 2.3 |
| 普通 PNG 直接拉伸做背景 | 边缘糊化、圆角畸变 | 5 |
| 点九图忘记加上 `.9.png` 后缀 | 系统不识别点九拉伸指示线 | 5 |
| `<inset>` 内部 drawable 未设置尺寸 | 内部 drawable 可能塌陷为 0 | 2.4 |
| `android:angle` 值超出 0-360 或使用非 45 倍数的值 | 渐变方向与预期不符 | 2.3 |

---

## 5. 最佳实践总结

1. **选择器优先级**：`<selector>` 中把约束条件多的 `<item>` 放在上面，没有约束的默认项放在最底。这是按匹配顺序决定的硬规则。

2. **用 shape 替代位图**：`<shape>` 绘制的图形几乎不占用 APK 空间，在任何分辨率下都不会失真模糊。优先用 shape 替代纯色/圆角 PNG。

3. **点九图（.9.png）**：如果必须使用位图背景且需要拉伸，将其制作成 `.9.png` 格式并直接引用（如 `android:background="@drawable/bg_stretch"`），系统会自动按点九线拉伸，避免边缘糊化。

4. **硬件加速兼容**：部分 `<shape>` 属性组合（如大量圆角 + 虚线 stroke）在硬件加速下可能有渲染异常，极端情况下可尝试在 View 上关闭硬件加速：`setLayerType(LAYER_TYPE_SOFTWARE, null)`。
