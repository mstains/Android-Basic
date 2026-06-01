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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import java.io.Serializable


/**
 * `fragment`添加到了容器中 其Id为`frameId`.
 * 操作由fragmentManager执行
 */
fun AppCompatActivity.replaceFragmentInActivity(fragment: Fragment, @IdRes frameId: Int) {
    supportFragmentManager.transact {
        replace(frameId, fragment)
    }
}

fun AppCompatActivity.replaceFragmentInActivityNotState(fragment: Fragment, @IdRes frameId: Int) {
    supportFragmentManager.transactAllowingStateLoss {
        replace(frameId, fragment)
    }
}

/**
 * `fragment`添加到了容器中 其tag为`tag`.
 * 操作由fragmentManager执行
 */
fun AppCompatActivity.addFragmentToActivity(fragment: Fragment) {
    supportFragmentManager.transact {
        add(fragment, fragment.javaClass.simpleName)
    }
}

fun AppCompatActivity.addFragmentToActivity(fragment: Fragment, tag: String) {
    supportFragmentManager.transact {
        add(fragment, tag)
    }
}


fun AppCompatActivity.showFragment(fragment: Fragment) {
    supportFragmentManager.transact {
        show(fragment)
    }
}

fun AppCompatActivity.hideFragment(fragment: Fragment) {
    supportFragmentManager.transact {
        hide(fragment)
    }
}

fun AppCompatActivity.addFragmentToActivityNotState(fragment: Fragment, @IdRes frameId: Int) {
    supportFragmentManager.transactAllowingStateLoss {
        add(frameId, fragment, fragment.javaClass.simpleName)
    }
}

fun AppCompatActivity.showFragmentToActivityNotState(fragment: Fragment) {
    supportFragmentManager.transactAllowingStateLoss {
        show(fragment)
    }
}

fun AppCompatActivity.hideFragmentToActivityNotState(fragment: Fragment) {
    supportFragmentManager.transactAllowingStateLoss {
        hide(fragment)
    }
}

/**
 * `fragment`添加到了容器中 其tag为`tag`. 允许其状态丢失
 * 操作由fragmentManager执行
 */
fun AppCompatActivity.addFragmentToActivityAllowingStateLoss(fragment: Fragment, tag: String) {
    supportFragmentManager.transactAllowingStateLoss {
        add(fragment, tag)
    }
}

fun AppCompatActivity.hideFragmentToActivityAllowingStateLoss(fragment: Fragment) {
    supportFragmentManager.transactAllowingStateLoss {
        hide(fragment)
    }
}


/**
 * 配置ActionBar
 */
fun AppCompatActivity.setupActionBar(@IdRes toolbarId: Int, action: ActionBar.() -> Unit) {
    setSupportActionBar(findViewById(toolbarId))
    supportActionBar?.run {
        action()
    }
}

/**
 * 打开软键盘
 */
fun AppCompatActivity.openKeyBoard(editText: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, InputMethodManager.RESULT_SHOWN)
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

/**
 * 关闭软键盘
 */
fun AppCompatActivity.closeKeyBoard(editText: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(editText.windowToken, 0)
}

/**
 * 设置状态栏透明
 */
@Deprecated("使用egdetoedge()")
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
        //虚拟键盘也透明
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }
}

/**
 * 启动FragmentTransaction，然后commit
 */
inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commit()
}

/**
 * 隐藏所有Fragment
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
 * 重启app
 */
fun Activity.restartApp() {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    val componentName = intent!!.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    startActivity(mainIntent)
    Runtime.getRuntime().exit(0)

}


/**
 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
 */
fun Context.dip2px(dpValue: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

/**
 * 封装获取intent中string值
 * */
fun Activity.getStringExtra(keyName: String, defaultValue: String = ""): String {

    return intent.getStringExtra(keyName) ?: defaultValue

}

/**
 * 获取intent中List<String>值
 * */
fun Activity.getStringListExtra(
    keyName: String, defaultValue: MutableList<String> = mutableListOf()
): MutableList<String> {

    return intent.getStringArrayListExtra(keyName) ?: defaultValue

}


/**
 * 封装获取intent中boolean值
 * */
fun Activity.getBooleanExtra(keyName: String, defaultValue: Boolean = false): Boolean {

    return intent.getBooleanExtra(keyName, defaultValue)
}


/**
 * 封装获取intent中double值
 * */
fun Activity.getDoubleExtra(keyName: String, defaultValue: Double = 0.0): Double =
    intent.getDoubleExtra(keyName, defaultValue)


/**
 * 封装获取intent中int值
 * */
fun Activity.getIntExtra(keyName: String, defaultValue: Int = 0): Int =
    intent.getIntExtra(keyName, defaultValue)


/**
 * 封装获取intent中Long值
 * */
fun Activity.getLongExtra(keyName: String, defaultValue: Long = 0L): Long =
    intent.getLongExtra(keyName, defaultValue)

/**
 * 封装获取intent中Float值
 * */
fun Activity.getFloatExtra(keyName: String, defaultValue: Float = 0f): Float {

    return intent.getFloatExtra(keyName, defaultValue)
}


/**
 * 获取Serializable
 * */
fun <T> Activity.getSerializableExtra(keyName: String): T? {

    return try {
        intent.getSerializableExtra(keyName) as T?
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

}

/**
 * 获取ParcelableExtra
 * */
fun <T : Parcelable> Activity.getParcelableExtra(keyName: String): T? =
    intent.getParcelableExtra<T>(keyName)

















