package com.umcbms.app.api.respose

import com.google.gson.annotations.SerializedName

data class SchemaIndex(
    @SerializedName("data")
    val data: List<SchemaData>? = emptyList()
)
