package com.letter.basic.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleObserver
import androidx.viewbinding.ViewBinding
import com.letter.basic.dialog.builder.WindowBuilder

/**
 * 支持 ViewBinding 的通用 DialogFragment 基类。
 *
 * 提供窗口参数配置（[getWindowBuild]）、生命周期感知与 ViewBinding 懒加载能力。
 * 子类按需重写 [initIntent] / [initBundle] / [initBase] / [initView] / [initData] / [initListener]。
 *
 * @param VB ViewBinding 类型
 * @author Boqing.wu
 * @since 2026-06-04
 */
abstract class BaseMultiStateVBDialogFragment<VB : ViewBinding> : DialogFragment(), LifecycleObserver {


    /**
     * 当前 Dialog 的 ViewBinding 实例。
     *
     * 在 onCreateView 中由 [createViewBinding] 赋值，Fragment 重建时由系统管理生命周期。
     */
    protected var viewBinding: VB? = null

    private val mDisplayMetrics: DisplayMetrics? by lazy {
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
        dm
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Dialog)
        init()
    }

    /**
     * 内置初始化流程。
     *
     * 按 initIntent → initBundle → initBase 顺序调度，由基类在 onCreate 末尾调用。
     *
     * **静默失败语义**：当 Dialog 尚未 attach 到 Activity（`activity == null`）时，
     * [initIntent] **不会**被触发；当 [arguments] 为 null 时 [initBundle] **不会**被触发。
     * 这是 by design 的契约约束（参数来源缺失 → 对应回调无意义），但调用方无法从代码感知失败，
     * 子类若有强制参数需求，应自行在 [onCreate] 入口做 assert。
     */
    private fun init() {
        activity?.apply {
            initIntent(intent)
        }
        arguments?.apply {
            initBundle(this)
        }
        initBase()
    }

    /**
     * 获取 Activity 启动 Intent 中传递的参数。
     *
     * **触发顺序**：[initIntent] 先于 [initBundle] 先于 [initBase] 执行；
     * 仅当 Dialog 已 attach 到 Activity 时触发，`activity == null` 时**静默跳过**。
     *
     * @param intent 启动当前 Dialog 的宿主 Activity 的 Intent
     */
    open fun initIntent(intent: Intent) {

    }

    /**
     * 获取 Bundle 中传递的参数。
     *
     * **触发顺序**：[initBundle] 后于 [initIntent]、先于 [initBase] 执行；
     * 仅当 [arguments] 不为 null 时触发，否则**静默跳过**。
     *
     * @param bundle 调用 setArguments(Bundle) 时传入的参数 Bundle
     */
    open fun initBundle(bundle: Bundle) {

    }

    /**
     * 加载必要初始化对象。
     *
     * **触发顺序**：[initBase] 是 init 三步中的最后一步，先于 [onCreateView] 与 ViewBinding 创建。
     * 子类重写以完成 ViewBinding 创建之前必须就绪的初始化（如构造数据对象、注册 ViewModel 之外的回调）。
     * 与 [initView] 的区别：[initBase] 时尚无 view 可引用，**不应**访问任何 UI 元素。
     */
    open fun initBase() {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        initWindow()
        viewBinding = createViewBinding(inflater, container)
        return viewBinding!!.root
    }


    /**
     * 配置 Dialog 的 Window 参数。
     *
     * 子类重写以自定义窗口尺寸、位置、内外边距、是否可取消等。
     * 默认实现：宽度撑满、高度自适应、底部对齐、点击外部不消失、可通过返回键取消。
     *
     * @param dm 宿主 Activity 的 DisplayMetrics，可为 null（Dialog 尚未 attach 时）
     * @return WindowBuilder 实例，包含所有窗口配置
     */
    open fun getWindowBuild(dm: DisplayMetrics?): WindowBuilder {
        val builder = WindowBuilder()
        builder.apply {

            widthParam = WindowManager.LayoutParams.MATCH_PARENT
            heightParam = WindowManager.LayoutParams.WRAP_CONTENT
            isTouchOutside = false
            isCancelable = true
            gravity = Gravity.BOTTOM
        }
        return builder
    }

    /**
     * 加载窗口参数。
     *
     * 把 [getWindowBuild] 返回的配置实际应用到当前 Dialog 的 Window 上。
     * 调用时机：[onCreateView] 入口处，**先于** [createViewBinding]；此时 [dialog] 可能尚未创建，
     * 故内部用 `dialog?.apply { ... }` 包裹，重复调用无副作用。
     */
    protected open fun initWindow() {

        val builder = getWindowBuild(mDisplayMetrics)
        dialog?.apply {
            window?.apply {
                builder.apply {
                    decorView.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
                    attributes.width = widthParam
                    attributes.height = heightParam
                    attributes.gravity = gravity
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    setCanceledOnTouchOutside(isTouchOutside)
                }

            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        isCancelable = getWindowBuild(mDisplayMetrics).isCancelable
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        initData()
        initListener()
    }


    /**
     * 初始化视图。
     *
     * 由基类在 [onActivityCreated] 中按 `initView → initData → initListener` 顺序第一步调用。
     * 子类重写以配置视图属性（文本、图片、可见性等）；**仅**操作 [viewBinding]，不发起数据请求。
     */
    open fun initView() {

    }

    /**
     * 加载数据。
     *
     * 由基类在 [onActivityCreated] 中按 `initView → initData → initListener` 顺序第二步调用。
     * 子类重写以加载页面所需的初始数据（网络/数据库/SharedPreferences 等）。
     */
    open fun initData() {

    }

    /**
     * 加载监听器。
     *
     * 由基类在 [onActivityCreated] 中按 `initData → initListener` 顺序第三步（最后一步）调用。
     * 子类重写以注册 UI 事件监听（点击、文本变化等）。
     */
    open fun initListener() {

    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            // 兜底捕获：DialogFragment 重复 show / 事务状态丢失等场景下
            // super.show 会抛 IllegalStateException / TransactionTooLargeException，
            // 此处仅 printStackTrace 不向上抛，避免 show 失败导致宿主 Activity 崩溃。
            // **调用方完全感知不到失败**——若需严格感知，应自行在 show 前用 isAdded / isStateSaved 防御。
            e.printStackTrace()
        }
    }

    /**
     * 使用类名作为默认 tag 显示 Dialog。
     *
     * tag 自动取 `this::class.java.simpleName`；**同一 Fragment 重复 show 会触发 IllegalStateException**，
     * 该异常会被本方法静默吞掉（见 [show] 注释），调用方应自行去重。
     *
     * @param transaction FragmentManager 实例，用于调度 show
     */
    fun show(transaction: FragmentManager) {
        try {
            this.show(transaction, this::class.java.simpleName)
        } catch (e: Exception) {
            // 与 show(manager, tag) 同策略：静默吞掉 IllegalStateException / 事务冲突等异常
            e.printStackTrace()
        }


    }


    /**
     * 判断 Dialog 是否正在显示。
     *
     * @return true 表示 Dialog 处于可见状态，false 表示未显示或 Dialog 对象为 null
     */
    fun isShowing(): Boolean {
        dialog?.let {
            return it.isShowing

        }

        return false
    }


    /**
     * 创建 ViewBinding 实例。
     *
     * @param inflater LayoutInflater 实例
     * @param container 父容器 ViewGroup，可为 null
     * @return 子类持有的 ViewBinding 实例
     */
    abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
}
