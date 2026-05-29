package com.letter.basic.activity

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

/**
 * @Package:        com.energy.sources.base.activity
 * @ClassName:      BaseMultiStateVBVMActivity
 * @Description:    activity基类，多个页面状态的activity，带有网络请求
 * @Author:         Boqing.wu
 * @CreateDate:     2024/12/31 上午10:34
 * @UpdateUser:     更新者：
 * @UpdateDate:     2024/12/31 上午10:34
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */
abstract class BaseMultiStateVBVMActivity<VB : ViewBinding, VM : ViewModel> :
    BaseMultiStateVBActivity<VB>() {

    protected val mViewModel: VM by lazy {
        ViewModelProvider.NewInstanceFactory().create(providerVMClass())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startObserve()
        initMultiState()
    }


    open fun initMultiState() {
        mViewModel.let {
            lifecycle.addObserver(this@BaseMultiStateVBVMActivity)
        }
    }


    /**
     * 接口请求回调
     * */
    abstract fun startObserve()


    /**
     * 创建ViewModel
     * */
    abstract fun providerVMClass(): Class<VM>


}
