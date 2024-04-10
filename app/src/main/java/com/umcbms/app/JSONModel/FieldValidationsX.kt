package com.umcbms.app.JSONModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FieldValidationsX(
   var maxChar: String? = null,
   var minChar: String? = null,
   var maxLimit: Long? = null,
   var minLimit: Long? = null,
   var dbTable: String? = null,
   var access: String? = null,
   var qcRequired: Boolean? = null,
   var valueRequired: Boolean? = null,
   var multiSelect: Boolean? = null
) : Parcelable