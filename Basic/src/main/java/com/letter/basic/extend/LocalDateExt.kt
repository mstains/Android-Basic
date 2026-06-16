/**
 * [LocalDate] 上的扩展方法集合。
 *
 * 包含 9 个方法，按"判断 / 跨时区判断 / 差值 / 提取 / 运算"五组：
 * - 判断（系统时区）：[isToday] / [isYesterday] / [isTomorrow]
 * - 判断（指定时区）：[isTodayIn] / [isYesterdayIn] / [isTomorrowIn]
 * - 差值：[daysBetween]
 * - 提取：[dayOfWeek]
 * - 运算：[plusDaysOrNull]
 *
 * 所有"判断"类方法使用系统时区（[LocalDate.now]），与原 DateExt.kt 保持一致；
 * "判断（系统时区）"组固定使用 [ZoneId.systemDefault]；"判断（指定时区）"组
 * 通过 [isTodayIn] / [isYesterdayIn] / [isTomorrowIn] 显式传入 [ZoneId]，
 * 满足国际行程等多时区场景的需求。
 *
 * @author Boqing.wu
 * @since 2026-06-11
 */
package com.letter.basic.extend

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
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

/**
 * 在当前 [LocalDate] 上加 [days] 天，返回平移后的日期。
 *
 * 接收者为 null 或 [days] 为 null 时返回 null。
 * 底层 [LocalDate.plusDays] 在跨年、闰年等临界场景下不会抛错，
 * 但在年份超过 [LocalDate.MAX] / [LocalDate.MIN] 范围时可能抛出
 * [java.time.DateTimeException]，此处捕获后返回 null 以保持
 * 与 [daysBetween] / [dayOfWeek] 一致的容错风格。
 *
 * @param days 要加上的天数，可为负数；null 时返回 null
 * @return 平移后的 [LocalDate]；接收者或参数为 null、计算越界时返回 null
 */
fun LocalDate?.plusDaysOrNull(days: Int?): LocalDate? {
    if (this == null || days == null) return null
    return try {
        this.plusDays(days.toLong())
    } catch (_: Exception) {
        null
    }
}

/**
 * 判断当前 [LocalDate] 是否为 [zoneId] 时区的"今天"。
 *
 * 与 [isToday] 的区别：后者固定按系统时区（[ZoneId.systemDefault]）判定，
 * 本方法允许调用方指定任意 [ZoneId]，适用于国际行程等多时区场景
 * （例如：杭州是 6/16 时，判断檀香山当地的 6/15 是否是"檀香山今天"）。
 *
 * @param zoneId 目标时区
 * @return true 表示等于 [LocalDate.now] 在 [zoneId] 下的日期，false 表示不是
 */
fun LocalDate.isTodayIn(zoneId: ZoneId): Boolean = this == LocalDate.now(zoneId)

/**
 * 判断当前 [LocalDate] 是否为 [zoneId] 时区的"昨天"。
 *
 * @param zoneId 目标时区
 * @return true 表示等于 [LocalDate.now].minusDays(1) 在 [zoneId] 下的日期
 */
fun LocalDate.isYesterdayIn(zoneId: ZoneId): Boolean = this == LocalDate.now(zoneId).minusDays(1)

/**
 * 判断当前 [LocalDate] 是否为 [zoneId] 时区的"明天"。
 *
 * @param zoneId 目标时区
 * @return true 表示等于 [LocalDate.now].plusDays(1) 在 [zoneId] 下的日期
 */
fun LocalDate.isTomorrowIn(zoneId: ZoneId): Boolean = this == LocalDate.now(zoneId).plusDays(1)
