package com.umcbms.app

import android.text.InputFilter
import android.text.Spanned


class InputFilterMinMax(private val min: Long, private val max: Long) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val input = (dest.toString().substring(0, dstart) +
                source.toString() + dest.toString().substring(dend))
        return if (isInRange(input)) {
            null
        } else {
            ""
        }
    }

    private fun isInRange(input: String): Boolean {
        val value = input.toLongOrNull()
        return value != null && value in min..max
    }
}
