package com.letter.basic.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

/**
 * 支持 ViewBinding + ViewModel 的通用 Fragment 基类。
 *
 * 通过 [providerVMClass] 暴露 ViewModel 实例为 [mViewModel]，
 * 子类在 [startObserve] 中订阅数据流。
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型
 * @author Boqing.wu
 * @since 2026-06-04
 */
abstract class BaseMultiStateVBVMFragment<VB : ViewBinding, VM : ViewModel> :
    BaseMultiStateVBFragment<VB>() {

    /**
     * 当前 Fragment 持有的 ViewModel 实例。
     *
     * 懒加载创建，作用域跟随 Fragment 的 ViewModelStore。
     */
    protected val mViewModel: VM by lazy {
        // 通过 ViewModelProvider 获取实例以纳入 ViewModelStore 管理，
        // 确保 Fragment 重建（如配置变更）时 ViewModel 可被保留而非重新创建
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(providerVMClass())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        startObserve()
    }




    private fun initViewModel() {
        // 当前为占位实现，未触发 mViewModel 懒加载。
        // 真实 ViewModel 初始化由子类在 providerVMClass() 引用 mViewModel 时按需触发。
        // TODO(letter#待建): 后续若需在 initViewModel 中做 ViewModel 字段初始化
        //   (如 SavedStateHandle 注入) → 在此处扩展
    }

    /**
     * 统一错误消息处理。
     *
     * 默认空实现，子类按需重写以集中处理 ViewModel 抛出的业务错误（弹 Toast / 跳转错误页 / 上报日志等）。
     * **code 来自业务层约定**——本框架不规定错误码协议，调用方需自行保证 ViewModel 抛出的 code / message
     * 与子类实现的处理逻辑一致。
     *
     * @param code 业务错误码，可为 null
     * @param message 错误描述，可为 null
     */
    protected open fun initErrorMessage(code: String?, message: String?) {

    }

    /**
     * 接口请求回调入口。
     *
     * 子类实现，建议在此处订阅 [mViewModel] 暴露的 LiveData / StateFlow 数据流。
     * 基类在 onCreate 中 super.onCreate 之后立即调用。
     */
    abstract fun startObserve()


    /**
     * 提供 ViewModel 的 Class 对象。
     *
     * @return 子类持有的 ViewModel Class，供 [ViewModelProvider] 创建实例
     */
    abstract fun providerVMClass(): Class<VM>


    override fun onDestroy() {
        super.onDestroy()
    }
}
