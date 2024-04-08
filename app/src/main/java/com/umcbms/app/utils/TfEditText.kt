package com.umcbms.app.utils

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class TfEditText : AppCompatEditText {
    private var _ctx: Context? = null
    private var keyImeChangeListener: KeyImeChange? = null

    constructor(context: Context) : super(context) {
        if (!isInEditMode) {
            this._ctx = context
            init()
        }
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        if (!isInEditMode) {
            this._ctx = context
            init()
        }
    }

    private fun init() {
        try {
            // typeface = _ctx?.let { CommonUtils.getRegularFont(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun setKeyImeChangeListener(listener: KeyImeChange?) {
        keyImeChangeListener = listener
    }

    interface KeyImeChange {
        fun onKeyIme(keyCode: Int, event: KeyEvent?)
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyImeChangeListener != null) {
            keyImeChangeListener!!.onKeyIme(keyCode, event)
        }
        return false
    }
}
