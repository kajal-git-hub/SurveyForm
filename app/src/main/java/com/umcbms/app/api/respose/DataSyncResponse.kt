package com.umcbms.app.api.respose

import com.umcbms.app.JSONModel.JSONFormDataModel
import com.google.gson.annotations.SerializedName

data class DataSyncResponse(


    @SerializedName("title")
    val title: String? = "",

    @SerializedName("message")
    val message: String? = "",

    @SerializedName("data")
    val data: String? = "",

    )

data class DataSyncList(

    @SerializedName("message")
    val message: String? = "",

    @SerializedName("code")
    val code: Int,

    @SerializedName("data")
    val data: ArrayList<DataSyncData> = arrayListOf()
)

data class DataSyncData(

    @SerializedName("id")
    val id: String? = "",
    @SerializedName("field_value_map")
    val field_value_map: MutableMap<String, Any> = mutableMapOf(),
    @SerializedName("schema")
    val schema: JSONFormDataModel? = null,
    @SerializedName("created_at")
    val created_at: String? = ""

)

data class DataSyncDataResponse(
    @SerializedName("title")
    val title: String? = "",

    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String? = "",

    @SerializedName("data")
    val data: DataSyncData = DataSyncData()
)

data class SyncDataResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("message")
    val message: String? = "",

    @SerializedName("data")
    val data: SyncData?
)
data class SyncData(

    @SerializedName("form_response_id")
    val form_response_id: String = "",


)