package com.letter.basic.application

import android.app.Application
import android.content.Context
import com.letter.basic.i18n.AppLocale
import com.letter.basic.i18n.LocaleStorage

/**
 * 演示模块的 Application。
 *
 * 职责：作为宿主注入 [LocaleStorage]（本演示用 SharedPreferences 实现，宿主可自选方案）
 * 并回放上次的语言设置。
 */
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 宿主提供 sp 实现注入存储；Basic 库内部不内置存储方案
        AppLocale.init(object : LocaleStorage {
            override fun readTag(): String? =
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getString(KEY_LOCALE_TAG, null)

            override fun writeTag(tag: String) {
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_LOCALE_TAG, tag)
                    .apply()
            }
        })
        // 回放上次语言设置，须先于任何 Activity 创建
        AppLocale.restore()
    }

    private companion object {
        // 与改造前 AppLocale 内置的 key 保持一致，升级后旧数据可直接读出
        const val PREFS_NAME = "app_locale"
        const val KEY_LOCALE_TAG = "app_locale_tag"
    }
}
