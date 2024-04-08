package com.umcbms.app.api.respose

import com.google.gson.annotations.SerializedName

data class LogoutResponse(
    val code: Int,
    @SerializedName("message")
    val message:  String? = "",
    val data: List<Any>
)