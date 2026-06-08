package com.letter.basic.utils

import android.app.Activity
import android.app.Application
import java.util.*


/**
 * 全局 Activity 栈管理器。
 *
 * 通过 [addActivity] / [removeActivity] 维护一份运行中 Activity 列表，
 * 支持一键结束（[clearAll]）与数量查询（[getSize]）。
 * 非线程安全：仅在主线程调用。
 */
object ActivityController {

    private val actList = LinkedList<Activity>()

    private var mApplication: Application? = null

    /**
     * 注入全局 [Application] 引用，供 [getApplication] 查询。
     *
     * @param application Application 实例，由调用方在 Application.onCreate 中传入
     */
    fun setApplication(application: Application) {
        this.mApplication = application
    }

    /**
     * 将 Activity 加入栈顶。
     *
     * @param activity 待加入的 Activity 实例
     */
    fun addActivity(activity: Activity) {
        actList.add(activity)
    }

    /**
     * 从栈中移除 Activity。
     *
     * @param activity 待移除的 Activity 实例；若不存在则不报错
     */
    fun removeActivity(activity: Activity) {
        actList.remove(activity)
    }

    /**
     * 结束栈中所有未处于 finishing 状态的 Activity。
     *
     * 用于退出登录、切换账号等"清空页面栈"场景。
     */
    fun clearAll() {
        actList.forEach {
            if (!it.isFinishing) {
                it.finish()
            }
        }
    }

    /**
     * 获取当前栈中 Activity 数量。
     *
     * @return 栈中 Activity 实例个数
     */
    fun getSize(): Int {
        return actList.size
    }

    /**
     * 获取通过 [setApplication] 注入的 Application 引用。
     *
     * @return Application 引用，未注入时为 null
     */
    fun getApplication(): Application? {
        return mApplication
    }
}
