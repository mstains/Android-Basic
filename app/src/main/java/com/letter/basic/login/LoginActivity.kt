package com.letter.basic.login

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.letter.basic.R
import com.letter.basic.activity.BaseMultiStateVBActivity
import com.letter.basic.databinding.ActivityLoginBinding

/**
 * 本地登录演示容器页。
 *
 * 通过 TabLayout + ViewPager2 承载 [AccountLoginFragment]（账号密码登录）与
 * [PhoneLoginFragment]（手机号验证码登录）两个相互独立的登录方式：
 * - 切换 Tab 时各 Fragment 实例由 ViewPager2 持有，输入内容保留不互相干扰
 * - 不调用任何接口，账号校验与验证码生成见 [LocalAccount] / [LoginValidator]
 *
 * @author letter
 */
class LoginActivity : BaseMultiStateVBActivity<ActivityLoginBinding>() {

    override fun initView() {
        // 两个登录方式的数据完全独立：每个 Tab 对应独立 Fragment 实例，
        // FragmentStateAdapter 保证切换时状态不丢失
        viewBinding.vpLogin.adapter = LoginTabAdapter(this)

        // Tab 标题跟随 Fragment 顺序绑定，position 0 = 账号密码，1 = 手机号
        TabLayoutMediator(viewBinding.tabLogin, viewBinding.vpLogin) { tab, position ->
            tab.text = if (position == 0) {
                getString(R.string.app_login_account_tab_text)
            } else {
                getString(R.string.app_login_phone_tab_text)
            }
        }.attach()
    }

    override fun createViewBinding(inflater: LayoutInflater): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(inflater)
    }
}

/**
 * 登录 Tab 页适配器。
 *
 * 仅服务于 [LoginActivity]，故声明为私有内部类；两个登录 Fragment 均为
 * 无参构造，无需 ViewModel 或参数传递。
 */
private class LoginTabAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> AccountLoginFragment()
        else -> PhoneLoginFragment()
    }

    companion object {
        private const val TAB_COUNT = 2
    }
}
