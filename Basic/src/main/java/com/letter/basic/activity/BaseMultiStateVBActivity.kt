package com.letter.basic.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding


/**
 * 支持 ViewBinding 的通用 Activity 基类。
 *
 * 通过 [createViewBinding] 抽象方法由子类提供绑定实例，
 * 基类在 onCreate 中按 initStatusBar → initView → initData → initListener 顺序调用。
 *
 * @param VB ViewBinding 类型，由子类在 createViewBinding 中具体化
 * @author Boqing.wu
 * @since 2026-06-04
 */
abstract class BaseMultiStateVBActivity<VB : ViewBinding> : BaseCommonMultiStateActivity() {

    /**
     * 当前 Activity 的 ViewBinding 实例。
     *
     * 懒加载创建，由 [createViewBinding] 在首次访问时构造。
     * 生命周期与 Activity 一致，无需手动释放。
     */
    protected val viewBinding: VB by lazy { createViewBinding(LayoutInflater.from(this)) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initStatusBar()
        initView()
        initData()
        initListener()
    }


    /**
     * 自定义标题栏返回操作。
     *
     * 子类可重写以拦截标题栏返回按钮，默认行为为结束当前 Activity。
     */
    protected open fun topDefineCancel() {

        finish()

    }


    /**
     * 标题栏右侧文字点击回调。
     *
     * 子类重写以响应右侧文字按钮的点击事件，默认空实现。
     */
    protected open fun topRightTextDefineCancel() {

    }


    /**
     * 创建 ViewBinding 实例。
     *
     * 由子类重写，使用 ViewBinding 的 inflate 方法构造绑定实例。
     *
     * @param inflater LayoutInflater 实例，由基类从当前 Activity 上下文创建
     * @return 子类持有的 ViewBinding 实例
     */
    abstract fun createViewBinding(inflater: LayoutInflater): VB
}
