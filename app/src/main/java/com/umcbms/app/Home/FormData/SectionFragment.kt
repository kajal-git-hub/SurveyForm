package com.umcbms.app.Home.FormData

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.umcbms.app.Home.FormData.viewModel.SectionViewModel
import com.umcbms.app.Home.FormData.SectionFragment.*
import com.umcbms.app.JSONModel.JSONFormDataModel
import com.umcbms.app.JSONModel.skiplogic.SkipLogicElement
import com.umcbms.app.MasterDB.SkipLogicCondition
import com.umcbms.app.R
import com.umcbms.app.api.request.DistrictIdsRequest
import com.umcbms.app.api.request.StateIdsRequest
import com.umcbms.app.api.respose.State
import com.umcbms.app.isValidNumber
import com.umcbms.app.validateText
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val TAG = "SectionFragment"

interface OnTabChangedListener {
    fun onTabChanged(tabIndex: Int)
}

@Suppress("DEPRECATION", "UNREACHABLE_CODE")
class SectionFragment : Fragment(), OnTabChangedListener {

    private lateinit var jsonData: JSONFormDataModel
    private var endSection: Int = 0
    private var subId: Int = 0
    private var jsonString: String = ""
    private var formJsonId: Long = 0

    private lateinit var linearLayoutSection: LinearLayout
    private lateinit var ll: LinearLayout
    private lateinit var etInputTextAddable: EditText
    private lateinit var bt: Button
    private lateinit var imgCapture: ImageView
    private lateinit var selectedDate: Calendar
    private lateinit var textDateEt: EditText
    private lateinit var textLocationEt: EditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var etImgCapture: EditText
    private lateinit var etuploadImage: EditText
    private var sectionObject: JSONObject? = null
    private lateinit var dataArray: JSONArray
    private lateinit var jsonObject: JSONObject
    val skipLogicArrayList = mutableMapOf<String, List<SkipLogicElement>>()
    private lateinit var sectionViewModel: SectionViewModel
    var districtList = ArrayList<State>()


    var isOnCreatedView = false
    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val REQUEST_IMAGE_CAPTURE = 2
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        var validationFalse = ""
        var sectionPos: Int = 0
        var demoMap = mutableMapOf<String, String>()
        var formSkipLogics = mutableListOf<SkipLogicElement>()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bt = Button(requireContext())
   //     dbHelper = MasterDBHelper(requireContext())

