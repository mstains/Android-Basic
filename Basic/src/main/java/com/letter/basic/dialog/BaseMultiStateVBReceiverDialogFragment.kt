package com.letter.basic.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.letter.basic.manager.ReceiverManager
import com.letter.basic.receiver.ReceiverImpl


/**
 * 支持 ViewBinding + 广播监听的 DialogFragment 基类。
 *
 * 在 onActivityCreated 中调用 [initBroadcast]，onDestroy 时自动注销所有广播。
 *
 * @param VB ViewBinding 类型
 * @author Boqing.wu
 * @since 2026-06-04
 */
abstract class BaseMultiStateVBReceiverDialogFragment<VB : ViewBinding> : BaseMultiStateVBDialogFragment<VB>(),
    ReceiverImpl {

    private val mReceiverManager: ReceiverManager by lazy { ReceiverManager(requireContext(),this) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBroadcast()
    }


    /**
     * 初始化广播监听。
     *
     * 子类重写，在此处调用 [registerAction] 注册所需 Action。
     * 基类在 onActivityCreated 中 super.onActivityCreated 之后调用。
     */
    open fun initBroadcast() {

    }

    /**
     * 广播接收回调。
     *
     * 子类重写以处理广播消息。
     *
     * @param context 广播上下文，可为 null
     * @param intent 接收到的广播 Intent，可为 null
     */
    override fun onReceive(context: Context?, intent: Intent?) {

    }

    /**
     * 注册指定 Action 的广播。
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
     * 注销当前 DialogFragment 注册的全部广播。
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
