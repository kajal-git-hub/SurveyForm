package com.umcbms.app.JSONModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OptionsList(
    var id: Int? = null,
    var value: String? = null
) : Parcelable
