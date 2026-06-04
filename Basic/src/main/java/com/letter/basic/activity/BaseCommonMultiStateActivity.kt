package com.letter.basic.activity


/**
 * 通用 Activity 基类，提供初始化钩子方法。
 *
 * 子类按需重写 [initStatusBar] / [initView] / [initData] / [initListener]，
 * 由基类统一调度调用顺序，避免每个子类重复编排 onCreate 流程。
 *
 * @author Boqing.wu
 * @since 2026-06-04
 */
abstract class BaseCommonMultiStateActivity : BaseMultiStateActivity() {

    /**
     * 初始化视图。
     *
     * 子类重写以初始化视图：使用 ViewBinding 后一般无需 findViewById，
     * 仅当引入第三方控件需要额外配置时才需重写。
     */
    open fun initView() {

    }

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
    open fun initListener() {

    }

    /**
     * 初始化状态栏。
     *
     * 由子类实现，配置状态栏颜色、图标深浅、是否全屏等。
     */
    abstract fun initStatusBar()
}
