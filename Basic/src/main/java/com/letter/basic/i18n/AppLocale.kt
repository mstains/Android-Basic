/**
 * App 级别语言环境持有者。
 *
 * 作为 App 内"语言"唯一真相源，所有需要 locale 的下游（时间格式化、字符串资源、
 * 第三方库）统一从此处读取，避免各处直接调 [Locale.getDefault] 拿到系统 Locale
 * 而忽略用户在 App 内手动切换的结果。
 *
 * 设计要点：
 * - 使用 [AtomicReference] 持有当前 locale，多线程读取无需加锁。
 * - `set` 与 `restore` 均不触发 Activity 重建；recreate 的责任完全落在扩展层
 *   `BaseCommonMultiStateActivityExt`，避免双重触发。
 * - 持久化通过 [LocaleStorage] 接口外置，由宿主 App 实现并通过 [init] 注入，
 *   本类不再内置任何具体存储方案（SharedPreferences / DataStore 等均由宿主选择）。
 *
 * @author Boqing.wu
 * @since 2026-06-11
 */
package com.letter.basic.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

/**
 * App 级别语言环境持有者。
 *
 * 由宿主 App 在 [android.app.Application.onCreate] 第一时间调用 [init] 注入存储，
 * 并调用 [restore] 回放上次选择；在用户切换语言时调用 [set] 注入新 locale。
 *
 * 未调用 [init] / [restore] 时 [current] 始终等于 [Locale.getDefault]，行为与改造前
 * 完全一致，保证"语言切换只是这个依赖的一个功能，不影响其他功能正常使用"。
 */
object AppLocale {

    /**
     * 宿主注入的语言存储实现。
     *
     * 为 null 表示尚未注入，此时 [set] 不持久化、[restore] 不生效（均静默跳过，不抛异常）。
     * [Volatile] 保证跨线程可见性，与 [ref] 的线程安全策略一致。
     */
    @Volatile
    private var storage: LocaleStorage? = null

    /**
     * 当前 App 生效的 locale。
     *
     * 启动初值为 [Locale.getDefault]，调用 [restore] 或 [set] 后被覆盖。
     * 多线程安全：内部 [AtomicReference] 保证可见性。
     */
    val current: Locale
        get() = ref.get()

    // AtomicReference 初值 = 系统 Locale，保证 restore 未调用时行为与之前一致
    // @Suppress("ConstantLocale")：Linter 警告"在字段初始化时赋值 Locale.getDefault()，
    // 用户在运行时改系统语言时该值不会更新"。这是 AppLocale 的设计意图——本类负责
    // "App 内手动切换"的语言；跟随系统语言走 set(null) 路径，该路径会主动重读 Locale.getDefault()。
    @Suppress("ConstantLocale")
    private val ref = AtomicReference(Locale.getDefault())

    /**
     * 注入语言设置的持久化存储实现。
     *
     * 必须在 [restore] / [set] 之前调用，通常在 [android.app.Application.onCreate] 中完成。
     * 未注入时 [set] 不持久化（仅本次进程内生效）、[restore] 不生效，均不抛异常。
     *
     * @param storage 宿主实现的 [LocaleStorage]，存储方案（sp / DataStore 等）由宿主自选
     */
    fun init(storage: LocaleStorage) {
        this.storage = storage
    }

    /**
     * 切换 App 语言环境。
     *
     * 流程：
     * 1. 写入持久化（委托 [LocaleStorage.writeTag]，value = `locale.toLanguageTag()`）。
     * 2. 通知 [AppCompatDelegate.setApplicationLocales]，由 AndroidX 触发系统级
     *    locale 切换并按需重建 Activity。
     * 3. 立即更新 [current] 引用，确保 recreate 期间的代码也能拿到新 locale。
     *
     * 注意：本方法不主动调用 [android.app.Activity.recreate]，避免与扩展层
     * `switchLanguage` 内的 recreate 重复触发。
     *
     * @param locale 新的语言环境，传 `null` 表示回退到 [Locale.getDefault]
     */
    fun set(locale: Locale?) {
        val tag = locale?.toLanguageTag().orEmpty()
        // 持久化委托宿主注入的 LocaleStorage；未注入时静默跳过，仅本次进程内生效
        storage?.writeTag(tag)
        // 通知 AppCompatDelegate：API 33+ 写入系统 LocaleManager，< 33 写入 AndroidX 内部存储
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        // 立即更新内存引用，让 recreate 期间依赖 AppLocale.current 的代码拿到新值
        ref.set(locale ?: Locale.getDefault())
    }

    /**
     * App 启动时回放上次保存的 locale。
     *
     * 必须在 [android.app.Application.onCreate] 第一时间调用，先于任何 Activity 创建，
     * 否则首帧格式化结果会短暂停留在系统 Locale 上。
     *
     * 未注入存储、未调用本方法或上次未设置时，[current] 保持 [Locale.getDefault]，
     * 与本次改造前行为完全一致——本方法不构成"必须调用"的硬约束。
     */
    fun restore() {
        val tag = storage?.readTag().orEmpty()
        // 空字符串视为"未设置"（含未注入存储），保持 ref 当前的初值（Locale.getDefault）
        if (tag.isEmpty()) return
        ref.set(Locale.forLanguageTag(tag))
    }
}
