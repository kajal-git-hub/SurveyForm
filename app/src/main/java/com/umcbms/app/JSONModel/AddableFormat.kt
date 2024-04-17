package com.umcbms.app.JSONModel


data class AddableFormat(
    var localId: String? = null,
    var id: String? = null,
    var label: String? = null,
    var placeholder: String? = null,
    var valueRequired: Boolean? = null,
    var multiSelect: Boolean? = null,
    var defaultVisibility: Boolean? = null,
    var options: List<String>? = null,
    var customOption: Any? = null,
    var customValidation: Any? = null,
    var type: String? = null,
    var minLimit:Long? = null,
    var maxLimit:Long?= null,
    var qNumber: String? = null,
    var skipLogic: List<SkipLogicModel>? = null,
    var visibleLogic: List<VisibleLogicModel>? = null,
)