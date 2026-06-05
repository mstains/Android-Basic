package com.letter.basic.extend

import java.time.DayOfWeek
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
 * 接收者或 [other] 为 null 时返回 null；底层 [java.time.temporal.ChronoUnit.DAYS.between]
 * 在历法异常或年份越界等极端场景下可能抛出 [java.time.DateTimeException]，
 * 一并捕获后返回 null，避免上层业务处理受检异常。
 *
 * @param other 目标日期，可为 null
 * @return 间隔天数，正值表示 [other] 在当前日期之后，负值反之；任一为 null 或异常时返回 null
 */
fun LocalDate?.daysBetween(other: LocalDate?): Long? {
    if (this == null || other == null) return null
    return try {
        java.time.temporal.ChronoUnit.DAYS.between(this, other)
    } catch (_: Exception) {
        null
    }
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
 * 获取 [LocalDate] 的中文星期名（长格式）。
 *
 * 内部基于 [DayOfWeek.value] 映射：1→"星期一"、…、7→"星期日"。
 *
 * @return "星期一" / "星期二" / ... / "星期日"；接收者为 null 时返回 null
 */
fun LocalDate?.dayOfWeekChinese(): String? {
    return when (this?.dayOfWeek()?.value) {
        1 -> "星期一"
        2 -> "星期二"
        3 -> "星期三"
        4 -> "星期四"
        5 -> "星期五"
        6 -> "星期六"
        7 -> "星期日"
        else -> null
    }
}

/**
 * 获取 [LocalDate] 的中文星期名（短格式）。
 *
 * 内部基于 [DayOfWeek.value] 映射：1→"周一"、…、7→"周日"。
 *
 * @return "周一" / "周二" / ... / "周日"；接收者为 null 时返回 null
 */
fun LocalDate?.dayOfWeekShortChinese(): String? {
    return when (this?.dayOfWeek()?.value) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> null
    }
}

/**
 * 计算两个日期字符串之间相差的天数。
 *
 * 字符串按 [pattern] 解析为 [LocalDate] 后再求差，任一参数为 null 或解析失败时返回 null。
 * 解析过程复用同文件 [formatDate]，自动具备格式不兼容时的容错能力。
 *
 * @param other 目标日期字符串
 * @param pattern 日期格式模板，默认 `yyyy-MM-dd`
 * @return 间隔天数，正值表示 [other] 在当前日期之后，负值反之；任一为 null 或解析失败时返回 null
 */
fun String?.daysBetween(other: String?, pattern: String = "yyyy-MM-dd"): Long? {
    if (this == null || other == null) return null
    val from = this.formatDate(pattern) ?: return null
    val to = other.formatDate(pattern) ?: return null
    return from.daysBetween(to)
}

/**
 * 计算两个 [LocalDateTime] 之间相差的天数（按日期部分比较，忽略时分秒）。
 *
 * 先各自 [LocalDateTime.toLocalDate] 再调 [daysBetween]，因此"今天 23:59:59"与"明天 00:00:01"
 * 按整日差 1 天计算，而不是 0 天或负数。
 *
 * @param other 目标 [LocalDateTime]
 * @return 间隔天数；任一为 null 或异常时返回 null
 */
fun LocalDateTime?.daysBetween(other: LocalDateTime?): Long? {
    if (this == null || other == null) return null
    return try {
        java.time.temporal.ChronoUnit.DAYS.between(this.toLocalDate(), other.toLocalDate())
    } catch (_: Exception) {
        null
    }
}