        // Log.d("TAG", "onCreate: 1")
        sectionViewModel = ViewModelProvider(this).get(SectionViewModel::class.java)
        //statesList.addAll(AddFormDataActivity.statesList)

    }

    /*

        override fun onResume() {
            Log.d("TAG", "onResume: $")
            super.onResume()
            val cursor = dbHelper.getSectionTable("NMST_GEN")
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                        val gId = cursor.getInt(cursor.getColumnIndex("garima_id_worker"))
                        if (sId == subId) {
                            if (cursor.isLast) {
                                //if (sId != subId) {
                                    Log.d("TAG", "onResume: $gId")
                              //  }
                            }
                        }
                    } while (cursor.moveToNext())
                } else {

                }
            }
           // Log.d("TAG", "onResume: $demoMap")
        }
         override fun onResume() {
             super.onResume()

             val editTextWithTag = ll.findViewWithTag<EditText>("dw")
             val editTextValue = editTextWithTag?.text.toString()
             Log.d("TAGb", "onResume: $editTextValue")


             val jsonObject = JSONObject(jsonString)
             val dataArray = jsonObject.getJSONArray("data")

             if (sectionPos == 0) {
                 val sectionObject = dataArray.getJSONObject(sectionPos)
                val title = sectionObject.getString("title")
                var builder = AlertDialog.Builder(requireContext())
                builder.setTitle(sectionPos.toString() + title)
                val x = builder.create()
                x.show()
            } else {

                val sectionObject = dataArray.getJSONObject(sectionPos - 1)
                val formAcronym = jsonObject.getString("acronym")
                val secAcronym = sectionObject?.getString("acronym")

                val tableName = formAcronym + "_" + secAcronym
                validationFalse = ""


                if (sectionObject != null) {
                    validation(
                        sectionObject = sectionObject,
                        tableName = tableName,
                        formAcronym = formAcronym
                    )
                }

                if (validationFalse != ""){
                    Toast.makeText(requireContext(),"$validationFalse", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(),"True", Toast.LENGTH_SHORT).show()
                }
                val title = sectionObject.getString("title")
                var builder = AlertDialog.Builder(requireContext())
                builder.setTitle(sectionPos.toString() + title)
                val x = builder.create()
                x.show()
            }

        }

        private fun validation(sectionObject: JSONObject, tableName: String, formAcronym: String) {
            val childrenArray = sectionObject.getJSONArray("children")

            if (sectionObject.getString("type") == "SECTION") {
                //Log.d("TAG", "validation: $tableName")

                val data = dbHelper.getSectionTableDataById(tableName, subId.toLong())
                //Log.d("TAG", "validation Dt : $data")

                for (j in 0 until childrenArray.length()) {
                    val childObject = childrenArray.getJSONObject(j)

                    when (childObject.getString("type")) {
                        "TEXT" -> {
                            try {
                                val label = childObject.getJSONObject("properties").getString("label")
                                val childId = childObject.getString("id")
                                val fieldValidations = childObject.getJSONObject("fieldValidations")
                                val maxChar = fieldValidations.getString("maxChar").toInt()
                                val minChar = fieldValidations.getString("minChar").toInt()
                                val valueRequired = fieldValidations.getBoolean("valueRequired")

                                if (valueRequired) {
                                    if (data != null) {
                                        for ((columnName, columnValue) in data) {
                                            if (childId == columnName) {
                                                if (validateText(columnValue, minChar, maxChar)) {

                                                } else {
                                                    validationFalse =
                                                        validationFalse + "  " + "$label Input must be between $minChar and $maxChar characters."
                                                }
                                            }
                                        }
                                    } else {
                                        validationFalse =
                                            validationFalse + "  " + "$label Input must be between $minChar and $maxChar characters."
                                    }
                                }
                            }catch (e: Exception){
                                Log.d("TAG", "validation: ${e.message}")
                            }
                        }

                        "NUMBERS" -> {
                            try {
                                val label = childObject.getJSONObject("properties").getString("label")
                                val childId = childObject.getString("id")
                                val fieldValidations = childObject.getJSONObject("fieldValidations")
                                val maxLimit = fieldValidations.getString("maxLimit").toInt()
                                val minLimit = fieldValidations.getString("minLimit").toInt()
                                val valueRequired = fieldValidations.getBoolean("valueRequired")

                                if (valueRequired) {
                                    if (data != null) {
                                        for ((columnName, columnValue) in data) {
                                            if (childId == columnName) {
                                                if (isValidNumber(columnValue, minLimit, maxLimit)) {

                                                } else {
                                                    validationFalse =
                                                        validationFalse + "  " + "$label Enter a valid number between $minLimit and $maxLimit."
                                                }
                                            }
                                        }
                                    } else {
                                        validationFalse =
                                            validationFalse + "  " + "$label Enter a valid number between $minLimit and $maxLimit."
                                    }
                                }
                            }catch (e: Exception){
                                Log.d("TAG", "validation: ${e.message}")
                            }
                        }

                        "DROPDOWN" -> {
                            try {
                                val label = childObject.getJSONObject("properties").getString("label")
                                val childId = childObject.getString("id")
                                val fieldValidations = childObject.getJSONObject("fieldValidations")
                                val valueRequired = fieldValidations.getBoolean("valueRequired")
                                val optionsList = childObject.getJSONArray("options")

                                if (valueRequired) {
                                    if (data != null) {
                                        for ((columnName, columnValue) in data) {
                                            if (childId == columnName) {

                                                if (optionsList != null) {
                                                    if (columnValue != "NULL") {

                                                    } else {
                                                        validationFalse =
                                                            validationFalse + "  " + "$label Enter a value.."
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        validationFalse =
                                            validationFalse + "  " + "$label Enter a value."
                                    }
                                }
                            }catch (e: Exception){
                                Log.d("TAG", "validation: ${e.message}")
                            }

                        }

                        "TEXT_AREA" -> {
                            try {
                                val label = childObject.getJSONObject("properties").getString("label")
                                val childId = childObject.getString("id")
                                val fieldValidations = childObject.getJSONObject("fieldValidations")
                                val maxChar = fieldValidations.getString("maxChar").toInt()
                                val minChar = fieldValidations.getString("minChar").toInt()
                                val valueRequired = fieldValidations.getBoolean("valueRequired")

                                if (valueRequired) {
                                    if (data != null) {
                                        for ((columnName, columnValue) in data) {
                                            if (childId == columnName) {
                                                if (validateText(columnValue, minChar, maxChar)) {

                                                } else {
                                                    validationFalse =
                                                        validationFalse + "  " + "$label Input must be between $minChar and $maxChar characters."
                                                }
                                            }
                                        }
                                    } else {
                                        validationFalse =
                                            validationFalse + "  " + "$label Input must be between $minChar and $maxChar characters."
                                    }
                                }
                            }catch (e: Exception){
                                Log.d("TAG", "validation: ${e.message}")
                            }

                        }

                        "RADIO" -> {
                            try {
                                val label = childObject.getJSONObject("properties").getString("label")
                                val childId = childObject.getString("id")
                                val fieldValidations = childObject.getJSONObject("fieldValidations")
                                val valueRequired = fieldValidations.getBoolean("valueRequired")

                                if (valueRequired) {
                                    if (data != null) {
                                        for ((columnName, columnValue) in data) {
                                            if (childId == columnName) {

                                                if (columnValue != "NULL") {

                                                } else {
                                                    validationFalse =
                                                        validationFalse + "  " + "$label Enter a value.."
                                                }
                                            }
                                        }
                                    } else {
                                        validationFalse =
                                            validationFalse + "  " + "$label Enter a value."
                                    }
                                }
                            }catch (e: Exception){
                                Log.d("TAG", "validation: ${e.message}")
                            }

                        }

                        "UPLOAD_IMAGE" -> {
                            try {
                                val label = childObject.getJSONObject("properties").getString("label")
                                val childId = childObject.getString("id")
                                val fieldValidations = childObject.getJSONObject("fieldValidations")
                                val valueRequired = fieldValidations.getBoolean("valueRequired")

                                if (valueRequired) {
                                    if (data != null) {
                                        for ((columnName, columnValue) in data) {
                                            if (childId == columnName) {

                                                if (columnValue != "NULL") {

                                                } else {
                                                    validationFalse =
                                                        validationFalse + "  " + "$label Enter a value.."
                                                }
                                            }
                                        }
                                    } else {
                                        validationFalse =
                                            validationFalse + "  " + "$label Enter a value."
                                    }
                                }
                            }catch (e: Exception){
                                Log.d("TAG", "validation: ${e.message}")
                            }
                        }

                        "CAPTURE_IMAGE" -> {
                            try {
                                val label = childObject.getJSONObject("properties").getString("label")
                                val childId = childObject.getString("id")
                                val fieldValidations = childObject.getJSONObject("fieldValidations")
                                val valueRequired = fieldValidations.getBoolean("valueRequired")

                                if (valueRequired) {
                                    if (data != null) {
                                        for ((columnName, columnValue) in data) {
                                            if (childId == columnName) {
                                                if (columnValue != "NULL") {

                                                } else {
                                                    validationFalse =
                                                        validationFalse + "  " + "$label Enter a value.."
                                                }
                                            }
                                        }
                                    } else {
                                        validationFalse =
                                            validationFalse + "  " + "$label Enter a value."
                                    }
                                }
                            }catch (e: Exception){
                                Log.d("TAG", "validation: ${e.message}")
                            }
                        }

                        "DATE" -> {
                            try {
                                val label = childObject.getJSONObject("properties").getString("label")
                                val childId = childObject.getString("id")
                                val fieldValidations = childObject.getJSONObject("fieldValidations")
                                val valueRequired = fieldValidations.getBoolean("valueRequired")

                                if (valueRequired) {
                                    if (data != null) {
                                        for ((columnName, columnValue) in data) {
                                            if (childId == columnName) {

                                                if (columnValue != "NULL") {

                                                } else {
                                                    validationFalse =
                                                        validationFalse + "  " + "$label Enter a value.."
                                                }
                                            }
                                        }
                                    } else {
                                        validationFalse =
                                            validationFalse + "  " + "$label Enter a value."
                                    }
                                }
                            }catch (e: Exception){
                                Log.d("TAG", "validation: ${e.message}")
                            }
                        }

                        "LOCATION" -> {
                            try {
                                val label = childObject.getJSONObject("properties").getString("label")
                                val childId = childObject.getString("id")
                                val fieldValidations = childObject.getJSONObject("fieldValidations")
                                val valueRequired = fieldValidations.getBoolean("valueRequired")

                                if (valueRequired) {
                                    if (data != null) {
                                        for ((columnName, columnValue) in data) {
                                            if (childId == columnName) {

                                                if (columnValue != "NULL") {

                                                } else {
                                                    validationFalse =
                                                        validationFalse + "  " + "$label Enter a value.."
                                                }
                                            }
                                        }
                                    } else {
                                        validationFalse =
                                            validationFalse + "  " + "$label Enter a value."
                                    }
                                }
                            }catch (e: Exception){
                                Log.d("TAG", "validation: ${e.message}")
                            }
                        }

                        "ADDABLE" -> {
                            val fieldValidations = childObject.getJSONObject("fieldValidations")

                        }

                        "SECTION" -> {
                            val childSecAcronym = childObject.getString("acronym")

                            val tabName = formAcronym + "_" + childSecAcronym

                            validation(childObject, tabName, formAcronym)
                        }
                    }

                    if (childObject.getString("type") == "SECTION") {
                        val childSecAcronym = childObject.getString("acronym")
                        val childTabName = formAcronym + "_" + childSecAcronym
                        validation(childObject, childTabName, formAcronym)
                    } else {
                        try {
                            val label = childObject.getJSONObject("properties").getString("label")
                            val placeholder =
                                childObject.getJSONObject("properties").getString("placeholder")
                            val childId = childObject.getString("id")
                            val fieldValidations = childObject.getJSONObject("fieldValidations")

                            Log.d("TAG", "validation: label: $label placeholder: $placeholder childId: $childId fieldValidations: $fieldValidations")

                        }catch (e: Exception){
                            Log.d("TAG", "Exception: ${e.message}")
                        }
                 }
                }

            }

        }
    */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_section, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isOnCreatedView=true
        tabChangeLoadData()

    }
    override fun onTabChanged(tabIndex: Int) {
        tabChangeLoadData()
    }

    fun tabChangeLoadData(){
        //jsonString = arguments?.getString("jsonString")!!
//        jsonData = (arguments?.getParcelable("jsonData") as? JSONFormDataModel)!!
        endSection = arguments?.getInt("endSection")!!
        sectionPos = arguments?.getInt("sectionPos")!!
        subId = arguments?.getInt("subId")!!
        formJsonId = arguments?.getLong("formJsonId", 0)!!

        linearLayoutSection = view?.findViewById(R.id.linearLayoutSection)!!
        linearLayoutSection.removeAllViews()

        //imgCaptureBitmap = null
        etuploadImage = EditText(requireContext())
        selectedDate = Calendar.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        initUI()

       // jsonObject = JSONObject(jsonString)
        jsonObject = JSONObject(AddFormDataActivity.formJsonData)
        dataArray = jsonObject.getJSONArray("data")
        sectionObject = dataArray.getJSONObject(sectionPos)
        val formAcronym = jsonObject.getString("acronym")
        val secAcronym = sectionObject?.getString("acronym")
        val tableName = formAcronym + "_" + secAcronym
        if (sectionObject != null) {
            displaySectionData(
                sectionObject = sectionObject!!,
                bt = bt,
                tableName = tableName,
                formAcronym = formAcronym
            )
        }

        if (sectionPos == endSection - 1) {
            buttonViewSubmit(bt)
        }
    }
    @SuppressLint("Range")
    override fun onResume() {
        super.onResume()


    }

    override fun onPause() {
        super.onPause()
       // Log.d(TAG, "onPause: ${AddFormDataActivity.formJsonData}")

      /*  Log.d(TAG, "onPause: ")
        //val editTextWithTag = ll.findViewWithTag<EditText>()
        for (i in 0 until ll.childCount) {
            val view = ll.getChildAt(i)
            if (view is EditText) {
                if (view.text.toString() != "") {
                    val editTextValue = view.text.toString()
                    Log.d("TAG", "EditText Value: $editTextValue")
                }
            }
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isOnCreatedView = false
        Log.d(TAG, "onDestroyView: ")
    }

    private fun displaySectionData(
        sectionObject: JSONObject,
        indentation: String = "",
        bt: Button,
        tableName: String,
        formAcronym: String
    ) {
        val childrenArray = sectionObject.getJSONArray("children")

        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)

            when (childObject.getString("type")) {
                "TEXT" -> {
                    val label = childObject.getJSONObject("properties").getString("label")
                    val placeholder =
                        childObject.getJSONObject("properties").getString("placeholder")
                    val childId = childObject.getString("id")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    try {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    } catch (_: Exception) {
                    }
                    inputText(placeholder, label, tableName, childId, fieldValidations, skipLogic)
                }

                "NUMBERS" -> {
                    val label = childObject.getJSONObject("properties").getString("label")
                    val placeholder =
                        childObject.getJSONObject("properties").getString("placeholder")
                    val childId = childObject.getString("id")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    try {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    } catch (_: Exception) {
                    }
                    inputNumber(placeholder, label, tableName, childId, fieldValidations, skipLogic)
                }

                "DROPDOWN" -> {
                    val label = childObject.getJSONObject("properties").getString("label")
                    val placeholder =
                        childObject.getJSONObject("properties").getString("placeholder")

                    val childId = childObject.getString("id")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")

                    var optionsList: JSONArray? = null
                    var skipLogic: JSONArray? = null

                    try {
                        // optionsList = childObject.getJSONArray("options")
                        skipLogic = childObject.getJSONArray("skipLogic")
                    } catch (_: Exception) {
                    }

                    val options = mutableMapOf<Int, String>()
                    options.clear()

                    if (childObject.has("options")) {
                        optionsList = childObject.getJSONArray("options")
                        if (optionsList.length() > 0) {
                            for (i in 0 until optionsList.length()) {
                                val id = optionsList.getJSONObject(i).getInt("id")
                                val value = optionsList.getJSONObject(i).getString("value")
                                options.put(id, value)
                            }
                        }
                        dropDown(
                            label,
                            options,
                            tableName,
                            childId,
                            fieldValidations,
                            placeholder,
                            skipLogic
                        )
                    } else {
                        if (childObject.has("relativeOptionsLogic")) {
                            val relativeOptionsLogic = childObject.getString("relativeOptionsLogic")

                            dropDown(
                                label,
                                options,
                                tableName,
                                childId,
                                fieldValidations,
                                placeholder,
                                skipLogic,
                                relativeOptionsLogic
                            )
                        } else {
                            dropDown(
                                label,
                                options,
                                tableName,
                                childId,
                                fieldValidations,
                                placeholder,
                                skipLogic
                            )
                        }
                    }
                }

                "TEXT_AREA" -> {
                    val label = childObject.getJSONObject("properties").getString("label")
                    val placeholder =
                        childObject.getJSONObject("properties").getString("placeholder")
                    val childId = childObject.getString("id")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    try {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    } catch (_: Exception) {
                    }
                    textArea(placeholder, label, tableName, childId, fieldValidations, skipLogic)
                }

                "RADIO" -> {
                    val label = childObject.getJSONObject("properties").getString("label")
                    val childId = childObject.getString("id")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    val valueRequired = fieldValidations.getBoolean("valueRequired")

                    val rg = RadioGroup(requireContext())
                    val tvRadio = TextView(requireContext())
                    tvRadio.text = label
                    tvRadio.textSize = 20f
                    val layoutParamsTV = LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                    )
                    layoutParamsTV.setMargins(0, 25, 0, 0)
                    tvRadio.layoutParams = layoutParamsTV
                    ll.addView(tvRadio)

                    if (valueRequired) {
                        val spannable = SpannableString("$label*")
                        spannable.setSpan(
                            ForegroundColorSpan(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            ),
                            label.length,
                            spannable.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        tvRadio.text = spannable
                    }

                    var optionsList: JSONArray? = null
                    var skipLogic: JSONArray? = null
                    try {
                        optionsList = childObject.getJSONArray("options")
                        skipLogic = childObject.getJSONArray("skipLogic")
                    } catch (_: Exception) {
                    }

                    if (optionsList != null) {
                        radioButton(optionsList, rg, tableName, childId, skipLogic, tvRadio)
                    }
                    ll.addView(rg)
                }

                "UPLOAD_IMAGE" -> {
                    val childId = childObject.getString("id")
                    val label = childObject.getJSONObject("properties").getString("label")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    try {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    } catch (_: Exception) {
                    }
                    uploadImage(label, tableName, childId, fieldValidations, skipLogic)
                }

                "CAPTURE_IMAGE" -> {
                    val childId = childObject.getString("id")
                    val label = childObject.getJSONObject("properties").getString("label")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    try {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    } catch (_: Exception) {
                    }
                    captureImage(label, tableName, childId, fieldValidations, skipLogic)
                }

                "DATE" -> {
                    val childId = childObject.getString("id")
                    val label = childObject.getJSONObject("properties").getString("label")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    try {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    } catch (_: Exception) {
                    }
                    dateSelect(label, tableName, childId, fieldValidations, skipLogic)
                }

                "LOCATION" -> {
                    val childId = childObject.getString("id")
                    val label = childObject.getJSONObject("properties").getString("label")
                    val placeholder =
                        childObject.getJSONObject("properties").getString("placeholder")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    try {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    } catch (_: Exception) {
                    }
                    getCurrentLocation(
                        label,
                        placeholder,
                        tableName,
                        childId,
                        fieldValidations,
                        skipLogic
                    )

                }

                "ADDABLE" -> {
                    childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    try {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    } catch (_: Exception) {
                    }

                    val count = 0
                    val idDBAddable: Long = 0
                   // val tableName = tableName +"_"+ childObject.getString("acronym")

                    addable(childObject, bt, skipLogic, tableName, count, idDBAddable)
                }

                "SECTION" -> {
                    val sectionTitle = childObject.getString("title")
                    val childSecAcronym = childObject.getString("acronym")
                    val secTv = TextView(requireContext())
                    secTv.text = sectionTitle
                    secTv.textSize = 26f
                    secTv.setTypeface(null, Typeface.BOLD)
                    val layoutParamsTV = LinearLayout.LayoutParams(
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                    )
                    layoutParamsTV.setMargins(0, 40, 0, 20)
                    secTv.layoutParams = layoutParamsTV
                    ll.addView(secTv)

                    val tabName = formAcronym + "_" + childSecAcronym

                    displaySectionData(childObject, "$indentation   ", bt, tabName, formAcronym)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentLocation(
        label: String,
        placeholder: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?
    ) {

        val tv = TextView(requireContext())
        tv.text = label
        tv.textSize = 20f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 25, 0, 0)
        tv.layoutParams = layoutParamsTV

        ll.addView(tv)

        val conLayout = ConstraintLayout(requireContext())
        ll.addView(conLayout)
        val btnLoc = Button(requireContext())

        textLocationEt = EditText(requireContext())

        textLocationEt.hint = placeholder
        /*textLocationEt.maxLines = 2
        textLocationEt.minLines = 2*/
        textLocationEt.textSize = 18f
        textLocationEt.isFocusable = false

        textLocationEt.setBackgroundResource(R.drawable.et_back)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        textLocationEt.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)

        val paramsEditText = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        paramsEditText.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        paramsEditText.endToStart = btnLoc.id
        paramsEditText.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        paramsEditText.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

        textLocationEt.layoutParams = paramsEditText
        conLayout.addView(textLocationEt)

        //viewDataSetFromDbText(tableName, childId, textLocationEt)

        btnLoc.text = "LIVE"
        btnLoc.textSize = 16f
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        btnLoc.setTextColor(textColor)
        btnLoc.setBackgroundResource(R.drawable.btn_back)
        val paramsButton = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        paramsButton.startToEnd = textLocationEt.id
        paramsButton.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        paramsButton.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        paramsButton.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

        btnLoc.layoutParams = paramsButton
        btnLoc.setOnClickListener {
            checkLocationPermission(label, placeholder)
        }
        conLayout.addView(btnLoc)

        val valueRequired = fieldValidations.getBoolean("valueRequired")
        if (valueRequired) {
            val spannable = SpannableString("$label*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                label.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = spannable
        }

        if (skipLogic != null) {
            for (i in 0 until skipLogic.length()) {
                val skipLogicObject = skipLogic.getJSONObject(i)
                val relation = skipLogicObject.getString("relation")
                val flag = skipLogicObject.getBoolean("flag")

                val dataArray = skipLogicObject.getJSONArray("data")
                for (j in 0 until dataArray.length()) {
                    val dataObject = dataArray.getJSONObject(j)
                    val skipLogicQ = dataObject.getString("skipLogicQ")
                    val skipLogicVal = dataObject.getString("skipLogicVal")
                    val flagChild = dataObject.getBoolean("flag")

                    skipLogicFun(
                        skipLogicQ = skipLogicQ,
                        skipLogicVal = skipLogicVal,
                        type = "LOCATION",
                        textView = tv,
                        editText = textLocationEt,
                        button = btnLoc
                    )

                    val objDataArray = jsonObject.getJSONArray("data")
                    for (m in 0 until objDataArray.length()) {
                        val sectionObject = objDataArray.getJSONObject(m)
                        skipLogicUpdate(
                            sectionObject = sectionObject,
                            skipLogicQ = skipLogicQ,
                            skipLogicVal = skipLogicVal,
                            type = "LOCATION",
                            textView = tv,
                            editText = textLocationEt,
                            button = btnLoc
                        )
                    }
                }
            }
        }

        textLocationEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            @SuppressLint("Range")
            override fun afterTextChanged(s: Editable?) {
                /*val cursor = dbHelper.getSectionTable(tableName)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))

                            if (sId != subId) {
                                if (cursor.isLast) {
                                    if (sId != subId) {
                                        dbHelper.insertSectionData(
                                            tableName,
                                            childId,
                                            s.toString(),
                                            subId
                                        )
                                    }
                                }
                            } else {
                                dbHelper.updateSectionData(
                                    tableName,
                                    childId,
                                    s.toString(),
                                    subId
                                )
                            }
                        } while (cursor.moveToNext())
                    } else {
                        dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                    }
                } else {
                    dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                }*/
            }
        })
    }

    private fun checkLocationPermission(label: String, placeholder: String) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            liveLocation(label, placeholder)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun liveLocation(label: String, placeholder: String) {

        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                it?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    textLocationEt.setText("$latitude, $longitude")
                    //Log.d("TAG", "liveLocation: $latitude + $longitude")
                }
            }.addOnFailureListener {

            }
    }

    private fun dateSelect(
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?
    ) {
        val tv = TextView(requireContext())
        tv.text = label
        tv.textSize = 20f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 25, 0, 0)
        tv.layoutParams = layoutParamsTV
        ll.addView(tv)

        textDateEt = EditText(requireContext())
        textDateEt.hint = "Click and select date"
        textDateEt.textSize = 18f
        textDateEt.isFocusable = false
        textDateEt.isClickable = true
        textDateEt.setBackgroundResource(R.drawable.et_back)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        textDateEt.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        ll.addView(textDateEt)

        val dateError = TextView(requireContext())
        dateError.visibility = View.GONE
        dateError.setTextColor(Color.RED)
        dateError.textSize = 14f
        val layoutParamsErr = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        dateError.gravity = Gravity.END
        dateError.layoutParams = layoutParamsErr
        dateError.text = "Please Select Value"
        ll.addView(dateError)
        val valueRequired = fieldValidations.getBoolean("valueRequired")

        textDateEt.setOnClickListener {
            showDatePickerDialog(
                valueRequired,
                tableName,
                childId,
                textDateEt,
                dateError,
                fieldValidations
            )
        }

        viewDataSetFromDbText(tableName, childId, textDateEt)

        if (valueRequired) {
            val spannable = SpannableString("$label*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                label.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = spannable
        }

        if (skipLogic != null) {
            for (i in 0 until skipLogic.length()) {
                val skipLogicObject = skipLogic.getJSONObject(i)
                val relation = skipLogicObject.getString("relation")
                val flag = skipLogicObject.getBoolean("flag")

                val dataArray = skipLogicObject.getJSONArray("data")
                for (j in 0 until dataArray.length()) {
                    val dataObject = dataArray.getJSONObject(j)
                    val skipLogicQ = dataObject.getString("skipLogicQ")
                    val skipLogicVal = dataObject.getString("skipLogicVal")
                    val flagChild = dataObject.getBoolean("flag")

                    skipLogicFun(
                        skipLogicQ = skipLogicQ,
                        skipLogicVal = skipLogicVal,
                        type = "DATE",
                        textView = tv,
                        editText = textDateEt
                    )

                    val objDataArray = jsonObject.getJSONArray("data")
                    for (m in 0 until objDataArray.length()) {
                        val sectionObject = objDataArray.getJSONObject(m)
                        skipLogicUpdate(
                            sectionObject = sectionObject,
                            skipLogicQ = skipLogicQ,
                            skipLogicVal = skipLogicVal,
                            type = "DATE",
                            textView = tv,
                            editText = textDateEt
                        )
                    }
                }
            }
        }

        textDateEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            @SuppressLint("Range")
            override fun afterTextChanged(s: Editable?) {
                /*val cursor = dbHelper.getSectionTable(tableName)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))

                            if (sId != subId) {
                                if (cursor.isLast) {
                                    if (sId != subId) {
                                        dbHelper.insertSectionData(
                                            tableName,
                                            childId,
                                            s.toString(),
                                            subId
                                        )
                                    }
                                }
                            } else {
                                dbHelper.updateSectionData(
                                    tableName,
                                    childId,
                                    s.toString(),
                                    subId
                                )
                            }
                        } while (cursor.moveToNext())
                    } else {
                        dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                    }
                } else {
                    dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                }*/
            }

        })

    }

    private fun showDatePickerDialog(
        valueRequired: Boolean,
        tableName: String,
        childId: String,
        textDateEt: EditText,
        dateError: TextView,
        fieldValidations: JSONObject
    ) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->

                //YYYYMMDD
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val formattedDate = sdf.format(selectedDate.time)
                this.textDateEt.setText(formattedDate)

                dateCheckValidation(valueRequired, tableName, childId, textDateEt, dateError)
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )

        try {
            val getMaxLimit = fieldValidations.getInt("maxLimit")
            val getMinLimit = fieldValidations.getInt("minLimit")

            val minLimit = getFormattedDateInMillis("yyyyMMdd", getMinLimit.toString())
            val maxLimit = getFormattedDateInMillis("yyyyMMdd", getMaxLimit.toString())

            datePickerDialog.datePicker.minDate = minLimit
            datePickerDialog.datePicker.maxDate = maxLimit
        } catch (_: Exception) {
        }

        datePickerDialog.setOnCancelListener {
            dateCheckValidation(valueRequired, tableName, childId, textDateEt, dateError)
        }
        datePickerDialog.show()
    }

    private fun getFormattedDateInMillis(format: String, date: String): Long {
        val sdf = SimpleDateFormat(format, Locale.US)
        val formattedDate = sdf.parse(date)
        return formattedDate?.time ?: 0
    }
    @SuppressLint("Range")
    private fun dateCheckValidation(
        valueRequired: Boolean,
        tableName: String,
        childId: String,
        textSelect: EditText,
        dateError: TextView
    ) {
        if (valueRequired) {
           /* val cursor = dbHelper.getSectionTable(tableName)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                        val value = cursor.getString(cursor.getColumnIndex(childId))
                        if (sId == subId) {
                            if (cursor.isLast) {
                                if (!value.isNullOrBlank() && value != "NULL") {
                                    textSelect.error = null
                                    dateError.visibility = View.GONE
                                } else {
                                    textSelect.error = "Please Select Value"
                                    dateError.visibility = View.VISIBLE
                                }
                            }
                        } else {
                            if (cursor.isLast) {
                                textSelect.error = "Please Select Value"
                                dateError.visibility = View.VISIBLE
                            }
                        }
                    } while (cursor.moveToNext())
                }
            } else {

            }*/
        }
    }

    @SuppressLint("QueryPermissionsNeeded", "Range")
    private fun captureImage(
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?
    ) {
        val tv = TextView(requireContext())
        tv.text = label
        tv.textSize = 20f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 25, 0, 0)
        tv.layoutParams = layoutParamsTV
        ll.addView(tv)

        imgCapture = ImageView(requireContext())
        imgCapture.id = View.generateViewId()
        imgCapture.setImageResource(R.drawable.ic_capture)
        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.image_width_250dp),
            resources.getDimensionPixelSize(R.dimen.image_height_250dp)
        )
        imgCapture.layoutParams = layoutParams
        imgCapture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
        ll.addView(imgCapture)
        etImgCapture = EditText(requireContext())
        etImgCapture.visibility = EditText.GONE
        ll.addView(etImgCapture)

        /*val cursor = dbHelper.getSectionTable(tableName)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                    val value = cursor.getString(cursor.getColumnIndex(childId))
                    if (sId == subId) {
                        if (cursor.isLast) {
                            if (value != "" && value != "NULL" && value != "null") {
                                value?.let {
                                    val stringToBitmap = stringToBitmap(it)
                                    imgCapture.setImageBitmap(stringToBitmap)
                                }
                            }
                        }
                    }
                } while (cursor.moveToNext())
            }
        }*/

        val valueRequired = fieldValidations.getBoolean("valueRequired")
        if (valueRequired) {
            val spannable = SpannableString("$label*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                label.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = spannable
        }
        if (skipLogic != null) {
            for (i in 0 until skipLogic.length()) {
                val skipLogicObject = skipLogic.getJSONObject(i)
                val relation = skipLogicObject.getString("relation")
                val flag = skipLogicObject.getBoolean("flag")

                val dataArray = skipLogicObject.getJSONArray("data")
                for (j in 0 until dataArray.length()) {
                    val dataObject = dataArray.getJSONObject(j)
                    val skipLogicQ = dataObject.getString("skipLogicQ")
                    val skipLogicVal = dataObject.getString("skipLogicVal")
                    val flagChild = dataObject.getBoolean("flag")

                    skipLogicFun(
                        skipLogicQ = skipLogicQ,
                        skipLogicVal = skipLogicVal,
                        type = "IMAGE",
                        textView = tv,
                        imageView = imgCapture
                    )

                    val objDataArray = jsonObject.getJSONArray("data")
                    for (m in 0 until objDataArray.length()) {
                        val sectionObject = objDataArray.getJSONObject(m)
                        skipLogicUpdate(
                            sectionObject = sectionObject,
                            skipLogicQ = skipLogicQ,
                            skipLogicVal = skipLogicVal,
                            type = "IMAGE",
                            textView = tv,
                            imageView = imgCapture
                        )
                    }
                }
            }
        }

        etImgCapture.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
               /* val cursor = dbHelper.getSectionTable(tableName)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))

                            if (sId != subId) {
                                if (cursor.isLast) {
                                    if (sId != subId) {
                                        dbHelper.insertSectionData(
                                            tableName,
                                            childId,
                                            s.toString(),
                                            subId
                                        )
                                    }
                                }
                            } else {
                                dbHelper.updateSectionData(
                                    tableName,
                                    childId,
                                    s.toString(),
                                    subId
                                )
                            }
                        } while (cursor.moveToNext())
                    } else {
                        dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                    }
                } else {
                    dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                }*/
            }
        })
    }

    @SuppressLint("Range")
    private fun uploadImage(
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?
    ) {

        val tv = TextView(requireContext())
        tv.text = label
        tv.textSize = 20f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 25, 0, 0)
        tv.layoutParams = layoutParamsTV
        ll.addView(tv)

        val img = ImageView(requireContext())
        img.id = View.generateViewId()
        img.setImageResource(R.drawable.ic_select)
        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.image_width_300dp),
            resources.getDimensionPixelSize(R.dimen.image_height_200dp)
        )
        img.layoutParams = layoutParams
        val someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle the result in this block
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                var imgUri2 = "".toUri()
                if (data != null) {
                    imgUri2 = (data.data ?: "") as Uri
                    Log.d("TAGB", "uploadImage:$childId + $imgUri2")
                }
                if (imgUri2 != "".toUri()) {
                    img.setImageURI(imgUri2)
                    val base64String  = uriToBase64(requireContext(), imgUri2)
                    val sizeInBytes = base64String?.length ?: 0
                    Log.d("TAGB", "uploadImage2 base64String: $childId + $sizeInBytes + $base64String")
                    etuploadImage.setText(base64String)
                }
            }
        }
        img.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
          //  startActivityForResult(intent, PICK_IMAGE_REQUEST)
             someActivityResultLauncher.launch(intent)
        }
        ll.addView(img)

        /*val cursor = dbHelper.getSectionTable(tableName)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                    val value = cursor.getString(cursor.getColumnIndex(childId))
                    if (sId == subId) {
                        if (cursor.isLast) {
                            if (value != "" && value != "NULL") {
                                value?.let {
                                    img.setImageURI(it.toUri())
                                }
                            }
                        }
                    }
                } while (cursor.moveToNext())
            }
        }*/

        //etuploadImage = EditText(requireContext())
        etuploadImage.tag = childId
        etuploadImage.visibility = EditText.GONE
        try {
            ll.addView(etuploadImage)
        } catch (_: Exception) {
        }


        val valueRequired = fieldValidations.getBoolean("valueRequired")
        if (valueRequired) {
            val spannable = SpannableString("$label*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                label.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = spannable
        }

        if (skipLogic != null) {
            for (i in 0 until skipLogic.length()) {
                val skipLogicObject = skipLogic.getJSONObject(i)
                val relation = skipLogicObject.getString("relation")
                val flag = skipLogicObject.getBoolean("flag")

                val dataArray = skipLogicObject.getJSONArray("data")
                for (j in 0 until dataArray.length()) {
                    val dataObject = dataArray.getJSONObject(j)
                    val skipLogicQ = dataObject.getString("skipLogicQ")
                    val skipLogicVal = dataObject.getString("skipLogicVal")
                    val flagChild = dataObject.getBoolean("flag")

                    skipLogicFun(
                        skipLogicQ = skipLogicQ,
                        skipLogicVal = skipLogicVal,
                        type = "IMAGE",
                        textView = tv,
                        imageView = img
                    )

                    val objDataArray = jsonObject.getJSONArray("data")
                    for (m in 0 until objDataArray.length()) {
                        val sectionObject = objDataArray.getJSONObject(m)
                        skipLogicUpdate(
                            sectionObject = sectionObject,
                            skipLogicQ = skipLogicQ,
                            skipLogicVal = skipLogicVal,
                            type = "IMAGE",
                            textView = tv,
                            imageView = img
                        )
                    }
                }
            }
        }

        etuploadImage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                //  if (childId == )
               /* val cursor = dbHelper.getSectionTable(tableName)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))

                            if (sId != subId) {
                                if (cursor.isLast) {
                                    if (sId != subId) {
                                        dbHelper.insertSectionData(
                                            tableName,
                                            childId,
                                            s.toString(),
                                            subId
                                        )
                                    }
                                }
                            } else {
                                dbHelper.updateSectionData(
                                    tableName,
                                    childId,
                                    s.toString(),
                                    subId
                                )
                            }
                        } while (cursor.moveToNext())
                    } else {
                        dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                    }
                } else {
                    dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                }*/
            }
        })

    }

    fun uriToBase64(context: Context, uri: Uri): String? {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.contentResolver.openInputStream(uri)
            val buffer = ByteArray(8192) // Adjust buffer size as necessary
            val outputStream = ByteArrayOutputStream()
            var bytesRead: Int
            while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            val imageBytes = outputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun textArea(
        placeholder: String,
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?
    ) {
        val textAreaTV = TextView(requireContext())
        textAreaTV.text = label
        textAreaTV.textSize = 20f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 25, 0, 0)
        textAreaTV.layoutParams = layoutParamsTV

        ll.addView(textAreaTV)

        val textAreaEt = EditText(requireContext())
        textAreaEt.hint = placeholder
        textAreaEt.textSize = 18f
        textAreaEt.inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
        textAreaEt.setBackgroundResource(R.drawable.et_back)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        textAreaEt.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)

        viewDataSetFromDbText(tableName, childId, textAreaEt)

        val valueRequired = fieldValidations.getBoolean("valueRequired")

        if (valueRequired) {
            val spannable = SpannableString("$label*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                label.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textAreaTV.text = spannable
            try {
                val maxChar = fieldValidations.getString("maxChar").toInt()
                val minChar = fieldValidations.getString("minChar").toInt()
                textAreaEt.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        if (validateText(textAreaEt.text.toString(), minChar, maxChar)) {
                        } else {
                            textAreaEt.error =
                                "Input must be between $minChar and $maxChar characters."
                        }
                    }
                }

            } catch (_: Exception) {
            }
        }

        if (skipLogic != null) {
            for (i in 0 until skipLogic.length()) {
                val skipLogicObject = skipLogic.getJSONObject(i)
                val relation = skipLogicObject.getString("relation")
                val flag = skipLogicObject.getBoolean("flag")

                val dataArray = skipLogicObject.getJSONArray("data")
                for (j in 0 until dataArray.length()) {
                    val dataObject = dataArray.getJSONObject(j)
                    val skipLogicQ = dataObject.getString("skipLogicQ")
                    val skipLogicVal = dataObject.getString("skipLogicVal")
                    val flagChild = dataObject.getBoolean("flag")

                    skipLogicFun(
                        skipLogicQ = skipLogicQ,
                        skipLogicVal = skipLogicVal,
                        type = "TEXT_AREA",
                        textView = textAreaTV,
                        editText = textAreaEt
                    )

                    val objDataArray = jsonObject.getJSONArray("data")
                    for (m in 0 until objDataArray.length()) {
                        val sectionObject = objDataArray.getJSONObject(m)
                        skipLogicUpdate(
                            sectionObject = sectionObject,
                            skipLogicQ = skipLogicQ,
                            skipLogicVal = skipLogicVal,
                            type = "TEXT_AREA",
                            textView = textAreaTV,
                            editText = textAreaEt
                        )
                    }
                }
            }
        }

        inputTextAreaET(textAreaEt, tableName, childId)

        ll.addView(textAreaEt)
    }

    private fun inputTextAreaET(textAreaEt: EditText, tableName: String, childId: String) {

        textAreaEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            @SuppressLint("Range")
            override fun afterTextChanged(s: Editable?) {
                //validateInputText(s.toString())
                /*val cursor = dbHelper.getSectionTable(tableName)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))

                            if (sId != subId) {
                                if (cursor.isLast) {
                                    if (sId != subId) {
                                        dbHelper.insertSectionData(
                                            tableName,
                                            childId,
                                            s.toString(),
                                            subId
                                        )
                                    }
                                }
                            } else {
                                dbHelper.updateSectionData(
                                    tableName,
                                    childId,
                                    s.toString(),
                                    subId
                                )
                            }
                        } while (cursor.moveToNext())
                    } else {
                        dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                    }
                } else {
                    dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                }*/
            }

        })

    }

    @SuppressLint("Range", "SetTextI18n")
    private fun dropDown(
        label: String,
        options: MutableMap<Int, String>,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        placeholder: String,
        skipLogic: JSONArray?,
        relativeOptionsLogic: String? = null
    ) {
        val tvDropDown = TextView(requireContext())
        tvDropDown.text = label
        tvDropDown.textSize = 20f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 25, 0, 0)
        tvDropDown.layoutParams = layoutParamsTV
        ll.addView(tvDropDown, layoutParamsTV)

        val textSelect = EditText(requireContext())
        textSelect.hint = placeholder
        textSelect.textSize = 18f
        textSelect.isFocusable = false
        textSelect.isClickable = true
        textSelect.setBackgroundResource(R.drawable.et_back)
        textSelect.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        textSelect.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        ll.addView(textSelect)

        val dropDownError = TextView(requireContext())
        dropDownError.visibility = View.GONE
        dropDownError.setTextColor(Color.RED)
        dropDownError.textSize = 14f
        val layoutParamsErr = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        dropDownError.gravity = Gravity.END
        dropDownError.layoutParams = layoutParamsErr
        dropDownError.text = "Please Select Value"
        ll.addView(dropDownError)

        val textSelectId = EditText(requireContext())
        textSelectId.tag = childId
        textSelectId.visibility = View.GONE
        ll.addView(textSelectId)

        val valueRequired = fieldValidations.getBoolean("valueRequired")
        val multiSelect = fieldValidations.getBoolean("multiSelect")

        viewDataSetFromDbText(tableName, childId, textSelect, "DROPDOWN", options)

        if (valueRequired) {
            val spannable = SpannableString("$label*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                label.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tvDropDown.text = spannable
        }

        if (skipLogic != null) {
            for (i in 0 until skipLogic.length()) {
                val skipLogicObject = skipLogic.getJSONObject(i)
                val relation = skipLogicObject.getString("relation")
                val flag = skipLogicObject.getBoolean("flag")

                val dataArray = skipLogicObject.getJSONArray("data")
                for (j in 0 until dataArray.length()) {
                    val dataObject = dataArray.getJSONObject(j)
                    val skipLogicQ = dataObject.getString("skipLogicQ")
                    val skipLogicVal = dataObject.getString("skipLogicVal")
                    val flagChild = dataObject.getBoolean("flag")

                    skipLogicFun(
                        skipLogicQ = skipLogicQ,
                        skipLogicVal = skipLogicVal,
                        type = "DROPDOWN",
                        textView = tvDropDown,
                        editText = textSelect,
                        childId = childId
                    )


                    val objDataArray = jsonObject.getJSONArray("data")
                    for (m in 0 until objDataArray.length()) {

                        val sectionObject = objDataArray.getJSONObject(m)
                        //objSec(sectionObject, skipLogicQ, skipLogicVal, tvDropDown, textSelect, relation)

                        skipLogicUpdate(
                            sectionObject = sectionObject,
                            skipLogicQ = skipLogicQ,
                            skipLogicVal = skipLogicVal,
                            type = "DROPDOWN",
                            textView = tvDropDown,
                            editText = textSelect,
                            relation = relation
                        )
                    }
                }
            }
        }

        textSelect.setOnClickListener {
            if (fieldValidations.has("dbTable") && fieldValidations.getString("dbTable") != "null") {
                val dbTable = fieldValidations.getString("dbTable")
                if (dbTable == "state") {
                   /* AddFormDataActivity.statesList.forEachIndexed { index, state ->
                        options[state.id] = state.name
                    }*/

                    showDropDownDialog(
                        multiSelect,
                        options,
                        tableName,
                        childId,
                        textSelect,
                        placeholder,
                        textSelectId,
                        valueRequired,
                        dropDownError
                    )
                } else if (dbTable == "district") {
                    val dataArr = ArrayList<Int>()
                   /* val cursor = dbHelper.getSectionTable(tableName)
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                                val value =
                                    cursor.getString(cursor.getColumnIndex(relativeOptionsLogic))
                                if (sId == subId) {
                                    if (cursor.isLast) {
                                        if (options != null) {
                                            if (!value.isNullOrBlank() && value != "NULL") {
                                                dataArr.add(value.toInt())
                                                Log.d("TAG", " index: ${value.toInt()}")
                                                val retrievedList = value.split(", ")
                                                var dataValue: String? = null
                                                for (i in retrievedList) {
                                                    for ((index, data) in options) {
                                                        if (index == i.toInt()) {
                                                            dataArr.add(index)
                                                            *//*if (dataValue == null) {
                                                                dataValue = data
                                                            } else {
                                                                dataValue = "$dataValue, $data"
                                                            }*//*
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } while (cursor.moveToNext())
                        }
                    }*/
                    if (dataArr.isNotEmpty()) {

                        /*sectionViewModel.fetchDistrictList(StateIdsRequest(dataArr))

                        sectionViewModel.districtList.observe(
                            this,
                            Observer { resource ->

                                when (resource.status) {
                                    Status.LOADING -> {
                                        // Handle loading state if needed
                                    }

                                    Status.SUCCESS -> {

                                        resource.data?.forEachIndexed { index, state ->
                                            options.put(state.id, state.name)
                                        }
                                        // Use the districtList to populate your UI or perform any other actions
                                        Log.d("TAG", "District List: $districtList")
                                        Log.d("TAG", "dropDown2:options $options")

                                        showDropDownDialog(
                                            multiSelect,
                                            options,
                                            tableName,
                                            childId,
                                            textSelect,
                                            placeholder,
                                            textSelectId,
                                            valueRequired,
                                            dropDownError
                                        )
                                    }

                                    Status.ERROR -> {
                                        val errorMsg = resource.message ?: "Unknown error occurred"
                                        // Handle error state if needed
                                        Log.e("TAG", "Error: $errorMsg")
                                    }
                                }
                            })*/

                        getDistrictList(StateIdsRequest(dataArr)) { options ->
                            showDropDownDialog(
                                multiSelect,
                                options,
                                tableName,
                                childId,
                                textSelect,
                                placeholder,
                                textSelectId,
                                valueRequired,
                                dropDownError
                            )
                        }

                        //getDistrictList(StateIdsRequest(dataArr), options)
                       /* showDropDownDialog(
                            multiSelect,
                            options,
                            tableName,
                            childId,
                            textSelect,
                            placeholder,
                            textSelectId,
                            valueRequired,
                            dropDownError
                        )*/
                    } else {
                        Log.d("TAG", "dropDown: $$dataArr")
                    }

                }else if (dbTable == "city"){
                    val dataArr = ArrayList<Int>()

                   /* val cursor = dbHelper.getSectionTable(tableName)
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                                val value =
                                    cursor.getString(cursor.getColumnIndex(relativeOptionsLogic))
                                if (sId == subId) {
                                    if (cursor.isLast) {
                                        if (options != null) {
                                            if (!value.isNullOrBlank() && value != "NULL") {
                                                dataArr.add(value.toInt())
                                                Log.d("TAG", " index: ${value.toInt()}")
                                                val retrievedList = value.split(", ")
                                                var dataValue: String? = null
                                                for (i in retrievedList) {
                                                    for ((index, data) in options) {
                                                        if (index == i.toInt()) {
                                                            dataArr.add(index)
                                                            *//*if (dataValue == null) {
                                                                dataValue = data
                                                            } else {
                                                                dataValue = "$dataValue, $data"
                                                            }*//*
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } while (cursor.moveToNext())
                        }
                    }*/
                    if (dataArr.isNotEmpty()) {
/*
                        getCitiesList(DistrictIdsRequest(dataArr), options)


                        showDropDownDialog(
                            multiSelect,
                            options,
                            tableName,
                            childId,
                            textSelect,
                            placeholder,
                            textSelectId,
                            valueRequired,
                            dropDownError
                        )*/
                        getCitiesList(DistrictIdsRequest(dataArr)) { options ->
                            showDropDownDialog(
                                multiSelect,
                                options,
                                tableName,
                                childId,
                                textSelect,
                                placeholder,
                                textSelectId,
                                valueRequired,
                                dropDownError
                            )
                        }

                    } else {
                        Log.d("TAG", "dropDown: $$dataArr")
                    }
                }
            } else {
                showDropDownDialog(
                    multiSelect,
                    options,
                    tableName,
                    childId,
                    textSelect,
                    placeholder,
                    textSelectId,
                    valueRequired,
                    dropDownError
                )
            }

        }
    }

    /*fun getCitiesList(districtIdsRequest: DistrictIdsRequest, options: MutableMap<Int, String>) {
        sectionViewModel.getCitiesList(districtIdsRequest).observe(this) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    var data = resource.data?.body()
                     if (data != null) {
                         Log.d("TAG", "onCreate: ${resource.data?.body()}")
                         districtList.addAll(data.data)
                     }
                    if (data != null) {
                        data.data.forEachIndexed { index, state ->
                            options.put(state.id, state.name)
                        }
                    }
                }

                Status.ERROR -> {
                    Log.d("TAG", "Error: ${resource.message}")
                }

                Status.LOADING -> {
                }
            }
        }
    }*/

    private fun getCitiesList(districtIdsRequest: DistrictIdsRequest, callback: (MutableMap<Int, String>) -> Unit) {
        /*sectionViewModel.getCitiesList(districtIdsRequest).observe(this) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val data = resource.data?.body()
                    if (data != null) {
                        val optionsMap: MutableMap<Int, String> = mutableMapOf()
                        data.data.forEachIndexed { _, city ->
                            optionsMap[city.id] = city.name
                        }
                        callback(optionsMap)
                    }
                }
                Status.ERROR -> {
                    Log.d("TAG", "Error: ${resource.message}")
                }

                Status.LOADING -> {
                }
            }
        }*/
    }
    private fun getDistrictList(stateIdsRequest: StateIdsRequest, callback: (MutableMap<Int, String>) -> Unit) {
        /*sectionViewModel.getDistrictList(stateIdsRequest).observe(this) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val data = resource.data?.body()
                    if (data != null) {
                        val optionsMap: MutableMap<Int, String> = mutableMapOf()
                        data.data.forEachIndexed { _, state ->
                            optionsMap[state.id] = state.name
                        }
                        callback(optionsMap)
                    }
                }
                Status.ERROR -> {
                    Log.d("TAG", "Error: ${resource.message}")
                }

                Status.LOADING -> {
                }
            }
        }*/
    }

    /*fun getDistrictList(stateIdsRequest: StateIdsRequest, options: MutableMap<Int, String>) {
        sectionViewModel.getDistrictList(stateIdsRequest).observe(this) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    var data = resource.data?.body()
                     if (data != null) {
                         Log.d("TAG", "onCreate: ${resource.data?.body()}")
                         districtList.addAll(data.data)
                     }
                    if (data != null) {
                        data.data.forEachIndexed { index, state ->
                            options.put(state.id, state.name)
                        }
                    }
                }

                Status.ERROR -> {
                    Log.d("TAG", "Error: ${resource.message}")
                }

                Status.LOADING -> {
                }
            }
        }
    }*/

    @SuppressLint("Range")
    private fun objSec(
        sectionObject: JSONObject,
        skipLogicQ: String,
        skipLogicVal: String,
        tvDropDown: TextView,
        textSelect: EditText,
        relation: String,
        isDialogClick: Boolean = false
    ) {


        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val mainFlagList = arrayListOf<Boolean>()
            val childObject = childrenArray.getJSONObject(j)

            if (childObject.getString("type").equals("SECTION")) {
                objSec(
                    childObject,
                    skipLogicQ,
                    skipLogicVal,
                    tvDropDown,
                    textSelect,
                    relation,
                    isDialogClick
                )
            } else {
                val objId = childObject.getString("id")
                if (objId == skipLogicQ) {
                    var tbNM =
                        jsonObject.getString("acronym") + "_" + sectionObject.getString("acronym")
                    /*val cursor = dbHelper.getSectionTable(tbNM)
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                                val value = cursor.getString(cursor.getColumnIndex(skipLogicQ))
                                //if (sId == subId) {
                                //if (cursor.isLast) {
                                if (value != "" && value != "NULL") {
                                    if (value == skipLogicVal) {
                                        mainFlagList.add(true)
                                    } else {
                                        mainFlagList.add(false)
                                    }

                                    Log.d("TAG", "objSec: $value")
                                    //inputText.setText(value)
                                }
                                //}
                                //}
                            } while (cursor.moveToNext())
                        }
                        if (isDialogClick) {
                            if (relation.equals("or", true) && mainFlagList.contains(true)) {
                                tvDropDown.visibility = View.GONE
                                textSelect.visibility = View.GONE
                            } else if (relation.equals(
                                    "and",
                                    true
                                ) && mainFlagList.contains(false)
                            ) {
                                tvDropDown.visibility = View.VISIBLE
                                textSelect.visibility = View.VISIBLE
                            } else {
                                tvDropDown.visibility = View.VISIBLE
                                textSelect.visibility = View.VISIBLE
                            }
                        }
                    }*/
                    break
                }
            }
        }
    }


    @SuppressLint("Range")
    private fun showDropDownDialog(
        multiSelect: Boolean,
        options: Map<Int, String>,
        tableName: String,
        childId: String,
        textSelect: EditText,
        placeholder: String,
        textSelectId: EditText,
        valueRequired: Boolean,
        dropDownError: TextView
    ) {
        val selectedData = BooleanArray(options.size)
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(placeholder)
        builder.setCancelable(false)
        var selectedIndex = 0

        if (multiSelect) {
            showDropDownMultiSelect(tableName, childId, options, selectedData)
            builder.setMultiChoiceItems(
                options.values.toTypedArray(),
                selectedData
            ) { _, which, isChecked ->
                selectedData[which] = isChecked
            }
        } else {
            selectedIndex = showDropDownSingleSelect(tableName, childId)
            options.keys.forEachIndexed { index, s ->
                if (selectedIndex==s){
                    selectedIndex=index
                }
            }
            builder.setSingleChoiceItems(options.values.toTypedArray(), selectedIndex) { _, which ->
                Log.d("TAG", "showDropDownDialog: $selectedIndex")
                selectedIndex = which
            }
        }

        builder.setPositiveButton("OK") { _, _ ->
            val selectedItems = mutableListOf<String>()
            val selectedItemsId = mutableListOf<Int>()

            if (multiSelect) {
                for ((index, value) in options.values.withIndex()) {
                    if (selectedData[index]) {
                        selectedItems.add(value)
                        selectedItemsId.add(index)
                    }
                    textSelect.setText(selectedItems.joinToString(", "))
                    textSelectId.setText(selectedItemsId.joinToString(", "))
                }
            } else {
                if (selectedIndex != -1) {
                    //if (selectedItems.isNotEmpty() && selectedItemsId.isNotEmpty()) {
                        selectedItems.add(options.values.elementAt(selectedIndex))
                        selectedItemsId.add(options.keys.elementAt(selectedIndex))
                 //   }
                }

                demoMap.put(childId, selectedItemsId.toString())
                textSelect.setText(selectedItems.joinToString(", "))
                textSelectId.setText(selectedItemsId.joinToString(", "))
            }
            AddFormDataActivity.formJsonData = saveJson(textSelectId.text.toString(), childId)
            //Log.d("TAG", "buttonViewSubmitJp: ${AddFormDataActivity.formJsonData}")
            val selectedValue = selectedItems.joinToString(", ")
            /*val cursor = dbHelper.getSectionTable(tableName)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                        if (sId != subId) {
                            if (cursor.isLast) {
                                if (sId != subId) {
                                    dbHelper.insertSectionData(
                                        tableName,
                                        childId,
                                        textSelectId.text.toString(),
                                        subId
                                    )
                                }
                            }
                        } else {
                            dbHelper.updateSectionData(
                                tableName,
                                childId,
                                textSelectId.text.toString(),
                                subId
                            )
                        }
                    } while (cursor.moveToNext())
                } else {
                    dbHelper.insertSectionData(
                        tableName,
                        childId,
                        textSelectId.text.toString(),
                        subId
                    )
                }
            } else {
                dbHelper.insertSectionData(tableName, childId, textSelectId.text.toString(), subId)
            }*/

            dropDownCheckValidation(valueRequired, tableName, childId, textSelect, dropDownError)
        }
        builder.setNegativeButton("Cancel") { _, _ ->
            dropDownCheckValidation(valueRequired, tableName, childId, textSelect, dropDownError)
        }
        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("Range")
    private fun dropDownCheckValidation(
        valueRequired: Boolean,
        tableName: String,
        childId: String,
        textSelect: EditText,
        dropDownError: TextView
    ) {
        if (valueRequired) {
          /*  val cursor = dbHelper.getSectionTable(tableName)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                        val value = cursor.getString(cursor.getColumnIndex(childId))
                        if (sId == subId) {
                            if (cursor.isLast) {
                                if (!value.isNullOrBlank() && value != "NULL") {
                                    textSelect.error = null
                                    dropDownError.visibility = View.GONE
                                } else {
                                    textSelect.error = "Please Select Value"
                                    dropDownError.visibility = View.VISIBLE
                                }
                            }
                        } else {
                            if (cursor.isLast) {
                                textSelect.error = "Please Select Value"
                                dropDownError.visibility = View.VISIBLE
                            }
                        }
                    } while (cursor.moveToNext())
                }
            } else {

            }*/
        }
    }

    @SuppressLint("Range")
    private fun showDropDownMultiSelect(
        tableName: String,
        childId: String,
        options: Map<Int, String>,
        selectedData: BooleanArray
    ) {
        /*val cursor = dbHelper.getSectionTable(tableName)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                    val value = cursor.getString(cursor.getColumnIndex(childId))
                    if (sId == subId) {
                        if (cursor.isLast) {
                            if (options != null) {
                                if (!value.isNullOrBlank() && value != "NULL") {
                                    val valueArr = value.split(", ")
                                    for (arr in valueArr) {
                                        selectedData[arr.toInt()] = true
                                    }
                                }
                            }
                        }
                    }
                } while (cursor.moveToNext())
            }
        }*/
    }

    @SuppressLint("Range")
    private fun showDropDownSingleSelect(
        tableName: String,
        childId: String,
    ): Int {
        var selectedIndex = 0
        /*val cursor = dbHelper.getSectionTable(tableName)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                    val value = cursor.getString(cursor.getColumnIndex(childId))
                    if (sId == subId) {
                        if (cursor.isLast) {
                            if (!value.isNullOrBlank() && value != "NULL") {
                                selectedIndex = value.toInt()
                            }
                        }
                    }
                } while (cursor.moveToNext())
            }
        }*/
        return selectedIndex
    }

    @SuppressLint("Range")
    private fun addable(
        childObject: JSONObject,
        bt: Button,
        skipLogic: JSONArray?,
        tabNameAddable: String,
        count: Int,
        idDBAddable: Long = 0
    ) {
        val idDb = idDBAddable
        val countAddable = count + 1

        val label = childObject.getJSONObject("properties").getString("label")

        val relativeLayout = RelativeLayout(requireContext())
        val linearLayoutAddable = LinearLayout(requireContext())
        val layoutParamsLl = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayoutAddable.layoutParams = layoutParamsLl
        linearLayoutAddable.orientation = LinearLayout.VERTICAL
        ll.addView(linearLayoutAddable, layoutParamsLl)

        try {
           /* val cursor = dbHelper.getSectionTable(tabNameAddable)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                        //val value = cursor.getLong(cursor.getColumnIndex(childId))
                        if (sId == subId) {
                           // if (cursor.isLast) {
                                Log.d("TAG", "addable: $subId")
                                *//*if (value.toString() != "NULL" && value.toInt() != 0) {
                                    inputText.setText(value.toString())
                                }*//*
                          //  }
                        }
                    } while (cursor.moveToNext())
                }
            }*/
        } catch (_: Exception) {
        }


       /* val cursor = dbHelper.getSectionTable(tabNameAddable)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                    if (sId != subId) {
                        if (cursor.isLast) {
                            if (sId != subId) {
                                idDb = dbHelper.insertAddableData(
                                    tabNameAddable,
                                    subId
                                )
                            }
                        }
                    }else{
                        if (cursor.isLast) {
                            if (sId == subId) {
                                idDb = dbHelper.insertAddableData(
                                    tabNameAddable,
                                    subId
                                )
                            }
                        }
                    }
                } while (cursor.moveToNext())
            } else {
                idDb = dbHelper.insertAddableData(
                    tabNameAddable,
                    subId
                )
            }
        } else {
            idDb = dbHelper.insertAddableData(
                tabNameAddable,
                subId
            )
        }*/

        textViewWithDelete(label, relativeLayout, linearLayoutAddable, countAddable, idDb, tabNameAddable)

        val btn = Button(requireContext())
        //try {
        val addableFormat = childObject.getJSONArray("addableFormat")
            for (k in 0 until addableFormat.length()) {
                val addableItem = addableFormat.getJSONObject(k)
                val addableId = "abc" //addableItem.getString("id")
                val addableType = addableItem.getString("type")
                val addableLabel = addableItem.getString("label")
                val addablePlaceholder = addableItem.getString("placeholder")
                val valueRequired = addableItem.getBoolean("valueRequired")

                when (addableType) {
                    "ADDABLE_TEXT" -> {
                        //val fieldValidations = childObject.getJSONObject("fieldValidations")
                        inputTextAddable(
                            addablePlaceholder,
                            addableLabel,
                            linearLayoutAddable,
                            valueRequired,
                            tabNameAddable,
                            addableId,
                            countAddable,
                            idDb
                        )
                    }

                    "ADDABLE_DROPDOWN" -> {
                        val multiSelect = addableItem.getBoolean("multiSelect")
                        addableDropDown(
                            addableLabel,
                            addableItem,
                            linearLayoutAddable,
                            valueRequired,
                            tabNameAddable,
                            addableId,
                            addablePlaceholder,
                            multiSelect,
                            countAddable,
                            idDb
                        )
                    }
                }
            }
            val addButtonTitle = childObject.getString("addButtonTitle")

            buttonView(btn, addButtonTitle, childObject, bt, tabNameAddable, countAddable, idDb)


        if (skipLogic != null) {
            for (i in 0 until skipLogic.length()) {
                val skipLogicObject = skipLogic.getJSONObject(i)
                val relation = skipLogicObject.getString("relation")
                val flag = skipLogicObject.getBoolean("flag")

                val dataArray = skipLogicObject.getJSONArray("data")
                for (j in 0 until dataArray.length()) {
                    val dataObject = dataArray.getJSONObject(j)
                    val skipLogicQ = dataObject.getString("skipLogicQ")
                    val skipLogicVal = dataObject.getString("skipLogicVal")
                    val flagChild = dataObject.getBoolean("flag")

                    skipLogicFun(
                        skipLogicQ = skipLogicQ,
                        skipLogicVal = skipLogicVal,
                        type = "ADDABLE",
                        button = btn,
                        linearLayout = linearLayoutAddable
                    )

                    val objDataArray = jsonObject.getJSONArray("data")
                    for (m in 0 until objDataArray.length()) {
                        val sectionObject = objDataArray.getJSONObject(m)
                        skipLogicUpdate(
                            sectionObject = sectionObject,
                            skipLogicQ = skipLogicQ,
                            skipLogicVal = skipLogicVal,
                            type = "ADDABLE",
                            button = btn,
                            linearLayout = linearLayoutAddable
                        )
                    }
                    /*val editTextWithTag = ll.findViewWithTag<EditText>(skipLogicQ)
                    if (editTextWithTag != null) {
                        editTextWithTag.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                                if (s.toString() == skipLogicVal) {
                                    linearLayoutAddable.visibility = View.GONE
                                    btn.visibility = View.GONE
                                } else {
                                    linearLayoutAddable.visibility = View.VISIBLE
                                    btn.visibility = View.VISIBLE
                                }
                            }

                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {

                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {

                            }
                        })
                    }*/

                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun buttonViewSubmit(
        bt: Button
    ) {
        bt.text = "Submit"
        bt.textSize = 25f
        bt.gravity = Gravity.CENTER_HORIZONTAL
        bt.textAlignment = Button.TEXT_ALIGNMENT_CENTER
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        bt.setTextColor(textColor)
        bt.setBackgroundResource(R.color.blue)
        val layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 200, 0, 200)
        bt.layoutParams = layoutParams
        if (bt.parent != null) {
            (bt.parent as? ViewGroup)?.removeView(bt)
        }
        ll.addView(bt)

        bt.setOnClickListener {
           // val newJson = saveJson()
           // Log.d("TAG", "buttonViewSubmit: ${newJson}")

          //  dbHelper.updateSubmissionTable("Saved", subId)

            val intent = Intent(requireActivity(), FormDataActivity::class.java)
            intent.putExtra("formJsonData", jsonString)
            intent.putExtra("formJsonId", formJsonId)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun saveJson(value: String, childId: String): String {
        val jsonObjectOld = JSONObject(AddFormDataActivity.formJsonData)
        val dataArray = jsonObjectOld.getJSONArray("data")

        val sectionLength = dataArray.length()

        for (i in 0 until sectionLength) {
            val sectionObject = dataArray.getJSONObject(i)

            val formAcronym = jsonObjectOld.getString("acronym")
            val secAcronym = sectionObject?.getString("acronym")

            val tableName = formAcronym + "_" + secAcronym

            saveDataApi(sectionObject, i, tableName, formAcronym, value, childId)
        }
        return jsonObjectOld.toString()
    }

    @SuppressLint("Range", "SuspiciousIndentation")
    private fun saveDataApi(
        sectionObject: JSONObject,
        i: Int,
        tableName: String,
        formAcronym: String,
        value: String,
        childId: String
    ) {

        val childrenArray = sectionObject.getJSONArray("children")
        //val data = dbHelper.getSectionTableDataById(tableName, subId.toLong())

        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)

            if (childObject.getString("type") == "SECTION") {
                val childSecAcronym = childObject.getString("acronym")
                val childTabName = formAcronym + "_" + childSecAcronym
                saveDataApi(childObject, i, childTabName, formAcronym, value, childId)

            } else {
                val columnNm = childObject.getString("id")

              //  if (data != null) {
                    /*for ((columnName, columnValue) in data) {
                        if (columnNm == columnName) {*/
                if (childId == columnNm) {
                    childObject.put("value", value)
                }
                      /*  }
                    }*/
                /*} else {
                    // Log.d("TAG","No data found for ID: $id")
                }*/
            }
        }
    }

    private fun addableDropDown(
        addableLabel: String,
        addableItem: JSONObject,
        linearLayoutAddable: LinearLayout,
        valueRequired: Boolean,
        tableName: String,
        childId: String,
        addablePlaceholder: String,
        multiSelect: Boolean,
        countAddable: Int,
        idDb: Long
    ) {
        /*val tvAddableDropDown = TextView(requireContext())
        tvAddableDropDown.text = addableLabel
        tvAddableDropDown.textSize = 20f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 15, 0, 0)
        tvAddableDropDown.layoutParams = layoutParamsTV
        linearLayoutAddable.addView(tvAddableDropDown, layoutParamsTV)

        val spinner = Spinner(requireContext())
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 10, 0, 10)
        spinner.layoutParams = layoutParams
        val options = ArrayList<String>()
        options.clear()
        val addableOptionArr = addableItem.getJSONArray("options")
        for (i in 0 until addableOptionArr.length()) {
            options.add(addableOptionArr.getString(i))
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        linearLayoutAddable.addView(spinner, layoutParams)

        if (valueRequired) {
            val spannable = SpannableString("$addableLabel*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                addableLabel.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tvAddableDropDown.text = spannable
        }*/


        val tvAddableDropDown = TextView(requireContext())
        tvAddableDropDown.text = addableLabel
        tvAddableDropDown.textSize = 20f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 25, 0, 0)
        tvAddableDropDown.layoutParams = layoutParamsTV
        linearLayoutAddable.addView(tvAddableDropDown, layoutParamsTV)

        val textSelect = EditText(requireContext())
        textSelect.hint = addablePlaceholder
        textSelect.textSize = 18f
        textSelect.isFocusable = false
        textSelect.isClickable = true
        textSelect.setBackgroundResource(R.drawable.et_back)
        textSelect.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        textSelect.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        linearLayoutAddable.addView(textSelect)

        val dropDownError = TextView(requireContext())
        dropDownError.visibility = View.GONE
        dropDownError.setTextColor(Color.RED)
        dropDownError.textSize = 14f
        val layoutParamsErr = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        dropDownError.gravity = Gravity.END
        dropDownError.layoutParams = layoutParamsErr
        dropDownError.text = "Please Select Value"
        linearLayoutAddable.addView(dropDownError)

        val textSelectId = EditText(requireContext())
        textSelectId.tag = childId
        textSelectId.visibility = View.GONE
        linearLayoutAddable.addView(textSelectId)

        val addableOptionArr = addableItem.getJSONArray("options")
        /*val options = ArrayList<String>()
        options.clear()

        for (i in 0 until addableOptionArr.length()) {
            options.add(addableOptionArr.getString(i))
        }*/

        //viewDataSetFromDbText(tableName, childId, textSelect, "DROPDOWN", options)

        if (valueRequired) {
            val spannable = SpannableString("$addableLabel*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                addableLabel.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tvAddableDropDown.text = spannable
        }

        textSelect.setOnClickListener {
                addableDropDownDialog(addableOptionArr, addablePlaceholder, multiSelect, tableName, childId, valueRequired, textSelect, textSelectId, idDb)
        }


    }

    @SuppressLint("Range")
    private fun addableDropDownDialog(
        addableOptionArr: JSONArray,
        addablePlaceholder: String,
        multiSelect: Boolean,
        tableName: String,
        childId: String,
        valueRequired: Boolean,
        textSelect: EditText,
        textSelectId: EditText,
        idDb: Long
    ) {

        val options = mutableMapOf<Int, String>()

        for (i in 0 until addableOptionArr.length()) {
            val optionId = i
            val optionName = addableOptionArr.getString(i)
            options[optionId] = optionName
        }

        val selectedData = BooleanArray(options.size)
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(addablePlaceholder)
        builder.setCancelable(false)
        var selectedIndex = 0

        if (multiSelect) {
            // Handle multi-select logic
        } else {
            builder.setSingleChoiceItems(options.values.toTypedArray(), selectedIndex) { _, which ->
                selectedIndex = which
            }
        }

        builder.setPositiveButton("OK") { _, _ ->
            val selectedItems = mutableListOf<String>()
            val selectedItemsId = mutableListOf<Int>()

            if (!multiSelect) {
                if (selectedIndex != -1) {
                    selectedItems.add(options.values.elementAt(selectedIndex))
                    selectedItemsId.add(options.keys.elementAt(selectedIndex))
                }
                textSelect.setText(selectedItems.joinToString(", "))
                textSelectId.setText(selectedItemsId.joinToString(", "))
            }
            val selectedValue = selectedItems.joinToString(", ")

            /*val cursor = dbHelper.getSectionTable(tableName)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val id = cursor.getInt(cursor.getColumnIndex("id"))
                        if (id == idDb.toInt()){
                            dbHelper.updateAddableData(
                                tableName,
                                childId,
                                textSelect.text.toString(),
                                idDb.toInt()
                            )
                        }
                    } while (cursor.moveToNext())
                }
            }*/  }

        builder.setNegativeButton("Cancel") { _, _ ->
       }

        val dialog = builder.create()
        dialog.show()

        /*
         builder.setPositiveButton("OK") { _, _ ->
             val selectedItems = mutableListOf<String>()
             val selectedItemsId = mutableListOf<Int>()

             } else {
                 if (selectedIndex != -1) {
                     //if (selectedItems.isNotEmpty() && selectedItemsId.isNotEmpty()) {
                     selectedItems.add(options.values.elementAt(selectedIndex))
                     selectedItemsId.add(options.keys.elementAt(selectedIndex))
                     //   }
                 }

                 demoMap.put(childId, selectedItemsId.toString())
                 textSelect.setText(selectedItems.joinToString(", "))
                 textSelectId.setText(selectedItemsId.joinToString(", "))
             }
             val selectedValue = selectedItems.joinToString(", ")
             val cursor = dbHelper.getSectionTable(tableName)
             if (cursor != null) {
                 if (cursor.moveToFirst()) {
                     do {
                         val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                         if (sId != subId) {
                             if (cursor.isLast) {
                                 if (sId != subId) {
                                     dbHelper.insertSectionData(
                                         tableName,
                                         childId,
                                         textSelectId.text.toString(),
                                         subId
                                     )
                                 }
                             }
                         } else {
                             dbHelper.updateSectionData(
                                 tableName,
                                 childId,
                                 textSelectId.text.toString(),
                                 subId
                             )
                         }
                     } while (cursor.moveToNext())
                 } else {
                     dbHelper.insertSectionData(
                         tableName,
                         childId,
                         textSelectId.text.toString(),
                         subId
                     )
                 }
             } else {
                 dbHelper.insertSectionData(tableName, childId, textSelectId.text.toString(), subId)
             }

             dropDownCheckValidation(valueRequired, tableName, childId, textSelect, dropDownError)
         }
         builder.setNegativeButton("Cancel") { _, _ ->
             dropDownCheckValidation(valueRequired, tableName, childId, textSelect, dropDownError)
         }
         val dialog = builder.create()
         dialog.show()*/
    }


    private fun initUI() {
        val sv = ScrollView(requireContext())
        val layoutParamsSv = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.MATCH_PARENT
        )
        sv.layoutParams = layoutParamsSv
        sv.overScrollMode = View.OVER_SCROLL_NEVER
        sv.isVerticalScrollBarEnabled = false
        sv.isHorizontalScrollBarEnabled = false
        linearLayoutSection.addView(sv, layoutParamsSv)


        ll = LinearLayout(requireContext())

        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        ll.setPadding(0, 0, 0, paddingInPxTB)

        val layoutParamsLl = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        ll.layoutParams = layoutParamsLl
        ll.orientation = LinearLayout.VERTICAL
        sv.addView(ll, layoutParamsLl)
    }

    @SuppressLint("SetTextI18n")
    private fun textViewWithDelete(
        label: String,
        relativeLayout: RelativeLayout,
        linearLayoutAddable: LinearLayout,
        countAddable: Int,
        idDb: Long,
        tabNameAddable: String
    ) {
        val layoutParamsLl = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsLl.setMargins(0, 40, 0, 20)
        relativeLayout.layoutParams = layoutParamsLl
        linearLayoutAddable.addView(relativeLayout, layoutParamsLl)

        val tv = TextView(requireContext())
        if (countAddable < 10){
            tv.text = "0$countAddable $label"
        }else{
            tv.text = "$countAddable $label"
        }
        tv.textSize = 26f
        tv.setTypeface(null, Typeface.BOLD)
        val layoutParamsTV = RelativeLayout.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.addRule(RelativeLayout.ALIGN_PARENT_START)
        tv.layoutParams = layoutParamsTV
        relativeLayout.addView(tv, layoutParamsTV)

        val imageView = ImageView(requireContext())
        val layoutParamsImageView = RelativeLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT
        )
        layoutParamsImageView.addRule(RelativeLayout.ALIGN_PARENT_END)
        layoutParamsImageView.addRule(RelativeLayout.CENTER_VERTICAL)
        imageView.layoutParams = layoutParamsImageView
        imageView.setImageResource(R.drawable.ic_delete)
        relativeLayout.addView(imageView, layoutParamsImageView)

        imageView.setOnClickListener {
            ll.removeView(linearLayoutAddable)
          //  dbHelper.deleteAddableDataById(tabNameAddable, idDb)
            //Log.d("TAG", "textViewWithDelete: ${etInputTextAddable.id} + ${linearLayoutAddable.childCount}")
        }
    }

    private fun buttonView(
        btn: Button,
        label: String,
        childObject: JSONObject,
        bt: Button,
        tableName: String,
        countAddable: Int,
        idDBAddable: Long
    ) {

        btn.text = label
        btn.textSize = 18f
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        btn.setTextColor(textColor)
        btn.setBackgroundResource(R.color.green)
        val layoutParamsBtn = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsBtn.setMargins(0, 80, 0, 20)
        btn.layoutParams = layoutParamsBtn

        btn.setOnClickListener {
            ll.removeView(btn)
            if (bt.parent != null) {
                (bt.parent as? ViewGroup)?.removeView(bt)
            }
            addable(childObject, bt, null, tableName, countAddable, idDBAddable)
        }
        ll.addView(btn)
    }

    private fun checkBox(options: List<String>?) {

        for (i in 0 until options?.size!!) {
            val cb = CheckBox(requireContext())
            cb.text = options[i]
            cb.id = View.generateViewId()
            cb.textSize = 22f
            ll.addView(cb)
            cb.setOnCheckedChangeListener { buttonView, isChecked ->
            }
        }
    }

    @SuppressLint("Range")
    private fun radioButton(
        optionsList: JSONArray,
        rg: RadioGroup,
        tableName: String,
        childId: String,
        skipLogic: JSONArray?,
        tvRadio: TextView
    ) {

        val optionArr = optionsList
        for (i in 0 until optionArr.length()) {
            val radioButton = RadioButton(requireContext())
            radioButton.text = optionArr.getJSONObject(i).getString("value")
            radioButton.tag = optionArr.getJSONObject(i).getString("value")
            radioButton.textSize = 18f
            radioButton.id = View.generateViewId()
            rg.addView(radioButton)

            /*val cursor = dbHelper.getSectionTable(tableName)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                        val value = cursor.getString(cursor.getColumnIndex(childId))
                        if (sId == subId) {
                            if (cursor.isLast) {
                                if (value != "" && value != "NULL") {
                                    val rb = rg.findViewWithTag<RadioButton>(
                                        optionArr.getJSONObject(i).getString("value")
                                    )
                                    if (rb.text.toString() == value) {
                                        rb.isChecked = true
                                    }
                                }
                            }
                        }
                    } while (cursor.moveToNext())
                }
            }*/


            if (skipLogic != null) {
                for (i in 0 until skipLogic.length()) {
                    val skipLogicObject = skipLogic.getJSONObject(i)
                    val relation = skipLogicObject.getString("relation")
                    val flag = skipLogicObject.getBoolean("flag")

                    val dataArray = skipLogicObject.getJSONArray("data")
                    for (j in 0 until dataArray.length()) {
                        val dataObject = dataArray.getJSONObject(j)
                        val skipLogicQ = dataObject.getString("skipLogicQ")
                        val skipLogicVal = dataObject.getString("skipLogicVal")
                        val flagChild = dataObject.getBoolean("flag")

                        val editTextWithTag = ll.findViewWithTag<EditText>(skipLogicQ)
                        if (editTextWithTag != null) {
                            editTextWithTag.addTextChangedListener(object : TextWatcher {
                                override fun afterTextChanged(s: Editable?) {
                                    if (s.toString() == skipLogicVal) {
                                        rg.visibility = View.GONE
                                        tvRadio.visibility = View.GONE
                                    } else {
                                        rg.visibility = View.VISIBLE
                                        tvRadio.visibility = View.VISIBLE
                                    }
                                }

                                override fun beforeTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    count: Int,
                                    after: Int
                                ) {

                                }

                                override fun onTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    before: Int,
                                    count: Int
                                ) {

                                }
                            })
                        }

                        skipLogicFun(
                            skipLogicQ = skipLogicQ,
                            skipLogicVal = skipLogicVal,
                            type = "RADIO",
                            textView = tvRadio,
                            radioGroup = rg
                        )

                        val objDataArray = jsonObject.getJSONArray("data")
                        for (m in 0 until objDataArray.length()) {
                            val sectionObject = objDataArray.getJSONObject(m)
                            skipLogicUpdate(
                                sectionObject = sectionObject,
                                skipLogicQ = skipLogicQ,
                                skipLogicVal = skipLogicVal,
                                type = "RADIO",
                                textView = tvRadio,
                                radioGroup = rg
                            )
                        }
                    }
                }
            }

            radioButton.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked) {
                 /*   val cursor = dbHelper.getSectionTable(tableName)
                    if (cursor != null) {
                        Log.d("TAG", "radioButton: 1$")
                        if (cursor.moveToFirst()) {
                            do {
                                val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))

                                if (sId != subId) {

                                    if (cursor.isLast) {
                                        if (sId != subId) {
                                            val buttonText =
                                                buttonView.text?.toString() ?: "null or empty"

                                            dbHelper.insertSectionData(
                                                tableName,
                                                childId,
                                                buttonView.text.toString(),
                                                subId
                                            )
                                        }
                                    }
                                } else {
                                    val buttonText = buttonView.text?.toString() ?: "null or empty"
                                    Log.d("TAG", "radioButton: 4 ${buttonText}")
                                    dbHelper.updateSectionData(
                                        tableName,
                                        childId,
                                        buttonText,
                                        subId
                                    )
                                    cursor.close()
                                }
                            } while (cursor.moveToNext())
                        } else {
                            dbHelper.insertSectionData(
                                tableName,
                                childId,
                                buttonView.text.toString(),
                                subId
                            )
                        }
                    } else {
                        dbHelper.insertSectionData(
                            tableName,
                            childId,
                            buttonView.text.toString(),
                            subId
                        )
                    }*/
                } else {

                }
            }
        }
    }

    @SuppressLint("Range")
    private fun inputNumber(
        placeholder: String,
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?
    ) {
        val tvInputNumber = TextView(requireContext())
        tvInputNumber.text = label
        tvInputNumber.textSize = 20f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 25, 0, 0)
        tvInputNumber.layoutParams = layoutParamsTV

        ll.addView(tvInputNumber)

        val etInputNumber = EditText(requireContext())
        etInputNumber.tag = childId
        etInputNumber.hint = placeholder
        etInputNumber.textSize = 18f
        etInputNumber.inputType = InputType.TYPE_CLASS_NUMBER
        etInputNumber.setBackgroundResource(R.drawable.et_back)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        etInputNumber.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        ll.addView(etInputNumber)

        viewDataSetFromDbNumber(tableName, childId, etInputNumber)

        val valueRequired = fieldValidations.getBoolean("valueRequired")

        if (valueRequired) {
            val spannable = SpannableString("$label*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                label.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tvInputNumber.text = spannable
            try {
                etInputNumber.setOnFocusChangeListener { e, hasFocus ->
                    try {
                        Log.d("TAG", "inputNumber: $e")
                        val maxLimit = fieldValidations.getString("maxLimit").toLong()
                        val minLimit = fieldValidations.getString("minLimit").toLong()
                        if (!hasFocus) {
                            if (isValidNumber(etInputNumber.text.toString(), minLimit, maxLimit)) {
                            } else {
                                etInputNumber.error =
                                    "Enter a valid number between $minLimit and $maxLimit"
                            }
                        } else {
                            Log.d("TAG", "inputNumber: p")
                        }
                    } catch (_: Exception) {
                    }
                }
            } catch (_: Exception) {
                Log.d("TAG", "inputNumber: _")
            }

        }
        if (skipLogic != null) {
            for (i in 0 until skipLogic.length()) {
                val skipLogicObject = skipLogic.getJSONObject(i)
                val relation = skipLogicObject.getString("relation")
                val flag = skipLogicObject.getBoolean("flag")

                val skipLogicConditions = mutableListOf<SkipLogicCondition>()
                skipLogicConditions.clear()

                val dataArray = skipLogicObject.getJSONArray("data")
                for (j in 0 until dataArray.length()) {
                    val dataObject = dataArray.getJSONObject(j)
                    val skipLogicQ = dataObject.getString("skipLogicQ")
                    val skipLogicVal = dataObject.getString("skipLogicVal")
                    val flagChild = dataObject.getBoolean("flag")
                    skipLogicConditions.add(SkipLogicCondition(skipLogicQ, skipLogicVal))

                    skipLogicFun(
                        skipLogicQ = skipLogicQ,
                        skipLogicVal = skipLogicVal,
                        type = "NUMBERS",
                        textView = tvInputNumber,
                        editText = etInputNumber
                    )

                    val objDataArray = jsonObject.getJSONArray("data")
                    for (m in 0 until objDataArray.length()) {
                        val sectionObject = objDataArray.getJSONObject(m)
                        skipLogicUpdate(
                            sectionObject = sectionObject,
                            skipLogicQ = skipLogicQ,
                            skipLogicVal = skipLogicVal,
                            type = "NUMBERS",
                            textView = tvInputNumber,
                            editText = etInputNumber,
                        )
                    }
                }
                // handleSkipLogic(skipLogicConditions, tvInputNumber, etInputNumber, relation)
            }
        }
        inputNumberET(etInputNumber, tableName, childId, fieldValidations)
    }

    private fun handleSkipLogic(
        skipLogicConditions: List<SkipLogicCondition>,
        tvInputNumber: TextView,
        etInputNumber: EditText,
        relation: String
    ) {


        if (relation == "and") {
            var b = 0
            for (i in skipLogicConditions) {
                b++
                var x = checkCondition(i)
                if (x == false) {
                    Log.d("TAGb", "hello: $b")
                    break
                } else {
                    if (b == skipLogicConditions.size) {
                        Log.d("TAGb", "hello 1: $b")
                    }
                }

            }
        } else {
        }

        /* if (relation == "and") {
             // "and" relation: Hide if any condition is not satisfied
             var b = 0
             for (i in skipLogicConditions){
                 b++
                 var x = isConditionSatisfied(i)
                 if(x == false){
                     Log.d("TAGb", "hello: $b")
                     break
                 }else{
                     if (b == skipLogicConditions.size){
                         Log.d("TAGb", "hello 1: $b")
                     }
                 }
             }
             *//*if (skipLogicConditions.any { !isConditionSatisfied(it) }) {
                tvInputNumber.visibility = View.GONE
                etInputNumber.visibility = View.GONE
            } else {
                tvInputNumber.visibility = View.VISIBLE
                etInputNumber.visibility = View.VISIBLE
            }*//*
        } else {
            for (i in skipLogicConditions){

                var x = isConditionSatisfied(i)
                if(x == false){
                    Log.d("TAGb", "hello 2: $i")
                    //break
                }else{

                    Log.d("TAGb", "hello 3: $i")

                }
            }
            // "or" relation: Hide if none of the conditions are satisfied
            *//*if (skipLogicConditions.any { isConditionSatisfied(it) }) {
                tvInputNumber.visibility = View.GONE
                etInputNumber.visibility = View.GONE
            } else {
                tvInputNumber.visibility = View.VISIBLE
                etInputNumber.visibility = View.VISIBLE
            }}*/

    }

    private fun checkCondition(i: SkipLogicCondition): Boolean {
        val editTextWithTag = ll.findViewWithTag<EditText>(i.skipLogicQ)
        var x = false
        if (editTextWithTag != null) {

            editTextWithTag.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() == i.skipLogicVal) {
                        x = true
                        // tvInputNumber.visibility = View.GONE
                        // etInputNumber.visibility = View.GONE
                    } else {
                        x = false
                        //  tvInputNumber.visibility = View.VISIBLE
                        //  etInputNumber.visibility = View.VISIBLE
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                }
            })

        }
        return x
    }

    private fun isConditionSatisfied(condition: SkipLogicCondition): Boolean {
        val editTextWithTag = ll.findViewWithTag<EditText>(condition.skipLogicQ)
        return editTextWithTag?.text?.toString() == condition.skipLogicVal
    }

    private fun inputNumberET(
        etInputNumber: EditText,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject
    ) {
        etInputNumber.addTextChangedListener(object : TextWatcher {

            @SuppressLint("Range")
            override fun afterTextChanged(s: Editable?) {

                /*val maxLimit = fieldValidations.getString("maxLimit").toLong()
                val minLimit = fieldValidations.getString("minLimit").toLong()
               // if (!hasFocus) {
                    if (isValidNumber(etInputNumber.text.toString(), minLimit, maxLimit)) {
                        Log.d("TAG", "inputNumber: $minLimit + $maxLimit")
                    } else {
                        Log.d("TAG", "inputNumber: $minLimit + $maxLimit")
                        etInputNumber.error = "Enter a valid number between $minLimit and $maxLimit"
                    }
              //  }*/

               /* val cursor = dbHelper.getSectionTable(tableName)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                            if (sId != subId) {
                                if (cursor.isLast) {
                                    if (sId != subId) {
                                        dbHelper.insertSectionData(
                                            tableName,
                                            childId,
                                            s.toString(),
                                            subId
                                        )
                                    }
                                }
                            } else {
                                dbHelper.updateSectionData(
                                    tableName,
                                    childId,
                                    s.toString(),
                                    subId
                                )
                            }
                        } while (cursor.moveToNext())
                    } else {
                        dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                    }
                } else {
                    dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                }*/
            }

            override fun beforeTextChanged(
                beforeTextChangeds: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor", "Range")
    private fun inputText(
        placeholder: String,
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?
    ) {
        val tv = TextView(requireContext())
        tv.text = label
        tv.textSize = 20f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 25, 0, 0)
        tv.layoutParams = layoutParamsTV
        ll.addView(tv)

        val etInputText = EditText(requireContext())
        // etInputText.setTag("dw")
        etInputText.hint = placeholder
        etInputText.textSize = 18f
        etInputText.inputType = InputType.TYPE_CLASS_TEXT
        etInputText.setBackgroundResource(R.drawable.et_back)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        etInputText.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        ll.addView(etInputText)

        viewDataSetFromDbText(tableName, childId, etInputText)

        val valueRequired = fieldValidations.getBoolean("valueRequired")

        if (valueRequired) {
            val spannable = SpannableString("$label*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                label.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = spannable

            try {
                val maxChar = fieldValidations.getString("maxChar").toInt()
                val minChar = fieldValidations.getString("minChar").toInt()
                etInputText.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        if (validateText(etInputText.text.toString(), minChar, maxChar)) {
                        } else {
                            etInputText.error =
                                "Input must be between $minChar and $maxChar characters."
                        }
                    }
                }
            } catch (_: Exception) {
            }

        }

        if (skipLogic != null) {
            for (i in 0 until skipLogic.length()) {
                val skipLogicObject = skipLogic.getJSONObject(i)

                val relation = skipLogicObject.getString("relation")
                val flag = skipLogicObject.getBoolean("flag")
                var isDisplay =true
                if (skipLogicObject.has("is_display")) {
                    isDisplay = skipLogicObject.getBoolean("is_display")
                }

                val dataArray = skipLogicObject.getJSONArray("data")
                for (j in 0 until dataArray.length()) {
                    val dataObject = dataArray.getJSONObject(j)
                    val skipLogicQ = dataObject.getString("skipLogicQ")
                    val skipLogicVal = dataObject.getString("skipLogicVal")
                    val flagChild = dataObject.getBoolean("flag")

                    skipLogicFun(
                        skipLogicQ = skipLogicQ,
                        skipLogicVal = skipLogicVal,
                        type = "TEXT",
                        textView = tv,
                        editText = etInputText,
                        isDisplay= isDisplay
                    )

                    val objDataArray = jsonObject.getJSONArray("data")
                    for (m in 0 until objDataArray.length()) {
                        val sectionObject = objDataArray.getJSONObject(m)
                        skipLogicUpdate(
                            sectionObject = sectionObject,
                            skipLogicQ = skipLogicQ,
                            skipLogicVal = skipLogicVal,
                            type = "TEXT",
                            textView = tv,
                            editText = etInputText
                        )
                    }
                }
            }
        }
        inputTextET(etInputText, tableName, childId)
    }

    private fun inputTextET(etInputText: EditText, tableName: String, childId: String) {

        etInputText.addTextChangedListener(object : TextWatcher {
            @SuppressLint("Range")
            override fun afterTextChanged(s: Editable?) {
                AddFormDataActivity.formJsonData = saveJson(s.toString(), childId)
                /*val cursor = dbHelper.getSectionTable(tableName)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))

                            if (sId != subId) {
                                if (cursor.isLast) {
                                    if (sId != subId) {
                                        dbHelper.insertSectionData(
                                            tableName,
                                            childId,
                                            s.toString(),
                                            subId
                                        )
                                    }
                                }
                            } else {
                                dbHelper.updateSectionData(
                                    tableName,
                                    childId,
                                    s.toString(),
                                    subId
                                )
                            }
                        } while (cursor.moveToNext())
                    } else {
                        dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                    }
                } else {
                    dbHelper.insertSectionData(tableName, childId, s.toString(), subId)
                }*/
            }

            override fun beforeTextChanged(
                beforeTextChangeds: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })
    }

    @SuppressLint("Range")
    private fun viewDataSetFromDbText(
        tableName: String,
        childId: String,
        inputText: EditText,
        dropDown: String? = null,
        options: MutableMap<Int, String>? = null
    ) {
        try {
            if (dropDown == "DROPDOWN") {
                /*val cursor = dbHelper.getSectionTable(tableName)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                            val value = cursor.getString(cursor.getColumnIndex(childId))
                            if (sId == subId) {
                                if (cursor.isLast) {
                                    if (options != null) {
                                        if (!value.isNullOrBlank() && value != "NULL") {
                                            val retrievedList = value.split(", ")
                                            var dataValue: String? = null
                                            for (i in retrievedList) {
                                                for ((index, data) in options) {
                                                    if (index == i.toInt()) {
                                                        if (dataValue == null) {
                                                            dataValue = data
                                                        } else {
                                                            dataValue = "$dataValue, $data"
                                                        }

                                                    }
                                                }
                                            }
                                            inputText.setText(dataValue)
                                        }
                                    }
                                }
                            }
                        } while (cursor.moveToNext())
                    }
                }*/
            } else {
                /*val cursor = dbHelper.getSectionTable(tableName)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                            val value = cursor.getString(cursor.getColumnIndex(childId))
                            if (sId == subId) {
                                if (cursor.isLast) {
                                    if (value != "" && value != "NULL") {

                                        inputText.setText(value)
                                    }
                                }
                            }
                        } while (cursor.moveToNext())
                    }
                }*/
            }

        } catch (_: Exception) {
        }

    }

    @SuppressLint("Range")
    private fun viewDataSetFromDbNumber(tableName: String, childId: String, inputText: EditText) {
        /*try {
            val cursor = dbHelper.getSectionTable(tableName)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                        val value = cursor.getLong(cursor.getColumnIndex(childId))
                        if (sId == subId) {
                            if (cursor.isLast) {
                                if (value.toString() != "NULL" && value.toInt() != 0) {
                                    inputText.setText(value.toString())
                                }
                            }
                        }
                    } while (cursor.moveToNext())
                }
            }
        } catch (_: Exception) {
        }*/

    }

    @SuppressLint("ResourceType")
    private fun inputTextAddable(
        placeholder: String,
        label: String,
        linearLayoutAddable: LinearLayout,
        valueRequired: Boolean,
        tableName: String,
        childId: String,
        countAddable: Int,
        idDb: Long
    ) {
        val tv = TextView(requireContext())
        tv.text = label
        tv.textSize = 20f
        linearLayoutAddable.addView(tv)
        etInputTextAddable = EditText(requireContext())
        etInputTextAddable.hint = placeholder
        etInputTextAddable.textSize = 18f
        etInputTextAddable.inputType = InputType.TYPE_CLASS_TEXT
        etInputTextAddable.id = View.generateViewId()

        etInputTextAddable.setBackgroundResource(R.drawable.et_back)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        etInputTextAddable.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        linearLayoutAddable.addView(etInputTextAddable)

        if (valueRequired) {
            val spannable = SpannableString("$label*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                label.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = spannable
        }

        etInputTextAddable.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                /*val cursor = dbHelper.getSectionTable(tableName)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            val id = cursor.getInt(cursor.getColumnIndex("id"))

                            if (id == idDb.toInt()){
                                dbHelper.updateAddableData(
                                    tableName,
                                    childId,
                                    s.toString(),
                                    idDb.toInt()
                                )
                            }
                        } while (cursor.moveToNext())
                    }
                }*/
            }

            override fun beforeTextChanged(
                beforeTextChangeds: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

            }
        })
    }

/*    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == PICK_IMAGE_REQUEST) {
            imgUri = (data.data ?: "") as Uri
            if (imgUri != "".toUri()) {
                //img.setImageURI(imgUri)
                etuploadImage.setText(imgUri.toString())
            }
        } else if (data != null && requestCode == REQUEST_IMAGE_CAPTURE) {
            val imageBitmap = data.extras?.get("data") as Bitmap
            imgCapture.setImageBitmap(imageBitmap)

            var imageBitmapString = bitmapToString(imageBitmap)

            etImgCapture.setText(imageBitmapString)
        }
    }*/

    @SuppressLint("Range")
    private fun skipLogicFun(
        skipLogicQ: String,
        skipLogicVal: String,
        type: String,
        textView: TextView? = null,
        editText: EditText? = null,
        radioGroup: RadioGroup? = null,
        imageView: ImageView? = null,
        button: Button? = null,
        linearLayout: LinearLayout? = null,
        childId: String? = null,
        isDisplay: Boolean = true
    ) {

        val editTextWithTag = ll.findViewWithTag<EditText>(skipLogicQ)
        editTextWithTag?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                skipLogicViewShowHide(
                    type,
                    s.toString(),
                    skipLogicVal,
                    textView,
                    editText,
                    radioGroup,
                    imageView,
                    button,
                    linearLayout,
                    isDisplay
                )
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

            }
        })
    }

    @SuppressLint("Range")
    private fun skipLogicUpdate(
        sectionObject: JSONObject,
        skipLogicQ: String,
        skipLogicVal: String,
        type: String,
        textView: TextView? = null,
        editText: EditText? = null,
        radioGroup: RadioGroup? = null,
        imageView: ImageView? = null,
        button: Button? = null,
        linearLayout: LinearLayout? = null,
        relation: String? = null,
        isDisplay: Boolean = true
    ) {
        try {
            val childrenArray = sectionObject.getJSONArray("children")

            for (j in 0 until childrenArray.length()) {
                val childObject = childrenArray.getJSONObject(j)

                if (childObject.getString("type").equals("SECTION")) {
                    skipLogicUpdate(
                        childObject,
                        skipLogicQ,
                        skipLogicVal,
                        type,
                        textView,
                        editText,
                        radioGroup,
                        imageView,
                        button,
                        linearLayout,
                        relation
                    )
                } else {
                    val objId = childObject.getString("id")
                    if (objId == skipLogicQ) {
                        val tbNM =
                            jsonObject.getString("acronym") + "_" + sectionObject.getString("acronym")
                     /*   val cursor = dbHelper.getSectionTable(tbNM)

                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                do {

                                    val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                                    val value = cursor.getString(cursor.getColumnIndex(skipLogicQ))
                                    if (sId == subId) {
                                    //    Log.d("TAG", "skipLogicUpdate: 1")
                                        if (cursor.isLast) {
                                            if (value != "" && value != "NULL") {
                                                skipLogicViewShowHide(
                                                    type,
                                                    value,
                                                    skipLogicVal,
                                                    textView,
                                                    editText,
                                                    radioGroup,
                                                    imageView,
                                                    button,
                                                    linearLayout
                                                )
                                            }
                                        }
                                    } else {
                                        if (cursor.isLast) {
                                            // SkipLogic default view hide
                                            defaultViewHide(
                                                type,
                                                textView,
                                                editText,
                                                radioGroup,
                                                imageView,
                                                button,
                                                linearLayout,
                                                isDisplay
                                            )
                                        }
                                    }
                                } while (cursor.moveToNext())
                            }else{
                                // SkipLogic default view hide
                                defaultViewHide(
                                    type,
                                    textView,
                                    editText,
                                    radioGroup,
                                    imageView,
                                    button,
                                    linearLayout,
                                    isDisplay
                                )
                            }
                        }else{
                            // SkipLogic default view hide
                            defaultViewHide(
                                type,
                                textView,
                                editText,
                                radioGroup,
                                imageView,
                                button,
                                linearLayout,
                                isDisplay
                            )
                        }*/
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG,"EXCEPTION ="+e.toString())
        }

    }

    private fun skipLogicViewShowHide(
        type: String,
        value: String,
        skipLogicVal: String,
        textView: TextView? = null,
        editText: EditText? = null,
        radioGroup: RadioGroup? = null,
        imageView: ImageView? = null,
        button: Button? = null,
        linearLayout: LinearLayout? = null,
        isDisplay: Boolean = true
    ) {
        if (isDisplay) {
            if (type == "RADIO") {
                if (value == skipLogicVal) {
                    textView?.visibility = View.GONE
                    radioGroup?.visibility = View.GONE
                } else {
                    textView?.visibility = View.VISIBLE
                    radioGroup?.visibility = View.VISIBLE
                }
            } else if (type == "IMAGE") {
                if (value == skipLogicVal) {
                    textView?.visibility = View.GONE
                    imageView?.visibility = View.GONE
                } else {
                    textView?.visibility = View.VISIBLE
                    imageView?.visibility = View.VISIBLE
                }
            } else if (type == "LOCATION") {
                if (value == skipLogicVal) {
                    textView?.visibility = View.GONE
                    editText?.visibility = View.GONE
                    button?.visibility = View.GONE
                } else {
                    textView?.visibility = View.VISIBLE
                    editText?.visibility = View.VISIBLE
                    button?.visibility = View.VISIBLE
                }
            } else if (type == "ADDABLE") {
                if (value == skipLogicVal) {
                    linearLayout?.visibility = View.GONE
                    button?.visibility = View.GONE
                } else {
                    linearLayout?.visibility = View.VISIBLE
                    button?.visibility = View.VISIBLE
                }
            } else {
                if (value == skipLogicVal) {
                    textView?.visibility = View.GONE
                    editText?.visibility = View.GONE
                } else {
                    textView?.visibility = View.VISIBLE
                    editText?.visibility = View.VISIBLE
                }
            }
        }else{
            if (type == "RADIO") {
                if (value != skipLogicVal) {
                    textView?.visibility = View.GONE
                    radioGroup?.visibility = View.GONE
                } else {
                    textView?.visibility = View.VISIBLE
                    radioGroup?.visibility = View.VISIBLE
                }
            } else if (type == "IMAGE") {
                if (value != skipLogicVal) {
                    textView?.visibility = View.GONE
                    imageView?.visibility = View.GONE
                } else {
                    textView?.visibility = View.VISIBLE
                    imageView?.visibility = View.VISIBLE
                }
            } else if (type == "LOCATION") {
                if (value != skipLogicVal) {
                    textView?.visibility = View.GONE
                    editText?.visibility = View.GONE
                    button?.visibility = View.GONE
                } else {
                    textView?.visibility = View.VISIBLE
                    editText?.visibility = View.VISIBLE
                    button?.visibility = View.VISIBLE
                }
            } else if (type == "ADDABLE") {
                if (value != skipLogicVal) {
                    linearLayout?.visibility = View.GONE
                    button?.visibility = View.GONE
                } else {
                    linearLayout?.visibility = View.VISIBLE
                    button?.visibility = View.VISIBLE
                }
            } else {
                if (!value.contains(skipLogicVal)) {
                    textView?.visibility = View.GONE
                    editText?.visibility = View.GONE
                } else {
                    textView?.visibility = View.VISIBLE
                    editText?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun defaultViewHide(
        type: String,
        textView: TextView? = null,
        editText: EditText? = null,
        radioGroup: RadioGroup? = null,
        imageView: ImageView? = null,
        button: Button? = null,
        linearLayout: LinearLayout? = null,
        isDisplay: Boolean = true
    ) {
        if (isDisplay) {
            if (type == "RADIO") {
                textView?.visibility = View.GONE
                radioGroup?.visibility = View.GONE

            } else if (type == "IMAGE") {
                textView?.visibility = View.GONE
                imageView?.visibility = View.GONE

            } else if (type == "LOCATION") {

                textView?.visibility = View.GONE
                editText?.visibility = View.GONE
                button?.visibility = View.GONE

            } else if (type == "ADDABLE") {
                linearLayout?.visibility = View.GONE
                button?.visibility = View.GONE

            } else {
                textView?.visibility = View.GONE
                editText?.visibility = View.GONE
            }
        }else{
            if (type == "RADIO") {
                textView?.visibility = View.GONE
                radioGroup?.visibility = View.GONE

            } else if (type == "IMAGE") {
                textView?.visibility = View.GONE
                imageView?.visibility = View.GONE

            } else if (type == "LOCATION") {

                textView?.visibility = View.GONE
                editText?.visibility = View.GONE
                button?.visibility = View.GONE

            } else if (type == "ADDABLE") {
                linearLayout?.visibility = View.GONE
                button?.visibility = View.GONE

            } else {
                textView?.visibility = View.GONE
                editText?.visibility = View.GONE
            }
        }
    }

    @SuppressLint("Range")
    fun demoDb(skipLogicQ: String?, skipLogicVal: String?, relation: String?) {
        // Log.d("TAG", " = 444")
        val objDataArray = jsonObject.getJSONArray("data")
        for (m in 0 until objDataArray.length()) {
            val sectionObject = objDataArray.getJSONObject(m)
            try {
                val childrenArray = sectionObject?.getJSONArray("children")
                if (childrenArray != null) {

                    for (j in 0 until childrenArray.length()) {

                        val childObject = childrenArray.getJSONObject(j)

                        if (childObject.getString("type").equals("SECTION")) {
                            // demoDb()
                        } else {

                            val id = childObject.getString("id")
                            if (id == skipLogicQ) {
                                val mainFlagList = arrayListOf<Boolean>()
                                val tbNM =
                                    jsonObject.getString("acronym") + "_" + sectionObject.getString(
                                        "acronym"
                                    )
                                /*val cursor = dbHelper.getSectionTable(tbNM)
                                if (cursor != null) {
                                    if (cursor.moveToFirst()) {
                                        do {
                                            val sId = cursor.getInt(cursor.getColumnIndex("sub_id"))
                                            val value =
                                                cursor.getString(cursor.getColumnIndex(skipLogicQ))
                                            if (sId == subId) {
                                                if (cursor.isLast) {
                                                    if (value != "" && value != "NULL") {
                                                        if (value == skipLogicVal) {
                                                            mainFlagList.add(true)
                                                        } else {
                                                            mainFlagList.add(false)
                                                        }
                                                    }
                                                }
                                            }
                                        } while (cursor.moveToNext())
                                    }
                                }*/

                                var title = sectionObject.getString("title")

                                if (relation.equals("or", true) && mainFlagList.contains(true)) {
                                    //visibilityGone
                                    Log.d("TAG", "or $title: visibilityGone ")
                                } else if (relation.equals(
                                        "or",
                                        true
                                    ) && mainFlagList.all { false }
                                ) {
                                    //visibility
                                    Log.d("TAG", "2 or $title: visibility")
                                } else if (relation.equals("and", true) && mainFlagList.contains(
                                        false
                                    )
                                ) {
                                    //visibility
                                    Log.d("TAG", "and $title: visibility ")
                                } else if (relation.equals(
                                        "and",
                                        true
                                    ) && mainFlagList.all { true }
                                ) {
                                    //visibilityGone
                                    Log.d("TAG", "$2 and $title: visibilityGone ")
                                } else {
                                    Log.d("TAG", "$title else ")
                                }
                            }

                        }
                    }

                }
            } catch (_: Exception) {
            }
        }

    }

    private fun bitmapToString(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun stringToBitmap(encodedString: String): Bitmap {
        val decodedByteArray = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
    }
}