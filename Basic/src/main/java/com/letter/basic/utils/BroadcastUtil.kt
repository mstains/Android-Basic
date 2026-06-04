package com.letter.basic.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * 基于 [LocalBroadcastManager] 的广播注册 / 注销工具。
 *
 * [LocalBroadcastManager] 已被官方标记为废弃，建议在 AndroidX 项目中改用
 * `androidx.lifecycle.LifecycleObserver` + `ContextCompat.registerReceiver` 方案。
 * 本类保留仅用于兼容老代码。
 */
object BroadcastUtil {

    /**
     * 注册本地广播接收器监听指定 [action]。
     *
     * @param action 要监听的广播 Action
     * @param receiver 接收器实例
     * @param context 用于获取 [LocalBroadcastManager] 单例的上下文
     */
    fun registerLocalAction(action: String,
                            receiver: BroadcastReceiver,
                            context: Context) {
        val filter = IntentFilter()
        filter.addAction(action)
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
    }

    /**
     * 注销单个本地广播接收器。
     *
     * @param context 用于获取 [LocalBroadcastManager] 单例的上下文
     * @param receiver 之前注册的接收器实例
     */
    fun unRegisterAction(context: Context, receiver: BroadcastReceiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
    }

    /**
     * 注销 Map 中保存的所有本地广播接收器并清空 Map。
     *
     * @param context 用于获取 [LocalBroadcastManager] 单例的上下文
     * @param map 以 Action 为键、接收器为值的 Map；为 null 时不执行任何操作
     */
    fun unRegisterAction(context: Context, map: HashMap<String, out BroadcastReceiver>?) {
        map?.apply {
            map.keys.forEach {
                val receiver = get(it)
                receiver?.let {
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
                }
            }

            map.clear()
        }
    }
}
