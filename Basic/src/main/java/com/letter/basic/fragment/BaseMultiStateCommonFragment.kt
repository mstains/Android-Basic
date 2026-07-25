package com.letter.basic.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment


/**
 * 通用 Fragment 基类。
 *
 * 把 onCreateView 模板方法下沉到 [createView]，子类专注于返回 View 实例。
 *
 * @author Boqing.wu
 * @since 2026-06-04
 */
abstract class BaseMultiStateCommonFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return createView(inflater, container, savedInstanceState)
    }


    /**
     * 创建 Fragment 根视图。
     *
     * 子类重写以返回具体 View 实例，基类在 onCreateView 中调用。
     *
     * @param inflater 用于加载 XML 布局的 LayoutInflater
     * @param container 父容器 ViewGroup，可为 null
     * @param savedInstanceState 恢复状态用的 Bundle，可为 null
     * @return Fragment 根 View 实例
     */
    abstract fun createView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View

}
