---
name: android-custom-view
description: Android 自定义 View / ViewGroup 开发规范与最佳实践。当用户需要创建自定义 View、自定义 ViewGroup、编写 onDraw/onMeasure/onLayout、处理触摸事件分发、自定义属性定义、或优化自定义 View 性能时自动触发。
license: Apache-2.0
metadata:
  author: Android-Basic
  last-updated: 2026-07-26
  keywords: [custom-view, viewgroup, ondraw, onmeasure, canvas, touch-event, performance]
---

# Android 自定义 View 开发规范

编写 Android 自定义 View 和 ViewGroup 时的开发指南，涵盖 measure-layout-draw 三大流程、绘制 API、触摸交互、性能优化和常见陷阱。

---

## 1. measure-layout-draw 三大流程

### 1.1 onMeasure — 尺寸测量

`onMeasure` 的核心职责是根据父容器传入的 MeasureSpec 确定 View 的最终尺寸。

MeasureSpec 有三种模式：

| 模式 | 含义 | 典型场景 |
|------|------|----------|
| EXACTLY | 精确值 | `match_parent`、固定 dp 值 |
| AT_MOST | 上限值 | `wrap_content` |
| UNSPECIFIED | 无限制 | ScrollView 内部测量 |

`wrap_content` 适配的关键在于处理 AT_MOST 模式。如果 View 没有提供默认尺寸，AT_MOST 下测得的结果会是 0，导致 View 不可见。使用 `resolveSize(desiredSize, measureSpec)` 可以安全处理三种模式，避免手动判断模式类型：

```kotlin
// 正确：提供默认尺寸并安全解析
override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val defWidth = 200.dpToPx()
    val defHeight = 200.dpToPx()
    setMeasuredDimension(
        resolveSize(defWidth, widthMeasureSpec),
        resolveSize(defHeight, heightMeasureSpec)
    )
}
```

注意事项：
- 计算完成后必须调用 `setMeasuredDimension()`，否则运行时会抛出 `IllegalStateException`。
- 如果重写了 `onMeasure` 又调用 `super.onMeasure()`，注意 `super` 可能已调用过 `setMeasuredDimension`，重复调用会导致问题。

### 1.2 onLayout — 子 View 布局

只有在继承 `ViewGroup` 时才需要重写 `onLayout`。

- 使用 `child.layout(left, top, right, bottom)` 摆放每个子 View。
- 计算坐标时必须扣除父容器的 `padding` 和子 View 的 `margin`（通过 `LayoutParams` 获取）。
- `left` / `top` 是相对于父容器左上角的坐标，`right` / `bottom` 需加上子 View 的测量宽高。

```kotlin
override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    val child = getChildAt(0) ?: return
    val childLeft = paddingLeft + (child.layoutParams as MarginLayoutParams).leftMargin
    val childTop = paddingTop + (child.layoutParams as MarginLayoutParams).topMargin
    child.layout(
        childLeft,
        childTop,
        childLeft + child.measuredWidth,
        childTop + child.measuredHeight
    )
}
```

### 1.3 onDraw — 绘制

`onDraw` 是自定义 View 最核心的方法，所有视觉内容在此绘制。

内边距处理：绘制坐标必须将 `paddingLeft`、`paddingTop`、`paddingRight`、`paddingBottom` 纳入计算。如果不处理，设置 `padding` 后视觉内容会被截断：

```kotlin
override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    // 绘制区域 = 控件尺寸 - padding
    val left = paddingLeft.toFloat()
    val top = paddingTop.toFloat()
    val right = (width - paddingRight).toFloat()
    val bottom = (height - paddingBottom).toFloat()
    // 后续绘制均在 left/top/right/bottom 范围内
}
```

---

## 2. 绘制核心 API

### 2.1 画布变换

绘制对称或旋转图案（如钟表刻度、圆环进度条）时，直接计算三角函数坐标非常繁琐。利用画布变换让坐标系跟随内容旋转/平移，可大幅简化计算：

```kotlin
canvas.save()
canvas.translate(centerX, centerY)  // 原点移到圆心
canvas.rotate(angle)                 // 旋转画布
canvas.drawLine(0f, 0f, 0f, radius, paint)  // 用简单坐标绘制
canvas.restore()
```

关键规则：`save()` 和 `restore()` 必须成对出现。`restore()` 将画布状态恢复到最近一次 `save()` 时。如果只 `save()` 不 `restore()`，后续所有绘制的坐标系都会错乱。

