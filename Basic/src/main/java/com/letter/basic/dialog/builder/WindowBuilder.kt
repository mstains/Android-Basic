package com.letter.basic.dialog.builder

import android.view.Gravity
import android.view.WindowManager

/**
 * Dialog Window 参数构造器。
 *
 * 采用流式 API：每个 setter 返回 `this` 以支持链式调用，
 * 避免 DialogFragment 子类编写大量临时变量。
 */
class WindowBuilder {

    var leftPadding = 0

    var rightPadding = 0

    var topPadding = 0

    var bottomPadding = 0

    var widthParam = WindowManager.LayoutParams.MATCH_PARENT

    var heightParam = WindowManager.LayoutParams.MATCH_PARENT

    var gravity = Gravity.CENTER

    var isTouchOutside = true

    var isCancelable = true

    var isAnim = true

    var isFocus = true

    /**
     * 设置 Dialog 根 View 的四向内边距。
     *
     * @param leftPadding 左边距（像素）
     * @param topPadding 上边距（像素）
     * @param rightPadding 右边距（像素）
     * @param bottomPadding 下边距（像素）
     * @return 当前 builder 以支持链式调用
     */
    fun padding(leftPadding: Int, topPadding: Int, rightPadding: Int, bottomPadding: Int): WindowBuilder {
        this.leftPadding = leftPadding
        this.topPadding = topPadding
        this.rightPadding = rightPadding
        this.bottomPadding = bottomPadding
        return this
    }

    /**
     * 设置 Dialog Window 宽度。
     *
     * @param widthParam 像素值或 [WindowManager.LayoutParams.MATCH_PARENT] / [WindowManager.LayoutParams.WRAP_CONTENT]
     * @return 当前 builder 以支持链式调用
     */
    fun width(widthParam: Int): WindowBuilder {
        this.widthParam = widthParam
        return this
    }

    /**
     * 设置 Dialog Window 高度。
     *
     * @param heightParam 像素值或 [WindowManager.LayoutParams.MATCH_PARENT] / [WindowManager.LayoutParams.WRAP_CONTENT]
     * @return 当前 builder 以支持链式调用
     */
    fun height(heightParam: Int): WindowBuilder {
        this.heightParam = heightParam
        return this
    }

    /**
     * 设置 Dialog 在屏幕中的对齐方式。
     *
     * @param gravity 取值参考 [android.view.Gravity]，如 [Gravity.BOTTOM] / [Gravity.CENTER]
     * @return 当前 builder 以支持链式调用
     */
    fun gravity(gravity: Int): WindowBuilder {
        this.gravity = gravity
        return this
    }

    /**
     * 设置点击 Dialog 外部是否自动 dismiss。
     *
     * @param isTouchOutside true 表示点击外部区域触发 dismiss，false 表示不响应
     * @return 当前 builder 以支持链式调用
     */
    fun onTouchOutSide(isTouchOutside: Boolean): WindowBuilder {
        this.isTouchOutside = isTouchOutside
        return this
    }

    /**
     * 设置是否允许通过返回键或外部触摸取消 Dialog。
     *
     * @param isCancelable true 表示可取消，false 表示强制保持 Dialog 显示
     * @return 当前 builder 以支持链式调用
     */
    fun cancelable(isCancelable: Boolean): WindowBuilder {
        this.isCancelable = isCancelable
        return this
    }

    /**
     * 设置是否使用 Dialog 主题默认的进出场动画。
     *
     * @param isAnim true 表示使用系统动画，false 表示无动画
     * @return 当前 builder 以支持链式调用
     */
    fun anim(isAnim: Boolean): WindowBuilder {
        this.isAnim = isAnim
        return this
    }

    /**
     * 设置 Dialog 窗口是否获取输入焦点。
     *
     * @param isFocus true 表示 Dialog 获得焦点（可接收返回键、按键），false 表示不抢焦点
     * @return 当前 builder 以支持链式调用
     */
    fun focus(isFocus: Boolean): WindowBuilder {
        this.isFocus = isFocus
        return this
    }
}
