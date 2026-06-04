package com.letter.basic.extend

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.InvocationTargetException


/**
 * Android 内部 / 外部存储目录查询扩展。
 *
 * 把 `Context` 自带的 `getFilesDir` / `getCacheDir` / `getExternalFilesDir` 等 API
 * 统一暴露为 String 类型路径，并补齐多个隐藏目录的便捷方法。
 *
 * 注意：Android 10+ 受 Scoped Storage 限制，部分外部存储方法在主存储中返回的路径不可直接访问，
 * 如需跨应用文件操作请使用 [MediaStore] / [Storage Access Framework]。
 */

/**
 * 获取当前应用的内部存储 `files` 目录绝对路径。
 *
 * 路径格式：`/data/data/<packageName>/files`（部分设备映射为 `/data/user/0/<packageName>/files`）。
 * 应用卸载时目录会被清除；非 root 设备其他应用无法访问。
 *
 * @receiver 调用方 Context
 * @return files 目录绝对路径
 */
fun Context.getFilesDir(): String {
    val dir: File = filesDir
    return dir.absolutePath
}

/**
 * 获取当前应用的内部存储 `cache` 目录绝对路径。
 *
 * 路径格式：`/data/data/<packageName>/cache`。
 * 系统在空间不足时可能自动清理；应用卸载时会被清除。
 *
 * @receiver 调用方 Context
 * @return cache 目录绝对路径
 */
fun Context.getCacheDir(): String {
    val dir: File = cacheDir
    return dir.absolutePath
}

/**
 * 获取当前应用的外部存储 `cache` 目录绝对路径。
 *
 * 路径格式：`/storage/emulated/0/Android/data/<packageName>/cache`。
 * 应用卸载时会被清除；不需要额外权限；普通手机文件管理器可访问。
 *
 * @receiver 调用方 Context
 * @return external cache 目录绝对路径；不可用时返回 null
 */
fun Context.getExternalCacheDir(): String? {
    val dir = externalCacheDir
    return dir?.absolutePath
}

/**
 * 获取所有外部存储设备上的 `cache` 目录列表。
 *
 * Android 4.4 新增接口，适用于多存储设备（如内置 + 外置 OTG）的设备。
 *
 * @receiver 调用方 Context
 * @return 外部存储 cache 目录列表，元素可能为 null
 */
fun Context.getExternalCacheDirs(): List<File?> {
    val list: MutableList<File?> = ArrayList()
    list.addAll(this.externalCacheDirs)
    return list
}

/**
 * 获取当前应用外部存储 `files` 目录下名为 `aa` 的子目录绝对路径。
 *
 * 路径格式：`/storage/emulated/0/Android/data/<packageName>/files/aa`。
 * 应用卸载时会被清除；普通手机文件管理器可访问。
 *
 * @receiver 调用方 Context
 * @return external files/aa 目录绝对路径；不可用时返回 null
 */
fun Context.getExternalFilesDir(): String? {
    val dir = this.getExternalFilesDir("aa")
    return dir?.absolutePath
}


/**
 * 获取当前应用外部存储 OBB 目录绝对路径。
 *
 * 路径格式：`/storage/emulated/0/Android/obb/<packageName>`，通常用于存放游戏数据包。
 *
 * @receiver 调用方 Context
 * @return OBB 目录绝对路径
 */
fun Context.getObbDir(): String {
    val dir: File = this.obbDir
    return dir.absolutePath
}

/**
 * 获取所有外部存储设备上的 OBB 目录列表。
 *
 * Android 4.4 新增接口。
 *
 * @receiver 调用方 Context
 * @return 外部存储 OBB 目录列表
 */
fun Context.getObbDirs(): List<File> {
    val list: MutableList<File> = ArrayList()
    list.addAll(this.obbDirs)

    return list
}

/**
 * 获取当前应用内部存储 `no_backup` 目录绝对路径。
 *
 * 路径格式：`/data/data/<packageName>/no_backup`。
 * Android 5.0 新增接口，低于 5.0 设备返回空字符串。
 *
 * @receiver 调用方 Context
 * @return no_backup 目录绝对路径；不支持时返回空字符串
 */
fun Context.getNoBackupFilesDir(): String {
    val dir: File? = this.noBackupFilesDir
    return if (dir == null) "" else dir.absolutePath
}

/**
 * 获取当前应用内部存储 `code_cache` 目录绝对路径。
 *
 * 用于运行时存放编译或优化后的代码缓存。
 * Android 5.0 新增接口，低于 5.0 设备返回空字符串。
 *
 * @receiver 调用方 Context
 * @return code_cache 目录绝对路径；不支持时返回空字符串
 */
fun Context.getCodeCacheDir(): String {
    val dir: File? = this.codeCacheDir
    return if (dir == null) "" else dir.absolutePath
}

