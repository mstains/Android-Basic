package com.letter.basic.extend

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import java.io.Serializable

/**
 * [Context] / [Activity] / [Fragment] 上的 [Activity] / [Service] 启动与参数传递扩展。
 *
 * 通过 `Pair<String, Any?>` 变长参数构造 Intent extras，屏蔽显式 `putExtra` 样板代码。
 * 兼容 Activity 间跳转、Activity → Service 启动 / 停止场景。
 */

/**
 * 启动 [T] 类型的 Activity，可携带 extras。
 *
 * @param T 目标 Activity 类型，由 reified 关键字在编译期获取 Class
 * @receiver 启动方 Context
 * @param params Intent extras 键值对，键为 String，值可为基本类型 / String / Parcelable / Serializable / Bundle / 数组
 */
@Deprecated("废弃，请使用 ResultCallbackLauncher")
inline fun <reified T : Activity> Context.baseStartActivity(vararg params: Pair<String, Any?>) {
    internalStartActivity(this, T::class.java, params)
}

/**
 * 启动 [T] 类型的 Activity（Activity 接收者重载）。
 *
 * @param T 目标 Activity 类型
 * @receiver 启动方 Activity
 * @param params Intent extras 键值对
 */
@Deprecated("废弃，请使用 ResultCallbackLauncher")
inline fun <reified T : Activity> Activity.baseStartActivity(vararg params: Pair<String, Any?>) {
    internalStartActivity(this, T::class.java, params)
}

/**
 * 启动 [T] 类型的 Activity（Fragment 接收者重载）。
 *
 * @param T 目标 Activity 类型
 * @receiver 启动方 Fragment
 * @param params Intent extras 键值对
 */
@Deprecated("废弃，请使用 ResultCallbackLauncher")
inline fun <reified T : Activity> Fragment.baseStartActivity(vararg params: Pair<String, Any?>) {
    internalStartActivity(requireContext(), T::class.java, params)
}

/**
 * 启动 [T] 类型的 Activity 并通过 [requestCode] 接收返回结果（已废弃）。
 *
 * @param T 目标 Activity 类型
 * @receiver 启动方 Activity
 * @param requestCode 请求码（用于 onActivityResult 识别）
 * @param params Intent extras 键值对
 */
@Deprecated("废弃，请使用 ResultCallbackLauncher")
inline fun <reified T : Activity> Activity.baseStartActivityForResult(
    requestCode: Int, vararg params: Pair<String, Any?>
) {
    startActivityForResult(createIntent(this, T::class.java, params), requestCode)
}

/**
 * 启动 [T] 类型的 Activity 并通过 [requestCode] 接收返回结果（Fragment 重载，已废弃）。
 *
 * @param T 目标 Activity 类型
 * @receiver 启动方 Fragment
 * @param requestCode 请求码
 * @param params Intent extras 键值对
 */
@Deprecated("废弃，请使用 ResultCallbackLauncher")
inline fun <reified T : Activity> Fragment.baseStartActivityForResult(
    requestCode: Int, vararg params: Pair<String, Any?>
) {
    startActivityForResult(createIntent(requireContext(), T::class.java, params), requestCode)
}

/**
 * 启动 [T] 类型的 Service。
 *
 * @param T 目标 Service 类型
 * @receiver 启动方 Context
 * @param params Intent extras 键值对
 */
inline fun <reified T : Service> Context.startService(vararg params: Pair<String, Any?>) =
    internalStartService(this, T::class.java, params)


/**
 * 启动 [T] 类型的 Service（Fragment 接收者重载）。
 *
 * @param T 目标 Service 类型
 * @receiver 启动方 Fragment
 * @param params Intent extras 键值对
 */
inline fun <reified T : Service> Fragment.startService(vararg params: Pair<String, Any?>) =
    internalStartService(activity!!, T::class.java, params)


