package com.umcbms.app.JSONModel

data class ChildrenX(
    val addButtonTitle: String? = null,
    val addableDefault: Int? = null,
    val addableMax: Int? = null,
    val addable_format: List<AddableFormat>? = null,
    val fieldValidations: FieldValidationsX? = null,
    val filterHome: Boolean? = null,
    val id: String? = "",
    val properties: PropertiesX? = null,
    val type: String? = "",
    val viewHome: Boolean? = null,
    val options: List<OptionsList>? = null
)