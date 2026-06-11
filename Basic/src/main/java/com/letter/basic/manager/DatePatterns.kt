/**
 * 固定 pattern 的 [DateTimeFormatter] 模板集合。
 *
 * ⚠️ **本类只放与 locale 无关的固定格式模板** —— 所有字段的 pattern 是字面字符串，
 * **不会**随 App 内语言切换而改变输出。
 *
 * **何时用本类**：
 * - 接口字段（如 ISO_8601、RFC_2822）—— 服务端契约不允许变化
 * - 文件名 / 缓存键（如 COMPACT_DATE_TIME）—— 跨设备/跨进程必须稳定
 * - 报表列宽 / 协议字段（如 YEAR_MONTH）—— 长度固定
 *
 * ❌ **不要把本类用于 UI 日期展示** —— 走 locale 的 UI 展示请改用
 * [AppDateFormatter]，由 [com.letter.basic.i18n.AppLocale.current] 决定输出。
 * 混用本类会出现"中文 App 仍展示 `MM/dd/yyyy`"的问题。
 *
 * 历史变更：本类由 `DateManager` 重命名而来。
 * 原 `DateManager.US_DATE` / `EU_DATE` / `CN_DATE` / `CN_DATE_TIME` 4 个 locale 相关模板
 * 已删除（违反"业务方不传 pattern"原则），改用 [AppDateFormatter.medium] / [short] 等覆盖。
 *
 * @author Boqing.wu
 * @since 2026-06-11
 */
package com.letter.basic.manager

import java.time.format.DateTimeFormatter

/**
 * 固定 pattern 的 [DateTimeFormatter] 模板集合。
 *
 * 详见文件级 KDoc 中的"何时用 / 不要用"边界说明。
 */
object DatePatterns {

    /**
     * ISO 8601 纯日期格式：`yyyy-MM-dd`，典型场景为接口字段与日志输出。
     */
    val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * 标准的日期 + 时间（秒级）格式：`yyyy-MM-dd HH:mm:ss`，典型场景为本地数据库存储。
     */
    val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * ISO 8601 云端时间戳格式：`yyyy-MM-dd'T'HH:mm:ss'Z'`，典型场景为云端 API 字段。
     */
    val ISO_8601: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    /**
     * 紧凑型日期：`yyyyMMdd`，典型场景为文件名 / 缓存目录命名。
     */
    val COMPACT_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    /**
     * 紧凑型日期时间：`yyyyMMdd_HHmmss`，典型场景为截图 / 导出文件命名。
     */
    val COMPACT_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    /**
     * 完整时间：`HH:mm:ss`，典型场景为独立展示时间段。
     */
    val TIME_FULL: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    /**
     * 短时间：`HH:mm`，典型场景为列表项的次要时间展示。
     */
    val TIME_SHORT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * 年月格式：`yyyy-MM`，典型场景为月历控件、报表月份列。
     */
    val YEAR_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    /**
     * 月日格式：`MM-dd`，典型场景为纪念日 / 节日展示（不含年份）。
     */
    val MONTH_DAY: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd")

    /**
     * RFC 2822 邮件 / HTTP 时间格式：`EEE, dd MMM yyyy HH:mm:ss z`，典型场景为解析 HTTP Date 头。
     */
    val RFC_2822: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
}
