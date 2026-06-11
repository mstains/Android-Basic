/**
 * [LocalDate] 上的扩展方法集合。
 *
 * 包含 5 个方法，按"判断 / 差值 / 提取"三组：
 * - 判断：[isToday] / [isYesterday] / [isTomorrow]
 * - 差值：[daysBetween]
 * - 提取：[dayOfWeek]
 *
 * 所有"判断"类方法使用系统时区（[LocalDate.now]），与原 DateExt.kt 保持一致；
 * 如需支持用户自定义时区，请改用 [java.time.ZonedDateTime] 相关 API，本类不引入新行为。
 *
 * @author Boqing.wu
 * @since 2026-06-11
 */
package com.letter.basic.extend

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 判断当前 [LocalDate] 是否为系统时区的"今天"。
 *
 * @return true 表示等于 [LocalDate.now]，false 表示不是
 */
fun LocalDate.isToday(): Boolean = this == LocalDate.now()

/**
 * 判断当前 [LocalDate] 是否为系统时区的"昨天"。
 *
 * @return true 表示等于 [LocalDate.now].minusDays(1)
 */
fun LocalDate.isYesterday(): Boolean = this == LocalDate.now().minusDays(1)

/**
 * 判断当前 [LocalDate] 是否为系统时区的"明天"。
 *
 * @return true 表示等于 [LocalDate.now].plusDays(1)
 */
fun LocalDate.isTomorrow(): Boolean = this == LocalDate.now().plusDays(1)

/**
 * 计算当前 [LocalDate] 与 [other] 之间相差的天数。
 *
 * 接收者或 [other] 为 null 时返回 null；底层 [ChronoUnit.DAYS.between]
 * 在历法异常或年份越界等极端场景下可能抛出 [java.time.DateTimeException]，
 * 一并捕获后返回 null，避免上层业务处理受检异常。
 *
 * @param other 目标日期，可为 null
 * @return 间隔天数，正值表示 [other] 在当前日期之后，负值反之；任一为 null 或异常时返回 null
 */
fun LocalDate?.daysBetween(other: LocalDate?): Long? {
    if (this == null || other == null) return null
    return try {
        ChronoUnit.DAYS.between(this, other)
    } catch (_: Exception) {
        null
    }
}

/**
 * 获取 [LocalDate] 对应的 [DayOfWeek] 枚举值。
 *
 * 内部读取 `LocalDate.dayOfWeek` 属性（[DayOfWeek] 枚举本身不会因时区或历法异常而抛错，
 * 此处仍包一层 try/catch 以保持与其他扩展方法一致的容错风格）。
 *
 * @return [DayOfWeek] 枚举（周一为 [DayOfWeek.MONDAY]，周日为 [DayOfWeek.SUNDAY]）；接收者为 null 时返回 null
 */
fun LocalDate?.dayOfWeek(): DayOfWeek? {
    if (this == null) return null
    return try {
        this.dayOfWeek
    } catch (_: Exception) {
        null
    }
}
