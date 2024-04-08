package com.umcbms.app.JSONModel


data class Children(
    var id: String? = null,
    var type: String? = null,
    var properties: PropertiesX? = null,
    var fieldValidations: FieldValidationsX? = null,
    var skipLogic: List<SkipLogicModel>? = null,
    var visibleLogic: List<VisibleLogicModel>? = null,
    var options: List<OptionsList>? = null,
    var addableFormat: List<AddableFormat>? = null,
    var addButtonTitle: String? = null,
    var addableMax: String? = null,
    var addableDefault: String? = null,
    var viewHome: Boolean? = null,
    var filterHome: Boolean? = null,
    var defaultVisibility: Boolean? = null,
    var homeLabel: String? = null,
    var title: String? = null,
    var acronym: String?= null,
    var isActive: Boolean? = null,
    var relativeOptionsLogic: String? = null,
    var autoPopulateId: String? = null,
    var children: List<Children>? = null,
    var primaryView: Boolean? = null,
    var secondaryView: Boolean? = null,
    var qcRaised: Boolean? = null,
    var qcRemark: String? = null,
    var viewIndex: Int? = null,
    var value: Any? = null,
    var customOption: Any? = null,
)

//var children: List<ChildrenX>? = null,

