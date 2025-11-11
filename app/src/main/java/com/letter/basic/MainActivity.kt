package com.letter.basic


import android.view.LayoutInflater
import com.letter.basic.activity.BaseMultiStateVBActivity

import com.letter.basic.databinding.ActivityMainBinding


class MainActivity : BaseMultiStateVBActivity<ActivityMainBinding>() {




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