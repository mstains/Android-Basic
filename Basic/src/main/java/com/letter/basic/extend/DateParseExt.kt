/**
 * 字符串 → 日期的解析方向扩展集合。
 *
 * 包含 4 个 [String] 接收者的扩展方法：
 * - [formatDate] / [formatDateTime]：字符串按指定 pattern 解析为日期/日期时间
 * - [format]：字符串按 sourcePattern 解析后按 pattern 重新格式化为字符串
 * - [daysBetween]：两个字符串按相同 pattern 解析后求天数差
 *
 * 全部解析方法在格式不匹配或字符串为 null 时返回 null，不抛出异常，
 * 避免上层业务处理 [java.time.format.DateTimeParseException]。
 *
 * @author Boqing.wu
 * @since 2026-06-11
 */
package com.letter.basic.extend

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 将字符串按 [pattern] 解析为 [LocalDate]。
 *
 * 字符串为 null 或不匹配 pattern 时返回 null，而非抛出异常。
 *
 * @param pattern 日期格式模板，参考 [DateTimeFormatter]，默认 `yyyy-MM-dd`
 * @return 解析成功返回 [LocalDate]，失败或接收者为 null 时返回 null
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
 * @return 解析成功返回 [LocalDateTime]，失败或接收者为 null 时返回 null
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
 * 将日期字符串按 [sourcePattern] 解析为 [LocalDate] 后，再按 [pattern] 重新格式化为字符串。
 *
 * "format(a, b)" 的语义：a 是源字符串的格式，b 是目标输出格式。
 * 解析过程复用同文件 [formatDate]，自动具备格式不兼容时的容错能力。
 *
 * @param sourcePattern 源字符串的格式模板（必须显式传）
 * @param pattern 目标输出格式模板（必须显式传）
 * @return 格式化结果；接收者为 null 或解析失败时返回 null
 */
fun String?.format(sourcePattern: String, pattern: String): String? =
    this?.formatDate(sourcePattern)?.let { parsed ->
        try {
            parsed.format(DateTimeFormatter.ofPattern(pattern))
        } catch (_: Exception) {
            null
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
