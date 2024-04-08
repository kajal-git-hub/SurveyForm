package com.umcbms.app.api.request

import com.google.gson.annotations.SerializedName


data class DistrictIdsRequest(
    @SerializedName("district_ids")
    val districtIds: List<Int>? = emptyList()
)