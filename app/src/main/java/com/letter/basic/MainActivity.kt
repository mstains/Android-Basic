package com.letter.basic


import android.Manifest
import android.view.LayoutInflater
import android.widget.Toast
import com.letter.basic.activity.BaseMultiStateVBActivity

import com.letter.basic.databinding.ActivityMainBinding
import com.letter.basic.extend.launchImagesAndVideos
import com.letter.basic.extend.launchPermissions
import com.letter.basic.extend.registerMultiplePermissionsLauncher
import com.letter.basic.extend.registerMultiplePhotoPickerLauncher
import com.letter.basic.utils.toPermissionChineseNames


/**
 * 示例 Activity。
 *
 * 演示 Basic 库中两类典型能力：
 * 1. 通过 [registerMultiplePermissionsLauncher] + [launchPermissions] 申请相机权限。
 * 2. 通过 [registerMultiplePhotoPickerLauncher] + [launchImagesAndVideos] 多选图片 / 视频。
 *
 * @author letter
 */
class MainActivity : BaseMultiStateVBActivity<ActivityMainBinding>() {

    // 权限申请启动器：延迟初始化，由 Basic 库内部完成 ActivityResultRegistry 绑定
    private val mPermissionLauncher = registerMultiplePermissionsLauncher()

    // 多选图片启动器：默认最多 9 项
    private val mPhotoPickLauncher = registerMultiplePhotoPickerLauncher()

    /**
     * 注册按钮点击监听。
     *
     * - 相机权限按钮：申请通过后提示"权限申请通过"，被拒绝时列出被拒绝项的中文名。
     * - 打开相册按钮：调用 [launchImagesAndVideos] 多选图片 / 视频，并对每个 Uri 显示 Toast。
     */
    override fun initListener() {
        // 相机权限按钮点击：直接展示申请结果
        viewBinding.btCameraPermission.setOnClickListener {
            mPermissionLauncher.launchPermissions(Manifest.permission.CAMERA) { allGranted, _, deniedList ->
                if (allGranted) {
                    Toast.makeText(this, "权限申请通过", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "${deniedList.toPermissionChineseNames()}被拒绝", Toast.LENGTH_SHORT).show()
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

    /**
     * 初始化状态栏。
     *
     * 示例未对状态栏做定制，保持系统默认样式。
     */
    override fun initStatusBar() {
    }
}
