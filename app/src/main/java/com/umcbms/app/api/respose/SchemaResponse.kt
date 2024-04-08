package com.umcbms.app.api.respose

import com.umcbms.app.JSONModel.JSONFormDataModel
import com.google.gson.annotations.SerializedName

data class SchemaResponse(
    @SerializedName("data")
    var data: JSONFormDataModel? = null
)