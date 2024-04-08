package com.umcbms.app.api.respose

import com.google.gson.annotations.SerializedName


data class StatesResponse(
    val code: Int,
    val message: String,
    @SerializedName("data")val data: List<State> = emptyList()
)
data class CitiesResponse(
    val code: Int,
    val message: String,
    @SerializedName("data")val data: Cities = Cities()
)


