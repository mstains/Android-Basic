/**
 * [Long] 时间戳转换扩展集合。
 *
 * 包含 4 个方法，按"接收者是否可空"分组：
 * - 非空接收者（[Long]）：[toLocalDate] / [toLocalDateTime]，返回非空日期
 * - 可空接收者（[Long?]）：[toLocalDate] / [toLocalDateTime] 重载，返回可空日期
 *
 * 业务场景：服务端接口字段（Kotlin 端用 [Long?]，来自 Java / Gson 解析）为 null 时，
 * 可空重载返回 null 而非崩溃；非空重载保持高性能无 null 检查路径，调用方按需选择。
 *
 * 两个方法默认使用系统时区，调用方可显式传入 [ZoneId] 覆盖。
 *
 * @author Boqing.wu
 * @since 2026-06-11
 */
package com.letter.basic.extend
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 将时间戳（毫秒）转换为指定时区的 [LocalDate]。
 *
 * 非空接收者重载：调用方保证 timestamp 非 null（如本地时间戳、缓存值），走无 null 检查的快速路径。
 *
 * @param zoneId 时区，默认使用系统时区
 * @return 对应时区的 [LocalDate]
 */
fun Long.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

/**
 * 将时间戳（毫秒）转换为指定时区的 [LocalDateTime]。
 *
 * 非空接收者重载：调用方保证 timestamp 非 null（如本地时间戳、缓存值），走无 null 检查的快速路径。
 *
 * @param zoneId 时区，默认使用系统时区
 * @return 对应时区的 [LocalDateTime]
 */
fun Long.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDateTime()

/**
 * 将时间戳（毫秒）转换为指定时区的 [LocalDate]。
 *
 * 可空接收者重载：用于 Java 互操作场景下服务端返回的 `Long?`（如 Gson 解析的可空字段），
 * 接收者为 null 时返回 null 而非抛出 NPE，让上层业务能以链式 `.let { }` 优雅处理。
 *
 * @param zoneId 时区，默认使用系统时区
 * @return 对应时区的 [LocalDate]；接收者为 null 时返回 null
 */
fun Long?.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate? {
    if (this == null) return null
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}

/**
 * 将时间戳（毫秒）转换为指定时区的 [LocalDateTime]。
 *
 * 可空接收者重载：用于 Java 互操作场景下服务端返回的 `Long?`（如 Gson 解析的可空字段），
 * 接收者为 null 时返回 null 而非抛出 NPE，让上层业务能以链式 `.let { }` 优雅处理。
 *
 * @param zoneId 时区，默认使用系统时区
 * @return 对应时区的 [LocalDateTime]；接收者为 null 时返回 null
 */
fun Long?.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime? {
    if (this == null) return null
    return Instant.ofEpochMilli(this).atZone(zoneId).toLocalDateTime()
}
