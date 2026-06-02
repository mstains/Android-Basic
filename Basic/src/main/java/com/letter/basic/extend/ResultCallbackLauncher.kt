package com.letter.basic.extend

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 核心引擎：绑定生命周期的闭包式结果启动器
 * 完美解决 Activity 销毁重建、内存回收导致的回调丢失问题
 */
class ResultCallbackLauncher<I, O>(
    private val lifecycleOwner: LifecycleOwner,
    private val contract: ActivityResultContract<I, O>,
    private val registryProvider: () -> androidx.activity.result.ActivityResultRegistry
) : DefaultLifecycleObserver {

    private var launcher: ActivityResultLauncher<I>? = null
    private var callback: ((O) -> Unit)? = null

    init {
        // 核心安全设计：在初始化时自动挂载生命周期观察者
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        // 严格在 STARTED 状态之前完成底层的核心注册
        launcher = registryProvider().register("callback_launcher_${System.identityHashCode(this)}", contract) { result ->
            callback?.invoke(result)
        }
    }

    /**
     * 发起跳转并同步绑定闭包回调
     */
    fun launch(input: I, onResult: (O) -> Unit) {
        this.callback = onResult
        launcher?.launch(input) ?: error("ResultCallbackLauncher 尚未初始化，请确保在 onCreate 之后调用 launch")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        // 界面销毁时自动注销并清空闭包，彻底杜绝内存泄漏
        launcher?.unregister()
        callback = null
        launcher = null
        lifecycleOwner.lifecycle.removeObserver(this) // 显式移除观察者
    }
}

// =====================================================================
// 基础扩展：支持外部传入任何自定义 Contract
// =====================================================================

fun <I, O> ComponentActivity.registerResultLauncher(contract: ActivityResultContract<I, O>) =
    ResultCallbackLauncher(this, contract) { this.activityResultRegistry }

fun <I, O> Fragment.registerResultLauncher(contract: ActivityResultContract<I, O>) =
    ResultCallbackLauncher(this, contract) { this.requireActivity().activityResultRegistry }


// =====================================================================
// 场景一：万能 Activity 间跳转（一变量通吃应用内所有 Activity 交互）
// =====================================================================

fun ComponentActivity.registerActivityLauncher() = registerResultLauncher(ActivityResultContracts.StartActivityForResult())
fun Fragment.registerActivityLauncher() = registerResultLauncher(ActivityResultContracts.StartActivityForResult())

/**
 * 闭包式启动任意自定义 Activity
 * @param targetClazz 目标 Activity 类（例如: DetailActivity::class.java）
 * @param intentAction 用于 Intent 的初始化配置（如 putExtra 传参）
 * @param onResult 结果返回闭包（包含 resultCode 和返回的 Intent 携带数据）
 */
inline fun <reified T : Activity> ResultCallbackLauncher<Intent, ActivityResult>.launchActivity(
    context: Context,
    crossinline intentAction: Intent.() -> Unit = {},
    crossinline onResult: (resultCode: Int, data: Intent?) -> Unit
) {
    val intent = Intent(context, T::class.java).apply(intentAction)
    this.launch(intent) { result ->
        onResult(result.resultCode, result.data)
    }
}


// =====================================================================
// 场景二：多权限与单权限申请封装
// =====================================================================

fun ComponentActivity.registerMultiplePermissionsLauncher() = registerResultLauncher(ActivityResultContracts.RequestMultiplePermissions())
fun Fragment.registerMultiplePermissionsLauncher() = registerResultLauncher(ActivityResultContracts.RequestMultiplePermissions())

/**
 * 权限快捷申请扩展
 * @param permissions 要申请的权限列表（可变参数，直接逗号隔开）
 * @param onResult 经过平铺解析的极简结果闭包
 */
fun ResultCallbackLauncher<Array<String>, Map<String, Boolean>>.launchPermissions(
    vararg permissions: String,
    onResult: (allGranted: Boolean, grantedList: List<String>, deniedList: List<String>) -> Unit
) {
    this.launch(arrayOf(*permissions)) { resultMap ->
        val grantedList = resultMap.filterValues { it }.keys.toList()
        val deniedList = resultMap.filterValues { !it }.keys.toList()
        onResult(deniedList.isEmpty(), grantedList, deniedList)
    }
}


// =====================================================================
// 场景三：多媒体与相机
// =====================================================================

fun ComponentActivity.registerTakePicturePreviewLauncher() = registerResultLauncher(ActivityResultContracts.TakePicturePreview())
fun Fragment.registerTakePicturePreviewLauncher() = registerResultLauncher(ActivityResultContracts.TakePicturePreview())

fun ComponentActivity.registerTakePictureLauncher() = registerResultLauncher(ActivityResultContracts.TakePicture())
fun Fragment.registerTakePictureLauncher() = registerResultLauncher(ActivityResultContracts.TakePicture())


// =====================================================================
// 场景四：现代相册媒体选择器 (PhotoPicker)
// =====================================================================

fun ComponentActivity.registerPhotoPickerLauncher() = registerResultLauncher(ActivityResultContracts.PickVisualMedia())
fun Fragment.registerPhotoPickerLauncher() = registerResultLauncher(ActivityResultContracts.PickVisualMedia())

fun ResultCallbackLauncher<PickVisualMediaRequest, Uri?>.launchImageOnly(onResult: (Uri?) -> Unit) {
    this.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly), onResult)
}

fun ResultCallbackLauncher<PickVisualMediaRequest, Uri?>.launchVideoOnly(onResult: (Uri?) -> Unit) {
    this.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly), onResult)
}

fun ComponentActivity.registerMultiplePhotoPickerLauncher(maxItems: Int = 9) = registerResultLauncher(ActivityResultContracts.PickMultipleVisualMedia(maxItems))
fun Fragment.registerMultiplePhotoPickerLauncher(maxItems: Int = 9,) = registerResultLauncher(ActivityResultContracts.PickMultipleVisualMedia(maxItems))

fun ResultCallbackLauncher<PickVisualMediaRequest, List<Uri>>.launchImagesAndVideos(onResult: (List<Uri>) -> Unit) {
    this.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo), onResult)
}

