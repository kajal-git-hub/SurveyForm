package com.umcbms.app.api.respose

import com.google.gson.annotations.SerializedName


data class QCResponse(

    @SerializedName("code")
    val code: Int = 0,

    @SerializedName("message")
    val message: String? = "",

    @SerializedName("data")
    val data: QCResponseData = QCResponseData()
)

data class QCResponseData(

    @SerializedName("count")
    val count: Int = 0,

    @SerializedName("data")
    val data: ArrayList<QCData> = arrayListOf()
)
data class QCData(

    @SerializedName("id")
    val id: String = "0",
    @SerializedName("qc_id")
    val qcId: String? = "",
    @SerializedName("form_id")
    val formId: String = "0",
    @SerializedName("status")
    val status: String? = "",
    @SerializedName("review_status")
    val review_status: String? = "",
    @SerializedName("primary_view_map")
    val primary_view_map: MutableMap<String,String> = mutableMapOf(),
    @SerializedName("qc_schema")
    val qc_schema: String = "",
    @SerializedName("created_at")
    val created_at: String? = ""

)