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
 * - 持久化用 [SharedPreferences] 而非 DataStore，保持 Basic 库零外部依赖。
 *
 * @author Boqing.wu
 * @since 2026-06-11
 */
package com.letter.basic.i18n

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

/**
 * App 级别语言环境持有者。
 *
 * 由宿主 App 在用户切换语言时调用 [set] 注入新 locale，
 * 在 [android.app.Application.onCreate] 第一时间调用 [restore] 回放上次选择。
 *
 * 未调用 [restore] 时 [current] 始终等于 [Locale.getDefault]，行为与改造前完全一致，
 * 保证"语言切换只是这个依赖的一个功能，不影响其他功能正常使用"。
 */
object AppLocale {

    /**
     * SharedPreferences 文件名。
     *
     * 固定值便于跨进程 / 跨模块复用，避免与宿主 App 自身的 prefs 冲突。
     */
    private const val PREFS_NAME = "app_locale"

    /**
     * 存储 locale 的 key。
     *
     * value 为 [Locale.toLanguageTag] 字符串（如 "zh-CN"、"en"），空字符串表示"未设置"。
     */
    private const val KEY_LOCALE_TAG = "app_locale_tag"

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
     * 切换 App 语言环境。
     *
     * 流程：
     * 1. 写入持久化（key = [KEY_LOCALE_TAG]，value = `locale.toLanguageTag()`）。
     * 2. 通知 [AppCompatDelegate.setApplicationLocales]，由 AndroidX 触发系统级
     *    locale 切换并按需重建 Activity。
     * 3. 立即更新 [current] 引用，确保 recreate 期间的代码也能拿到新 locale。
     *
     * 注意：本方法不主动调用 [android.app.Activity.recreate]，避免与扩展层
     * `switchLanguage` 内的 recreate 重复触发。
     *
     * @param locale 新的语言环境，传 `null` 表示回退到 [Locale.getDefault]
     * @param context 用于获取 [SharedPreferences] 与调用 [AppCompatDelegate] 的 Context
     */
    fun set(locale: Locale?, context: Context) {
        val appContext = context.applicationContext
        val tag = locale?.toLanguageTag().orEmpty()
        // 持久化：apply 异步写入，不阻塞调用方线程
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOCALE_TAG, tag)
            .apply()
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
     * 未调用本方法或上次未设置时，[current] 保持 [Locale.getDefault]，与本次改造前
     * 行为完全一致——本方法不构成"必须调用"的硬约束。
     *
     * @param context 用于获取 [SharedPreferences] 的 Context
     */
    fun restore(context: Context) {
        val appContext = context.applicationContext
        val tag = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOCALE_TAG, null)
            .orEmpty()
        // 空字符串视为"未设置"，保持 ref 当前的初值（Locale.getDefault）
        if (tag.isEmpty()) return
        ref.set(Locale.forLanguageTag(tag))
    }
}
