package com.letter.basic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.letter.basic.adapter.viewholder.ViewBindingHolder
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.dragswipe.listener.DragAndSwipeDataCallback

/**
 * RecyclerView 选择模式。
 */
enum class SelectionMode {
    /** 无选择功能，默认模式，向后兼容。 */
    NONE,

    /** 单选：选中一项时自动取消其他选中项，再次点击同一项可取消选中。 */
    SINGLE,

    /** 多选：可同时选中多项，受 [BaseMultiStateVBQuickAdapter.maxSelectCount] 限制。 */
    MULTIPLE
}

/**
 * 基于 ViewBinding 的通用 RecyclerView Adapter 基类。
 *
 * 集成多状态页面、拖拽排序、侧滑删除，以及单选 / 多选功能。
 * 选择功能默认为 [SelectionMode.NONE]，不启用时无额外开销。
 *
 * 选择模式通过 [selectionMode] 切换，切换时自动清空当前选中状态。
 * 子类在 [onBindViewHolder] 中通过 [isSelected] 读取当前 item 选中状态来更新 UI。
 *
 * @param T  列表数据实体类型，必须为非空 Any 子类
 * @param VB ViewBinding 类型，由子类在 [createViewBinding] 中具体化
 *
 * @author Boqing.wu
 * @since 1.0
 */
