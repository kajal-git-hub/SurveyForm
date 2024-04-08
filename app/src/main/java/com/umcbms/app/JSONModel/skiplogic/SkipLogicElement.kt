package com.umcbms.app.JSONModel.skiplogic

import com.umcbms.app.JSONModel.SkipLogicData

data class SkipLogicElement(
    val relation: String?,
    val flag: Boolean?,
    val data: List<SkipLogicData>?
)
