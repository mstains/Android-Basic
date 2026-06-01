package com.letter.basic.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding


/**
 * @Package:        com.energy.sources.base.activity
 * @ClassName:      BaseMultiStateVBActivity
 * @Description:    activity基类，多个页面状态的activity
 * @Author:         Boqing.wu
 * @CreateDate:     2024/12/31 上午9:49
 * @UpdateUser:     更新者：
 * @UpdateDate:     2024/12/31 上午9:49
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */
abstract class BaseMultiStateVBActivity<VB : ViewBinding> : BaseCommonMultiStateActivity() {

    /**
     * viewBinding应用对象
     * */
    protected val viewBinding: VB by lazy { createViewBinding(LayoutInflater.from(this)) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initStatusBar()
        initView()
        initData()
        initListener()


    }


    /**
     * 自定义标题栏返回操作
     */
    protected open fun topDefineCancel() {

        finish()

    }


    /**
     * 右侧文字点击
     */
    protected open fun topRightTextDefineCancel() {

    }


    /**
     * 创建viewBinding
     * */
    abstract fun createViewBinding(inflater: LayoutInflater): VB
}
