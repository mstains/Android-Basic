package com.letter.basic.activity

import android.os.Bundle
import androidx.viewbinding.ViewBinding

import com.letter.basic.receiver.ReceiverImpl
import com.letter.basic.manager.ReceiverManager

/**
 * 支持 ViewBinding + 广播监听的 Activity 基类。
 *
 * 在 onCreate 中调用 [initBroadcast]，由子类注册所需 Action；
 * onDestroy 时自动注销所有已注册广播，避免内存泄漏。
 * 广播接收回调直接继承自 [ReceiverImpl.onReceive]，签名保持一致。
 *
 * @param VB ViewBinding 类型
 * @author Boqing.wu
 * @since 2026-06-04
 */
abstract class BaseMultiStateVBReceiverActivity<VB : ViewBinding> :
    BaseMultiStateVBActivity<VB>(), ReceiverImpl {

    private val mReceiverManager: ReceiverManager by lazy { ReceiverManager(this, this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBroadcast()

    }

    /**
     * 初始化广播监听。
     *
     * 由子类重写，在此处调用 [registerAction] 注册所需 Action。
     * 基类在 onCreate 中 super.onCreate 之后立即调用。
     */
    abstract fun initBroadcast()


    /**
     * 注册指定 Action 的广播。
     *
     * 重复注册同一 Action 会被 [ReceiverManager] 内部去重。
     *
     * @param action 要监听的广播 Action 字符串
     */
    override fun registerAction(action: String) {
        mReceiverManager.registerAction(action)
    }

    /**
     * 注销指定 Action 的广播。
     *
     * @param action 要注销的广播 Action 字符串
     */
    override fun unRegisterAction(action: String) {
        mReceiverManager.unRegisterAction(action)
    }

    /**
     * 注销当前 Activity 注册的全部广播。
     *
     * onDestroy 时自动调用，避免内存泄漏。
     */
    override fun unRegisterAllAction() {
        mReceiverManager.unRegisterAllAction()
    }


    override fun onDestroy() {
        super.onDestroy()
        unRegisterAllAction()
    }
}
