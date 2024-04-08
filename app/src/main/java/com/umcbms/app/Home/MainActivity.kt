package com.umcbms.app.Home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umcbms.app.INSERT
import com.umcbms.app.JSONModel.JSONFormDataModel
import com.umcbms.app.LoginActivity
import com.umcbms.app.LoginViewModel
import com.umcbms.app.MasterDB.FormData
import com.umcbms.app.MasterDB.FormDataID
import com.umcbms.app.MasterDB.FormIndexData
import com.umcbms.app.MasterDB.FormJsonDataModel
import com.umcbms.app.MasterDB.FormListModel
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.R
import com.umcbms.app.UPDATE
import com.umcbms.app.api.Status
import com.umcbms.app.getCurrentDateTime
import com.umcbms.app.getPrefStringData
import com.umcbms.app.setPrefStringData
import com.google.gson.Gson
import com.umcbms.app.setPrefBooleanData
import com.umcbms.app.showToast

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewForm: RecyclerView
    private lateinit var ivLogout: ImageView

    //    private lateinit var ivEdit: ImageView
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var dbHelper: MasterDBHelper
    private lateinit var txtTitleName: TextView
    private lateinit var txtTittleUser:TextView

    private var formName = ArrayList<FormListModel>()
    private var formJsonData = ArrayList<FormJsonDataModel>()
    private var formJsonId = ArrayList<FormDataID>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasReadMediaImagesPermission(this)
        ) {
            // Request permission if not already granted on Android 13
            requestReadMediaImagesPermission(this)
        }
        txtTitleName = findViewById(R.id.txtTitleName_Home)
        txtTittleUser = findViewById(R.id.txtTitleName_User)
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName


        val userName = getPrefStringData(this, "userName")
        txtTitleName.text = "Urban Management Center"
        txtTittleUser.text = userName + " ( Ver.$versionName )"
                loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        dbHelper = MasterDBHelper(this)

        ivLogout = findViewById(R.id.ivLogout)
