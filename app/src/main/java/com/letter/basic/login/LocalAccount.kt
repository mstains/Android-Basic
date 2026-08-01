package com.letter.basic.login

import kotlin.random.Random

/**
 * 本地演示登录的硬编码账号与验证码生成器。
 *
 * 纯本地演示用：账号数据写死在常量中，不涉及任何网络或持久化。
 * [generateVerifyCode] 每次调用生成新的 6 位随机数字验证码，供 UI 层
 * Toast 展示并与用户输入比对。
 *
 * @author letter
 */
object LocalAccount {

    /** 演示用户名。 */
    const val ACCOUNT_NAME = "admin"

    /** 演示密码。 */
    const val ACCOUNT_PASSWORD = "123456"

    /** 演示手机号。 */
    const val PHONE_NUMBER = "13800138000"

    /** 验证码位数。 */
    private const val CODE_LENGTH = 6

    private val random = Random.Default

    /**
     * 生成 6 位随机数字验证码。
     *
     * 逐位随机以保证首位可为 0（如 "012345"），避免 [100000, 999999]
     * 区间随机造成的首位分布偏差。
     *
     * @return 6 位纯数字字符串
     */
    fun generateVerifyCode(): String =
        buildString(CODE_LENGTH) { repeat(CODE_LENGTH) { append(random.nextInt(10)) } }
}
