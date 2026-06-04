package com.letter.basic.receiver

import android.content.Context
import android.content.Intent

/**
 * 广播接收者抽象接口。
 *
 * 实现类需要持有 [ReceiverManager] 实例并对外暴露注册 / 注销入口，
 * 业务层只需关心 [onReceive] 回调处理。
 */
interface ReceiverImpl {

    /**
     * 广播到达时的回调。
     *
     * @param context 广播上下文，可为 null（部分系统广播场景）
     * @param intent 接收到的广播 Intent，可为 null
     */
    fun onReceive(context: Context?, intent: Intent?)

    /**
     * 注册监听指定 Action 的广播。
     *
     * @param action 要监听的广播 Action 字符串
     */
    fun registerAction(action: String)

    /**
     * 注销指定 Action 的广播。
     *
     * @param action 要注销的广播 Action 字符串
     */
    fun unRegisterAction(action: String)

    /**
     * 注销当前实例注册的全部广播。
     */
    fun unRegisterAllAction()
}
