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
 * 生命周期安全的结果启动器。
 *
 * 内部使用 [DefaultLifecycleObserver] 监听 [lifecycleOwner] 的 onCreate / onDestroy：
 * - onCreate：通过 [registryProvider] 获取的 [androidx.activity.result.ActivityResultRegistry]
 *   注册一个 key 唯一的 [ActivityResultLauncher]，避免多实例冲突。
 * - onDestroy：注销 launcher、清空回调、移除观察者，彻底释放引用以防内存泄漏。
 *
 * 调用方应使用顶层 [registerResultLauncher] 工厂方法创建实例，
 * 然后在子类字段中持有并在合适的生命周期调用 [launch]。
 *
 * @param I 启动输入类型
 * @param O 启动结果类型
 * @param lifecycleOwner 绑定生命周期的所有者（Activity / Fragment）
 * @param contract 注册到 ActivityResultRegistry 的结果契约
 * @param registryProvider 提供 [androidx.activity.result.ActivityResultRegistry] 的工厂
 */
class ResultCallbackLauncher<I, O>(
    private val lifecycleOwner: LifecycleOwner,
    private val contract: ActivityResultContract<I, O>,
    private val registryProvider: () -> androidx.activity.result.ActivityResultRegistry
) : DefaultLifecycleObserver {

    private var launcher: ActivityResultLauncher<I>? = null
    private var callback: ((O) -> Unit)? = null

    init {
        // 必须立即挂载：onCreate 之前若 owner 已处于 STARTED 状态，
        // 系统不会再回调 onCreate，launcher 将无法注册。
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        // 使用 System.identityHashCode 区分多个同类型 launcher，避免注册 key 冲突
        launcher = registryProvider().register("callback_launcher_${System.identityHashCode(this)}", contract) { result ->
            callback?.invoke(result)
        }
    }

    /**
     * 发起跳转并同步绑定结果回调。
     *
     * @param input 启动输入参数，类型由 contract 决定
     * @param onResult 结果返回闭包，回调运行在主线程
     * @throws IllegalStateException 当 launcher 尚未初始化（owner 未进入 onCreate 之后）时抛出
     */
    fun launch(input: I, onResult: (O) -> Unit) {
        this.callback = onResult
        launcher?.launch(input) ?: error("ResultCallbackLauncher 尚未初始化，请确保在 onCreate 之后调用 launch")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        launcher?.unregister()
        callback = null
        launcher = null
        lifecycleOwner.lifecycle.removeObserver(this)
    }
}

/**
 * 通用结果启动器注册入口（Activity 重载）。
 *
 * @param I 输入类型
 * @param O 输出类型
 * @param contract 结果契约
 * @return 与当前 Activity 生命周期绑定的 [ResultCallbackLauncher]
 */
fun <I, O> ComponentActivity.registerResultLauncher(contract: ActivityResultContract<I, O>) =
    ResultCallbackLauncher(this, contract) { this.activityResultRegistry }

/**
 * 通用结果启动器注册入口（Fragment 重载）。
 *
 * @param I 输入类型
 * @param O 输出类型
 * @param contract 结果契约
 * @return 与当前 Fragment 生命周期绑定的 [ResultCallbackLauncher]
 */
fun <I, O> Fragment.registerResultLauncher(contract: ActivityResultContract<I, O>) =
    ResultCallbackLauncher(this, contract) { this.requireActivity().activityResultRegistry }


/**
 * 注册 [ActivityResultContracts.StartActivityForResult] 启动器（Activity 重载）。
 *
 * @return 用于启动任意 Activity 并接收 [ActivityResult] 的 [ResultCallbackLauncher]
 */
fun ComponentActivity.registerActivityLauncher() = registerResultLauncher(ActivityResultContracts.StartActivityForResult())

/**
 * 注册 [ActivityResultContracts.StartActivityForResult] 启动器（Fragment 重载）。
 *
 * @return 用于启动任意 Activity 并接收 [ActivityResult] 的 [ResultCallbackLauncher]
 */
fun Fragment.registerActivityLauncher() = registerResultLauncher(ActivityResultContracts.StartActivityForResult())

/**
 * 启动任意自定义 Activity 并通过闭包接收结果。
 *
 * @param T 目标 Activity 类型
 * @param context 启动方 Context（用于构造 Intent）
 * @param intentAction 在 Intent 上执行的配置 Lambda（用于 putExtra 等）
 * @param onResult 结果返回闭包，包含 resultCode 与返回的 Intent 数据
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

/**
 * 启动任意自定义 Activity，无需接收返回结果。
 *
 * 适用于纯展示型跳转（详情页、设置页等），不关心 resultCode 与 data。
 * 不保存 callback 引用，回调到达时由 launcher 内部直接丢弃，
 * 相比传入空 Lambda 的写法可避免无意义的闭包分配。
 *
 * @param T 目标 Activity 类型
 * @param context 启动方 Context（用于构造 Intent）
 * @param intentAction 在 Intent 上执行的配置 Lambda（用于 putExtra 等）
 */
inline fun <reified T : Activity> ResultCallbackLauncher<Intent, ActivityResult>.launchActivity(
    context: Context,
    crossinline intentAction: Intent.() -> Unit = {}
) {
    val intent = Intent(context, T::class.java).apply(intentAction)
    this.launch(intent) {}
}