### 2.2 文字居中绘制

在 Canvas 上绘制居中文字时，不能直接把 Y 坐标设为居中线。文字以 baseline 为基准绘制，而 baseline 不在文字的正中间，因此直接居中会导致文字偏上。

正确做法是通过 `Paint.fontMetrics` 计算 baseline 偏移量：

```kotlin
val fm = paint.fontMetrics
// baseline 位于居中线下方偏移量
val baselineY = centerY - (fm.ascent + fm.descent) / 2
canvas.drawText(text, centerX, baselineY, paint)
```

- `ascent` 是负值（baseline 上方），`descent` 是正值（baseline 下方）。
- 另一种等价写法：`centerY + (fm.bottom - fm.top) / 2 - fm.bottom`，两种方法结果一致。

### 2.3 高级特效

- **圆角裁剪 / 遮罩**：使用 `PorterDuffXfermode(PorterDuff.Mode.SRC_IN)` 实现像素级混合。注意，部分 Xfermode 模式在硬件加速下不生效，需要在绘制前关闭硬件加速：`setLayerType(LAYER_TYPE_SOFTWARE, null)`。
- **颜色渐变**：使用 `LinearGradient`（线性渐变）或 `RadialGradient`（径向渐变）作为 `Paint.shader`，可绘制渐变进度条、渐变背景等。

---

## 3. 触摸交互与手势处理

### 3.1 事件消费

`onTouchEvent` 中，`ACTION_DOWN` 返回 `true` 意味着「我准备消费这个手势序列的后续所有事件（MOVE / UP）」。如果返回 `false`，后续事件将不再派发给此 View：

```kotlin
override fun onTouchEvent(event: MotionEvent): Boolean {
    return when (event.action) {
        MotionEvent.ACTION_DOWN -> true  // 声明要消费整个手势
        MotionEvent.ACTION_MOVE -> {
            // 处理滑动
            true
        }
        MotionEvent.ACTION_UP -> {
            performClick()  // 通知无障碍服务
            true
        }
        else -> super.onTouchEvent(event)
    }
}
```

### 3.2 滑动阈值

在 `ViewGroup` 中拦截手势时，不应在手指刚接触时就拦截——用户可能只是想点击。使用 `ViewConfiguration.get(context).scaledTouchSlop` 作为判定「滑动了」的最小距离阈值：

```kotlin
private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
private var lastY = 0f

override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
    return when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            lastY = event.y
            false
        }
        MotionEvent.ACTION_MOVE -> {
            if (abs(event.y - lastY) > touchSlop) true else false
        }
        else -> false
    }
}
```

### 3.3 无障碍合规

如果自定义 View 在 `onTouchEvent` 中拦截了 `ACTION_UP` 或在内部处理了点击逻辑，必须调用 `performClick()` 并重写此方法。这是因为 TalkBack 等无障碍服务通过 `performClick()` 触发 View 的点击行为——如果只处理 `ACTION_UP` 而不调用它，TalkBack 用户无法操作该 View。同时 Android Lint 会报 `ClickableViewAccessibility` 警告：

```kotlin
override fun performClick(): Boolean {
    super.performClick()
    // 在此处理点击业务逻辑
    return true
}
```

---

## 4. 自定义属性

通过 XML 声明式配置 View 属性比在代码中逐个 setter 调用更清晰。

### 4.1 属性定义（attrs.xml）

```xml
<declare-styleable name="CircleProgressView">
    <attr name="progressColor" format="color" />
    <attr name="progress" format="float" />
    <attr name="strokeWidth" format="dimension" />
</declare-styleable>
```

### 4.2 安全解析

`context.obtainStyledAttributes()` 返回的 `TypedArray` 持有底层 Native 资源引用。如果不手动回收，这些引用将无法被 GC 释放，造成 Native 层内存泄漏——这种泄漏在 profiler 中不易察觉。

因此必须在 `try-finally` 中使用 `TypedArray`，并在 `finally` 中调用 `recycle()`：

```kotlin
init {
    context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView).apply {
        try {
            progressColor = getColor(
                R.styleable.CircleProgressView_progressColor, Color.BLUE
            )
            progress = getFloat(
                R.styleable.CircleProgressView_progress, 0f
            )
            strokeWidth = getDimension(
                R.styleable.CircleProgressView_strokeWidth, 4.dpToPx().toFloat()
            )
        } finally {
            recycle()
        }
    }
}
```

