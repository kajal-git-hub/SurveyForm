package com.umcbms.app.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

class TfButton : AppCompatButton {
    private var _ctx: Context? = null

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
            //typeface = CommonUtils.getRegularFont(_ctx!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
