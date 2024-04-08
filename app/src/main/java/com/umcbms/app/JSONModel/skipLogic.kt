package com.umcbms.app.JSONModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SkipLogicModel(
    var skipLogicQ: String? = null,
    var skipLogicVal: String? = null,
    var relation: String? = null,
    var flag: Boolean? = null,
    var data: ArrayList<SkipLogicModel>? = null
) : Parcelable
/*data class SkipLogic(
    var skipLogicQ: String? = null,
    var relation: String? = null,
    var flag: Boolean? = null,
    var data: ArrayList<SkipLogicModel>? = null
)*/

@Parcelize
data class VisibleLogicModel(
    var visibleLogicQ: String? = null,
    var visibleLogicVal: String? = null,
    var relation: String? = null,
    var flag: Boolean? = null,
    var data: ArrayList<VisibleLogicModel>? = null
) : Parcelable

