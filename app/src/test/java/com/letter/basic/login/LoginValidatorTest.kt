package com.letter.basic.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * [LoginValidator] 登录校验纯函数单元测试。
 *
 * 覆盖三类路径：
 * - 正常路径：合法输入返回 null（校验通过）
 * - 边界条件：空字符串输入
 * - 异常路径：格式非法输入返回对应 [LoginError] 错误码
 *
 * @author letter
 */
class LoginValidatorTest {

    // ===== 账号表单校验（用户名 + 密码）=====

    /**
     * 用户名（或密码）为空 → 返回 [LoginError.EMPTY_USERNAME]。
     */
    @Test
    fun `validateAccountForm with empty username returns error`() {
        assertEquals(LoginError.EMPTY_USERNAME, LoginValidator.validateAccountForm("", "123456"))
    }

    /**
     * 密码为空 → 返回 [LoginError.EMPTY_PASSWORD]。
     */
    @Test
    fun `validateAccountForm with empty password returns error`() {
        assertEquals(LoginError.EMPTY_PASSWORD, LoginValidator.validateAccountForm("admin", ""))
    }

    /**
     * 用户名与密码均非空 → 返回 null（校验通过）。
     */
    @Test
    fun `validateAccountForm with valid input returns null`() {
        assertNull(LoginValidator.validateAccountForm("admin", "123456"))
    }

    // ===== 手机号校验 =====

    /**
     * 手机号为空 → 返回 [LoginError.PHONE_INVALID]。
     */
    @Test
    fun `validatePhone with empty input returns error`() {
        assertEquals(LoginError.PHONE_INVALID, LoginValidator.validatePhone(""))
    }

    /**
     * 手机号不足 11 位 → 返回 [LoginError.PHONE_INVALID]。
     */
    @Test
    fun `validatePhone with short number returns error`() {
        assertEquals(LoginError.PHONE_INVALID, LoginValidator.validatePhone("1380013800"))
    }

    /**
     * 手机号不以 1 开头 → 返回 [LoginError.PHONE_INVALID]。
     */
    @Test
    fun `validatePhone not starting with 1 returns error`() {
        assertEquals(LoginError.PHONE_INVALID, LoginValidator.validatePhone("23800138000"))
    }

    /**
     * 合法 11 位手机号 → 返回 null（校验通过）。
     */
    @Test
    fun `validatePhone with valid phone returns null`() {
        assertNull(LoginValidator.validatePhone("13800138000"))
    }

    // ===== 验证码校验 =====

    /**
     * 验证码为空 → 返回 [LoginError.CODE_EMPTY]。
     */
    @Test
    fun `validateVerifyCode with empty input returns error`() {
        assertEquals(LoginError.CODE_EMPTY, LoginValidator.validateVerifyCode(""))
    }

    /**
     * 验证码含非数字字符 → 返回 [LoginError.CODE_INVALID]。
     */
    @Test
    fun `validateVerifyCode with non numeric returns error`() {
        assertEquals(LoginError.CODE_INVALID, LoginValidator.validateVerifyCode("12a456"))
    }

    /**
     * 验证码长度不足 6 位 → 返回 [LoginError.CODE_INVALID]。
     */
    @Test
    fun `validateVerifyCode with short code returns error`() {
        assertEquals(LoginError.CODE_INVALID, LoginValidator.validateVerifyCode("12345"))
    }

    /**
     * 合法 6 位数字验证码 → 返回 null（校验通过）。
     */
    @Test
    fun `validateVerifyCode with valid 6 digits returns null`() {
        assertNull(LoginValidator.validateVerifyCode("123456"))
    }
}
