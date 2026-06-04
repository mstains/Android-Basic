package com.letter.basic.extend

import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


/**
 * [Fragment] / [FragmentManager] / [FragmentTransaction] 扩展方法集合。
 *
 * 主要覆盖 ChildFragmentManager 场景的 replace / add / show / hide 操作，
 * 以及从 arguments / intent 读取各类参数的便捷方法。
 */

/**
 * 启动 [FragmentTransaction]，执行 [action] 后以 `commitAllowingStateLoss()` 方式提交。
 *
 * @receiver 调用方 FragmentManager
 * @param action 在 [FragmentTransaction] 上下文中执行的配置 Lambda
 */
inline fun FragmentManager.transactAllowingStateLoss(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commitAllowingStateLoss()
}


/**
 * 在 ChildFragmentManager 中通过 [FragmentTransaction.replace] 替换子 Fragment。
 *
 * @receiver 调用方父 Fragment
 * @param child 子 Fragment 实例
 * @param frameId 子 Fragment 容器 ID
 */
fun Fragment.replaceChildInFragment(child: Fragment, @IdRes frameId: Int) {
    childFragmentManager.transact {
        replace(frameId, child, child.javaClass.simpleName)
    }
}

/**
 * 在 ChildFragmentManager 中以 `commitAllowingStateLoss()` 方式替换子 Fragment。
 *
 * @receiver 调用方父 Fragment
 * @param child 子 Fragment 实例
 * @param frameId 子 Fragment 容器 ID
 * @param tag 子 Fragment 的 tag
 */
fun Fragment.replaceChildNotState(child: Fragment, @IdRes frameId: Int, tag: String) {
    childFragmentManager.transactAllowingStateLoss {
        replace(frameId, child, tag)
    }
}


/**
 * 在 ChildFragmentManager 中以 `commitAllowingStateLoss()` 方式替换子 Fragment，tag 为类名。
 *
 * @receiver 调用方父 Fragment
 * @param child 子 Fragment 实例
 * @param frameId 子 Fragment 容器 ID
 */
fun Fragment.replaceChildNotState(child: Fragment, @IdRes frameId: Int) {
    childFragmentManager.transactAllowingStateLoss {
        replace(frameId, child, child.javaClass.simpleName)
    }
}

/**
 * 在 ChildFragmentManager 中替换子 Fragment，并指定 tag。
 *
 * @receiver 调用方父 Fragment
 * @param child 子 Fragment 实例
 * @param frameId 子 Fragment 容器 ID
 * @param tag 子 Fragment 的 tag
 */
fun Fragment.replaceChildInFragment(child: Fragment, @IdRes frameId: Int, tag: String) {
    childFragmentManager.transact {
        replace(frameId, child, tag)
    }
}


/**
 * 在 ChildFragmentManager 中以 `commitAllowingStateLoss()` 方式添加子 Fragment，tag 为类名。
 *
 * @receiver 调用方父 Fragment
 * @param child 子 Fragment 实例
 */
fun Fragment.addChildToFragment(child: Fragment) {
    childFragmentManager.transactAllowingStateLoss {
        add(child, child.javaClass.simpleName)
    }
}

/**
 * 在 ChildFragmentManager 中以 `commitAllowingStateLoss()` 方式添加子 Fragment 到指定容器。
 *
 * @receiver 调用方父 Fragment
 * @param child 子 Fragment 实例
 * @param frameId 容器 ID
 */
fun Fragment.addChildToFragmentNotState(child: Fragment, @IdRes frameId: Int) {
    childFragmentManager.transactAllowingStateLoss {
        add(frameId, child, child.javaClass.simpleName)
    }
}

/**
 * 在 ChildFragmentManager 中添加子 Fragment 到指定容器，tag 为类名。
 *
 * @receiver 调用方父 Fragment
 * @param child 子 Fragment 实例
 * @param frameId 容器 ID
 */
fun Fragment.addChildToFragment(child: Fragment, @IdRes frameId: Int) {
    childFragmentManager.transact {
        add(frameId, child, child.javaClass.simpleName)
    }
}

/**
 * 在 ChildFragmentManager 中添加子 Fragment 到指定容器并指定 tag。
 *
 * @receiver 调用方父 Fragment
 * @param child 子 Fragment 实例
 * @param frameId 容器 ID
 * @param tag 子 Fragment 的 tag
 */
fun Fragment.addChildToFragment(child: Fragment, @IdRes frameId: Int, tag: String) {
    childFragmentManager.transact {
        add(frameId, child, tag)
    }
}

/**
 * 在 ChildFragmentManager 中显示指定子 Fragment。
 *
 * @receiver 调用方父 Fragment
 * @param child 待显示的子 Fragment
 */
fun Fragment.showChildInFragment(child: Fragment) {
    childFragmentManager.transact {
        show(child)
    }
}

/**
 * 在 ChildFragmentManager 中隐藏指定子 Fragment。
 *
 * @receiver 调用方父 Fragment
 * @param child 待隐藏的子 Fragment
 */
