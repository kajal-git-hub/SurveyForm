package com.umcbms.app.api.respose

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int? = 0,

    @SerializedName("name")
    val name: String? = "",

    @SerializedName("email")
    val email: String? = "",

    @SerializedName("email_verified_at")
    val emailVerifiedAt: String? = "",

    @SerializedName("status")
    val status: Int? = 0,

    @SerializedName("created_by")
    val createdBy: String? = "",

    @SerializedName("deleted_at")
    val deletedAt: String? = "",

    @SerializedName("created_at")
    val createdAt: String? = "",

    @SerializedName("updated_at")
    val updatedAt: String? = ""
)