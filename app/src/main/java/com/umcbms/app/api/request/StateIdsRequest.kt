package com.umcbms.app.api.request

import com.google.gson.annotations.SerializedName

data class StateIdsRequest(
    @SerializedName("state_ids")
    val stateIds: List<Int>? = emptyList()
)