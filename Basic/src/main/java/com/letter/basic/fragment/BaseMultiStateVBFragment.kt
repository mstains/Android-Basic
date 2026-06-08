package com.letter.basic.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * 支持 ViewBinding 的通用 Fragment 基类。
 *
 * 子类在 [createViewBinding] 中创建绑定实例，基类在 onViewCreated 中按
 * initView → initData → initListener 顺序调度。
 *
 * @param VB ViewBinding 类型
 * @author Boqing.wu
 * @since 2026-06-04
 */
abstract class BaseMultiStateVBFragment<VB : ViewBinding> : BaseMultiStateCommonFragment() {

    /**
     * 当前 Fragment 的 ViewBinding 实例。
     *
     * 在 [createView] 中由 [createViewBinding] 赋值，onDestroyView 时由子类负责置空。
     */
    protected var viewBinding: VB? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        initListener()
    }

    /**
     * 创建 Fragment 根视图并初始化 ViewBinding。
     *
     * 子类一般无需重写，基类已实现 ViewBinding 创建流程。
     *
     * @param inflater 用于加载 XML 布局的 LayoutInflater
     * @param container 父容器 ViewGroup，可为 null
     * @param savedInstanceState 恢复状态用的 Bundle，可为 null
     * @return ViewBinding 根 View 实例
     */
    override fun createView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewBinding = createViewBinding(inflater, container)
        return viewBinding!!.root
    }


    /**
     * 初始化视图。
     *
     * 子类重写以配置视图属性（文本、图片、可见性等）。
     */
    abstract fun initView()

    /**
     * 初始化数据。
     *
     * 子类重写以加载页面所需的初始数据。
     */
    open fun initData() {

    }

    /**
     * 初始化监听器。
     *
     * 子类重写以注册 UI 事件监听（点击、文本变化等）。
     */
    open fun initListener() {}

    /**
     * 创建 ViewBinding 实例。
     *
     * @param inflater LayoutInflater 实例
     * @param container 父容器 ViewGroup，可为 null
     * @return 子类持有的 ViewBinding 实例
     */
    abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
}
