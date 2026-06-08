package com.letter.basic.manager

import java.time.format.DateTimeFormatter

/**
 * 常用 [DateTimeFormatter] 模板集合。
 *
 * 按使用场景分组：纯日期、日期时间、纯时间、年月、国际化、中文、HTTP / RFC。
 * 所有 formatter 在 object 初始化时构造（线程安全，可全局共享），无需加锁。
 */
object DateManager {

    /**
     * ISO 8601 纯日期格式：`yyyy-MM-dd`，典型场景为接口字段与日志输出。
     */
    val DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * 标准的日期 + 时间（秒级）格式：`yyyy-MM-dd HH:mm:ss`，典型场景为本地数据库存储。
     */
    val DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * 标准的日期 + 时间（毫秒级）格式：`yyyy-MM-dd HH:mm:ss.SSS`，典型场景为日志打点与性能分析。
     */
    val DATE_TIME_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    /**
     * ISO 8601 云端时间戳格式：`yyyy-MM-dd'T'HH:mm:ss'Z'`，典型场景为云端 API 字段。
     */
    val ISO_8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    /**
     * 紧凑型日期：`yyyyMMdd`，典型场景为文件名 / 缓存目录命名。
     */
    val COMPACT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd")

    /**
     * 紧凑型日期时间：`yyyyMMdd_HHmmss`，典型场景为截图 / 导出文件命名。
     */
    val COMPACT_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    /**
     * 完整时间：`HH:mm:ss`，典型场景为独立展示时间段。
     */
    val TIME_FULL = DateTimeFormatter.ofPattern("HH:mm:ss")

    /**
     * 短时间：`HH:mm`，典型场景为列表项的次要时间展示。
     */
    val TIME_SHORT = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * 年月格式：`yyyy-MM`，典型场景为月历控件、报表月份列。
     */
    val YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM")

    /**
     * 月日格式：`MM-dd`，典型场景为纪念日 / 节日展示（不含年份）。
     */
    val MONTH_DAY = DateTimeFormatter.ofPattern("MM-dd")

    /**
     * 美式日期格式：`MM/dd/yyyy`，典型场景为面向美国用户的本地化展示。
     */
    val US_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    /**
     * 欧式日期格式：`dd/MM/yyyy`，典型场景为面向欧洲用户的本地化展示。
     */
    val EU_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /**
     * 中文日期格式：`yyyy年MM月dd日`，典型场景为面向中文用户的 UI 展示。
     */
    val CN_DATE = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    /**
     * 中文日期时间格式：`yyyy年MM月dd日 HH:mm:ss`，典型场景为面向中文用户的订单 / 物流时间展示。
     */
    val CN_DATE_TIME = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")

    /**
     * RFC 2822 邮件 / HTTP 时间格式：`EEE, dd MMM yyyy HH:mm:ss z`，典型场景为解析 HTTP Date 头。
     */
    val RFC_2822 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
}
