package com.letter.basic.extend

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 将字符串解析为 LocalDate，字符串为 null 或解析失败时返回 null
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
 * 将字符串解析为 LocalDateTime，字符串为 null 或解析失败时返回 null
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
 * 将 LocalDate 格式化为字符串，pattern 不合法时返回 null
 */
fun LocalDate.toDisplayString(pattern: String = "yyyy-MM-dd"): String? {
    return try {
        this.format(DateTimeFormatter.ofPattern(pattern))
    } catch (_: Exception) {
        null
    }
}

/**
 * 将 LocalDateTime 格式化为字符串，pattern 不合法时返回 null
 */
fun LocalDateTime.toDisplayString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String? {
    return try {
        this.format(DateTimeFormatter.ofPattern(pattern))
    } catch (_: Exception) {
        null
    }
}

fun Long.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}

fun Long.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDateTime()
}

fun LocalDate.isToday(): Boolean {
    return this == LocalDate.now()
}

fun LocalDate.isYesterday(): Boolean {
    return this == LocalDate.now().minusDays(1)
}

fun LocalDate.isTomorrow(): Boolean {
    return this == LocalDate.now().plusDays(1)
}

fun LocalDate.daysBetween(other: LocalDate): Long {
    return java.time.temporal.ChronoUnit.DAYS.between(this, other)
}

fun LocalDateTime.date(): LocalDate {
    return this.toLocalDate()
}

fun LocalDateTime.time(): LocalTime {
    return this.toLocalTime()
}