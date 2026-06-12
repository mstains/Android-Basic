package com.letter.basic


import android.Manifest
import android.view.LayoutInflater
import android.widget.Toast
import com.letter.basic.activity.BaseMultiStateVBActivity

import com.letter.basic.databinding.ActivityMainBinding
import com.letter.basic.extend.launchActivity
import com.letter.basic.extend.launchImagesAndVideos
import com.letter.basic.extend.launchPermissions
import com.letter.basic.extend.registerActivityLauncher
import com.letter.basic.extend.registerMultiplePermissionsLauncher
import com.letter.basic.extend.registerMultiplePhotoPickerLauncher
import com.letter.basic.extend.switchLanguage
import com.letter.basic.utils.toPermissionChineseNames
import java.util.Locale


/**
 * 示例 Activity。
 *
 * 演示 Basic 库中两类典型能力：
 * 1. 通过 [registerMultiplePermissionsLauncher] + [launchPermissions] 申请相机权限。
 * 2. 通过 [registerMultiplePhotoPickerLauncher] + [launchImagesAndVideos] 多选图片 / 视频。
 *
 * 同时演示 [switchLanguage] 扩展的 3 种调用方式（切到中文 / 切到英文 / 跟随系统）。
 *
 * @author letter
 */
class MainActivity : BaseMultiStateVBActivity<ActivityMainBinding>() {

    // 把 launcher 提升为字段而非方法内局部变量：避免 onCreate 重建导致 ActivityResultRegistry 中
    // 的 key 重复注册；registerMultiplePermissionsLauncher 内部用 by lazy 持有 lifecycleObserver，
    // 必须与 Activity 生命周期绑定。
    private val mPermissionLauncher = registerMultiplePermissionsLauncher()

    // maxItems 默认为 9，对齐 Android 13+ 系统 PhotoPicker 的分页阈值；
    // 超过 9 张会触发系统选择器分页，影响交互一致性。改大需配合自绘 UI 评估。
    private val mPhotoPickLauncher = registerMultiplePhotoPickerLauncher()


    private val activityLauncher = registerActivityLauncher()

    /**
     * 注册按钮点击监听。
     *
     * - 相机权限按钮：申请通过后提示"权限申请通过"，被拒绝时列出被拒绝项的中文名。
     * - 打开相册按钮：调用 [launchImagesAndVideos] 多选图片 / 视频，并对每个 Uri 显示 Toast。
     * - 切到中文 / 切到英文 / 跟随系统按钮：调用 [switchLanguage] 切换 App 语言并重建当前 Activity。
     */
    override fun initListener() {
        // 示例未做"拒绝后引导跳转设置页"的生产级 UX，仅 Toast 展示结果
        viewBinding.btCameraPermission.setOnClickListener {
            mPermissionLauncher.launchPermissions(Manifest.permission.CAMERA) { allGranted, _, deniedList ->
                if (allGranted) {
                    Toast.makeText(this, "权限申请通过", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this, "${deniedList.toPermissionChineseNames()}被拒绝", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewBinding.btOpenPhoto.setOnClickListener {
            mPhotoPickLauncher.launchImagesAndVideos { uris ->
                uris.forEach {
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                }
            }
        }


        viewBinding.btSwitchLanguage.setOnClickListener {

            activityLauncher.launchActivity<LanguageExampleActivity>(this)

        }


    }

    /**
     * 创建 [ActivityMainBinding] 实例。
     *
     * @param inflater LayoutInflater 实例
     * @return [ActivityMainBinding] 绑定实例
     */
    override fun createViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }


}
