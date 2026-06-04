package com.letter.basic.manager

import java.time.format.DateTimeFormatter

/**
 * 常用 [DateTimeFormatter] 模板集合。
 *
 * 按使用场景分组：纯日期、日期时间、纯时间、年月、国际化、中文、HTTP / RFC。
 * 所有 formatter 在初始化时构造（线程安全，可全局共享）。
 */
object DateManager {

    val DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val DATE_TIME_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    val ISO_8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    val COMPACT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd")

    val COMPACT_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    val TIME_FULL = DateTimeFormatter.ofPattern("HH:mm:ss")

    val TIME_SHORT = DateTimeFormatter.ofPattern("HH:mm")

    val YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM")

    val MONTH_DAY = DateTimeFormatter.ofPattern("MM-dd")

    val US_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    val EU_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val CN_DATE = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    val CN_DATE_TIME = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")

    val RFC_2822 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
}
