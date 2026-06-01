package com.letter.basic.manager

import java.time.format.DateTimeFormatter

object DateManager {

    // ========== 常用日期格式 ==========

    /** yyyy-MM-dd */
    val DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")


    /** yyyy-MM-dd HH:mm:ss */
    val DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /** yyyy-MM-dd HH:mm:ss.SSS */
    val DATE_TIME_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    /** yyyy-MM-dd'T'HH:mm:ss'Z' */
    val ISO_8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    /** yyyyMMdd（用于文件名等紧凑场景） */
    val COMPACT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd")

    /** yyyyMMdd_HHmmss（用于文件名等紧凑场景） */
    val COMPACT_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    // ========== 仅时间 ==========

    /** HH:mm:ss */
    val TIME_FULL = DateTimeFormatter.ofPattern("HH:mm:ss")

    /** HH:mm */
    val TIME_SHORT = DateTimeFormatter.ofPattern("HH:mm")

    // ========== 年/月 和 月/日 ==========

    /** yyyy-MM */
    val YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM")

    /** MM-dd */
    val MONTH_DAY = DateTimeFormatter.ofPattern("MM-dd")

    // ========== 国际化格式 ==========

    /** MM/dd/yyyy */
    val US_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    /** dd/MM/yyyy */
    val EU_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // ========== 中文格式 ==========

    /** yyyy年MM月dd日 */
    val CN_DATE = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    /** yyyy年MM月dd日 HH:mm:ss */
    val CN_DATE_TIME = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")

    // ========== HTTP / RFC 格式 ==========

    /** EEE, dd MMM yyyy HH:mm:ss z */
    val RFC_2822 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
}
