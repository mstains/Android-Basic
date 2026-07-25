package com.letter.basic.dialog

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

/**
 * 支持 ViewBinding + ViewModel 的通用 DialogFragment 基类。
 *
 * 通过 [providerVMClass] 暴露 ViewModel 实例为 [mViewModel]，
 * 子类在 [startObserve] 中订阅数据流。
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型
 * @author Boqing.wu
 * @since 2026-06-04
 */
abstract class BaseMultiStateVBVMDialogFragment<VB : ViewBinding, VM : ViewModel> :
    BaseMultiStateVBDialogFragment<VB>() {

    /**
     * 当前 DialogFragment 持有的 ViewModel 实例。
     *
     * 懒加载创建，作用域跟随 DialogFragment 的 ViewModelStore。
     */
    protected val mViewModel: VM by lazy {
        // 通过 ViewModelProvider 获取实例以纳入 ViewModelStore 管理，
        // 确保 DialogFragment 重建时 ViewModel 可被保留而非重新创建
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(providerVMClass())
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initViewModel()
        startObserve()
        super.onActivityCreated(savedInstanceState)
    }

    private fun initViewModel() {
        // 当前为占位实现，未触发 mViewModel 懒加载。
        // 真实 ViewModel 初始化由子类在 providerVMClass() 引用 mViewModel 时按需触发。
        // TODO(letter#待建): 后续若需在 initViewModel 中做 ViewModel 字段初始化
        //   (如 SavedStateHandle 注入) → 在此处扩展
    }


    /**
     * 接口请求回调入口。
     *
     * 子类实现，建议在此处订阅 [mViewModel] 暴露的 LiveData / StateFlow 数据流。
     * 基类在 onActivityCreated 中 super 之前调用。
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
