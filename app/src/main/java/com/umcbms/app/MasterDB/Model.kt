package com.umcbms.app.MasterDB

import android.os.Parcel
import android.os.Parcelable

data class FormData(
    var id: Long? = 0,
    var formId: Int? = 0,
    var formName: String? = "",
    var version: String? = "",
    var formSchema: String? = "",
    var createdBy: String? = "",
    var createdAt: String? = "",
    var updateAt: String? = "",
    var isDeleted: Boolean? = null,
    var deletedAt: String? = ""
)

data class FormSubmissionData(
    val id: Long = 0,
    val formId: Int,
    val status: String,
    val responseJson: String?,
    val createdBy: String?,
    val createdAt: String,
    val updateAt: String?,
    val isDeleted: Boolean?,
    val deletedAt: String?
)

data class FormListModel(
    val formName: String
)

data class FormJsonDataModel(
    val formSchema: String
)

data class FormDataListModel(
    val formName: String
)

data class FormDataID(
    val id: Int
)

data class SubID(
    val id: Long
)

data class SubFormId(
    val formId: Int
)

data class SubStatusDate(
    val status: String,
    val date: String
)

data class SkipLogicCondition(
    val skipLogicQ: String,
    val skipLogicVal: String
)

data class VisibleCondition(
    val childId: String?,
    val selectedItemsId: String?
)

data class FormIndexData(
    val id: Int? = null,
    val name: String? = null,
    val version: String? = null,
    val canSurvey: Int? = null,
    val canQC: Int? = null,
    val canValidate: Int? = null
)


data class AddableData(
    val key: String,
    val value: String
)


/*
data class ViewCreate(
    val primaryView: Boolean = false,
    val secondaryView: Boolean = false,
    val viewIndex: Int,
    val value: Any,
    val type: String?,
    val options: MutableMap<Int, String>? = null,
    val label: String?
)*/

data class ViewArrayModel(
    val id: String?,
    val type: String?,
    val label: String?,
    val viewIndex: Int,
    val options: MutableMap<Int, String>? = null,
    val primaryView: Boolean = false,
    val secondaryView: Boolean = false,
    val value: String
)

data class QCViewArrayModel(
    val id: String?,
    val type: String?,
    val label: String?,
    val options: MutableMap<Int, String>? = null,
    val value: String
)

data class ViewCreate(
    val primaryView: Boolean = false,
    val secondaryView: Boolean = false,
    val viewIndex: Int,
    val value: Any,
    val type: String?,
    val options: MutableMap<Int, String>? = null,
    val label: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readValue(Any::class.java.classLoader)!!, // !! is used here to force non-null
        parcel.readString(),
        parcel.readHashMap(Int::class.java.classLoader) as? MutableMap<Int, String>,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (primaryView) 1 else 0)
        parcel.writeByte(if (secondaryView) 1 else 0)
        parcel.writeInt(viewIndex)
        parcel.writeValue(value)
        parcel.writeString(type)
        parcel.writeMap(options)
        parcel.writeString(label)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ViewCreate> {
        override fun createFromParcel(parcel: Parcel): ViewCreate {
            return ViewCreate(parcel)
        }

        override fun newArray(size: Int): Array<ViewCreate?> {
            return arrayOfNulls(size)
        }
    }
}