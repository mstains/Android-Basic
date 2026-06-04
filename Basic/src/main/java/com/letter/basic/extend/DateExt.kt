package com.letter.basic.extend

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 日期 / 时间字符串与 [LocalDate] / [LocalDateTime] 之间的转换扩展。
 *
 * 全部解析方法在格式不匹配或字符串为 null 时返回 null，不抛出异常，
 * 避免上层业务处理 [java.time.format.DateTimeParseException]。
 */

/**
 * 将字符串按 [pattern] 解析为 [LocalDate]。
 *
 * 字符串为 null 或不匹配 pattern 时返回 null，而非抛出异常。
 *
 * @param pattern 日期格式模板，参考 [DateTimeFormatter]，默认 `yyyy-MM-dd`
 * @return 解析成功返回 [LocalDate]，失败或为 null 时返回 null
 */
fun String?.formatDate(pattern: String = "yyyy-MM-dd"): LocalDate? {
    if (this == null) return null
    return try {
        LocalDate.parse(this, DateTimeFormatter.ofPattern(pattern))
    } catch (_: Exception) {
        null
    }
}

/**
 * 将字符串按 [pattern] 解析为 [LocalDateTime]。
 *
 * 字符串为 null 或不匹配 pattern 时返回 null，而非抛出异常。
 *
 * @param pattern 日期时间格式模板，默认 `yyyy-MM-dd HH:mm:ss`
 * @return 解析成功返回 [LocalDateTime]，失败或为 null 时返回 null
 */
fun String?.formatDateTime(pattern: String = "yyyy-MM-dd HH:mm:ss"): LocalDateTime? {
    if (this == null) return null
    return try {
        LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))
    } catch (_: Exception) {
        null
    }
}

/**
 * 将 [LocalDate] 按 [pattern] 格式化为字符串。
 *
 * @param pattern 日期格式模板，默认 `yyyy-MM-dd`
 * @return 格式化结果；接收者为 null 或 pattern 不合法时返回 null
 */
fun LocalDate?.toDisplayString(pattern: String = "yyyy-MM-dd"): String? {
    return try {
        this?.format(DateTimeFormatter.ofPattern(pattern))
    } catch (_: Exception) {
        null
    }
}

/**
 * 将 [LocalDateTime] 按 [pattern] 格式化为字符串。
 *
 * @param pattern 日期时间格式模板，默认 `yyyy-MM-dd HH:mm:ss`
 * @return 格式化结果；接收者为 null 或 pattern 不合法时返回 null
 */
fun LocalDateTime?.toDisplayString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String? {
    return try {
        this?.format(DateTimeFormatter.ofPattern(pattern))
    } catch (_: Exception) {
        null
    }
}

/**
 * 将时间戳（毫秒）转换为指定时区的 [LocalDate]。
 *
 * @param zoneId 时区，默认使用系统时区
 * @return 对应时区的 [LocalDate]
 */
fun Long.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}

/**
 * 将时间戳（毫秒）转换为指定时区的 [LocalDateTime]。
 *
 * @param zoneId 时区，默认使用系统时区
 * @return 对应时区的 [LocalDateTime]
 */
fun Long.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDateTime()
}

/**
 * 判断当前 [LocalDate] 是否为系统时区的"今天"。
 *
 * @return true 表示等于 [LocalDate.now]，false 表示不是
 */
fun LocalDate.isToday(): Boolean {
    return this == LocalDate.now()
}

/**
 * 判断当前 [LocalDate] 是否为系统时区的"昨天"。
 *
 * @return true 表示等于 [LocalDate.now].minusDays(1)
 */
fun LocalDate.isYesterday(): Boolean {
    return this == LocalDate.now().minusDays(1)
}

/**
 * 判断当前 [LocalDate] 是否为系统时区的"明天"。
 *
 * @return true 表示等于 [LocalDate.now].plusDays(1)
 */
fun LocalDate.isTomorrow(): Boolean {
    return this == LocalDate.now().plusDays(1)
}

/**
 * 计算当前 [LocalDate] 与 [other] 之间相差的天数。
 *
 * @param other 目标日期
 * @return 间隔天数，正值表示 [other] 在当前日期之后，负值反之
 */
fun LocalDate.daysBetween(other: LocalDate): Long {
    return java.time.temporal.ChronoUnit.DAYS.between(this, other)
}

/**
 * 提取 [LocalDateTime] 的日期部分。
 *
 * @return 等价的 [LocalDate]
 */
fun LocalDateTime.date(): LocalDate {
    return this.toLocalDate()
}

/**
 * 提取 [LocalDateTime] 的时间部分。
 *
 * @return 等价的 [LocalTime]
 */
fun LocalDateTime.time(): LocalTime {
    return this.toLocalTime()
}
