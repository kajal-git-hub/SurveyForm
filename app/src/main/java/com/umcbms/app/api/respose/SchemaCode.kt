package com.umcbms.app.api.respose

data class SchemaCode(
    val code: Int,
    val message: String,
    val data: List<ProjectResponse>
)
