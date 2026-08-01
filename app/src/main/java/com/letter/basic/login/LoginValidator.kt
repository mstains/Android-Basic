package com.letter.basic.login

import androidx.annotation.StringRes
import com.letter.basic.R

/**
 * 登录表单校验错误码。
 *
 * 每个错误码持有对应的 strings 资源 ID，UI 层通过 [messageRes] 映射文案
 * 展示到输入框 error 或 Toast，保证业务文案统一收敛在 strings.xml。
 *
 * @author letter
 */
enum class LoginError(@StringRes val messageRes: Int) {

    /** 用户名为空。 */
    EMPTY_USERNAME(R.string.app_login_username_empty_text),

    /** 密码为空。 */
    EMPTY_PASSWORD(R.string.app_login_password_empty_text),

    /** 手机号格式非法（非 11 位或非 1 开头）。 */
    PHONE_INVALID(R.string.app_login_phone_invalid_text),

    /** 验证码为空。 */
    CODE_EMPTY(R.string.app_login_code_empty_text),

    /** 验证码格式非法（非 6 位数字）。 */
    CODE_INVALID(R.string.app_login_code_invalid_text),
}

/**
 * 登录表单格式校验纯函数集合。
 *
 * 只负责「格式是否合法」的判定，不感知账号数据与 UI；账号比对与验证码
 * 一致性由 Fragment 结合 [LocalAccount] 完成，保证本类可脱离 Android
 * 运行时做单元测试。
 *
 * @author letter
 */
object LoginValidator {

    /** 中国大陆手机号：1 开头 + 10 位数字。 */
    private val PHONE_REGEX = Regex("^1\\d{10}$")

    /** 6 位纯数字验证码。 */
    private val CODE_REGEX = Regex("^\\d{6}$")

    /**
     * 校验账号密码表单：用户名与密码均非空。
     *
     * @param username 用户名输入
     * @param password 密码输入
     * @return 校验失败时的 [LoginError]，通过时返回 null
     */
    fun validateAccountForm(username: String, password: String): LoginError? = when {
        username.isBlank() -> LoginError.EMPTY_USERNAME
        password.isBlank() -> LoginError.EMPTY_PASSWORD
        else -> null
    }

    /**
     * 校验手机号格式：1 开头且共 11 位数字。
     *
     * @param phone 手机号输入
     * @return 校验失败时的 [LoginError.PHONE_INVALID]，通过时返回 null
     */
    fun validatePhone(phone: String): LoginError? =
        if (PHONE_REGEX.matches(phone)) null else LoginError.PHONE_INVALID

    /**
     * 校验验证码格式：6 位纯数字。
     *
     * @param code 验证码输入
     * @return 校验失败时的 [LoginError]，通过时返回 null
     */
    fun validateVerifyCode(code: String): LoginError? = when {
        code.isBlank() -> LoginError.CODE_EMPTY
        !CODE_REGEX.matches(code) -> LoginError.CODE_INVALID
        else -> null
    }
}
