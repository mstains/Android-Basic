package com.letter.basic.login

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.letter.basic.R
import com.letter.basic.databinding.FragmentAccountLoginBinding
import com.letter.basic.fragment.BaseMultiStateVBFragment

/**
 * 账号密码登录 Fragment。
 *
 * 登录流程：格式校验（失败 → 输入框下方红字）→ 比对 [LocalAccount] 硬编码账号
 * （失败 → Toast 提示）→ 成功 → Toast「登录成功」，停留本页。
 * 不调用任何接口，纯本地演示。
 *
 * @author letter
 */
class AccountLoginFragment : BaseMultiStateVBFragment<FragmentAccountLoginBinding>() {

    override fun initView() {
        // 无初始视图配置；监听注册见 initListener
    }

    override fun initListener() {
        viewBinding?.btAccountLogin?.setOnClickListener { login() }
    }

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentAccountLoginBinding {
        return FragmentAccountLoginBinding.inflate(inflater, container, false)
    }

    /**
     * 执行账号密码登录。
     *
     * 格式错误在输入框下方红字提示；账号密码不匹配用 Toast 提示，
     * 与「格式问题就地纠正、业务失败轻提示」的交互约定一致。
     */
    private fun login() {
        val binding = viewBinding ?: return
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        when (val error = LoginValidator.validateAccountForm(username, password)) {
            LoginError.EMPTY_USERNAME -> {
                binding.tilUsername.error = getString(error.messageRes)
                binding.tilPassword.error = null
            }
            LoginError.EMPTY_PASSWORD -> {
                binding.tilPassword.error = getString(error.messageRes)
                binding.tilUsername.error = null
            }
            // else 覆盖 null（格式通过）及校验函数按语义不会返回的其他错误码
            else -> {
                // 格式通过后清除历史错误提示，避免上次失败残留
                binding.tilUsername.error = null
                binding.tilPassword.error = null
                if (username == LocalAccount.ACCOUNT_NAME && password == LocalAccount.ACCOUNT_PASSWORD) {
                    Toast.makeText(requireContext(), getString(R.string.app_login_success_text), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.app_login_account_error_text), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
