package com.umcbms.app.JSONModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PropertiesX(
    var label: String? = null,
    var placeholder: String? = null,
    var qNumber: String? = null
) : Parcelable