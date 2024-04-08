package com.umcbms.app.api.respose

import com.umcbms.app.JSONModel.JSONFormDataModel

data class ShowSchemaResponse(
    val code: Int,
    val message: String,
    val data: ProjectShowResponse
)

data class ProjectShowResponse (
    val projectName: String,
    val projectAcronym: String,
    val subunitName: String,
    val subunitAcronym: String,
    val data: JSONFormDataModel ? = null
)
