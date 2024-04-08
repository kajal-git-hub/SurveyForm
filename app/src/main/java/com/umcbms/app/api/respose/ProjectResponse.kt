package com.umcbms.app.api.respose

data class ProjectResponse(
    val id: Int,
    val name: String,
    val acronym: String,
    val deletedAt: String?, // Nullable field
    val createdAt: String,
    val updatedAt: String,
    val subunits: List<SchemaData>
)