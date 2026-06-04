package com.letter.basic.extend


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Parcelable
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import java.io.Serializable


/**
 * 通用 [AppCompatActivity] / [Fragment] / [FragmentManager] 扩展方法集合。
 *
 * 涵盖 Fragment 事务软提交、软键盘控制、状态栏配置、Intent extra 安全读取等场景。
 */

/**
 * 通过 [FragmentTransaction.replace] 替换容器中的 Fragment，并以 `commit()` 方式提交。
 *
 * 状态保存敏感场景请改用 [replaceFragmentInActivityNotState]。
 *
 * @receiver 调用方 Activity
 * @param fragment 待展示的目标 Fragment
 * @param frameId Fragment 容器 ID
 */
fun AppCompatActivity.replaceFragmentInActivity(fragment: Fragment, @IdRes frameId: Int) {
    supportFragmentManager.transact {
        replace(frameId, fragment)
    }
}

/**
 * 通过 [FragmentTransaction.replace] 替换容器中的 Fragment，并以 `commitAllowingStateLoss()` 方式提交。
 *
 * 适用于 Activity 状态已被保存、不希望触发 `IllegalStateException` 的场景。
 *
 * @receiver 调用方 Activity
 * @param fragment 待展示的目标 Fragment
 * @param frameId Fragment 容器 ID
 */
fun AppCompatActivity.replaceFragmentInActivityNotState(fragment: Fragment, @IdRes frameId: Int) {
    supportFragmentManager.transactAllowingStateLoss {
        replace(frameId, fragment)
    }
}

/**
 * 向 Activity 默认 Fragment 容器（无 frameId，使用 Fragment tag）添加 Fragment。
 *
 * @receiver 调用方 Activity
 * @param fragment 待添加的 Fragment
 */
fun AppCompatActivity.addFragmentToActivity(fragment: Fragment) {
    supportFragmentManager.transact {
        add(fragment, fragment.javaClass.simpleName)
    }
}

/**
 * 向 Activity 默认 Fragment 容器添加 Fragment。
 *
 * @receiver 调用方 Activity
 * @param fragment 待添加的 Fragment
 * @param tag Fragment 在 FragmentManager 中的 tag
 */
fun AppCompatActivity.addFragmentToActivity(fragment: Fragment, tag: String) {
    supportFragmentManager.transact {
        add(fragment, tag)
    }
}


/**
 * 显示已添加但处于 hide 状态的 Fragment。
 *
 * @receiver 调用方 Activity
 * @param fragment 待显示的 Fragment
 */
fun AppCompatActivity.showFragment(fragment: Fragment) {
    supportFragmentManager.transact {
        show(fragment)
    }
}

/**
 * 隐藏 Fragment（不销毁视图）。
 *
 * @receiver 调用方 Activity
 * @param fragment 待隐藏的 Fragment
 */
fun AppCompatActivity.hideFragment(fragment: Fragment) {
    supportFragmentManager.transact {
        hide(fragment)
    }
}

/**
 * 通过 `commitAllowingStateLoss()` 向指定容器添加 Fragment，tag 为类名。
 *
 * @receiver 调用方 Activity
 * @param fragment 待添加的 Fragment
 * @param frameId 容器 ID
 */
fun AppCompatActivity.addFragmentToActivityNotState(fragment: Fragment, @IdRes frameId: Int) {
    supportFragmentManager.transactAllowingStateLoss {
        add(frameId, fragment, fragment.javaClass.simpleName)
    }
}

/**
 * `commitAllowingStateLoss()` 方式显示 Fragment。
 *
 * @receiver 调用方 Activity
 * @param fragment 待显示的 Fragment
 */
fun AppCompatActivity.showFragmentToActivityNotState(fragment: Fragment) {
    supportFragmentManager.transactAllowingStateLoss {
        show(fragment)
    }
}

/**
 * `commitAllowingStateLoss()` 方式隐藏 Fragment。
 *
 * @receiver 调用方 Activity
 * @param fragment 待隐藏的 Fragment
 */
fun AppCompatActivity.hideFragmentToActivityNotState(fragment: Fragment) {
    supportFragmentManager.transactAllowingStateLoss {
        hide(fragment)
    }
}

/**
 * 通过 `commitAllowingStateLoss()` 方式向容器添加 Fragment。
 *
 * @receiver 调用方 Activity
 * @param fragment 待添加的 Fragment
 * @param tag Fragment 在 FragmentManager 中的 tag
 */
fun AppCompatActivity.addFragmentToActivityAllowingStateLoss(fragment: Fragment, tag: String) {
    supportFragmentManager.transactAllowingStateLoss {
        add(fragment, tag)
    }
}

/**
 * `commitAllowingStateLoss()` 方式隐藏 Fragment。
 *
 * @receiver 调用方 Activity
 * @param fragment 待隐藏的 Fragment
 */
fun AppCompatActivity.hideFragmentToActivityAllowingStateLoss(fragment: Fragment) {
    supportFragmentManager.transactAllowingStateLoss {
        hide(fragment)
    }
}


/**
 * 配置 ActionBar。
 *
 * 把指定 ID 的 View 当作 Toolbar 注入到 Activity，并暴露 ActionBar 供调用方配置。
 *
 * @receiver 调用方 Activity
 * @param toolbarId Toolbar 视图资源 ID
 * @param action 在 [ActionBar] 上下文执行的配置 Lambda
 */
fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: ActionBar.() -> Unit) {
    setSupportActionBar(findViewById(toolbarId))
    supportActionBar?.run {
        action()
    }
}

/**
 * 打开软键盘并把焦点定位到 [editText]。
 *
 * 同时调用 [InputMethodManager.showSoftInput] 与 [InputMethodManager.toggleSoftInput]
 * 是为了兼容部分定制 ROM（如 MIUI / EMUI）的隐藏式弹起行为。
 *
 * @receiver 调用方 Activity
 * @param editText 目标 EditText
 */
fun AppCompatActivity.openKeyBoard(editText: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, InputMethodManager.RESULT_SHOWN)
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

/**
 * 关闭与 [editText] 关联的软键盘。
 *
 * @receiver 调用方 Activity
 * @param editText 当前持有输入焦点的 EditText
 */
fun AppCompatActivity.closeKeyBoard(editText: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(editText.windowToken, 0)
}

/**
 * 设置状态栏全透明（已废弃，建议改用 edge-to-edge 方案）。
 *
 * Android 5.0+：使用 `SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN` 配合 `FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS`
 * 实现沉浸式；5.0 以下回退到 `FLAG_TRANSLUCENT_STATUS`。
 *
 * @receiver 调用方 Activity
 */
@Deprecated("使用 edge-to-edge 方案")
fun AppCompatActivity.setStatusBarFullTransparent() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    } else {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}

/**
 * 启动 [FragmentTransaction]，执行 [action] 后以 `commit()` 方式提交。
 *
 * @receiver 调用方 FragmentManager
 * @param action 在 [FragmentTransaction] 上下文中执行的配置 Lambda
 */
inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commit()
}

/**
 * 一键隐藏 Activity 中所有 Fragment。
 *
 * @receiver 调用方 Activity
 */
fun AppCompatActivity.hideAllFragment() {
    val fragments = supportFragmentManager.fragments
    if (fragments.isNotEmpty()) {
        fragments.forEach {
            supportFragmentManager.transactAllowingStateLoss {
                hide(it)
            }
        }
    }
}

/**
 * 重启当前应用。
 *
 * 结束当前进程并通过 `Intent.makeRestartActivityTask` 重新拉起启动 Activity。
 *
 * @receiver 调用方 Activity
 */
fun Activity.restartApp() {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    val componentName = intent!!.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}


/**
 * 将 dp 值转换为 px（像素）。
 *
 * 公式：`px = dp * density + 0.5f`，末尾 +0.5f 用于四舍五入。
 *
 * @receiver 调用方 Context
 * @param dpValue 待转换的 dp 值
 * @return 转换后的像素值
 */
fun Context.dip2px(dpValue: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

/**
 * 安全读取 Intent 中的 String 类型的 extra。
 *
 * @receiver 调用方 Activity
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Activity.getStringExtra(keyName: String, defaultValue: String = ""): String {
    return intent.getStringExtra(keyName) ?: defaultValue
}

/**
 * 安全读取 Intent 中的 String 列表类型的 extra。
 *
 * @receiver 调用方 Activity
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认列表
 * @return extra 值或默认列表
 */
fun Activity.getStringListExtra(
    keyName: String, defaultValue: MutableList<String> = mutableListOf()
): MutableList<String> {
    return intent.getStringArrayListExtra(keyName) ?: defaultValue
}


/**
 * 读取 Intent 中的 Boolean 类型 extra。
 *
 * @receiver 调用方 Activity
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Activity.getBooleanExtra(keyName: String, defaultValue: Boolean = false): Boolean {
    return intent.getBooleanExtra(keyName, defaultValue)
}


/**
 * 读取 Intent 中的 Double 类型 extra。
 *
 * @receiver 调用方 Activity
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Activity.getDoubleExtra(keyName: String, defaultValue: Double = 0.0): Double =
    intent.getDoubleExtra(keyName, defaultValue)


/**
 * 读取 Intent 中的 Int 类型 extra。
 *
 * @receiver 调用方 Activity
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Activity.getIntExtra(keyName: String, defaultValue: Int = 0): Int =
    intent.getIntExtra(keyName, defaultValue)


/**
 * 读取 Intent 中的 Long 类型 extra。
 *
 * @receiver 调用方 Activity
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Activity.getLongExtra(keyName: String, defaultValue: Long = 0L): Long =
    intent.getLongExtra(keyName, defaultValue)

/**
 * 读取 Intent 中的 Float 类型 extra。
 *
 * @receiver 调用方 Activity
 * @param keyName extra 键名
 * @param defaultValue 未找到时返回的默认值
 * @return extra 值或默认值
 */
fun Activity.getFloatExtra(keyName: String, defaultValue: Float = 0f): Float {
    return intent.getFloatExtra(keyName, defaultValue)
}


/**
 * 读取 Intent 中的 Serializable 类型 extra。
 *
 * @receiver 调用方 Activity
 * @param keyName extra 键名
 * @return 转换成功返回 `T?`；未找到或类型不匹配返回 null
 */
fun <T> Activity.getSerializableExtra(keyName: String): T? {
    return try {
        intent.getSerializableExtra(keyName) as T?
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 读取 Intent 中的 Parcelable 类型 extra。
 *
 * @receiver 调用方 Activity
 * @param keyName extra 键名
 * @return 转换成功返回 `T?`；未找到或类型不匹配返回 null
 */
fun <T : Parcelable> Activity.getParcelableExtra(keyName: String): T? =
    intent.getParcelableExtra<T>(keyName)
