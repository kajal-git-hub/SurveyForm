package com.umcbms.app.JSONModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SkipLogicData(
    var skipLogicQ: String? = null,
    var skipLogicVal: String? = null,
    var flag: Boolean? = null
) : Parcelable
