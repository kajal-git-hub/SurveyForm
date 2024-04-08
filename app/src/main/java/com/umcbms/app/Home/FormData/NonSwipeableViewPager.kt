package com.umcbms.app.Home.FormData

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonSwipeableViewPager : ViewPager {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Disable swipe by returning false
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Disable intercepting touch events to prevent swiping
        return false
    }
}