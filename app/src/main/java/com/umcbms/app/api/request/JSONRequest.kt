package com.umcbms.app.api.request


data class SyncedJSONRequest(
    var form_response_id: Int? = null,
    var data:  ArrayList<JSONRequest> = arrayListOf(),
)
data class JSONRequest(
    var id: String? = null,
    var flag: Boolean? = null,
    var value: Any? = null,
    var newBase64Values: Any? = null
)

data class QCJSONRequest(
    var id: String? = null,
    var flag: Boolean? = null,
    var qcResolve: Boolean? = null,
    var qcInteract: Boolean? = null,
    var value: Any? = null,
    var newBase64Values: Any? = null
)