package com.letter.basic.activity

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

/**
 * 支持 ViewBinding + ViewModel 的通用 Activity 基类。
 *
 * 通过 [providerVMClass] 提供 ViewModel Class，
 * 基类使用 [ViewModelProvider.NewInstanceFactory] 创建实例并暴露为 [mViewModel]。
 * 子类在 [startObserve] 中订阅 ViewModel 数据流。
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型
 * @author Boqing.wu
 * @since 2026-06-04
 */
abstract class BaseMultiStateVBVMActivity<VB : ViewBinding, VM : ViewModel> :
    BaseMultiStateVBActivity<VB>() {

    /**
     * 当前 Activity 持有的 ViewModel 实例。
     *
     * 懒加载创建，生命周期与 Activity 一致（实际作用域跟随 ViewModelStore）。
     */
    protected val mViewModel: VM by lazy {
        ViewModelProvider.NewInstanceFactory().create(providerVMClass())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startObserve()
    }


    /**
     * 接口请求回调入口。
     *
     * 由子类实现，建议在此处订阅 [mViewModel] 暴露的 LiveData / StateFlow 数据流。
     * 基类在 onCreate 中 super.onCreate 之后立即调用。
     */
    abstract fun startObserve()


    /**
     * 提供 ViewModel 的 Class 对象。
     *
     * @return 子类持有的 ViewModel Class，供 [ViewModelProvider] 创建实例
     */
    abstract fun providerVMClass(): Class<VM>
}