abstract class BaseMultiStateVBQuickAdapter<T : Any, VB : ViewBinding> :
    BaseQuickAdapter<T, ViewBindingHolder<VB>>(), DragAndSwipeDataCallback {

    init {
        isStateViewEnable = true


    }

    // ==================== 选择功能 ====================

    /**
     * 当前选择模式。
     *
     * 修改此属性时自动调用 [clearSelection] 清空选中状态。
     * 默认值为 [SelectionMode.NONE]，无选择功能。
     */
    var selectionMode: SelectionMode = SelectionMode.NONE
        set(value) {
            field = value
            clearSelection()
        }

    /**
     * 多选模式下的最大选中数量。
     *
     * 仅 [SelectionMode.MULTIPLE] 下生效，设为小于 1 的值时自动修正为 [Int.MAX_VALUE]（无上限）。
     * 默认无上限。
     */
    var maxSelectCount: Int = Int.MAX_VALUE
        set(value) {
            field = if (value < 1) Int.MAX_VALUE else value
        }

    /**
     * 选中状态变化回调。
     *
     * 每次选中/取消选中后触发，参数为当前所有选中项的列表。
     * 单选模式下列表最多包含 1 项，多选模式下可包含 0 ~ maxSelectCount 项。
     */
    var onSelectionChanged: ((List<T>) -> Unit)? = null

    /**
     * 多选达到上限回调。
     *
     * 仅在 [SelectionMode.MULTIPLE] 且选中数量达到 [maxSelectCount] 时触发，
     * 参数为当前上限值，方便调用方给出提示。
     */
    var onSelectionLimitReached: ((Int) -> Unit)? = null

    /** 当前选中的 position 集合。position 模式下为唯一数据源，key 模式下与 [mSelectedKeys] 同步维护。 */
    private val mSelectedPositions = mutableSetOf<Int>()

    /** 当前选中的 key 集合，仅 key 模式下使用。子类覆写 [getItemKey] 后启用。 */
    private val mSelectedKeys = mutableSetOf<Any?>()

    /** 是否已启用 key 模式。首次通过 [getItemKey] 查询到非 null key 时自动置为 true。 */
    private var mUsesKeyMode: Boolean = false

    // ==================== 选择 API ====================

    /**
     * 切换指定 position 的选中状态。
     *
     * 单选模式：选中新项时自动取消旧项，再次点击已选项可取消选中（允许空选）。
     * 多选模式：到达 [maxSelectCount] 上限时不再选中，触发 [onSelectionLimitReached]。
     * [SelectionMode.NONE] 模式下无操作。
     *
     * @param position 要切换的 item 位置
     * @return 操作后该 position 是否处于选中状态；[SelectionMode.NONE] 或 item 为 null 或达到上限时返回 false
     */
    fun toggleSelection(position: Int): Boolean {
        if (selectionMode == SelectionMode.NONE) return false
        val item = getItem(position) ?: return false
        val key = getItemKey(item)
        val isKeyMode = key != null
        if (isKeyMode) mUsesKeyMode = true

        val currentlySelected =
            if (isKeyMode) mSelectedKeys.contains(key) else mSelectedPositions.contains(position)

        if (currentlySelected) {
            if (isKeyMode) mSelectedKeys.remove(key)
            mSelectedPositions.remove(position)
        } else {
            if (selectionMode == SelectionMode.SINGLE) {
                // 单选：先取消之前的选中并刷新对应 UI
                val previous = mSelectedPositions.toList()
                mSelectedPositions.clear()
                mSelectedKeys.clear()
                previous.forEach { notifyItemChanged(it) }
            } else {
                // 多选：key 模式下以 mSelectedKeys 计数，position 模式下以 mSelectedPositions 计数
                val currentCount = if (mUsesKeyMode) mSelectedKeys.size else mSelectedPositions.size
                if (currentCount >= maxSelectCount) {
                    onSelectionLimitReached?.invoke(maxSelectCount)
                    return false
                }
            }
            mSelectedPositions.add(position)
            if (isKeyMode) mSelectedKeys.add(key)
        }

        val nowSelected = !currentlySelected
        notifyItemChanged(position)
        onSelectionChanged?.invoke(getSelectedItems())
        return nowSelected
    }

    /**
     * 设置指定 position 的选中状态。
     *
     * 与 [toggleSelection] 的区别：不切换，强制设为指定值，已处于目标状态时不重复操作。
     *
     * @param position 目标 item 位置
     * @param selected true 表示选中，false 表示取消选中
     * @return 操作后该 position 是否处于选中状态；[SelectionMode.NONE] 或 item 为 null 或达到上限时返回调用前的状态
     */
    fun setSelected(position: Int, selected: Boolean): Boolean {
        if (selectionMode == SelectionMode.NONE) return false

        val item = getItem(position) ?: return false
        val key = getItemKey(item)
        val isKeyMode = key != null
        if (isKeyMode) mUsesKeyMode = true

        val currentlySelected =
            if (isKeyMode) mSelectedKeys.contains(key) else mSelectedPositions.contains(position)

        if (selected) {
            if (currentlySelected) return true // 已选中，无操作，返回当前状态
            if (selectionMode == SelectionMode.SINGLE) {
                val previous = mSelectedPositions.toList()
                mSelectedPositions.clear()
                mSelectedKeys.clear()
                previous.forEach { notifyItemChanged(it) }
            } else {
                val currentCount = if (mUsesKeyMode) mSelectedKeys.size else mSelectedPositions.size
                if (currentCount >= maxSelectCount) {
                    onSelectionLimitReached?.invoke(maxSelectCount)
                    return false
                }
            }
            mSelectedPositions.add(position)
            if (isKeyMode) mSelectedKeys.add(key)
        } else {
            if (!currentlySelected) return false // 未选中，无操作，返回当前状态
            if (isKeyMode) mSelectedKeys.remove(key)
            mSelectedPositions.remove(position)
        }

        notifyItemChanged(position)
        onSelectionChanged?.invoke(getSelectedItems())
        return selected
    }

    /**
     * 通过 item 实体设置指定项的选中状态。
     *
     * 内部根据 [getItemKey] 或 [items.indexOf] 查找 item 对应的 position，
     * 找到后委托给 [setSelected(position, selected)]。
     *
     * @param item     目标数据实体
     * @param selected true 表示选中，false 表示取消选中
     * @return 操作后该 item 是否处于选中状态；找不到匹配 position 时返回 false
     */
    fun setSelected(item: T, selected: Boolean): Boolean {
        val position = findPositionByItem(item) ?: return false
        return setSelected(position, selected)
    }

    /**
     * 判断当前是否还能继续选中更多项。
     *
     * 仅在 [SelectionMode.MULTIPLE] 下有效，比较当前选中数量与 [maxSelectCount]。
     * [SelectionMode.NONE] 下始终返回 false，[SelectionMode.SINGLE] 下当前无选中时返回 true。
     *
     * @return true 表示还可继续选中；false 表示已达上限或选择模式不允许
     */
    fun canSelectMore(): Boolean {
        return when (selectionMode) {
            SelectionMode.NONE -> false
            SelectionMode.SINGLE -> {
                val currentCount = if (mUsesKeyMode) mSelectedKeys.size else mSelectedPositions.size
                currentCount == 0
            }

            SelectionMode.MULTIPLE -> {
                val currentCount = if (mUsesKeyMode) mSelectedKeys.size else mSelectedPositions.size
                currentCount < maxSelectCount
            }
        }
    }

    /**
     * 查询指定 position 是否处于选中状态。
     *
     * 子类在 [onBindViewHolder] 中调用此方法决定 UI 表现。
     *
     * @param position 目标 item 位置
     * @return true 表示已选中
     */
    fun isSelected(position: Int): Boolean {
        val item = getItem(position) ?: return false
        val key = getItemKey(item)
        return if (key != null) mSelectedKeys.contains(key) else mSelectedPositions.contains(
            position
        )
    }

    /**
     * 根据 item 实体查找其在列表中的 position。
     *
     * key 模式下通过 [getItemKey] 匹配；position 模式下通过引用相等 [items.indexOf] 查找。
     *
     * @param item 目标数据实体
     * @return item 在列表中的索引，未找到时返回 null
     */
    private fun findPositionByItem(item: T): Int? {
        items.forEachIndexed { index, it ->
            val existingKey = getItemKey(it)
            if (existingKey != null) {
                // key 模式：通过业务 key 匹配
                val targetKey = getItemKey(item)
                if (targetKey != null && existingKey == targetKey) return index
            } else if (it === item) {
                // position 模式：引用相等匹配
                return index
            }
        }
        return items.indexOf(item).takeIf { it >= 0 }
    }

    /**
     * 清空所有选中状态并刷新对应 UI。
     *
     * @return 清空前已选中的项数
     */
    fun clearSelection(): Int {
        val previous = mSelectedPositions.toList()
        val clearedCount = previous.size
        if (clearedCount == 0 && mSelectedKeys.isEmpty()) return 0
        mSelectedPositions.clear()
        mSelectedKeys.clear()
        previous.forEach { notifyItemChanged(it) }
        onSelectionChanged?.invoke(emptyList())
        return clearedCount
    }

    /**
     * 全选所有可见 item。
     *
     * 仅在 [SelectionMode.MULTIPLE] 下生效，选中数量不超过 [maxSelectCount]。
     *
     * @return 实际选中的 item 数量，非 [SelectionMode.MULTIPLE] 时返回 0
     */
    fun selectAll(): Int {
        if (selectionMode != SelectionMode.MULTIPLE) return 0
        val count = minOf(items.size, maxSelectCount)
        val previous = mSelectedPositions.toSet()
        mSelectedPositions.clear()
        mSelectedKeys.clear()
        for (i in 0 until count) {
            mSelectedPositions.add(i)
            val item = getItem(i)
            item?.let {
                val key = getItemKey(it)
                if (key != null) {
                    mUsesKeyMode = true
                    mSelectedKeys.add(key)
                }
            }
        }
        // 刷新所有可能变化的项
        val all = (previous + mSelectedPositions).toSet()
        all.forEach { notifyItemChanged(it) }
        onSelectionChanged?.invoke(getSelectedItems())
        return count
    }

    /**
     * 获取当前所有选中项的数据列表。
     *
     * key 模式下通过 [getItemKey] 匹配选中项，跨数据变化时可保持选中不变（需先调用 [refreshSelection]）。
     * position 模式下直接根据记录的下标获取。
     *
     * @return 选中数据列表，无选中时返回空列表
     */
    fun getSelectedItems(): List<T> {
        return if (mUsesKeyMode) {
            // key 模式：遍历所有 item，匹配 key 集合
            items.filter { item ->
                val key = getItemKey(item)
                key != null && mSelectedKeys.contains(key)
            }
        } else {
            // position 模式：按下标获取
            mSelectedPositions.mapNotNull { getItem(it) }
        }
    }

    /**
     * 获取当前所有选中 position 的只读副本。
     *
     * @return 选中的 position 集合
     */
    fun getSelectedPositions(): Set<Int> = mSelectedPositions.toSet()

    /**
     * 获取 item 的唯一标识键，用于跨数据变化的选中追踪。
     *
     * 默认返回 null，此时使用 position 追踪选中状态（数据变化后 position 可能漂移）。
     * 子类覆写此方法返回稳定的唯一标识（如数据库主键或业务 ID）时，
     * 自动启用 key 模式，选中追踪在数据刷新后依然准确。
     *
     * **key 模式使用流程**：
     * 1. 子类覆写本方法返回 item 的唯一标识
     * 2. 正常调用 [toggleSelection] / [setSelected] 操作选中
     * 3. 当列表数据发生变化（[submitList] / [setList]）后，调用 [refreshSelection] 重建 position 映射
     *
     * @param item 数据项
     * @return 唯一标识键，null 表示使用 position 模式
     */
    open fun getItemKey(item: T): Any? = null

    /**
     * 刷新选择状态映射。
     *
     * 在 key 模式下，当列表数据发生变化（如 [submitList] 提交新数据）后调用此方法，
     * 根据 [mSelectedKeys] 重建 [mSelectedPositions]，使 UI 刷新与选中状态保持一致。
     *
     * position 模式下此方法无操作。
     */
    fun refreshSelection() {
        if (!mUsesKeyMode) return
        val previous = mSelectedPositions.toSet()
        mSelectedPositions.clear()
        items.forEachIndexed { index, item ->
            val key = getItemKey(item)
            if (key != null && mSelectedKeys.contains(key)) {
                mSelectedPositions.add(index)
            }
        }
        // 刷新所有可能变化的项
        val all = (previous + mSelectedPositions).toSet()
        all.forEach { notifyItemChanged(it) }
    }

    // ==================== ViewHolder ====================

    override fun onCreateViewHolder(
        context: Context, parent: ViewGroup, viewType: Int
    ): ViewBindingHolder<VB> {
        return ViewBindingHolder(createViewBinding(LayoutInflater.from(context), parent, viewType))

    }

    override fun onBindViewHolder(
        holder: ViewBindingHolder<VB>, position: Int, item: T?
    ) {

        val entity = item ?: return

        onBindViewHolder(holder, holder.viewBinding, position, entity)

    }


    /**
     * 绑定数据到指定 position 的 Item 视图。
     *
     * 子类在此方法中完成 UI 数据填充与事件绑定。
     * 需注意 Adapter 的复用机制可能导致同一 position 被多次回调。
     *
     * @param holder       ViewBinding 的 ViewHolder 封装
     * @param viewBinding  当前 item 对应的 ViewBinding 实例
     * @param position     数据在列表中的位置
     * @param item         当前绑定的数据实体
     */
    abstract fun onBindViewHolder(
        holder: ViewBindingHolder<VB>, viewBinding: VB, position: Int, item: T
    )

    /**
     * 根据 viewType 创建对应的 ViewBinding 实例。
     *
     * 基类在 [onCreateViewHolder] 中回调此方法，子类只需返回对应布局的绑定对象。
     *
     * @param inflater  LayoutInflater 实例
     * @param parent    ViewGroup 父容器
     * @param viewType  由 [getItemViewType] 返回的视图类型
     * @return 对应布局的 ViewBinding 实例
     */
    abstract fun createViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB


    /**
     * 拖拽交换数据时同步选择状态，使选中跟随 item 而非 position。
     *
     * 交换两个位置的选中标记，确保拖拽后 item 保持一致。
     */
    override fun dataMove(fromPosition: Int, toPosition: Int) {
        // 交换选中标记，使状态跟随 item 移动
        val fromSelected = mSelectedPositions.remove(fromPosition)
        val toSelected = mSelectedPositions.remove(toPosition)
        if (toSelected) mSelectedPositions.add(fromPosition)
        if (fromSelected) mSelectedPositions.add(toPosition)

        swap(fromPosition, toPosition) {
            notifyDataSetChanged()
        }
    }

    /**
     * 侧滑删除时同步选择状态。
     *
     * 移除目标 position 的选中标记，并将后方标记前移一位以对应位置变化。
     */
    override fun dataRemoveAt(position: Int) {
        // 移除该位置的选中标记
        mSelectedPositions.remove(position)

        // key 模式：同步移除被删 item 的 key
        val removedItem = getItem(position)
        removedItem?.let {
            val key = getItemKey(it)
            if (key != null) mSelectedKeys.remove(key)
        }

        // 将 position 后续的选中下标前移一位
        val affected = mSelectedPositions.filter { it > position }.toList()
        mSelectedPositions.removeAll(affected.toSet())
        affected.forEach { mSelectedPositions.add(it - 1) }

        removeAt(position)
        onSelectionChanged?.invoke(getSelectedItems())
    }


    override fun onItemClick(v: View, position: Int) {
        // 多选模式下，已达上限且点击的是未选中项时，阻止默认点击行为，回调上限通知
        if (selectionMode == SelectionMode.MULTIPLE && !canSelectMore() && !isSelected(position)) {
            onSelectionLimitReached?.invoke(maxSelectCount)
            return
        }
        super.onItemClick(v, position)
    }

}
