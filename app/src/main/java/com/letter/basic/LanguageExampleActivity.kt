package com.letter.basic

import android.view.LayoutInflater
import com.letter.basic.activity.BaseMultiStateVBActivity
import com.letter.basic.databinding.ActivityLanguageExampleBinding
import com.letter.basic.extend.switchLanguage
import java.util.Locale

class LanguageExampleActivity : BaseMultiStateVBActivity<ActivityLanguageExampleBinding>() {




    override fun initListener() {
        // 切换语言演示：3 个按钮分别对应固定中文、固定英文、回退系统 Locale
        viewBinding.btSwitchChinese.setOnClickListener {
            switchLanguage(Locale.SIMPLIFIED_CHINESE)
        }
        viewBinding.btSwitchEnglish.setOnClickListener {
            switchLanguage(Locale.ENGLISH)
        }
        // 传 null 表示回退到 Locale.getDefault()，由系统语言决定
        viewBinding.btSwitchSystem.setOnClickListener {
            switchLanguage(null)
        }
    }




    /**
     * 创建 ViewBinding 实例。
     *
     * 由子类重写，使用 ViewBinding 的 inflate 方法构造绑定实例。
     *
     * @param inflater LayoutInflater 实例，由基类从当前 Activity 上下文创建
     * @return 子类持有的 ViewBinding 实例
     */
    override fun createViewBinding(inflater: LayoutInflater): ActivityLanguageExampleBinding {
        return ActivityLanguageExampleBinding.inflate(inflater)
    }



}