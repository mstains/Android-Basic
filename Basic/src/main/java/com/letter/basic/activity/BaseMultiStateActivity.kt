package com.letter.basic.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import com.letter.basic.utils.ActivityController


/**
 * 通用 Activity 基类。
 *
 * 自动加入 [ActivityController] 栈管理，便于一键结束全部 Activity。
 * 子类无需关心入栈/出栈逻辑。
 *
 * @author Boqing.wu
 * @since 2026-06-04
 */
open class BaseMultiStateActivity : AppCompatActivity(), LifecycleObserver {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityController.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityController.removeActivity(this)
    }
}
