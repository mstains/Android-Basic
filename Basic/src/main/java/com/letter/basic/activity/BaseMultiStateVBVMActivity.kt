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
     *
     * 注意：使用 [ViewModelProvider.NewInstanceFactory] 仅支持**无参构造**的 ViewModel；
     * 若子类 ViewModel 依赖 `Application` / `SavedStateHandle` 等入参，**会在运行时抛异常**，
     * 此时应改用 `AndroidViewModelFactory` 或自定义 Factory。
     */
    protected val mViewModel: VM by lazy {
        // 通过 ViewModelProvider 获取实例以纳入 ViewModelStore 管理，
        // 确保 Activity 重建（如配置变更）时 ViewModel 可被保留而非重新创建
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(providerVMClass())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // super.onCreate 已完成 4 步 init（initStatusBar → initView → initData → initListener），
        // 本方法仅在最后追加 startObserve，确保订阅在数据加载完成后就绪
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
