package com.letter.basic.application

import android.app.Application
import com.letter.basic.i18n.AppLocale

class BaseApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        AppLocale.restore(this)
    }
}