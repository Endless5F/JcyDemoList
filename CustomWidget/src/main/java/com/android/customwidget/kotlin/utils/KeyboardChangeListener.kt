package com.baidu.searchbox.live.api.imx.listener

import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener

/**
 * val contentView = dialog?.window!!.decorView.findViewById<ViewGroup>(android.R.id.content)
 */
class KeyboardChangeListener(private val contentView: View) : OnGlobalLayoutListener {
    private var mOriginHeight = 0
    private var mPreHeight = 0
    private var mKeyBoardListen: KeyBoardListener? = null

    init {
        addContentTreeObserver()
    }

    interface KeyBoardListener {
        /**
         * call back
         * @param isShow true is show else hidden
         * @param keyboardHeight keyboard height
         */
        fun onKeyboardChange(isShow: Boolean, keyboardHeight: Int)
    }

    fun setKeyBoardListener(keyBoardListen: KeyBoardListener?) {
        mKeyBoardListen = keyBoardListen
    }

    private fun addContentTreeObserver() {
        contentView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        val currHeight = contentView.height //初始高度
        if (currHeight == 0) {
            return
        }
        var hasChange = false
        if (mPreHeight == 0) {
            mPreHeight = currHeight
            mOriginHeight = currHeight
        } else {
            if (mPreHeight != currHeight) { //说明布局变化了，
                hasChange = true
                mPreHeight = currHeight
            } else { //又变化了，这里默认回到了初始状态
                hasChange = false
            }
        }
        if (hasChange) { //弹出状态
            val isShow: Boolean
            var keyboardHeight = 0
            if (mOriginHeight == currHeight) { //mOriginHeight，初始高度
                //hidden
                isShow = false
            } else {
                //show
                keyboardHeight = mOriginHeight - currHeight //键盘高度
                isShow = true
            }
            if (mKeyBoardListen != null) {
                mKeyBoardListen!!.onKeyboardChange(isShow, keyboardHeight)
            }
        }
    }

    /**
     * 注销
     */
    fun destroy() {
        contentView.viewTreeObserver?.removeOnGlobalLayoutListener(this)
    }
}