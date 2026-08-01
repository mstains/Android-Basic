package com.letter.basic.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [LocalAccount] 本地演示账号与验证码生成器单元测试。
 *
 * 覆盖两类路径：
 * - 正常路径：硬编码账号常量与 spec 一致；[LocalAccount.generateVerifyCode] 生成 6 位纯数字
 * - 边界条件：多次随机生成均满足长度与数字约束
 *
 * @author letter
 */
class LocalAccountTest {

    /**
     * 硬编码账号常量与 spec 保持一致。
     */
    @Test
    fun `account constants match spec`() {
        assertEquals("admin", LocalAccount.ACCOUNT_NAME)
        assertEquals("123456", LocalAccount.ACCOUNT_PASSWORD)
        assertEquals("13800138000", LocalAccount.PHONE_NUMBER)
    }

    /**
     * 随机验证码恒为 6 位纯数字（重复 100 次覆盖随机路径）。
     */
    @Test
    fun `generateVerifyCode always returns 6 digits`() {
        repeat(100) {
            val code = LocalAccount.generateVerifyCode()
            assertEquals("验证码长度应为 6: $code", 6, code.length)
            assertTrue("验证码应为纯数字: $code", code.all { it.isDigit() })
        }
    }
}
