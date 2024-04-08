package com.umcbms.app.MasterDB

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.umcbms.app.api.respose.State
import org.json.JSONObject

private const val TAG = "MasterDBHelper"

class MasterDBHelper(val context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "MyDatabase"
        const val DATABASE_VERSION = 1

        const val ACCESS_PERMISSION_TABLE_NAME = "ACCESS_PERMISSION"
        const val TABLE_NAME = "FORM_MASTER"
        const val FORM_SUBMISSION_TABLE_NAME = "FORM_SUBMISSION_TABLE"
        const val FORM_INDEX_TABLE_NAME = "FORM_INDEX_TABLE"

        const val STATE_MASTER_TABLE_NAME = "STATE_MASTER"
        const val DISTRICT_MASTER_TABLE_NAME = "DISTRICT_MASTER"
        const val CITY_MASTER_TABLE_NAME = "CITY_MASTER"

        const val USER_STATE_TABLE_NAME = "USER_STATE"
        const val USER_DISTRICT_TABLE_NAME = "USER_DISTRICT"
        const val USER_CITY_TABLE_NAME = "USER_CITY"

        const val ID = "id"
        const val FORM_ID = "form_id"
        const val SUBUNIT_ID = "subunit_id"
        const val SUBUNIT_NAME = "subunit_name"
        const val FORM_NAME = "form_name"
        const val PERMISSIONS = "permissions"
        const val VERSION = "version"
        const val FORM_SCHEMA = "form_schema"
        const val CREATED_BY = "created_by"
        const val CREATED_AT = "created_at"
        const val UPDATE_AT = "update_at"
        const val IS_DELETED = "is_deleted"
        const val DELETED_AT = "deleted_at"
        const val STATUS = "status"
        const val PRIMARY_VIEW = "primary_view"
        const val SECONDARY_VIEW = "secondary_view"
        const val QC_SCHEMA = "qc_schema"
        const val STATE_ID = "state_id"
        const val DISTRICT_ID = "district_id"
        const val RESPONSE_JSON = "response_json"
        const val SUB_ID = "sub_id"
        const val NAME = "name"
        const val CAN_SURVEY = "can_survey"
        const val CAN_QC = "can_qc"
        const val CAN_VALIDATE = "can_validate"
        const val RESPONSE_ID = "response_id"
        const val MASTER_FORM_ID = "master_form_id"
        const val QC_ID = "qc_id"
        const val FORM_SUB_ID = "form_sub_id"
        const val DRAFT = "Draft"
        const val SYNCED = "Synced"
        const val SAVED = "Saved"
        const val SUMMERY = "_SUMMERY"
        const val QC = "_QC"
        const val QCDOQNLOADPENDING = "QCDownloadPending"
        const val QCRAISED = "QCRaised"
        const val QCRESOLVED = "QCResolved"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $FORM_ID INTEGER,
                $FORM_NAME TEXT,
                $VERSION TEXT,
                $FORM_SCHEMA TEXT,
                $CREATED_BY TEXT,
                $CREATED_AT TEXT,
                $UPDATE_AT TEXT,
                $IS_DELETED INTEGER,
                $DELETED_AT TEXT
            )
        """.trimIndent()

        db?.execSQL(createTableQuery)

        val createTableAccessPermission = """
            CREATE TABLE $ACCESS_PERMISSION_TABLE_NAME (
                $SUBUNIT_ID INTEGER,
                $SUBUNIT_NAME TEXT,
                $PERMISSIONS TEXT
            )
        """.trimIndent()

        db?.execSQL(createTableAccessPermission)

        val createTableFormSubmission = """
             CREATE TABLE $FORM_SUBMISSION_TABLE_NAME (
                $ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $FORM_ID INTEGER,
                $STATUS TEXT,
                $RESPONSE_JSON TEXT,
                $CREATED_BY TEXT,
                $CREATED_AT TEXT,
                $UPDATE_AT TEXT,
                $IS_DELETED INTEGER,
                $DELETED_AT TEXT
            )
            """.trimIndent()

        db?.execSQL(createTableFormSubmission)

        val createTableFormIndex = """
             CREATE TABLE $FORM_INDEX_TABLE_NAME (
                $ID INTEGER,
                $NAME TEXT,
                $VERSION TEXT,
                $CAN_SURVEY INTEGER,
                $CAN_QC INTEGER,
                $CAN_VALIDATE INTEGER
            )
            """.trimIndent()

        db?.execSQL(createTableFormIndex)

        val createTableStateMaster = """
             CREATE TABLE $STATE_MASTER_TABLE_NAME (
                $ID INTEGER,
                $NAME TEXT,
                $STATUS INTEGER,
                $CREATED_AT TEXT,
                $UPDATE_AT TEXT,
                $DELETED_AT TEXT
            )
            """.trimIndent()

        db?.execSQL(createTableStateMaster)

        val createTableDistrictMaster = """
             CREATE TABLE $DISTRICT_MASTER_TABLE_NAME (
                $ID INTEGER,
                $NAME TEXT,
                $STATE_ID INTEGER,
                $STATUS INTEGER,
                $CREATED_AT TEXT,
                $UPDATE_AT TEXT,
                $DELETED_AT TEXT
            )
            """.trimIndent()

        db?.execSQL(createTableDistrictMaster)


        val createTableCityMaster = """
             CREATE TABLE $CITY_MASTER_TABLE_NAME (
                $ID INTEGER,
                $NAME TEXT,
                $DISTRICT_ID INTEGER,
                $STATUS INTEGER,
                $CREATED_AT TEXT,
                $UPDATE_AT TEXT,
                $DELETED_AT TEXT
            )
            """.trimIndent()

        db?.execSQL(createTableCityMaster)

        val createTableUserState = """
             CREATE TABLE $USER_STATE_TABLE_NAME (
                $ID INTEGER,
                $NAME TEXT,
                $STATUS INTEGER,
                $CREATED_AT TEXT,
                $UPDATE_AT TEXT,
                $DELETED_AT TEXT
            )
            """.trimIndent()

        db?.execSQL(createTableUserState)

        val createTableUserDistrict = """
             CREATE TABLE $USER_DISTRICT_TABLE_NAME (
                $ID INTEGER,
                $NAME TEXT,
                $STATE_ID INTEGER,
                $STATUS INTEGER,
                $CREATED_AT TEXT,
                $UPDATE_AT TEXT,
                $DELETED_AT TEXT
            )
            """.trimIndent()

        db?.execSQL(createTableUserDistrict)


        val createTableUserCity = """
             CREATE TABLE $USER_CITY_TABLE_NAME (
                $ID INTEGER,
                $NAME TEXT,
                $DISTRICT_ID INTEGER,
                $STATUS INTEGER,
                $CREATED_AT TEXT,
                $UPDATE_AT TEXT,
                $DELETED_AT TEXT
            )
            """.trimIndent()

        db?.execSQL(createTableUserCity)

    }

    fun insertStateMaster(dataList: List<State>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (data in dataList) {
                val values = ContentValues()
                values.put(ID, data.id)
                values.put(NAME, data.name)
                values.put(STATUS, data.status)
                values.put(CREATED_AT, data.createdAt)
                values.put(UPDATE_AT, data.updatedAt)
                values.put(DELETED_AT, data.deletedAt)

                db.insert(STATE_MASTER_TABLE_NAME, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun insertDistrictMaster(dataList: List<State>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (data in dataList) {
                val values = ContentValues()
                values.put(ID, data.id)
                values.put(NAME, data.name)
                values.put(STATE_ID, data.state_id)
                values.put(STATUS, data.status)
                values.put(CREATED_AT, data.createdAt)
                values.put(UPDATE_AT, data.updatedAt)
                values.put(DELETED_AT, data.deletedAt)

                db.insert(DISTRICT_MASTER_TABLE_NAME, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getSummeryTable(tableName: String): Cursor? {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $tableName", null)
    }

    fun insertCityMaster(dataList: List<State>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (data in dataList) {
                val values = ContentValues()
                values.put(ID, data.id)
                values.put(NAME, data.name)
                values.put(DISTRICT_ID, data.district_id)
                values.put(STATUS, data.status)
                values.put(CREATED_AT, data.createdAt)
                values.put(UPDATE_AT, data.updatedAt)
                values.put(DELETED_AT, data.deletedAt)

                db.insert(CITY_MASTER_TABLE_NAME, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @SuppressLint("Range")
    fun getSummeryTableByFormId(tableName: String): Cursor? {
        //val selectionArgs = arrayOf(masterFormId.toString())
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $tableName", null)
    }

    fun getSummeryTableByStatus(tableName: String, status: String): Cursor? {
        val db = readableDatabase
        val selection = "$STATUS = ?"
        val selectionArgs = arrayOf(status)
        return db.query(tableName, null, selection, selectionArgs, null, null, null)
    }

    fun getColumnNames(tableName: String): Array<String> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName LIMIT 0", null)
        val columnNames = cursor.columnNames
        cursor.close()
        return columnNames
    }

    fun insertData(tableName: String, values: ContentValues): Long {
        val db = this.writableDatabase

        return db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertSummeryData(
        tableName: String,
        masterFormId: Int,
        formSubId: Int? = null,
        responseId: String? = null,
        status: String,
        childId: String,
        data: String
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(childId, data)
        values.put(MASTER_FORM_ID, masterFormId)
        values.put(FORM_SUB_ID, formSubId)
        values.put(RESPONSE_ID, responseId)
        values.put(STATUS, status)
        return db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateSummeryData(tableName: String, values: ContentValues, id: Int) {
        val db = writableDatabase
        db.update(tableName, values, "$ID=?", arrayOf(id.toString()))
        db.close()
    }

    fun createSummeryTables(tableName: String, jsonString: String) {
        val jsonObject = JSONObject(jsonString)
        val dataArray = jsonObject.getJSONArray("data")
        //val tbNAcr = jsonObject?.getString("acronym")
        val colArr = mutableMapOf<String, String>()
        colArr.clear()

        for (i in 0 until dataArray.length()) {
            val sectionObject = dataArray.getJSONObject(i)
            createSummeryTablesSection(sectionObject, tableName, colArr)
        }

        createSummeryQuery(tableName, colArr)
        Log.d(TAG, "createSummeryTables: $colArr")
    }

    private fun createSummeryTablesSection(
        sectionObject: JSONObject,
        tableName: String,
        colArr: MutableMap<String, String>
    ) {

        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            when (childObject.getString("type")) {

                "SECTION" -> {
                    createSummeryTablesSection(childObject, tableName, colArr)
                }

                "ADDABLE" -> {}

                else -> {
                    if (childObject.has("primaryView")) {
                        colArr[childObject.getString("id")] = "TEXT"
                    }
                }
            }
        }
    }

    private fun createSummeryQuery(tabName: String, colArr: MutableMap<String, String>) {
        try {
            val db = writableDatabase

            val sqlCreateTable = StringBuilder()
            sqlCreateTable.append("CREATE TABLE IF NOT EXISTS $tabName ( $ID INTEGER PRIMARY KEY AUTOINCREMENT, $MASTER_FORM_ID INTEGER, $FORM_SUB_ID INTEGER, $RESPONSE_ID INTEGER, $STATUS TEXT,")

            for ((columnName, columnType) in colArr) {
                sqlCreateTable.append("$columnName $columnType, ")
            }

            sqlCreateTable.delete(sqlCreateTable.length - 2, sqlCreateTable.length)
            sqlCreateTable.append(")")
            db.execSQL(sqlCreateTable.toString())
        } catch (e: Exception) {
            Toast.makeText(context, "createDynamicTable Exception: $e", Toast.LENGTH_SHORT)
                .show()
        }
    }
    fun createTableQCQuery(tabName: String) {
        try {
            val db = writableDatabase

            val sqlCreateTable = StringBuilder()
            sqlCreateTable.append("CREATE TABLE IF NOT EXISTS $tabName ( $ID INTEGER PRIMARY KEY AUTOINCREMENT, $QC_ID INTEGER, $FORM_ID INTEGER, $STATUS TEXT, $PRIMARY_VIEW TEXT, $SECONDARY_VIEW TEXT, $QC_SCHEMA TEXT)")

            db.execSQL(sqlCreateTable.toString())
        } catch (e: Exception) {
            Toast.makeText(context, "createTableQCQuery Exception: $e", Toast.LENGTH_SHORT)
                .show()
        }
    }


    fun insertFormIndexData(formData: FormIndexData): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, formData.id)
        contentValues.put(NAME, formData.name)
        contentValues.put(VERSION, formData.version)
        contentValues.put(CAN_SURVEY, formData.canSurvey)
        contentValues.put(CAN_QC, formData.canQC)
        contentValues.put(CAN_VALIDATE, formData.canValidate)
        return db.insert(FORM_INDEX_TABLE_NAME, null, contentValues)
    }

    fun updateFormIndexData(formData: FormIndexData): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(NAME, formData.name)
        contentValues.put(VERSION, formData.version)
        contentValues.put(CAN_SURVEY, formData.canSurvey)
        contentValues.put(CAN_QC, formData.canQC)
        contentValues.put(CAN_VALIDATE, formData.canValidate)
        return db.update(
            FORM_INDEX_TABLE_NAME,
            contentValues,
            "$ID=?",
            arrayOf(formData.id.toString())
        )
    }

    @SuppressLint("Range", "Recycle")
    fun getFormIndexDataById(idDB: Int): FormIndexData? {
        var formData: FormIndexData? = null
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $FORM_INDEX_TABLE_NAME WHERE $ID=$idDB", null)

        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(ID))
            val name = cursor.getString(cursor.getColumnIndex(NAME))
            val version = cursor.getString(cursor.getColumnIndex(VERSION))
            val canSurvey = cursor.getInt(cursor.getColumnIndex(CAN_SURVEY))
            val canQC = cursor.getInt(cursor.getColumnIndex(CAN_QC))
            val canValidate = cursor.getInt(cursor.getColumnIndex(CAN_VALIDATE))

            formData = FormIndexData(id, name, version, canSurvey, canQC, canValidate)
        }
        return formData
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun createSectionTables(jsonString: String?) {
        val jsonObject = jsonString?.let { JSONObject(it) }
        val dataArray = jsonObject?.getJSONArray("data")
        val tbNAcr = jsonObject?.getString("acronym")

        for (i in 0 until dataArray?.length()!!) {
            val sectionObject = dataArray.getJSONObject(i)
            createSectionTablesIn(sectionObject, tbNAcr.toString())
        }
    }

    private fun createSectionTablesIn(sectionObject: JSONObject, tbNmAcr: String) {

        val childrenArray = sectionObject.getJSONArray("children")

        if (sectionObject.getString("type").equals("SECTION")) {

            val colArr = mutableMapOf<String, String>()
            val colArrSecIn = mutableMapOf<String, String>()

            colArr.clear()
            colArrSecIn.clear()

            for (j in 0 until childrenArray.length()) {

                val childObject = childrenArray.getJSONObject(j)
                val viewType = childObject.getString("type")
                if (viewType != "SECTION") {
                    var varType: String
                    if (viewType == "NUMBERS") {
                        varType = "INTEGER"
                        colArr.put(childObject.getString("id"), varType)
                    }/*else if ( viewType == "ADDABLE"){
                        val tbNmAcr2 = tbNmAcr +"_"+ sectionObject.getString("acronym")
                        createAddableTables(childObject, tbNmAcr2)
                    }*/
                    else {
                        varType = "TEXT"
                        colArr.put(childObject.getString("id"), varType)
                    }


                } else {
                    createSectionTablesIn(childObject, tbNmAcr)
                }
            }
            val tabName = tbNmAcr + "_" + sectionObject.getString("acronym")
            try {
                createDynamicTable(tabName, colArr, colArrSecIn, "tabNameSecIn")
            } catch (_: Exception) {
            }
            colArr.clear()
            colArrSecIn.clear()
        }
    }

    private fun createAddableTables(childObject: JSONObject, tbNmAcr: String) {
        val colAddableArr = mutableMapOf<String, String>()
        colAddableArr.clear()

        var varType: String

        val addableFormat = childObject.getJSONArray("addableFormat")
        for (k in 0 until addableFormat.length()) {
            val addableItem = addableFormat.getJSONObject(k)
            val addableType = addableItem.getString("type")
            val addableId = addableItem.getString("id")
            if (addableType == "ADDABLE_NUMBERS") {
                varType = "INTEGER"
                colAddableArr.put(addableId, varType)
            } else {
                varType = "TEXT"
                colAddableArr.put(addableId, varType)
            }
        }
        val tabName = tbNmAcr + "_" + childObject.getString("acronym")

        createDynamicAddableTable(tabName, colAddableArr)
    }

    private fun createDynamicAddableTable(
        tabName: String,
        colAddableArr: MutableMap<String, String>
    ) {
        addableTableCreateQuery(tabName, colAddableArr)
    }

    private fun addableTableCreateQuery(
        tabName: String,
        colAddableArr: MutableMap<String, String>
    ) {
        val db = writableDatabase

        val sqlCreateTable = StringBuilder()
        sqlCreateTable.append("CREATE TABLE IF NOT EXISTS $tabName ( $ID INTEGER PRIMARY KEY AUTOINCREMENT, $SUB_ID INTEGER,")

        for ((columnName, columnType) in colAddableArr) {
            sqlCreateTable.append("$columnName $columnType, ")
        }

        sqlCreateTable.delete(sqlCreateTable.length - 2, sqlCreateTable.length)
        sqlCreateTable.append(")")
        db.execSQL(sqlCreateTable.toString())
    }

    private fun createDynamicTable(
        tabName: String,
        colArr: MutableMap<String, String>,
        colArrSecIn: MutableMap<String, String>,
        tabNameSecIn: String
    ) {
        if (colArr != null) {
            tableCreateQuery(tabName, colArr)
        } else if (colArrSecIn != null) {
            tableCreateQuery(tabNameSecIn, colArrSecIn)
        }
    }

    private fun tableCreateQuery(tabName: String, colArr: MutableMap<String, String>) {
        try {
            val db = writableDatabase

            val sqlCreateTable = StringBuilder()
            sqlCreateTable.append("CREATE TABLE IF NOT EXISTS $tabName ( $ID INTEGER PRIMARY KEY AUTOINCREMENT, $SUB_ID INTEGER,")

            for ((columnName, columnType) in colArr) {
                sqlCreateTable.append("$columnName $columnType, ")
            }

            sqlCreateTable.delete(sqlCreateTable.length - 2, sqlCreateTable.length)
            sqlCreateTable.append(")")
            db.execSQL(sqlCreateTable.toString())
        } catch (e: Exception) {
            Toast.makeText(context, "createDynamicTable Exception: $e", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun insertSectionData(tableName: String, childId: String, data: String, subID: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(childId, data)
        values.put(SUB_ID, subID)
        // values.put("data", data)
        db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun updateSectionData(tableName: String, childId: String, data: String, subID: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(childId, data)
        db.update(tableName, values, "$SUB_ID=?", arrayOf(subID.toString()))
        db.close()
    }

    fun insertAddableData(tableName: String, subID: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(SUB_ID, subID)
        // values.put("data", data)
        var id =
            db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        return id
    }

    fun updateAddableData(tableName: String, childId: String, data: String, id: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(childId, data)
        db.update(tableName, values, "$ID=?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteAddableDataById(tableName: String, id: Long): Int {
        val db = writableDatabase

        val rowsAffected = db.delete(tableName, "$ID=?", arrayOf(id.toString()))

        db.close()
        return rowsAffected
    }

    fun getSectionTable(tabName: String): Cursor? {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $tabName", null)
    }

    fun getSectionTableWithSubIdCondition(tabName: String, subId: Int): Cursor? {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $tabName where $SUB_ID=$subId", null)
    }

    fun getSectionTableDataById(tableName: String, id: Long): Map<String, String>? {
        val data = mutableMapOf<String, String>()
        val db = readableDatabase

        val cursor = db.query(
            tableName,
            null,
            "$SUB_ID=?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val columnNames = cursor.columnNames
            for (columnName in columnNames) {
                val columnIndex = cursor.getColumnIndex(columnName)

                if (columnIndex != -1) {
                    val columnValue = cursor.getString(columnIndex)

                    if (columnValue != null) {
                        data[columnName] = columnValue
                    } else {
                        data[columnName] = "NULL"
                    }
                } else {
                    data[columnName] = "INVALID_COLUMN"
                }
            }
            cursor.close()
        }
        db.close()

        return if (data.isNotEmpty()) data else null
    }


    @SuppressLint("Range")
    fun getSubmissionTable(): List<FormSubmissionData> {
        val forms = mutableListOf<FormSubmissionData>()
        val cursor =
            readableDatabase.query(
                FORM_SUBMISSION_TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
            )

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(ID))
            val formId = cursor.getInt(cursor.getColumnIndex(FORM_ID))
            val status = cursor.getString(cursor.getColumnIndex(STATUS))
            val responseJson = cursor.getString(cursor.getColumnIndex(RESPONSE_JSON))
            val createdBy = cursor.getString(cursor.getColumnIndex(CREATED_BY))
            val createdAt = cursor.getString(cursor.getColumnIndex(CREATED_AT))
            val updateAt = cursor.getString(cursor.getColumnIndex(UPDATE_AT))
            val isDeleted = (cursor.getInt(cursor.getColumnIndex(IS_DELETED)) == 1)
            val deletedAt = cursor.getString(cursor.getColumnIndex(DELETED_AT))

            val formData = FormSubmissionData(
                id,
                formId,
                status,
                responseJson,
                createdBy,
                createdAt,
                updateAt,
                isDeleted,
                deletedAt
            )
            forms.add(formData)
        }

        cursor.close()
        return forms
    }

    @SuppressLint("Range")
    fun getSubmissionTableByFormId(formId: Int): List<FormSubmissionData> {
        val forms = mutableListOf<FormSubmissionData>()
        val selection = "$FORM_ID = ?"
        val selectionArgs = arrayOf(formId.toString())

        val cursor = readableDatabase.query(
            FORM_SUBMISSION_TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(ID))
            val formId = cursor.getInt(cursor.getColumnIndex(FORM_ID))
            val status = cursor.getString(cursor.getColumnIndex(STATUS))
            val responseJson = cursor.getString(cursor.getColumnIndex(RESPONSE_JSON))
            val createdBy = cursor.getString(cursor.getColumnIndex(CREATED_BY))
            val createdAt = cursor.getString(cursor.getColumnIndex(CREATED_AT))
            val updateAt = cursor.getString(cursor.getColumnIndex(UPDATE_AT))
            val isDeleted = (cursor.getInt(cursor.getColumnIndex(IS_DELETED)) == 1)
            val deletedAt = cursor.getString(cursor.getColumnIndex(DELETED_AT))

            val formData = FormSubmissionData(
                id,
                formId,
                status,
                responseJson,
                createdBy,
                createdAt,
                updateAt,
                isDeleted,
                deletedAt
            )
            forms.add(formData)
        }

        cursor.close()
        return forms
    }

    @SuppressLint("Range")
    fun getSubmissionTableById(id: Int): FormSubmissionData? {
        //   val forms = mutableListOf<FormSubmissionData>()
        val selection = "$ID = ?"
        val selectionArgs = arrayOf(id.toString())

        val cursor = readableDatabase.query(
            FORM_SUBMISSION_TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(ID))
            val formId = cursor.getInt(cursor.getColumnIndex(FORM_ID))
            val status = cursor.getString(cursor.getColumnIndex(STATUS))
            val responseJson = cursor.getString(cursor.getColumnIndex(RESPONSE_JSON))
            val createdBy = cursor.getString(cursor.getColumnIndex(CREATED_BY))
            val createdAt = cursor.getString(cursor.getColumnIndex(CREATED_AT))
            val updateAt = cursor.getString(cursor.getColumnIndex(UPDATE_AT))
            val isDeleted = (cursor.getInt(cursor.getColumnIndex(IS_DELETED)) == 1)
            val deletedAt = cursor.getString(cursor.getColumnIndex(DELETED_AT))
            // forms.add(formData)
            val formData = FormSubmissionData(
                id,
                formId,
                status,
                responseJson,
                createdBy,
                createdAt,
                updateAt,
                isDeleted,
                deletedAt
            )

            cursor.close()
            return formData
        }

        return null

    }

    fun insertForm(form: FormData): Long {
        val values = ContentValues().apply {
            put(FORM_ID, form.formId)
            put(FORM_NAME, form.formName)
            put(VERSION, form.version)
            put(FORM_SCHEMA, form.formSchema)
            put(CREATED_BY, form.createdBy)
            put(CREATED_AT, form.createdAt)
            put(UPDATE_AT, form.updateAt)
            put(IS_DELETED, form.isDeleted)
            put(DELETED_AT, form.deletedAt)
        }

        return writableDatabase.insert(TABLE_NAME, null, values)
    }

    fun updateForm(version: String, formSchema: String, updateAt: String, id: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(FORM_SCHEMA, formSchema)
        contentValues.put(VERSION, version)
        contentValues.put(UPDATE_AT, updateAt)
        return db.update(
            TABLE_NAME,
            contentValues,
            "$ID=?",
            arrayOf(id.toString())
        )
    }

    fun insertFormSubmissionData(formSubmission: FormSubmissionData): Long {
        val values = ContentValues().apply {
            put(FORM_ID, formSubmission.formId)
            put(STATUS, formSubmission.status)
            put(RESPONSE_JSON, formSubmission.responseJson)
            put(CREATED_BY, formSubmission.createdBy)
            put(CREATED_AT, formSubmission.createdAt)
            put(UPDATE_AT, formSubmission.updateAt)
            put(IS_DELETED, formSubmission.isDeleted)
            put(DELETED_AT, formSubmission.deletedAt)
        }

        return writableDatabase.insert(FORM_SUBMISSION_TABLE_NAME, null, values)
    }

    @SuppressLint("Range")
    fun updateSubmissionTable(id: Int, jsonString: String?): Int {
        /*  var cursor = getSubmissionTableWithFormIdCondition(id)
          if (cursor != null) {
              var oldStatus = cursor.getString(cursor.getColumnIndex("status"))
              if (oldStatus != "Saved") {*/
        val db = writableDatabase
        val values = ContentValues()
//        values.put(STATUS, status)
        values.put(RESPONSE_JSON, jsonString)
        return db.update(FORM_SUBMISSION_TABLE_NAME, values, "$ID=?", arrayOf(id.toString()))
        //db.close()
//            }
//        }
    }
    @SuppressLint("Range")
    fun updateQCTableSchema(tableName: String,id: Int, jsonString: String) {
        val db = writableDatabase
        val values = ContentValues()
//        values.put(STATUS, status)
        values.put(QC_SCHEMA, jsonString)
        db.update(tableName, values, "$QC_ID=?", arrayOf(id.toString()))
        db.close()

    }
    @SuppressLint("Range")
    fun updateQCTableSchema(tableName: String,id: Int,status: String, jsonString: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(STATUS, status)
        values.put(QC_SCHEMA, jsonString)
        db.update(tableName, values, "$QC_ID=?", arrayOf(id.toString()))
        db.close()

    }
    fun updateSubmissionTable(id: Int, status: String, jsonString: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(STATUS, status)
        values.put(RESPONSE_JSON, jsonString)
        db.update(FORM_SUBMISSION_TABLE_NAME, values, "$ID=?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteRecordById(tableName: String, id: Long): Int {
        val db = writableDatabase

        val rowsAffected = db.delete(tableName, "$ID=?", arrayOf(id.toString()))

        db.close()
        return rowsAffected
    }
    fun deleteRecordWithCondition(tableName: String, column: String, value: String): Int {
        val db = writableDatabase

        val rowsAffected = db.delete(tableName, "$column=?", arrayOf(value.toString()))

        db.close()
        return rowsAffected
    }
    fun getSingleRecord(tableName: String, id: Int): Cursor {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $tableName where $ID=$id", null)
        return cursor
    }

    fun getAllRecords(tableName: String): Cursor {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $tableName", null)
        return cursor
    }

    fun getAllRecordsWithCondition(tableName: String, condition: String): Cursor {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $tableName  where $condition", null)
        return cursor
    }

    fun getSubmissionTableWithFormIdCondition(formId: Int): Cursor? {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM $FORM_SUBMISSION_TABLE_NAME where $FORM_ID=$formId",
            null
        )
    }

    @SuppressLint("Range", "Recycle")
    fun getFormsById(formId: Int): FormData {
        val formData = FormData()
        val cursor =
            readableDatabase.rawQuery("SELECT * FROM $TABLE_NAME where $FORM_ID=$formId", null)
        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndex(ID))
            val formId = cursor.getInt(cursor.getColumnIndex(FORM_ID))
            val formName = cursor.getString(cursor.getColumnIndex(FORM_NAME))
            val version = cursor.getString(cursor.getColumnIndex(VERSION))
            val formSchema = cursor.getString(cursor.getColumnIndex(FORM_SCHEMA))
            val createdBy = cursor.getString(cursor.getColumnIndex(CREATED_BY))
            val createdAt = cursor.getString(cursor.getColumnIndex(CREATED_AT))
            val updateAt = cursor.getString(cursor.getColumnIndex(UPDATE_AT))
            val isDeleted = (cursor.getInt(cursor.getColumnIndex(IS_DELETED)) == 1)
            val deletedAt = cursor.getString(cursor.getColumnIndex(DELETED_AT))

            formData.id = id
            formData.formId = formId
            formData.formName = formName
            formData.version = version
            formData.formSchema = formSchema
            formData.createdBy = createdBy
            formData.createdAt = createdAt
            formData.updateAt = updateAt
            formData.isDeleted = isDeleted
            formData.deletedAt = deletedAt
            /*
               val formData = FormData(
                 id,
                 formId,
                 formName,
                 version,
                 formSchema,
                 createdBy,
                 createdAt,
                 updateAt,
                 isDeleted,
                 deletedAt
             )*/

        }
        return formData
    }

    @SuppressLint("Range")
    fun getAllForms(): List<FormData> {
        val forms = mutableListOf<FormData>()
        val cursor = readableDatabase.query(TABLE_NAME, null, null, null, null, null, null)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(ID))
            val formId = cursor.getInt(cursor.getColumnIndex(FORM_ID))
            val formName = cursor.getString(cursor.getColumnIndex(FORM_NAME))
            val version = cursor.getString(cursor.getColumnIndex(VERSION))
            val formSchema = cursor.getString(cursor.getColumnIndex(FORM_SCHEMA))
            val createdBy = cursor.getString(cursor.getColumnIndex(CREATED_BY))
            val createdAt = cursor.getString(cursor.getColumnIndex(CREATED_AT))
            val updateAt = cursor.getString(cursor.getColumnIndex(UPDATE_AT))
            val isDeleted = (cursor.getInt(cursor.getColumnIndex(IS_DELETED)) == 1)
            val deletedAt = cursor.getString(cursor.getColumnIndex(DELETED_AT))

            val formData = FormData(
                id,
                formId,
                formName,
                version,
                formSchema,
                createdBy,
                createdAt,
                updateAt,
                isDeleted,
                deletedAt
            )
            forms.add(formData)
        }
        cursor.close()
        return forms
    }
}