/**
 * 获取当前应用内部存储根目录绝对路径。
 *
 * 路径格式：`/data/data/<packageName>`。
 * Android 7.0 新增接口，低于 7.0 设备返回空字符串。
 *
 * @receiver 调用方 Context
 * @return 应用根目录绝对路径；不支持时返回空字符串
 */
fun Context.getDataDir(): String {
    var dir: File? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        dir = this.dataDir
    }
    return if (dir == null) "" else dir.absolutePath
}

/**
 * 获取当前 APK 安装包完整路径。
 *
 * 应用通常无需直接访问此路径。
 *
 * @receiver 调用方 Context
 * @return APK 完整文件路径
 */
fun Context.getPackageCodePath(): String {
    val dir: String = this.packageCodePath
    return dir
}

/**
 * 与 [getPackageCodePath] 一致，指向 APK 资源打包后的文件路径。
 *
 * @receiver 调用方 Context
 * @return 资源路径字符串
 */
fun Context.getPackageResourcePath(): String {
    val dir: String = this.packageResourcePath
    return dir
}

/**
 * 获取外部存储中指定公共目录（`aaa` 子目录）的绝对路径。
 *
 * 需要声明 `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` 权限，
 * 并且在系统应用信息中实际打开存储权限。应用卸载时不会被清除。
 *
 * @receiver 调用方 Context
 * @return 公共目录 `aaa` 绝对路径
 */
fun Context.getExternalStoragePublicDirectory(): String {
    val dir = Environment.getExternalStoragePublicDirectory("aaa")
    return dir.absolutePath
}

/**
 * 获取外部存储根目录绝对路径。
 *
 * 需要声明存储权限并在系统设置中开启。应用卸载时不会被清除。
 *
 * @receiver 调用方 Context
 * @return 外部存储根目录绝对路径
 */
fun Context.getExternalStorageDirectory(): String {
    val dir = Environment.getExternalStorageDirectory()
    return dir.absolutePath
}

/**
 * 获取外部存储的当前挂载状态字符串。
 *
 * 常用返回值：
 * - `MEDIA_MOUNTED`：可读写
 * - `MEDIA_MOUNTED_READ_ONLY`：只读
 * - `MEDIA_REMOVED` / `MEDIA_UNMOUNTED`：未挂载
 * - `MEDIA_UNKNOWN`：状态未知
 *
 * @receiver 调用方 Context
 * @return [Environment.getExternalStorageState] 的返回值
 */
fun Context.getExternalStorageState(): String {
    val state = Environment.getExternalStorageState()
    return state
}

/**
 * 获取内部存储 `/data` 目录绝对路径。
 *
 * 非 root 设备一般应用无法直接读写。
 *
 * @receiver 调用方 Context
 * @return `/data` 目录绝对路径
 */
fun Context.getDataDirectory(): String {
    val file = Environment.getDataDirectory()
    return file.absolutePath
}

/**
 * 获取系统下载缓存 `/data/cache` 目录绝对路径。
 *
 * 非 root 设备一般应用无法直接读写。
 *
 * @receiver 调用方 Context
 * @return 下载缓存目录绝对路径
 */
fun Context.getDownloadCacheDirectory(): String {
    val file = Environment.getDownloadCacheDirectory()
    return file.absolutePath
}


/**
 * 获取系统根目录 `/system` 绝对路径。
 *
 * 只读权限，不可写。
 *
 * @receiver 调用方 Context
 * @return 系统根目录绝对路径
 */
fun Context.getRootDirectory(): String {
    val file = Environment.getRootDirectory()
    return file.absolutePath
}

/**
 * 获取所有外部存储路径（如内置 + USB OTG）。
 *
 * 通过反射调用 [StorageManager.getVolumePaths]，属于隐藏 API，
 * 在部分定制 ROM 上可能失败。
 *
 * @receiver 调用方 Context
 * @return 外部存储路径数组；反射失败时返回空数组
 */
fun Context.getStoragePaths(): Array<String?>? {
    var paths = arrayOfNulls<String>(0)
    val sm =
        this.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    try {
        paths = sm.javaClass.getMethod("getVolumePaths", null).invoke(sm, null) as Array<String?>
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    }
    return paths
}

/**
 * 将 [Uri] 指向的内容复制到 [outFilePath] 指定的本地文件。
 *
 * 受 Android 10 Scoped Storage 限制，[outFilePath] 必须位于应用沙盒内
 * （如 `Context.getExternalFilesDir` / `cacheDir`），否则 `FileOutputStream` 构造会失败。
 *
 * @receiver 源 Uri
 * @param context 用于打开文件描述符的上下文
 * @param outFilePath 输出文件绝对路径
 * @return 写入完成的目标 [File] 实例
 */
fun Uri.copyAndConvert(context: Context, outFilePath: String): File {
    val pfd = context.contentResolver.openFileDescriptor(this, "r")
    FileInputStream(pfd?.fileDescriptor).use { fis ->
        FileOutputStream(File(outFilePath)).use { fos ->
            fis.copyTo(fos)
        }
    }
    return File(outFilePath)
}
