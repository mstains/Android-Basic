/**
 * [com.letter.basic.activity.BaseCommonMultiStateActivity] 的扩展方法集合。
 *
 * 当前承载"切换 App 语言"相关能力。基类本体只管生命周期钩子，业务动作走扩展函数，
 * 保持"基类只管生命周期"的边界。
 *
 * @author Boqing.wu
 * @since 2026-06-11
 */
package com.letter.basic.extend

import com.letter.basic.activity.BaseCommonMultiStateActivity
import com.letter.basic.i18n.AppLocale
import java.util.Locale

/**
 * 切换 App 语言并自动重建当前 Activity。
 *
 * 内部流程：
 * 1. 委托 [AppLocale.set] 写入持久化（宿主注入的 [com.letter.basic.i18n.LocaleStorage]） + 通知 [androidx.appcompat.app.AppCompatDelegate]
 * 2. 调用 [android.app.Activity.recreate] 立即重建当前 Activity，避免看到一帧旧资源
 *
 * 适合"切完语言直接看到效果"的简单场景。
 * 若需要自定义切换动画或单 Activity 架构，改用 [applyLanguage] 并传 `recreate = false`。
 *
 * @receiver 调用方 Activity（必须是 [BaseCommonMultiStateActivity] 子类）
 * @param locale 目标语言环境，传 `null` 表示回退到系统 Locale
 */
fun BaseCommonMultiStateActivity.switchLanguage(locale: Locale?) {
    // 默认走 recreate 路径，复用 applyLanguage 不重复实现
    applyLanguage(locale, recreate = true)
}

/**
 * 切换 App 语言，可选是否重建当前 Activity。
 *
 * - `recreate = true`（默认）：同 [switchLanguage]，立即重建当前 Activity
 * - `recreate = false`：仅持久化 + 通知 AppCompatDelegate，调用方自行刷新 UI
 *
 * 切到 `recreate = false` 模式的常见理由：
 * 1. 单 Activity 架构下，由 NavController 主动刷新当前 destination
 * 2. 自定义切换动画，不想走默认的 Activity 重建闪烁
 *
 * @receiver 调用方 Activity（必须是 [BaseCommonMultiStateActivity] 子类）
 * @param locale 目标语言环境，传 `null` 表示回退到系统 Locale
 * @param recreate 是否重建当前 Activity，默认 `true`
 */
fun BaseCommonMultiStateActivity.applyLanguage(locale: Locale?, recreate: Boolean = true) {
    // 1. 委托 AppLocale：写持久化（宿主注入的 LocaleStorage）+ 通知 AppCompatDelegate + 立即更新 current
    AppLocale.set(locale)
    // 2. 按需 recreate：AppCompatDelegate 会触发其他 Activity 重建，
    //    此处显式 recreate 是为了"当前 Activity 立即刷新"，避免看到一帧旧资源
    if (recreate) {
        recreate()
    }
}
