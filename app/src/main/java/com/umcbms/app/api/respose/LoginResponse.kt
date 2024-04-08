package com.umcbms.app.api.respose

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String? = "",

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("access")
    val access: ArrayList<Access>? = null,

    @SerializedName("superadmin")
    val superAdmin: Boolean? = false
)

data class Access(
    @SerializedName("subunit_id")
    val subunit_id: String? = "",
    @SerializedName("subunit_name")
    val subunit_name: String? = "",
    @SerializedName("permissions")
    val permissions: Any? = "",

)