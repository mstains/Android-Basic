/**
 * Locale 感知的时间格式化工具。
 *
 * 业务层只传"时间"和"风格"，不再传入中式 / 欧式 pattern 字符串。
 * 同一 API 在不同 App 语言下输出不同的本地化字符串：
 * - App = 简体中文 + [short] → `2024/6/11`
 * - App = 英文(美国) + [short] → `6/11/24`
 * - App = 英文(英国) + [short] → `11/06/2024`
 *
 * 与现有 [com.letter.basic.manager.DateManager] / [com.letter.basic.extend.DateExt] 的关系：
 * - 本工具是"新代码默认选择"，按 [AppLocale.current] 自动选 locale。
 * - 老 API 保留不破坏，老调用方零迁移成本。
 * - 强 pattern 兜底走 [format]，仅用于文件名、订单号等"必须固定格式"的场景。
 *
 * @author Boqing.wu
 * @since 2026-06-11
 */
package com.letter.basic.manager

import com.letter.basic.i18n.AppLocale
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Locale 感知的时间格式化工具。
 *
 * 所有方法默认从 [AppLocale.current] 读取当前 App 语言环境。
 * 调用方可通过最后一个 `locale` 参数显式覆盖，测试场景下常用。
 */
object AppDateFormatter {

    // (风格, locale) → DateTimeFormatter 的缓存
    // 同一组合只构造一次，避免高频调用下重复创建 formatter 带来的开销
    private val cache = ConcurrentHashMap<Key, DateTimeFormatter>()

    /**
     * 格式化 [LocalDate] 为短日期。
     *
     * 基于 [FormatStyle.SHORT]，典型输出：
     * - zh_CN → `2024/6/11`
     * - en_US → `6/11/24`
     * - en_GB → `11/06/2024`
     *
     * @param date 待格式化的日期
     * @param locale 目标 locale，默认 [AppLocale.current]
     * @return 短日期字符串
     */
    fun short(date: LocalDate, locale: Locale = AppLocale.current): String =
        date.format(formatter(FormatStyle.SHORT, locale))

    /**
     * 格式化 [LocalDate] 为中等长度日期。
     *
     * 基于 [FormatStyle.MEDIUM]，典型输出：
     * - zh_CN → `2024年6月11日`
     * - en_US → `Jun 11, 2024`
     * - en_GB → `11 Jun 2024`
     *
     * @param date 待格式化的日期
     * @param locale 目标 locale，默认 [AppLocale.current]
     * @return 中等长度日期字符串
     */
    fun medium(date: LocalDate, locale: Locale = AppLocale.current): String =
        date.format(formatter(FormatStyle.MEDIUM, locale))

    /**
     * 格式化 [LocalDate] 为长日期（含完整星期名）。
     *
     * 基于 [FormatStyle.LONG]，典型输出：
     * - zh_CN → `2024年6月11日`
     * - en_US → `June 11, 2024`
     *
     * @param date 待格式化的日期
     * @param locale 目标 locale，默认 [AppLocale.current]
     * @return 长日期字符串
     */
    fun long(date: LocalDate, locale: Locale = AppLocale.current): String =
        date.format(formatter(FormatStyle.LONG, locale))

    /**
     * 格式化 [LocalDate] 为完整日期（地区差异较大，含纪年等信息）。
     *
     * 基于 [FormatStyle.FULL]，典型输出：
     * - en_US → `Tuesday, June 11, 2024`
     *
     * @param date 待格式化的日期
     * @param locale 目标 locale，默认 [AppLocale.current]
     * @return 完整日期字符串
     */
    fun full(date: LocalDate, locale: Locale = AppLocale.current): String =
        date.format(formatter(FormatStyle.FULL, locale))

    /**
     * 格式化 [LocalDateTime] 为短日期 + 短时间。
     *
     * 日期走 [FormatStyle.SHORT]，时间走 [FormatStyle.SHORT]，典型输出：
     * - zh_CN → `2024/6/11 14:30`
     * - en_US → `6/11/24 2:30 PM`
     *
     * @param dateTime 待格式化的日期时间
     * @param locale 目标 locale，默认 [AppLocale.current]
     * @return 短日期时间字符串
     */
    fun shortDateTime(dateTime: LocalDateTime, locale: Locale = AppLocale.current): String =
        dateTime.format(dateTimeFormatter(FormatStyle.SHORT, FormatStyle.SHORT, locale))

    /**
     * 格式化 [LocalDateTime] 为中等日期 + 短时间。
     *
     * 日期走 [FormatStyle.MEDIUM]，时间走 [FormatStyle.SHORT]，典型输出：
     * - zh_CN → `2024年6月11日 14:30`
     * - en_US → `Jun 11, 2024 2:30 PM`
     *
     * @param dateTime 待格式化的日期时间
     * @param locale 目标 locale，默认 [AppLocale.current]
     * @return 中等日期时间字符串
     */
    fun mediumDateTime(dateTime: LocalDateTime, locale: Locale = AppLocale.current): String =
        dateTime.format(dateTimeFormatter(FormatStyle.MEDIUM, FormatStyle.SHORT, locale))

    /**
     * 强 pattern 兜底。
     *
     * 仅用于"产品强约束某个固定格式"的场景，如文件名、订单号、URL 参数。
     * 大多数 UI 展示请优先走 [short] / [medium] / [long] / [full]。
     *
     * 失败时回退为 `date.toString()`，避免上层处理异常。
     *
     * @param date 待格式化的日期
     * @param pattern 自定义 pattern，参考 [DateTimeFormatter]
     * @param locale 目标 locale，默认 [AppLocale.current]
     * @return 格式化结果；pattern 非法时回退为 `date.toString()`
     */
    fun format(date: LocalDate, pattern: String, locale: Locale = AppLocale.current): String =
        try {
            date.format(DateTimeFormatter.ofPattern(pattern, locale))
        } catch (_: Exception) {
            // pattern 非法时降级为默认 toString，不向调用方抛异常
            date.toString()
        }

    /**
     * 获取或构造 (日期风格, locale) 对应的 [DateTimeFormatter]。
     *
     * 缓存命中直接返回，未命中则构造并写入 [cache]。
     * [ConcurrentHashMap.getOrPut] 在并发下安全，重复构造只会发生在线程竞争瞬间。
     */
    private fun formatter(style: FormatStyle, locale: Locale): DateTimeFormatter =
        cache.getOrPut(Key(style, null, locale)) {
            DateTimeFormatter.ofLocalizedDate(style).withLocale(locale)
        }

    /**
     * 获取或构造 (日期风格, 时间风格, locale) 对应的 [DateTimeFormatter]。
     */
    private fun dateTimeFormatter(
        dateStyle: FormatStyle, timeStyle: FormatStyle, locale: Locale
    ): DateTimeFormatter = cache.getOrPut(Key(dateStyle, timeStyle, locale)) {
        DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle).withLocale(locale)
    }

    /**
     * 缓存 key。
     *
     * 纯日期格式化时 [timeStyle] 为 null，避免与"日期时间"组合共用 key 引发误命中。
     */
    private data class Key(
        val dateStyle: FormatStyle,
        val timeStyle: FormatStyle?,
        val locale: Locale
    )
}
