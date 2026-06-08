package com.letter.basic.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.letter.basic.receiver.ReceiverImpl
import com.letter.basic.utils.BroadcastUtil


/**
 * 广播注册 / 注销管理器。
 *
 * 内部使用 [BroadcastUtil]（基于 [androidx.localbroadcastmanager.content.LocalBroadcastManager]）
 * 维护 Action → Receiver 映射，对外屏蔽重复注册 / 反注册细节。
 *
 * @param mContext 用于获取 [LocalBroadcastManager] 单例的上下文
 * @param mReceiverImpl 实际处理广播回调的实现类
 */
class ReceiverManager(private val mContext: Context, private val mReceiverImpl: ReceiverImpl) {


    private var mReceiverMap: HashMap<String, Receiver>? = null

    /**
     * 注册监听指定 Action 的广播。
     *
     * 同一 Action 重复注册会被内部去重（基于 Map key 唯一性）。
     *
     * @param action 要监听的广播 Action 字符串
     */
    fun registerAction(action: String) {
        if (mReceiverMap == null) {
            mReceiverMap = HashMap()
        }
        mReceiverMap?.apply {
            if (!containsKey(action)) {
                val receiver = Receiver()
                this[action] = receiver
                BroadcastUtil.registerLocalAction(
                    action, receiver, mContext
                )
            }
        }
    }

    /**
     * 注销指定 Action 的广播。
     *
     * @param action 要注销的广播 Action 字符串；若未注册则不报错
     */
    fun unRegisterAction(action: String) {
        mReceiverMap?.let { map ->
            map[action]?.let { receiver ->
                BroadcastUtil.unRegisterAction(mContext, receiver)
            }
        }
    }

    /**
     * 注销当前管理器注册的全部广播并清空内部 Map。
     */
    fun unRegisterAllAction() {
        BroadcastUtil.unRegisterAction(mContext, mReceiverMap)
    }

    /**
     * 内部使用的广播接收者，转发回调到 [mReceiverImpl]。
     */
    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            this@ReceiverManager.mReceiverImpl.onReceive(context, intent)
        }
    }
}
