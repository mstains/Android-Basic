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
     * 由基类在 onCreate 中按 `initStatusBar → initView → initData → initListener` 顺序第二步调用。
     * 子类重写以配置视图属性（文本、图片、可见性等）；使用 ViewBinding 后一般无需 findViewById，
     * 仅当引入第三方控件需要额外配置时才需重写。
     *
     * 注意：本方法内**不应**发起网络/数据库请求，数据加载统一收敛到 [initData]，避免与生命周期错位。
     */
    open fun initView() {

    }

    /**
     * 初始化数据。
     *
     * 由基类在 onCreate 中按 `initView → initData → initListener` 顺序第三步调用。
     * 子类重写以加载页面所需的初始数据（网络请求、数据库查询、SharedPreferences 读取等）。
     * 默认不处理失败状态，子类按需自行实现错误提示或重试入口。
     */
    open fun initData() {

    }

    /**
     * 初始化监听器。
     *
     * 由基类在 onCreate 中按 `initData → initListener` 顺序第四步（也是最后一步）调用。
     * 子类重写以注册 UI 事件监听（点击、文本变化等）。仅注册监听器，**不要**在回调中触发二次 [initData]，
     * 避免在数据未就绪时与初次加载竞争。
     */
    open fun initListener() {

    }

    /**
     * 初始化状态栏。
     *
     * 由子类实现，配置状态栏颜色、图标深浅、是否全屏等。建议与 `setStatusBarFullTransparent` 扩展配合使用。
     * 由基类在 onCreate 中第一步调用，**先于** [initView] 执行，子类可在 [initView] 中读取状态栏高度做布局适配。
     */
    open fun initStatusBar() {


    }
}
