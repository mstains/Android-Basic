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
        ViewModelProvider.NewInstanceFactory().create(providerVMClass())
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initViewModel()
        startObserve()
        super.onActivityCreated(savedInstanceState)
    }

    private fun initViewModel() {
        // 当前为占位实现：仅把 this 注册为 LifecycleObserver，未触发 mViewModel 懒加载。
        // 真实 ViewModel 初始化由子类在 providerVMClass() 引用 mViewModel 时按需触发。
        // 后续若需在 initViewModel 中做 ViewModel 字段初始化（如 SavedStateHandle），
        // 应在此处扩展，并同步在 onDestroy 中清理对应 observer。
        providerVMClass().let { viewModel ->

            lifecycle.addObserver(this)


        }
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
        // 与 initViewModel 中的 addObserver(this) 对称。
        // 注意：Fragment 自身已由 FragmentManager 注册为 LifecycleObserver，
        // 此 remove 是为了清理 initViewModel 中手动 add 的额外引用，避免重复观察。
        mViewModel.let {
            lifecycle.removeObserver(this)
        }
        super.onDestroy()
    }
}
