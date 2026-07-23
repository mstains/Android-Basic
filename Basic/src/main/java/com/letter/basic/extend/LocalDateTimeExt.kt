/**
 * [LocalDateTime] 上的扩展方法集合。
 *
 * 包含 3 个方法：
 * - [daysBetween]：差天数
 * - [date]：提取日期部分
 * - [time]：提取时间部分
 *
 * [date] / [time] 是非 null 重载（[LocalDateTime] 本身非空，调用前无需 null 检查）；
 * [daysBetween] 是可空重载，与 [com.letter.basic.extend.LocalDateExt.daysBetween] 保持一致容错风格。
 *
 * @author Boqing.wu
 * @since 2026-06-11
 */
package com.letter.basic.extend

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * 提取 [LocalDateTime] 的日期部分。
 *
 * @return 等价的 [LocalDate]
 */
fun LocalDateTime.date(): LocalDate = this.toLocalDate()

/**
 * 提取 [LocalDateTime] 的时间部分。
 *
 * @return 等价的 [LocalTime]
 */
fun LocalDateTime.time(): LocalTime = this.toLocalTime()

/**
 * 计算两个 [LocalDateTime] 之间相差的天数（按日期部分比较，忽略时分秒）。
 *
 * 先各自 [LocalDateTime.toLocalDate] 再调 [ChronoUnit.DAYS.between]，因此"今天 23:59:59"与"明天 00:00:01"
 * 按整日差 1 天计算，而不是 0 天或负数。
 *
 * @param other 目标 [LocalDateTime]
 * @return 间隔天数；任一为 null 或异常时返回 null
 */
fun LocalDateTime?.daysBetween(other: LocalDateTime?): Long? {
    if (this == null || other == null) return null
    return try {
        ChronoUnit.DAYS.between(this.toLocalDate(), other.toLocalDate())
    } catch (_: Exception) {
        null
    }
}