---

## 5. 性能优化

### 5.1 禁止在 onDraw 中创建对象

`onDraw` 的调用频率很高（理想情况下每 16ms 一次）。在 `onDraw` 中 `new` 任何对象——`Paint()`、`Path()`、`RectF()`、字符串拼接、Kotlin 闭包——都会在短时间内制造大量临时对象，触发频繁 GC，导致卡顿和掉帧。

将所有可复用的对象声明为类成员变量，在构造函数或 `onSizeChanged()` 中初始化：

```kotlin
// 类级别声明，构造时初始化
private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
private val rect = RectF()
private val path = Path()

// 尺寸相关的对象在 onSizeChanged 中初始化
override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    rect.set(
        paddingLeft.toFloat(), paddingTop.toFloat(),
        (w - paddingRight).toFloat(), (h - paddingBottom).toFloat()
    )
}
```

### 5.2 精准触发重绘

选择正确的刷新方法可以避免不必要的重新测量和布局：

| 变更类型 | 调用方法 | 原因 |
|---------|---------|------|
| 仅外观变化（颜色、文本、透明度） | `invalidate()` | 只触发 `onDraw`，跳过测量和布局 |
| 尺寸、位置、可见性变化 | `requestLayout()` | 需要重新执行 measure -> layout -> draw 全流程 |

调用 `requestLayout()` 后系统会自动触发 `onDraw`，不需要额外调用 `invalidate()`。

---

## 6. 标准代码骨架

以下骨架涵盖了本文档中的核心规范。新建自定义 View 时可以以此为基础扩展，按注释中的编号对应前文的 6 个关注点：

```kotlin
class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 1. 全局复用对象（对应 5.1 节 - 禁止在 onDraw 中 new）
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4.dpToPx().toFloat()
    }
    private val bounds = RectF()

    private var progressColor: Int = Color.BLUE
    private var progress: Float = 0f

    init {
        // 2. 安全解析自定义属性（对应第 4 节）
        context.obtainStyledAttributes(attrs, R.styleable.CustomView).apply {
            try {
                progressColor = getColor(
                    R.styleable.CustomView_progressColor, progressColor
                )
                progress = getFloat(
                    R.styleable.CustomView_progress, progress
                )
            } finally {
                recycle()
            }
        }
    }

    // 3. 适配 wrap_content（对应 1.1 节）
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defSize = 200.dpToPx()
        setMeasuredDimension(
            resolveSize(defSize, widthMeasureSpec),
            resolveSize(defSize, heightMeasureSpec)
        )
    }

    // 4. 尺寸变化时预计算边界（对应 1.3 节 + 5.1 节）
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bounds.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingBottom).toFloat()
        )
    }

    // 5. 绘制逻辑（对应 1.3 节）
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bounds.isEmpty) return
        paint.color = progressColor
        // 具体绘制代码...
    }

    // 6. 触摸交互 & 无障碍合规（对应 3.1 节 + 3.3 节）
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> true
            MotionEvent.ACTION_UP -> {
                performClick()
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        // 在此处理点击业务逻辑
        return true
    }
}
```

---

## 7. 常见陷阱速查

| 陷阱 | 后果 | 对应章节 |
|------|------|----------|
| onMeasure 中忘记调用 `setMeasuredDimension()` | 运行时 `IllegalStateException` | 1.1 |
| wrap_content 时未给出默认尺寸 | View 尺寸为 0，不可见 | 1.1 |
| onDraw 中创建 `Paint()` / `Path()` 等对象 | 频繁 GC 导致掉帧 | 5.1 |
| 绘制坐标未考虑 padding | 设置 padding 后内容被截断 | 1.3 |
| TypedArray 未在 finally 中 recycle() | Native 层内存泄漏 | 4.2 |
| 文字居中直接用 centerY 作为 baseline | 文字偏上，视觉不居中 | 2.2 |
| canvas.save() 后忘记 restore() | 后续绘制坐标系错乱 | 2.1 |
| Xfermode 未关闭硬件加速 | 混合模式不生效 | 2.3 |
| 处理了 ACTION_UP 但未调用 performClick() | Lint 警告，TalkBack 无法触发点击 | 3.3 |
| 手指刚触摸就拦截手势（未用 touchSlop） | 点击被误判为滑动 | 3.2 |
| 外观变化时调用 requestLayout() 而非 invalidate() | 触发不必要的 measure/layout，浪费性能 | 5.2 |
