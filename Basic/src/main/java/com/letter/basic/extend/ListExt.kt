package com.letter.basic.extend


/**
 * 携带"原下标"和"新下标"以及原始元素的包装数据类。
 *
 * 用于 [filterWithIndex] / [mapWithIndex] / [forEachWithIndex] / [findWithIndex] 等扩展方法的返回值，
 * 在集合经过过滤、映射等操作后仍能追踪元素在原集合中的位置。
 *
 * @param T 元素类型
 * @property originalIndex 元素在源 [List] 中的下标（从 0 开始）
 * @property newIndex 元素在操作结果中的下标（从 0 开始）
 * @property item 元素本身
 */
data class IndexedItem<T>(
    val originalIndex: Int,
    val newIndex: Int,
    val item: T
)

/**
 * 过滤集合并返回带"原下标"与"新下标"的元素列表。
 *
 * 与 [kotlin.collections.filterIndexed] 相比，本方法同时返回原下标与过滤后下标，
 * 方便业务层在过滤后仍能定位到原集合中的位置。底层基于 [forEachIndexed] 顺序遍历，
 * `originalIndex` 由遍历顺序保证，`newIndex` 由命中顺序自增（从 0 开始）。
 *
 * @param predicate 元素过滤条件
 * @return 命中谓词的元素组成的 [IndexedItem] 列表；接收者为 null 时返回空列表
 */
fun <T> List<T>?.filterWithIndex(predicate: (T) -> Boolean): List<IndexedItem<T>> {
    if (this == null) return emptyList()
    val result = mutableListOf<IndexedItem<T>>()
    var newIndex = 0
    forEachIndexed { originalIndex, item ->
        if (predicate(item)) {
            result.add(IndexedItem(originalIndex, newIndex++, item))
        }
    }
    return result
}

/**
 * 映射集合并返回带"原下标"与"新下标"的元素列表。
 *
 * 与 [kotlin.collections.mapIndexed] 相比，将每个映射结果连同下标一起包装在 [IndexedItem] 中，
 * 以便与 [filterWithIndex] / [findWithIndex] 等接口保持一致的调用形态。
 *
 * 注意：map 保留顺序与长度，因此 [IndexedItem.originalIndex] 与 [IndexedItem.newIndex] 始终相等。
 * 若仅需原下标，建议直接使用 Kotlin 标准库 [kotlin.collections.mapIndexed]。
 *
 * @param transform 元素映射函数
 * @return 映射后元素组成的 [IndexedItem] 列表；接收者为 null 时返回空列表
 */
fun <T, R> List<T>?.mapWithIndex(transform: (T) -> R): List<IndexedItem<R>> {
    if (this == null) return emptyList()
    val result = mutableListOf<IndexedItem<R>>()
    forEachIndexed { originalIndex, item ->
        result.add(IndexedItem(originalIndex, originalIndex, transform(item)))
    }
    return result
}

/**
 * 按顺序遍历集合，并对每个元素执行 [action]，传入 [IndexedItem] 同时暴露原下标与遍历下标。
 *
 * 与 Kotlin 标准库 [kotlin.collections.forEachIndexed] 相比，多一层 [IndexedItem] 包装以与
 * [filterWithIndex] 等接口的回调签名保持一致。注意：forEach 不改变集合顺序与长度，
 * 因此 [IndexedItem.originalIndex] 与 [IndexedItem.newIndex] 始终相等。
 *
 * @param action 对每个元素执行的操作
 */
fun <T> List<T>?.forEachWithIndex(action: (IndexedItem<T>) -> Unit) {
    if (this == null) return
    forEachIndexed { originalIndex, item ->
        action(IndexedItem(originalIndex, originalIndex, item))
    }
}

/**
 * 查找第一个命中 [predicate] 的元素，并返回其原下标、查找下标与元素本身。
 *
 * 与 Kotlin 标准库 [kotlin.collections.find] 相比，多携带下标信息。注意：find 不改变集合，
 * 因此 [IndexedItem.originalIndex] 与 [IndexedItem.newIndex] 始终相等。
 *
 * @param predicate 元素命中条件
 * @return 命中的 [IndexedItem]，未找到或接收者为 null 时返回 null
 */
fun <T> List<T>?.findWithIndex(predicate: (T) -> Boolean): IndexedItem<T>? {
    if (this == null) return null
    for (originalIndex in indices) {
        val item = this[originalIndex]
        if (predicate(item)) {
            return IndexedItem(originalIndex, originalIndex, item)
        }
    }
    return null
}
