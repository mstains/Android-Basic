package com.letter.basic.adapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding


/**
 * 基于 ViewBinding 的 RecyclerView ViewHolder 基类。
 *
 * 使用 ViewBinding 替代传统 `findViewById`，在构造时即持有绑定引用，
 * 子类可直接通过 [viewBinding] 访问布局控件。
 *
 * @param VB ViewBinding 类型
 * @property viewBinding 当前 item 布局的 ViewBinding 实例
 * @author Boqing.wu
 * @since 2026-06-04
 */
open class ViewBindingHolder<VB : ViewBinding>(val viewBinding: VB) :
    RecyclerView.ViewHolder(viewBinding.root) {


}