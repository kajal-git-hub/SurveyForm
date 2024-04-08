package com.umcbms.app.Home.FormData

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.tabs.TabLayout

@SuppressLint("ClickableViewAccessibility")
class CustomTabLayout : TabLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.actionMasked == MotionEvent.ACTION_SCROLL && !isScrollingEnabled()) {

            return false
        }

        return super.onTouchEvent(event)
    }

    private fun isScrollingEnabled(): Boolean {

        return true
    }
}