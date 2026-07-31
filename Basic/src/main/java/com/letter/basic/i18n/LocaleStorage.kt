/**
 * App 语言设置的持久化存储抽象。
 *
 * 由宿主 App 实现并注入 [AppLocale]，Basic 库内部不再内置具体存储方案
 * （SharedPreferences / DataStore 等均由宿主自行选择）。
 *
 * @author Boqing.wu
 * @since 2026-07-31
 */
package com.letter.basic.i18n

/**
 * App 语言设置的持久化存储抽象。
 *
 * 由宿主 App 实现并通过 [AppLocale.init] 注入，Basic 库内部不再感知任何具体存储实现。
 * value 为 [java.util.Locale.toLanguageTag] 字符串（如 "zh-CN"、"en"），空字符串表示"未设置"。
 */
interface LocaleStorage {

    /**
     * 读取上次保存的 languageTag。
     *
     * 返回 null 或空字符串均视为"从未设置"，此时 [AppLocale.restore] 保持系统 Locale。
     *
     * @return 语言 tag；null 或空字符串 = 从未设置
     */
    fun readTag(): String?

    /**
     * 保存 languageTag。
     *
     * 传入空字符串表示清除设置，与"从未设置"等价。
     *
     * @param tag 语言 tag（[java.util.Locale.toLanguageTag] 格式），空字符串 = 清除
     */
    fun writeTag(tag: String)
}