/**
 * 注册 [ActivityResultContracts.RequestMultiplePermissions] 启动器（Activity 重载）。
 *
 * @return 用于批量申请权限并接收 `Map<permission, granted>` 的 [ResultCallbackLauncher]
 */
fun ComponentActivity.registerMultiplePermissionsLauncher() = registerResultLauncher(ActivityResultContracts.RequestMultiplePermissions())

/**
 * 注册 [ActivityResultContracts.RequestMultiplePermissions] 启动器（Fragment 重载）。
 *
 * @return 用于批量申请权限并接收 `Map<permission, granted>` 的 [ResultCallbackLauncher]
 */
fun Fragment.registerMultiplePermissionsLauncher() = registerResultLauncher(ActivityResultContracts.RequestMultiplePermissions())

/**
 * 批量申请权限并以平铺结构返回结果。
 *
 * @param permissions 要申请的权限列表（可变参数）
 * @param onResult 申请结果回调：
 * - `allGranted`：所有权限是否全部授权
 * - `grantedList`：被授权的权限列表
 * - `deniedList`：被拒绝的权限列表
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


/**
 * 注册 [ActivityResultContracts.TakePicturePreview] 启动器（Activity 重载）。
 *
 * @return 用于拍照并接收 Bitmap 缩略图的 [ResultCallbackLauncher]
 */
fun ComponentActivity.registerTakePicturePreviewLauncher() = registerResultLauncher(ActivityResultContracts.TakePicturePreview())

/**
 * 注册 [ActivityResultContracts.TakePicturePreview] 启动器（Fragment 重载）。
 *
 * @return 用于拍照并接收 Bitmap 缩略图的 [ResultCallbackLauncher]
 */
fun Fragment.registerTakePicturePreviewLauncher() = registerResultLauncher(ActivityResultContracts.TakePicturePreview())

/**
 * 注册 [ActivityResultContracts.TakePicture] 启动器（Activity 重载）。
 *
 * @return 用于拍照并接收是否成功的 [ResultCallbackLauncher]（需要自行提供输出 Uri）
 */
fun ComponentActivity.registerTakePictureLauncher() = registerResultLauncher(ActivityResultContracts.TakePicture())

/**
 * 注册 [ActivityResultContracts.TakePicture] 启动器（Fragment 重载）。
 *
 * @return 用于拍照并接收是否成功的 [ResultCallbackLauncher]
 */
fun Fragment.registerTakePictureLauncher() = registerResultLauncher(ActivityResultContracts.TakePicture())


/**
 * 注册 [ActivityResultContracts.PickVisualMedia] 单选启动器（Activity 重载）。
 *
 * @return 用于选择单张图片 / 视频并返回 Uri 的 [ResultCallbackLauncher]
 */
fun ComponentActivity.registerPhotoPickerLauncher() = registerResultLauncher(ActivityResultContracts.PickVisualMedia())

/**
 * 注册 [ActivityResultContracts.PickVisualMedia] 单选启动器（Fragment 重载）。
 *
 * @return 用于选择单张图片 / 视频并返回 Uri 的 [ResultCallbackLauncher]
 */
fun Fragment.registerPhotoPickerLauncher() = registerResultLauncher(ActivityResultContracts.PickVisualMedia())

/**
 * 启动 PhotoPicker 仅选择图片。
 *
 * @param onResult 结果回调，返回选中图片的 Uri（未选择时为 null）
 */
fun ResultCallbackLauncher<PickVisualMediaRequest, Uri?>.launchImageOnly(onResult: (Uri?) -> Unit) {
    this.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly), onResult)
}

/**
 * 启动 PhotoPicker 仅选择视频。
 *
 * @param onResult 结果回调，返回选中视频的 Uri（未选择时为 null）
 */
fun ResultCallbackLauncher<PickVisualMediaRequest, Uri?>.launchVideoOnly(onResult: (Uri?) -> Unit) {
    this.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly), onResult)
}

/**
 * 注册 [ActivityResultContracts.PickMultipleVisualMedia] 多选启动器（Activity 重载）。
 *
 * @param maxItems 最大可选项数，默认 9
 * @return 用于多选图片 / 视频并返回 Uri 列表的 [ResultCallbackLauncher]
 */
fun ComponentActivity.registerMultiplePhotoPickerLauncher(maxItems: Int = 9) = registerResultLauncher(ActivityResultContracts.PickMultipleVisualMedia(maxItems))

/**
 * 注册 [ActivityResultContracts.PickMultipleVisualMedia] 多选启动器（Fragment 重载）。
 *
 * @param maxItems 最大可选项数，默认 9
 * @return 用于多选图片 / 视频并返回 Uri 列表的 [ResultCallbackLauncher]
 */
fun Fragment.registerMultiplePhotoPickerLauncher(maxItems: Int = 9) = registerResultLauncher(ActivityResultContracts.PickMultipleVisualMedia(maxItems))

/**
 * 启动 PhotoPicker 多选图片与视频。
 *
 * @param onResult 结果回调，返回选中资源的 Uri 列表
 */
fun ResultCallbackLauncher<PickVisualMediaRequest, List<Uri>>.launchImagesAndVideos(onResult: (List<Uri>) -> Unit) {
    this.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo), onResult)
}