/**
 * 停止 [T] 类型的 Service。
 *
 * @param T 目标 Service 类型
 * @receiver 启动方 Context
 * @param params Intent extras 键值对
 */
inline fun <reified T : Service> Context.stopService(vararg params: Pair<String, Any?>) =
    internalStopService(this, T::class.java, params)


/**
 * 内部方法：通过 [ctx] 启动 [activity] 类型的 Activity，并填充 [params] extras。
 *
 * @param ctx 启动方 Context
 * @param activity 目标 Activity Class
 * @param params Intent extras 键值对
 */
fun internalStartActivity(
    ctx: Context, activity: Class<out Activity>, params: Array<out Pair<String, Any?>>
) {
    ctx.startActivity(createIntent(ctx, activity, params))
}


/**
 * 内部方法：构造携带 [params] extras 的 [Intent] 实例。
 *
 * @param ctx 启动方 Context
 * @param clazz 目标组件 Class
 * @param params Intent extras 键值对
 * @return 构造完成的 [Intent]
 */
fun <T> createIntent(
    ctx: Context, clazz: Class<out T>, params: Array<out Pair<String, Any?>>
): Intent {
    val intent = Intent(ctx, clazz)
    if (params.isNotEmpty()) fillIntentArguments(intent, params)
    return intent
}


/**
 * 把 [params] 中的键值对逐个填入 [intent] 的 extras。
 *
 * 支持 Int / Long / CharSequence / String / Float / Double / Char / Short / Boolean /
 * Serializable / Bundle / Parcelable / 各类数组；不支持的类型抛出 [RuntimeException]。
 *
 * @param intent 目标 Intent
 * @param params 键值对
 */
private fun fillIntentArguments(intent: Intent, params: Array<out Pair<String, Any?>>) {
    params.forEach {
        val value = it.second
        when (value) {
            null -> intent.putExtra(it.first, null as Serializable?)
            is Int -> intent.putExtra(it.first, value)
            is Long -> intent.putExtra(it.first, value)
            is CharSequence -> intent.putExtra(it.first, value)
            is String -> intent.putExtra(it.first, value)
            is Float -> intent.putExtra(it.first, value)
            is Double -> intent.putExtra(it.first, value)
            is Char -> intent.putExtra(it.first, value)
            is Short -> intent.putExtra(it.first, value)
            is Boolean -> intent.putExtra(it.first, value)
            is Serializable -> intent.putExtra(it.first, value)
            is Bundle -> intent.putExtra(it.first, value)
            is Parcelable -> intent.putExtra(it.first, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
                else -> throw RuntimeException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
            }

            is IntArray -> intent.putExtra(it.first, value)
            is LongArray -> intent.putExtra(it.first, value)
            is FloatArray -> intent.putExtra(it.first, value)
            is DoubleArray -> intent.putExtra(it.first, value)
            is CharArray -> intent.putExtra(it.first, value)
            is ShortArray -> intent.putExtra(it.first, value)
            is BooleanArray -> intent.putExtra(it.first, value)
            else -> throw RuntimeException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
        }
        return@forEach
    }
}


/**
 * 内部方法：构造 Service 启动 Intent 并调用 [Context.startService]。
 *
 * @param ctx 启动方 Context
 * @param service 目标 Service Class
 * @param params Intent extras 键值对
 * @return Service 组件名（由系统返回）
 */
fun internalStartService(
    ctx: Context, service: Class<out Service>, params: Array<out Pair<String, Any?>>
): ComponentName? = ctx.startService(createIntent(ctx, service, params))


/**
 * 内部方法：构造 Service 停止 Intent 并调用 [Context.stopService]。
 *
 * @param ctx 启动方 Context
 * @param service 目标 Service Class
 * @param params Intent extras 键值对
 * @return true 表示存在对应 Service 并已停止
 */
fun internalStopService(
    ctx: Context, service: Class<out Service>, params: Array<out Pair<String, Any?>>
): Boolean = ctx.stopService(createIntent(ctx, service, params))

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