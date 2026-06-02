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


class MainActivity : BaseMultiStateVBActivity<ActivityMainBinding>() {

    // 权限申请启动器
    private val mPermissionLauncher = registerMultiplePermissionsLauncher()

    // 多选图片启动器
    private val mPhotoPickLauncher = registerMultiplePhotoPickerLauncher()

    override fun initListener() {
        // 相机权限按钮点击
        viewBinding.btCameraPermission.setOnClickListener {


            mPermissionLauncher.launchPermissions(Manifest.permission.CAMERA) { allGranted, grantedList, deniedList ->

                if (allGranted){
                    Toast.makeText(this, "权限申请通过", Toast.LENGTH_SHORT).show()

                }
                else {
                    Toast.makeText(this, "${deniedList.toPermissionChineseNames()}被拒绝", Toast.LENGTH_SHORT).show()
                }



            }

        }


        viewBinding.btOpenPhoto.setOnClickListener {

            mPhotoPickLauncher.launchImagesAndVideos {
                it.forEach {
                    Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    /**
     * 创建viewBinding
     * */
    override fun createViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    /**
     * 初始化状态栏
     * */
    override fun initStatusBar() {

    }


}