//        ivEdit = findViewById(R.id.ivEdit)
        recyclerViewForm = findViewById(R.id.recyclerViewForm)

        // replaceFragment(ListFormsFragment())
        /*   val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavView)
           bottomNavView.setOnNavigationItemSelectedListener { menuItem ->
               when (menuItem.itemId) {
                   R.id.tab_listForms -> replaceFragment(ListFormsFragment())
                   R.id.tab_uploadJson -> replaceFragment(UploadJsonFragment())
                   else -> replaceFragment(ListFormsFragment())
               }
               true
           }*/

        ivLogout.setOnClickListener {
            logoutApi()
        }
        /*  ivEdit.setOnClickListener {
              val intentAdd = Intent(this, EditFormDataActivity::class.java)
              intentAdd.putExtra("summeryId", 15)
              intentAdd.putExtra("formId", 2)
              intentAdd.putExtra("responseId", 17)
              intentAdd.putExtra("status", "Synced")
              startActivity(intentAdd)
          }*/
        val token = getPrefStringData(this, "token")
        var cursor = dbHelper.getAllRecords(MasterDBHelper.STATE_MASTER_TABLE_NAME)
        if (cursor.count == 0) {
            Log.e("CheckStates", cursor.count.toString())
            getStateMaster(token!!)
            getDistrictMaster(token!!)
            getCityMaster(token!!)
        }

    }

    // Function to check if READ_MEDIA_IMAGES permission is granted
    fun hasReadMediaImagesPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED  && (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED)
    }

    // Function to request READ_MEDIA_IMAGES permission (optional)
    fun requestReadMediaImagesPermission(fragmentActivity: Activity) {
        Log.e("insidemedi","camera")
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                fragmentActivity,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                fragmentActivity,
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                22
            )
        }

        // Check if CAMERA permission is granted
        if (ContextCompat.checkSelfPermission(
                fragmentActivity,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.CAMERA)
        }

        // Request permissions if any are missing
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                fragmentActivity,
                permissionsToRequest.toTypedArray(),
                22
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        recyclerViewUpdate()

        if (isInternetAvailable(this)) {
            getSchemaIndex()
        } else {
            // No internet connection
        }
    }



    private fun logoutApi() {
        val token = getPrefStringData(this, "token")
        if (token != null) {

            loginViewModel.logoutApi(token).observe(this) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        setPrefBooleanData(this, "superAdmin", false)
                        val message = response.data?.body()?.message
                    }

                    Status.ERROR -> {
                        //Log.d("TAGB", "logoutApi: ${response.message}")
                        Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
                    }

                    Status.LOADING -> {}
                }
            }

            setPrefStringData(this, "token", null)
            deleteDatabase(MasterDBHelper.DATABASE_NAME)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

            Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            22 -> {
                // Check if permissions are granted
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    requestReadMediaImagesPermission(this )
                } else {
                    showToast(this,"Please Give All Required Permission")
                }
            }
        }
    }
    private fun recyclerViewUpdate() {
        val getFormList = getFormList()
        formName.clear()
        formJsonData.clear()
        formJsonId.clear()
        for (i in 0 until getFormList.size) {
            formName.add(FormListModel(getFormList[i].formName!!))
            formJsonData.add(FormJsonDataModel(getFormList[i].formSchema!!))
            formJsonId.add(FormDataID(getFormList[i].formId!!))
        }

        recyclerViewForm.layoutManager = LinearLayoutManager(this)
        val formAdapter = FormAdapter(this, formName, formJsonData, formJsonId,dbHelper)
        recyclerViewForm.adapter = formAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSchemaIndex() {
        val token = getPrefStringData(this, "token")
        if (token != null) {
            loginViewModel.getSchemaIndex(token).observe(this) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        val schemaRes = response.data?.body()
                        if (schemaRes != null && schemaRes.code == 1) {
                            val projectResponses = schemaRes.data

                            for (projectResponse in projectResponses) {
                                val schemaIndexData =
                                    projectResponse.subunits
                                if (schemaIndexData != null) {
                                    for (element in schemaIndexData) {
                                        val formIndexData =
                                            dbHelper.getFormIndexDataById(element.id!!)
                                        val id = formIndexData?.id
                                        val version = formIndexData?.version
                                        if (formIndexData == null) {
                                            dbHelper.insertFormIndexData(
                                                FormIndexData(
                                                    element.id,
                                                    element.name,
                                                    element.version,
                                                    element.canSurvey,
                                                    element.canQc,
                                                    element.canValidate
                                                )
                                            )

                                            getSchema(token, element.id, INSERT)

                                        } else {
                                            if (id == element.id) {
                                                if (version != element.version) {
                                                    dbHelper.updateFormIndexData(
                                                        FormIndexData(
                                                            element.id,
                                                            element.name,
                                                            element.version,
                                                            element.canSurvey,
                                                            element.canQc,
                                                            element.canValidate
                                                        )
                                                    )
                                                    Log.e("SchemaUpdate", element.id.toString())

                                                    getSchema(token, element.id, UPDATE)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        } else {
                            // Handle unsuccessful schema
                            Toast.makeText(this, "Schema null", Toast.LENGTH_SHORT).show()
                        }

                    }


                    Status.ERROR -> {
                        Toast.makeText(this, "Error: ${response.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    Status.LOADING -> {
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSchema(token: String, id: Int?, insertUpdate: String) {
        if (id != null) {
            loginViewModel.getSchema(token, id).observe(this) { response ->

                when (response.status) {
                    Status.SUCCESS -> {
                        val jsonData = response.data?.body()?.data?.data
//                        val jsonData = response.data?.body()?.data
                        val jsonString = Gson().toJson(jsonData)
//                        jsonFormSave(jsonString, jsonData, id, insertUpdate)
                        jsonFormSave(jsonString, jsonData, id, insertUpdate)

                    }

                    Status.ERROR -> {
                        Log.d(TAG, "getSchema: ${response.message}")
                        Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
                    }

                    Status.LOADING -> {}
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun jsonFormSave(
        jsonString: String,
        jsonData: JSONFormDataModel?,
        id: Int,
        insertUpdate: String
    ) {
        try {
            var row: Long = -1

            val currentDateTime = getCurrentDateTime()
            if (insertUpdate == INSERT) {
                row = dbHelper.insertForm(
                    FormData(
                        formId = id,
                        formName = jsonData?.form_name.toString(),//.data?.form_name.toString(),
                        version = jsonData?.version.toString(),
                        formSchema = jsonString,
                        createdBy = null,
                        createdAt = currentDateTime,
                        updateAt = null,
                        isDeleted = null,
                        deletedAt = null
                    )
                )


//                acrony changes


                var tableName = jsonData?.acronym + MasterDBHelper.SUMMERY
//                var tableName = jsonData?.subunitAcronym + MasterDBHelper.SUMMERY
                dbHelper.createSummeryTables(tableName, jsonString)

                getSyncedList(id, row, tableName)

            } else if (insertUpdate == UPDATE) {
                row = dbHelper.updateForm(
                    version = jsonData?.version.toString(),
                    formSchema = jsonString,
                    updateAt = currentDateTime,
                    id = id
                ).toLong()
            }

            if (row.toInt() != -1) {
                recyclerViewUpdate()
            }

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "thi JSON not uploaded",
                Toast.LENGTH_LONG
            )
                .show()
            Log.d(TAG, "onActivityResult: ${e.message}")

        }
    }

    private fun getSyncedList(id: Int, formMasterId: Long, tableName: String) {
        val token = getPrefStringData(this, "token")
        loginViewModel.getSyncedList(token.toString(), id).observe(this) { response ->

            when (response.status) {
                Status.SUCCESS -> {
                    if (response.data?.body()?.code == 1) {
                        val syncList = response.data?.body()?.data
                        syncList?.forEachIndexed { index, dataSyncData ->
                            Log.e(TAG, "getSyncedList: " + dataSyncData.id.toString())
                            getSyncedData(
                                token.toString(),
                                id,
                                formMasterId,
                                dataSyncData.id.toString(),
                                tableName
                            )
                        }
                    } else {
                        Log.d(TAG, "getSyncedList: ${response.data?.body()?.message}")
                        Toast.makeText(
                            this,
                            "getSyncedList : ${response.data?.body()?.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }


                }

                Status.ERROR -> {
                    Log.d(TAG, "getSyncedList: ${response.message}")
                    Toast.makeText(this, "getSyncedList : ${response.message}", Toast.LENGTH_SHORT)
                        .show()
                }

                Status.LOADING -> {}
            }

        }
    }

    private fun getSyncedData(
        token: String,
        formId: Int,
        formMasterId: Long,
        responseId: String,
        tableName: String
    ) {
        loginViewModel.getSyncedData(token.toString(), formId, responseId)
            .observe(this) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        Log.d(TAG, "getSyncedData:=" + response.data?.body())
                        if (response.data?.body()?.code == 1) {


                            val syncData = response.data?.body()?.data!!
//                        Log.d(TAG, "getSyncedData: "+syncData)
                            var columnNames = dbHelper.getColumnNames(tableName)
//                        Log.d(TAG, "getSyncedData: "+columnNames)
                            val values = ContentValues()
                            values.put(MasterDBHelper.MASTER_FORM_ID, formId.toInt())
                            values.put(MasterDBHelper.RESPONSE_ID, responseId)
                            values.put(MasterDBHelper.STATUS, "Synced")
                            columnNames.forEachIndexed { index, columnName ->
                                if (syncData.field_value_map.containsKey(columnName)) {
                                    syncData.field_value_map.forEach { (key, value) ->
                                        if (key == columnName) {
                                            values.put(key, value.toString())
                                        }
                                    }
                                }
                            }

                            dbHelper.insertData(tableName, values)
                        }


                    }

                    Status.ERROR -> {
                        Log.d(TAG, "getSyncedData: ${response.message}")
                        Toast.makeText(
                            this,
                            "getSyncedData : ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    Status.LOADING -> {}
                }

            }
    }

    fun getFormList(): ArrayList<FormData> {
        val formData = dbHelper.getAllForms()

        val formListDB = ArrayList<FormData>()

        formListDB.addAll(formData)

        return formListDB
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    /* private fun replaceFragment(fragment: Fragment) {
         supportFragmentManager.beginTransaction()
             .replace(R.id.nav_host_fragment, fragment)
             .commit()
     }*/

    private fun getStateMaster(token: String) {
        loginViewModel.getStateMaster(token).observe(this) { response ->
            Log.e("ResStates", response.toString())
            when (response.status) {

                Status.SUCCESS -> {
                    val stateList = response.data?.body()?.data
                    stateList?.let { dbHelper.insertStateMaster(it) }
                }

                Status.ERROR -> {
                    Log.d(TAG, "getStateMaster: ${response.message}")
                    Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
                }

                Status.LOADING -> {}
            }

        }
    }

    private fun getDistrictMaster(token: String) {
        loginViewModel.getDistrictMaster(token).observe(this) { response ->
            when (response.status) {
                Status.SUCCESS -> {
                    val districtList = response.data?.body()?.data
                    districtList?.let { dbHelper.insertDistrictMaster(it) }
                }

                Status.ERROR -> {
                    Log.d(TAG, "getDistrictMaster: ${response.message}")
                    Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
                }

                Status.LOADING -> {}
            }

        }
    }

    private fun getCityMaster(token: String) {
        loginViewModel.getCityMaster(token).observe(this) { response ->
            when (response.status) {
                Status.SUCCESS -> {
                    val cityList = response.data?.body()?.data
                    cityList?.let { dbHelper.insertCityMaster(it) }
                    getUserCities()
                }
                Status.ERROR -> {
                    Log.d(TAG, "getCityMaster: ${response.message}")
                    Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
                }

                Status.LOADING -> {}
            }

        }
    }
    @SuppressLint("Range")
    private fun getUserCities() {
        val token = getPrefStringData(this, "token")
        if (token != null) {
            loginViewModel.getUserCities(token).observe(this) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        val citiesResponse = response.data?.body()?.data
                        if (citiesResponse?.have_all==true){
                            addAllUserStates()
                            addAllUserDistricts()
                            addAllUserCities()
                        }else {
                            var cityIdsStr =citiesResponse?.citiesData?.joinToString(", ")
                            var cursorCity = dbHelper.getAllRecordsWithCondition(
                                MasterDBHelper.CITY_MASTER_TABLE_NAME,
                                "${MasterDBHelper.ID} IN ($cityIdsStr)"
                            )
                            if (cursorCity.moveToFirst()) {
                                do {
                                    val values = ContentValues()
                                    values.put(MasterDBHelper.ID, cursorCity.getInt(cursorCity.getColumnIndex(MasterDBHelper.ID)))
                                    values.put(MasterDBHelper.NAME, cursorCity.getString(cursorCity.getColumnIndex(MasterDBHelper.NAME)))
                                    values.put(MasterDBHelper.DISTRICT_ID, cursorCity.getInt(cursorCity.getColumnIndex(MasterDBHelper.DISTRICT_ID)))
                                    values.put(MasterDBHelper.STATUS, cursorCity.getInt(cursorCity.getColumnIndex(MasterDBHelper.STATUS)))
                                    values.put(MasterDBHelper.CREATED_AT,cursorCity.getString(cursorCity.getColumnIndex(MasterDBHelper.CREATED_AT)))
                                    values.put(MasterDBHelper.UPDATE_AT, cursorCity.getString(cursorCity.getColumnIndex(MasterDBHelper.UPDATE_AT)))
                                    values.put(MasterDBHelper.DELETED_AT, cursorCity.getString(cursorCity.getColumnIndex(MasterDBHelper.DELETED_AT)))

                                    dbHelper.insertData(MasterDBHelper.USER_CITY_TABLE_NAME,values)

                                    // THis is for District
                                    var cursorDistrict = dbHelper.getAllRecordsWithCondition(
                                        MasterDBHelper.USER_DISTRICT_TABLE_NAME,
                                        "${MasterDBHelper.ID}=${cursorCity.getInt(cursorCity.getColumnIndex(MasterDBHelper.DISTRICT_ID))}"
                                    )
                                    if (!cursorDistrict.moveToFirst()) {
                                        var cursorDistrictMaster = dbHelper.getAllRecordsWithCondition(
                                            MasterDBHelper.DISTRICT_MASTER_TABLE_NAME,
                                            "${MasterDBHelper.ID}=${cursorCity.getInt(cursorCity.getColumnIndex(MasterDBHelper.DISTRICT_ID))}"
                                        )
                                        if (cursorDistrictMaster.moveToFirst()) {
                                            val values = ContentValues()
                                            values.put(MasterDBHelper.ID, cursorDistrictMaster.getInt(cursorDistrictMaster.getColumnIndex(MasterDBHelper.ID)))
                                            values.put(MasterDBHelper.NAME, cursorDistrictMaster.getString(cursorDistrictMaster.getColumnIndex(MasterDBHelper.NAME)))
                                            values.put(MasterDBHelper.STATE_ID, cursorDistrictMaster.getInt(cursorDistrictMaster.getColumnIndex(MasterDBHelper.STATE_ID)))
                                            values.put(MasterDBHelper.STATUS, cursorDistrictMaster.getInt(cursorDistrictMaster.getColumnIndex(MasterDBHelper.STATUS)))
                                            values.put(MasterDBHelper.CREATED_AT,cursorDistrictMaster.getString(cursorDistrictMaster.getColumnIndex(MasterDBHelper.CREATED_AT)))
                                            values.put(MasterDBHelper.UPDATE_AT, cursorDistrictMaster.getString(cursorDistrictMaster.getColumnIndex(MasterDBHelper.UPDATE_AT)))
                                            values.put(MasterDBHelper.DELETED_AT, cursorDistrictMaster.getString(cursorDistrictMaster.getColumnIndex(MasterDBHelper.DELETED_AT)))

                                            dbHelper.insertData(MasterDBHelper.USER_DISTRICT_TABLE_NAME,values)

                                            // THis is for state
                                            var cursorState = dbHelper.getAllRecordsWithCondition(
                                                MasterDBHelper.USER_STATE_TABLE_NAME,
                                                "${MasterDBHelper.ID}=${cursorDistrictMaster.getInt(cursorDistrictMaster.getColumnIndex(MasterDBHelper.STATE_ID))}"
                                            )
                                            if (!cursorState.moveToFirst()) {
                                                var cursorStateMaster = dbHelper.getAllRecordsWithCondition(
                                                    MasterDBHelper.STATE_MASTER_TABLE_NAME,
                                                    "${MasterDBHelper.ID}=${cursorDistrictMaster.getInt(cursorDistrictMaster.getColumnIndex(MasterDBHelper.STATE_ID))}"
                                                )
                                                if (cursorStateMaster.moveToFirst()) {
                                                    val values = ContentValues()
                                                    values.put(MasterDBHelper.ID, cursorStateMaster.getInt(cursorStateMaster.getColumnIndex(MasterDBHelper.ID)))
                                                    values.put(MasterDBHelper.NAME, cursorStateMaster.getString(cursorStateMaster.getColumnIndex(MasterDBHelper.NAME)))
                                                    values.put(MasterDBHelper.STATUS, cursorStateMaster.getInt(cursorStateMaster.getColumnIndex(MasterDBHelper.STATUS)))
                                                    values.put(MasterDBHelper.CREATED_AT,cursorStateMaster.getString(cursorStateMaster.getColumnIndex(MasterDBHelper.CREATED_AT)))
                                                    values.put(MasterDBHelper.UPDATE_AT, cursorStateMaster.getString(cursorStateMaster.getColumnIndex(MasterDBHelper.UPDATE_AT)))
                                                    values.put(MasterDBHelper.DELETED_AT, cursorStateMaster.getString(cursorStateMaster.getColumnIndex(MasterDBHelper.DELETED_AT)))

                                                    dbHelper.insertData(MasterDBHelper.USER_STATE_TABLE_NAME,values)
                                                }

                                            }
                                        }

                                    }
                                } while (cursorCity.moveToNext())
                            }

                           /* cityList?.let { dbHelper.insertCityMaster(it) }*/
                        }
                    }
                    Status.ERROR -> {
                        Log.d(TAG, "getCities: ${response.message}")
                        Toast.makeText(this, "${response.message}", Toast.LENGTH_SHORT).show()
                    }

                    Status.LOADING -> {}
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun addAllUserStates() {
        var cursor = dbHelper.getAllRecords(
            MasterDBHelper.STATE_MASTER_TABLE_NAME
        )
        if (cursor.moveToFirst()) {
            do {
                val values = ContentValues()
                values.put(MasterDBHelper.ID, cursor.getInt(cursor.getColumnIndex(MasterDBHelper.ID)))
                values.put(MasterDBHelper.NAME, cursor.getString(cursor.getColumnIndex(MasterDBHelper.NAME)))
                values.put(MasterDBHelper.STATUS, cursor.getInt(cursor.getColumnIndex(MasterDBHelper.STATUS)))
                values.put(MasterDBHelper.CREATED_AT,cursor.getString(cursor.getColumnIndex(MasterDBHelper.CREATED_AT)))
                values.put(MasterDBHelper.UPDATE_AT, cursor.getString(cursor.getColumnIndex(MasterDBHelper.UPDATE_AT)))
                values.put(MasterDBHelper.DELETED_AT, cursor.getString(cursor.getColumnIndex(MasterDBHelper.DELETED_AT)))

                dbHelper.insertData(MasterDBHelper.USER_STATE_TABLE_NAME,values)
            } while (cursor.moveToNext())
        }
    }

    @SuppressLint("Range")
    private fun addAllUserDistricts() {
        var cursor = dbHelper.getAllRecords(
            MasterDBHelper.DISTRICT_MASTER_TABLE_NAME
        )
        if (cursor.moveToFirst()) {
            do {
                val values = ContentValues()
                values.put(MasterDBHelper.ID, cursor.getInt(cursor.getColumnIndex(MasterDBHelper.ID)))
                values.put(MasterDBHelper.NAME, cursor.getString(cursor.getColumnIndex(MasterDBHelper.NAME)))
                values.put(MasterDBHelper.STATE_ID, cursor.getInt(cursor.getColumnIndex(MasterDBHelper.STATE_ID)))
                values.put(MasterDBHelper.STATUS, cursor.getInt(cursor.getColumnIndex(MasterDBHelper.STATUS)))
                values.put(MasterDBHelper.CREATED_AT,cursor.getString(cursor.getColumnIndex(MasterDBHelper.CREATED_AT)))
                values.put(MasterDBHelper.UPDATE_AT, cursor.getString(cursor.getColumnIndex(MasterDBHelper.UPDATE_AT)))
                values.put(MasterDBHelper.DELETED_AT, cursor.getString(cursor.getColumnIndex(MasterDBHelper.DELETED_AT)))

                dbHelper.insertData(MasterDBHelper.USER_DISTRICT_TABLE_NAME,values)
            } while (cursor.moveToNext())
        }
    }

    @SuppressLint("Range")
    private fun addAllUserCities() {
        var cursor = dbHelper.getAllRecords(
            MasterDBHelper.CITY_MASTER_TABLE_NAME
        )
        if (cursor.moveToFirst()) {
            do {
                val values = ContentValues()
                values.put(MasterDBHelper.ID, cursor.getInt(cursor.getColumnIndex(MasterDBHelper.ID)))
                values.put(MasterDBHelper.NAME, cursor.getString(cursor.getColumnIndex(MasterDBHelper.NAME)))
                values.put(MasterDBHelper.DISTRICT_ID, cursor.getInt(cursor.getColumnIndex(MasterDBHelper.DISTRICT_ID)))
                values.put(MasterDBHelper.STATUS, cursor.getInt(cursor.getColumnIndex(MasterDBHelper.STATUS)))
                values.put(MasterDBHelper.CREATED_AT,cursor.getString(cursor.getColumnIndex(MasterDBHelper.CREATED_AT)))
                values.put(MasterDBHelper.UPDATE_AT, cursor.getString(cursor.getColumnIndex(MasterDBHelper.UPDATE_AT)))
                values.put(MasterDBHelper.DELETED_AT, cursor.getString(cursor.getColumnIndex(MasterDBHelper.DELETED_AT)))

                dbHelper.insertData(MasterDBHelper.USER_CITY_TABLE_NAME,values)
            } while (cursor.moveToNext())
        }
    }
}