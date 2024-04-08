package com.umcbms.app.api.respose

import com.google.gson.annotations.SerializedName

data class SchemaData(
    @SerializedName("id")
    val id: Int? = 0,

    @SerializedName("name")
    val name: String? = "",

    @SerializedName("version")
    val version: String? = "",

    @SerializedName("can_survey")
    val canSurvey: Int? = 0,

    @SerializedName("can_qc")
    val canQc: Int? = 0,

    @SerializedName("can_validate")
    val canValidate: Int? = 0
)