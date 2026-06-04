package com.letter.basic.utils

/**
 * Android 权限常量与其中文名称的对照枚举。
 *
 * 业务层可通过 [chineseNameOf] / [of] / [allMap] 在权限字符串与中文名之间转换，
 * 用于权限申请场景的用户提示。
 *
 * @property permission 对应的 Android Manifest 权限常量字符串
 * @property chineseName 该权限的中文友好名（用于 UI 展示）
 */
enum class ChinesePermission(
    val permission: String, val chineseName: String
) {

    // 危险权限 - 日历
    READ_CALENDAR(
        android.Manifest.permission.READ_CALENDAR, "读取日程"
    ),
    WRITE_CALENDAR(
        android.Manifest.permission.WRITE_CALENDAR, "写入日程"
    ),

    // 危险权限 - 相机
    CAMERA(
        android.Manifest.permission.CAMERA, "相机"
    ),

    // 危险权限 - 联系人
    READ_CONTACTS(
        android.Manifest.permission.READ_CONTACTS, "读取联系人"
    ),
    WRITE_CONTACTS(
        android.Manifest.permission.WRITE_CONTACTS, "写入联系人"
    ),
    GET_ACCOUNTS(
        android.Manifest.permission.GET_ACCOUNTS, "获取账户"
    ),

    // 危险权限 - 位置
    ACCESS_FINE_LOCATION(
        android.Manifest.permission.ACCESS_FINE_LOCATION, "精确位置（GPS）"
    ),
    ACCESS_COARSE_LOCATION(
        android.Manifest.permission.ACCESS_COARSE_LOCATION, "粗略位置（网络）"
    ),

    // 危险权限 - 麦克风
    RECORD_AUDIO(
        android.Manifest.permission.RECORD_AUDIO, "录音"
    ),

    // 危险权限 - 电话
    READ_PHONE_STATE(
        android.Manifest.permission.READ_PHONE_STATE, "读取电话状态"
    ),
    CALL_PHONE(
        android.Manifest.permission.CALL_PHONE, "拨打电话"
    ),
    READ_CALL_LOG(
        android.Manifest.permission.READ_CALL_LOG, "读取通话记录"
    ),
    WRITE_CALL_LOG(
        android.Manifest.permission.WRITE_CALL_LOG, "写入通话记录"
    ),
    ADD_VOICEMAIL(
        android.Manifest.permission.ADD_VOICEMAIL, "添加语音邮件"
    ),
    USE_SIP(
        android.Manifest.permission.USE_SIP, "使用SIP视频通话"
    ),
    PROCESS_OUTGOING_CALLS(
        android.Manifest.permission.PROCESS_OUTGOING_CALLS, "处理拨出电话"
    ),

    // 危险权限 - 传感器
    BODY_SENSORS(
        android.Manifest.permission.BODY_SENSORS, "身体传感器（心率等）"
    ),

    // 危险权限 - 短信
    SEND_SMS(
        android.Manifest.permission.SEND_SMS, "发送短信"
    ),
    RECEIVE_SMS(
        android.Manifest.permission.RECEIVE_SMS, "接收短信"
    ),
    READ_SMS(
        android.Manifest.permission.READ_SMS, "读取短信内容"
    ),
    RECEIVE_WAP_PUSH(
        android.Manifest.permission.RECEIVE_WAP_PUSH, "接收WAP推送"
    ),
    RECEIVE_MMS(
        android.Manifest.permission.RECEIVE_MMS, "接收彩信"
    ),

    // 危险权限 - 存储
    READ_EXTERNAL_STORAGE(
        android.Manifest.permission.READ_EXTERNAL_STORAGE, "读取外部存储"
    ),
    WRITE_EXTERNAL_STORAGE(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE, "写入外部存储"
    ),

    // 普通权限 - 网络
    INTERNET(
        android.Manifest.permission.INTERNET, "访问网络"
    ),
    ACCESS_NETWORK_STATE(
        android.Manifest.permission.ACCESS_NETWORK_STATE, "获取网络状态"
    ),
    ACCESS_WIFI_STATE(
        android.Manifest.permission.ACCESS_WIFI_STATE, "获取WiFi状态"
    ),
    CHANGE_WIFI_STATE(
        android.Manifest.permission.CHANGE_WIFI_STATE, "改变WiFi状态"
    ),
    CHANGE_NETWORK_STATE(
        android.Manifest.permission.CHANGE_NETWORK_STATE, "改变网络状态"
    ),

    // 普通权限 - 蓝牙
    BLUETOOTH(
        android.Manifest.permission.BLUETOOTH, "使用蓝牙"
    ),
    BLUETOOTH_ADMIN(
        android.Manifest.permission.BLUETOOTH_ADMIN, "蓝牙管理"
    ),

    // 普通权限 - NFC
    NFC(
        android.Manifest.permission.NFC, "NFC通讯"
    ),

    // 普通权限 - 设备控制
    VIBRATE(
        android.Manifest.permission.VIBRATE, "使用振动"
    ),
    WAKE_LOCK(
        android.Manifest.permission.WAKE_LOCK, "唤醒锁定（后台保持运行）"
    ),
    RECEIVE_BOOT_COMPLETED(
        android.Manifest.permission.RECEIVE_BOOT_COMPLETED, "开机自动启动"
    ),
    FLASHLIGHT(
        "android.permission.FLASHLIGHT", "使用闪光灯"
    ),
    SET_WALLPAPER(
        android.Manifest.permission.SET_WALLPAPER, "设置桌面壁纸"
    ),
    DISABLE_KEYGUARD(
        android.Manifest.permission.DISABLE_KEYGUARD, "禁用键盘锁"
    ),
    EXPAND_STATUS_BAR(
        android.Manifest.permission.EXPAND_STATUS_BAR, "状态栏控制"
    ),
    GET_TASKS(
        android.Manifest.permission.GET_TASKS, "获取任务信息"
    ),

    // 普通权限 - 系统设置
    WRITE_SETTINGS(
        android.Manifest.permission.WRITE_SETTINGS, "读写系统设置"
    ),
    SET_TIME(
        android.Manifest.permission.SET_TIME, "设置系统时间"
    ),
    SET_TIME_ZONE(
        android.Manifest.permission.SET_TIME_ZONE, "设置系统时区"
    ),
    CLEAR_APP_CACHE(
        android.Manifest.permission.CLEAR_APP_CACHE, "清除应用缓存"
    ),
    MODIFY_AUDIO_SETTINGS(
        android.Manifest.permission.MODIFY_AUDIO_SETTINGS, "修改声音设置"
    ),

    // 普通权限 - 通知
    ACCESS_NOTIFICATION_POLICY(
        android.Manifest.permission.ACCESS_NOTIFICATION_POLICY, "访问通知策略"
    ),
    POST_NOTIFICATIONS(
        "android.permission.POST_NOTIFICATIONS", "发送通知"
    ),

    // 特殊权限 - 悬浮窗 & 安装
    SYSTEM_ALERT_WINDOW(
        android.Manifest.permission.SYSTEM_ALERT_WINDOW, "悬浮窗"
    ),
    REQUEST_INSTALL_PACKAGES(
        android.Manifest.permission.REQUEST_INSTALL_PACKAGES, "安装未知来源应用"
    ),

    // 特殊权限 - 后台 & 电池
    REQUEST_IGNORE_BATTERY_OPTIMIZATIONS(
        "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", "忽略电池优化"
    ),
    KILL_BACKGROUND_PROCESSES(
        android.Manifest.permission.KILL_BACKGROUND_PROCESSES, "结束后台进程"
    ),

    // Android 12 蓝牙新权限
    BLUETOOTH_CONNECT(
        "android.permission.BLUETOOTH_CONNECT", "蓝牙连接"
    ),
    BLUETOOTH_SCAN(
        "android.permission.BLUETOOTH_SCAN", "蓝牙扫描"
    ),

    // Android 10 后台位置权限
    ACCESS_BACKGROUND_LOCATION(
        "android.permission.ACCESS_BACKGROUND_LOCATION", "后台定位"
    ),

    // Android 13 媒体权限
    READ_MEDIA_IMAGES(
        "android.permission.READ_MEDIA_IMAGES", "读取图片"
    ),
    READ_MEDIA_VIDEO(
        "android.permission.READ_MEDIA_VIDEO", "读取视频"
    ),
    READ_MEDIA_AUDIO(
        "android.permission.READ_MEDIA_AUDIO", "读取音频"
    ),

    // 其他
    USE_FINGERPRINT(
        android.Manifest.permission.USE_FINGERPRINT, "使用指纹"
    ),
    READ_LOGS(
        android.Manifest.permission.READ_LOGS, "读取系统日志"
    ),
    REBOOT(
        android.Manifest.permission.REBOOT, "重启设备"
    ),
    BATTERY_STATS(
        android.Manifest.permission.BATTERY_STATS, "电量统计"
    ),
    MANAGE_EXTERNAL_STORAGE(
        "android.permission.MANAGE_EXTERNAL_STORAGE", "管理所有文件"
    ), ;

    companion object {
        /**
         * 根据权限字符串查询中文名。
         *
         * @param permission Android Manifest 权限常量
         * @return 对应中文名，未找到时返回 null
         */
        @JvmStatic
        fun chineseNameOf(permission: String): String? =
            entries.find { it.permission == permission }?.chineseName

        /**
         * 根据权限字符串查询枚举项。
         *
         * @param permission Android Manifest 权限常量
         * @return 对应枚举项，未找到时返回 null
         */
        @JvmStatic
        fun of(permission: String): ChinesePermission? =
            entries.find { it.permission == permission }

        /**
         * 全量权限 → 中文名映射表。
         *
         * @return 不可变 Map，键为权限字符串，值为中文名
         */
        @JvmStatic
        val allMap: Map<String, String>
            get() = entries.associate { it.permission to it.chineseName }
    }
}

/**
 * 将单个权限字符串转换为中文名。
 *
 * @receiver 权限字符串（如 `android.permission.CAMERA`）
 * @return 对应中文名；未找到时返回原字符串本身，避免上层处理 null
 */
fun String.toPermissionChineseName(): String = ChinesePermission.chineseNameOf(this) ?: this

/**
 * 将多个权限字符串批量转换为中文名列表。
 *
 * @receiver 权限字符串数组
 * @return 按输入顺序的中文名列表，未匹配的项保留原字符串
 */
fun Array<out String>.toPermissionChineseNames(): List<String> =
    map { it.toPermissionChineseName() }

/**
 * 将多个权限字符串批量转换为中文名列表（List 重载）。
 *
 * @receiver 权限字符串列表
 * @return 按输入顺序的中文名列表，未匹配的项保留原字符串
 */
fun List<out String>.toPermissionChineseNames(): List<String> = map { it.toPermissionChineseName() }
