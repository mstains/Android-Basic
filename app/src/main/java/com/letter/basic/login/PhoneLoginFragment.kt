package com.letter.basic.login

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.letter.basic.R
import com.letter.basic.databinding.FragmentPhoneLoginBinding
import com.letter.basic.fragment.BaseMultiStateVBFragment

/**
 * 手机号验证码登录 Fragment。
 *
 * 登录流程：
 * 1. 输入手机号 → 格式校验（失败 → 红字）→ 比对 [LocalAccount.PHONE_NUMBER]
 *    （未注册 → Toast）→ 生成 6 位随机验证码 Toast 展示 → 启动 60s 倒计时
 * 2. 输入验证码 → 格式校验（失败 → 红字）→ 与本次生成码比对（不一致 → Toast）
 * 3. 全部通过 → Toast「登录成功」，停留本页
 *
 * 纯本地演示：验证码不走短信通道，直接 Toast 展示供用户输入。
 *
 * @author letter
 */
class PhoneLoginFragment : BaseMultiStateVBFragment<FragmentPhoneLoginBinding>() {

    private val mainHandler = Handler(Looper.getMainLooper())

    /** 倒计时剩余秒数；为 0 表示不处于倒计时状态。 */
    private var countDownRemaining = 0

    /** 本次生成的验证码；未点击获取时为 null。 */
    private var currentVerifyCode: String? = null

    /** 倒计时递减任务：每秒触发一次，归零后恢复按钮可用。 */
    private val countDownRunnable = object : Runnable {
        override fun run() {
            countDownRemaining--
            updateCountDownButton()
            if (countDownRemaining > 0) {
                mainHandler.postDelayed(this, COUNT_DOWN_INTERVAL_MS)
            }
        }
    }

    override fun initView() {
        // 初始无倒计时，刷新按钮为可用「获取验证码」状态
        updateCountDownButton()
    }

    override fun initListener() {
        viewBinding?.btGetCode?.setOnClickListener { sendVerifyCode() }
        viewBinding?.btPhoneLogin?.setOnClickListener { login() }
    }

    override fun onDestroyView() {
        // 移除未完成的倒计时回调，避免 Fragment 销毁后 Handler 仍持有 View 引用导致泄漏
        mainHandler.removeCallbacks(countDownRunnable)
        super.onDestroyView()
    }

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPhoneLoginBinding {
        return FragmentPhoneLoginBinding.inflate(inflater, container, false)
    }

    /**
     * 发送验证码：格式校验 → 账号校验 → 生成随机码并启动倒计时。
     *
     * 手机号格式错误就地红字提示；手机号未注册用 Toast 提示（业务失败轻提示约定）。
     */
    private fun sendVerifyCode() {
        val binding = viewBinding ?: return
        val phone = binding.etPhone.text.toString()

        when (val error = LoginValidator.validatePhone(phone)) {
            LoginError.PHONE_INVALID -> {
                binding.tilPhone.error = getString(error.messageRes)
                return
            }
            // else 覆盖 null（格式通过）及校验函数按语义不会返回的其他错误码
            else -> {
                binding.tilPhone.error = null
                if (phone != LocalAccount.PHONE_NUMBER) {
                    Toast.makeText(requireContext(), getString(R.string.app_login_phone_not_registered_text), Toast.LENGTH_SHORT).show()
                    return
                }
                // 演示场景：验证码直接展示，用户输入该码即可登录
                val code = LocalAccount.generateVerifyCode()
                currentVerifyCode = code
                Toast.makeText(requireContext(), getString(R.string.app_login_code_sent_text, code), Toast.LENGTH_SHORT).show()
                startCountDown()
            }
        }
    }

    /**
     * 执行手机号验证码登录。
     *
     * 手机号与验证码格式各自红字提示；业务失败（未注册 / 验证码不一致）用 Toast 提示。
     */
    private fun login() {
        val binding = viewBinding ?: return
        val phone = binding.etPhone.text.toString()
        val code = binding.etCode.text.toString()

        val phoneError = LoginValidator.validatePhone(phone)
        val codeError = LoginValidator.validateVerifyCode(code)
        binding.tilPhone.error = phoneError?.let { getString(it.messageRes) }
        binding.tilCode.error = codeError?.let { getString(it.messageRes) }
        if (phoneError != null || codeError != null) return

        if (phone != LocalAccount.PHONE_NUMBER) {
            Toast.makeText(requireContext(), getString(R.string.app_login_phone_not_registered_text), Toast.LENGTH_SHORT).show()
            return
        }
        val expectedCode = currentVerifyCode ?: return
        if (code != expectedCode) {
            Toast.makeText(requireContext(), getString(R.string.app_login_code_error_text), Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(requireContext(), getString(R.string.app_login_success_text), Toast.LENGTH_SHORT).show()
    }

    /**
     * 启动 60s 发送倒计时：按钮禁用并显示剩余秒数。
     *
     * 先移除旧回调再重新调度，保证重复点击「获取验证码」时倒计时从头开始。
     */
    private fun startCountDown() {
        countDownRemaining = COUNT_DOWN_TOTAL_SECONDS
        mainHandler.removeCallbacks(countDownRunnable)
        updateCountDownButton()
        mainHandler.postDelayed(countDownRunnable, COUNT_DOWN_INTERVAL_MS)
    }

    /**
     * 刷新获取验证码按钮状态：倒计时中禁用并显示剩余秒数，结束后恢复可点。
     */
    private fun updateCountDownButton() {
        val binding = viewBinding ?: return
        val inCountDown = countDownRemaining > 0
        binding.btGetCode.isEnabled = !inCountDown
        binding.btGetCode.text = if (inCountDown) {
            getString(R.string.app_login_resend_code_text, countDownRemaining)
        } else {
            getString(R.string.app_login_get_code_text)
        }
    }

    companion object {
        private const val COUNT_DOWN_TOTAL_SECONDS = 60
        private const val COUNT_DOWN_INTERVAL_MS = 1000L
    }
}