fun Fragment.hideChildInFragment(child: Fragment) {
    childFragmentManager.transact {
        hide(child)
    }
}

/**
 * 一键隐藏当前 Fragment 下所有子 Fragment。
 *
 * @receiver 调用方父 Fragment
 */
fun Fragment.hideAllChild() {
    val childs = childFragmentManager.fragments
    if (childs.size > 0) {
        val transaction = childFragmentManager.beginTransaction()
        childs.forEach {
            transaction.hide(it)
        }
        transaction.commit()
    }
}


/**
 * 安全读取宿主 Activity Intent 中的 String extra。
 *
 * @receiver 调用方 Fragment
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Fragment.getStringExtra(keyName: String, defaultValue: String = ""): String {
    return requireActivity().intent.getStringExtra(keyName) ?: defaultValue
}

/**
 * 读取 Fragment arguments 中的 String 值。
 *
 * @receiver 调用方 Fragment
 * @param keyName 键名
 * @param defaultValue 未找到时返回的默认值
 * @return 找到返回 String，否则返回默认值
 */
fun Fragment.getArgumentsStringExtra(keyName: String, defaultValue: String = ""): String {
    return arguments?.getString(keyName) ?: defaultValue
}

/**
 * 读取 Fragment arguments 中的 Int 值。
 *
 * @receiver 调用方 Fragment
 * @param keyName 键名
 * @param defaultValue 未找到时返回的默认值
 * @return 找到返回 Int，否则返回默认值
 */
fun Fragment.getArgumentsIntExtra(keyName: String, defaultValue: Int = 0): Int {
    return arguments?.getInt(keyName) ?: defaultValue
}

/**
 * 读取 Fragment arguments 中的 Long 值。
 *
 * @receiver 调用方 Fragment
 * @param keyName 键名
 * @param defaultValue 未找到时返回的默认值
 * @return 找到返回 Long，否则返回默认值
 */
fun Fragment.getArgumentsLongExtra(keyName: String, defaultValue: Long = 0L): Long {
    return arguments?.getLong(keyName) ?: defaultValue
}

/**
 * 读取 Fragment arguments 中的 Boolean 值。
 *
 * @receiver 调用方 Fragment
 * @param keyName 键名
 * @param defaultValue 未找到时返回的默认值
 * @return 找到返回 Boolean，否则返回默认值
 */
fun Fragment.getArgumentsBooleanExtra(keyName: String, defaultValue: Boolean = false): Boolean {
    return arguments?.getBoolean(keyName, defaultValue) ?: defaultValue
}

/**
 * 读取 Fragment arguments 中的 Serializable 值。
 *
 * @receiver 调用方 Fragment
 * @param keyName 键名
 * @return 转换成功返回 `T?`；未找到或类型不匹配返回 null
 */
fun <T> Fragment.getArgumentsSerializableExtra(keyName: String): T? {
    return try {
        arguments?.getSerializable(keyName) as T?
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


/**
 * 读取宿主 Activity Intent 中的 Boolean extra。
 *
 * @receiver 调用方 Fragment
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Fragment.getBooleanExtra(keyName: String, defaultValue: Boolean = false): Boolean =
    requireActivity().intent.getBooleanExtra(keyName, defaultValue)

/**
 * 读取宿主 Activity Intent 中的 Double extra。
 *
 * @receiver 调用方 Fragment
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Fragment.getDoubleExtra(keyName: String, defaultValue: Double = 0.0): Double =
    requireActivity().intent.getDoubleExtra(keyName, defaultValue)

/**
 * 读取宿主 Activity Intent 中的 Int extra。
 *
 * @receiver 调用方 Fragment
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Fragment.getIntExtra(keyName: String, defaultValue: Int = 0): Int =
    requireActivity().intent.getIntExtra(keyName, defaultValue)


/**
 * 读取宿主 Activity Intent 中的 Long extra。
 *
 * @receiver 调用方 Fragment
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Fragment.getLongExtra(keyName: String, defaultValue: Long = 0L): Long =
    requireActivity().intent.getLongExtra(keyName, defaultValue)


/**
 * 读取宿主 Activity Intent 中的 Float extra。
 *
 * @receiver 调用方 Fragment
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Fragment.getFloatExtra(keyName: String, defaultValue: Float = 0f): Float =
    requireActivity().intent.getFloatExtra(keyName, defaultValue)


/**
 * 通过宿主 Activity 读取 Serializable 类型 extra。
 *
 * @receiver 调用方 Fragment
 * @param keyName extra 键名
 * @return 转换成功返回 `T?`；未找到或类型不匹配返回 null
 */
fun <T> Fragment.getSerializableExtra(keyName: String): T? {
    return try {
        requireActivity().getSerializableExtra<T>(keyName)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 通过宿主 Activity 读取 Parcelable 类型 extra。
 *
 * @receiver 调用方 Fragment
 * @param keyName extra 键名
 * @return 转换成功返回 `T?`；未找到或类型不匹配返回 null
 */
fun <T : Parcelable> Fragment.getParcelableExtra(keyName: String): T? =
    requireActivity().getParcelableExtra<T>(keyName)
