package com.eknow.annularmenu.listener

import android.view.MotionEvent

/**
 * @Description:
 * @author: Eknow
 * @date: 2022/4/6 9:18
 */
interface OnMenuTouchListener {

    fun OnTouch(event: MotionEvent?, position: Int)
}