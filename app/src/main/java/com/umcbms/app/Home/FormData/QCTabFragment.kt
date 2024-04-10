package com.umcbms.app.Home.FormData

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Build
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
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.umcbms.app.Home.FormData.viewModel.SectionViewModel
import com.umcbms.app.JSONModel.SkipLogicModel
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.R
import com.umcbms.app.api.ApiConstants
import com.umcbms.app.api.request.JSONRequest
import com.umcbms.app.calculateAge
import com.umcbms.app.changeDateTimeFormat
import com.umcbms.app.getPrefIntData
import com.umcbms.app.isJsonArray
import com.umcbms.app.isLocalFilePath
import com.umcbms.app.isValidNumber
import com.umcbms.app.pathToBitmap
import com.umcbms.app.setMinMaxValues
import com.umcbms.app.setPrefIntData
import com.umcbms.app.utils.EditTextDebounce
import com.umcbms.app.validateText
import com.google.android.gms.location.LocationServices
import com.umcbms.app.compressImage
import com.umcbms.app.createFolderInInternalStorage
import com.google.gson.Gson
import com.umcbms.app.deleteLastUploadedImages
import com.umcbms.app.getAllFilesInFolder
import com.umcbms.app.getPathFromUri
import com.umcbms.app.saveImage
import com.umcbms.app.saveImageToInternalStorage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val TAG = "QCTabFragment"

class QCTabFragment : Fragment(), OnTabChangedListener {

    var isOnCreatedView = false
    private var validationTrueFalse = false

    private var endSection: Int = 0
    private var subId: Int = 0
    private var formId: Int = 0
    private val folderName: String = "BMSV2Img"
    var folderfiles = ArrayList<File>()
    private lateinit var dbHelper: MasterDBHelper

    private lateinit var jsonObject: JSONObject
    private lateinit var dataArray: JSONArray
    private var sectionObject: JSONObject? = null

    private lateinit var linearLayoutSection: LinearLayout
    private lateinit var buttonNext: Button
    private lateinit var buttonPrev: Button
    private lateinit var bt: Button

    private lateinit var sectionViewModel: SectionViewModel

    private var someActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var lastClickedImageViewTag: String? = null
    private var lastClickedImageViewTagUri: String? = null


    private lateinit var imageMutableArr: MutableMap<String, String>
    private lateinit var relationImage: EditText

    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val REQUEST_IMAGE_CAPTURE = 2
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001

        var sectionPos: Int = 0
        var globeCount: Int = 0
        var regular: Typeface? = null
        var medium: Typeface? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bt = Button(requireContext())
        bt.tag = "button_submit"
        dbHelper = MasterDBHelper(requireContext())

        regular = ResourcesCompat.getFont(requireContext(), R.font.regular)
        medium = ResourcesCompat.getFont(requireContext(), R.font.medium)

        sectionViewModel = ViewModelProvider(this).get(SectionViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab, container, false)
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonNext = view.findViewById(R.id.buttonNext)
        buttonPrev = view.findViewById(R.id.buttonPrev)

        val validationMutableList = mutableMapOf<String, Boolean>()

        buttonNext.setOnClickListener {
            validationMutableList.clear()
            val focus = false

            validationCheck(validationMutableList, focus)

            Log.d(TAG, "onViewCreated: $validationMutableList")

            validationTrueFalse = validationMutableList.all { it.value }
            Log.d(TAG, "onViewCreated2: $validationTrueFalse")
            if (validationTrueFalse) {
                QCFormDataActivity.viewPager.currentItem =
                    QCFormDataActivity.viewPager.currentItem + 1
            }
        }

        buttonPrev.setOnClickListener {
            QCFormDataActivity.viewPager.currentItem =
                QCFormDataActivity.viewPager.currentItem - 1
        }

        isOnCreatedView = true
        var oldImageUri: Uri? = null
        var newImageUri: Uri? = null
        someActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    var imgUri2: Uri? = null
                    if (data != null) {
                        if (data.extras != null && data.extras!!.containsKey("data")) {

                            val imageBitmap = data.extras!!.get("data") as Bitmap
                            imgUri2 = imageBitmap.saveImage(requireContext())
                        }else if (data.data != null) {

                            imgUri2 = data.data
                        }
                        if (newImageUri  == null) {
                            newImageUri = imgUri2
                        } else {
                            // Otherwise, set the new image URI
                            oldImageUri = newImageUri
                            newImageUri = imgUri2
                        }
                        if (oldImageUri != null && !::imageMutableArr.isInitialized) {
                            deleteLastUploadedImages(requireContext(),folderName,1,folderfiles)
                        }

                        var base64Encoded = getPathFromUri(requireContext(), newImageUri!!)

                        if (base64Encoded == null){
                            saveImageToInternalStorage(requireContext(),folderName,
                                File(newImageUri!!.path!!).name,
                                compressImage(newImageUri!!.path!!)!!)!!
                        }else
                            saveImageToInternalStorage(requireContext(),folderName,
                                File(base64Encoded!!).name,
                                compressImage(base64Encoded)!!)!!
                        folderfiles = getAllFilesInFolder(createFolderInInternalStorage(requireContext(),folderName).path)

                        lastClickedImageViewTagUri?.let { tag ->
                            val jsonArray = JSONArray()

                            if (base64Encoded == null) jsonArray.put(newImageUri?.path!!) else
                                jsonArray.put(base64Encoded)
                            saveValue(tag, jsonArray)

                            if (::imageMutableArr.isInitialized) {
                                imageMutableArr.forEach {
                                    if (it.key == tag) {
                                        if (base64Encoded == null) imageMutableArr[tag] = newImageUri?.path!!
                                        else
                                            imageMutableArr[tag] = base64Encoded.toString()
                                        relationImage.setText(tag)
                                    }
                                }
                            }
                        }

                        lastClickedImageViewTag?.let { tag ->
                            val clickedImageView =
                                linearLayoutSection.findViewWithTag<ImageView>(tag)
                            clickedImageView?.setImageURI(imgUri2)
                        }
                    }
                }
            }

        tabChangeLoadData()

    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onTabChanged(tabIndex: Int) {
        tabChangeLoadData()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun tabChangeLoadData() {
        linearLayoutSection = view?.findViewById(R.id.linearLayoutSection)!!
        linearLayoutSection.removeAllViews()

        endSection = arguments?.getInt("endSection")!!
        sectionPos = arguments?.getInt("sectionPos")!!
        subId = arguments?.getInt("subId")!!
        formId = arguments?.getInt("formId")!!

        jsonObject = JSONObject(QCFormDataActivity.formJsonData)
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
                formAcronym = formAcronym,
                linearLayout = linearLayoutSection
            )
        }

        if (sectionPos == endSection - 1) {
            buttonNext.visibility = View.GONE
            if (sectionPos == 0){
                buttonPrev.visibility = View.GONE
            }else{
                buttonPrev.visibility = View.VISIBLE
            }
            buttonViewSubmit(bt)
        }else {
            buttonNext.visibility = View.VISIBLE
            if (sectionPos == 0) {
                buttonPrev.visibility = View.GONE
            } else {
                buttonPrev.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isOnCreatedView = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displaySectionData(
        sectionObject: JSONObject,
        indentation: String = "",
        bt: Button,
        tableName: String,
        formAcronym: String,
        linearLayout: LinearLayout
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
                    if (childObject.has("skipLogic")) {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    }
                    var value: String = ""
                    if (childObject.has("value")) {
                        value = childObject.getString("value")
                    }

                    var qcRaised: Boolean = false
                    if (childObject.has("qcRaised")) {
                        qcRaised = childObject.getBoolean("qcRaised")
                    }
                    var qcRemark = ""
                    if (childObject.has("qcRemark")) {
                        qcRemark = childObject.getString("qcRemark")
                    }
                    var qcResolved: Boolean = false
                    if (childObject.has("qcResolved")) {
                        qcResolved = childObject.getBoolean("qcResolved")
                    }
                    inputText(
                        placeholder,
                        label,
                        tableName,
                        childId,
                        fieldValidations,
                        skipLogic,
                        value,
                        qcRaised,
                        qcRemark,
                        qcResolved,
                        linearLayout = linearLayout
                    )
                }

                "NUMBERS" -> {
                    val label = childObject.getJSONObject("properties").getString("label")
                    val placeholder =
                        childObject.getJSONObject("properties").getString("placeholder")
                    val childId = childObject.getString("id")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    if (childObject.has("skipLogic")) {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    }
                    var value: String = ""
                    if (childObject.has("value")) {
                        value = childObject.getString("value").toDouble().toLong().toString()
                    }
                    var qcRaised: Boolean = false
                    if (childObject.has("qcRaised")) {
                        qcRaised = childObject.getBoolean("qcRaised")
                    }
                    var qcRemark = ""
                    if (childObject.has("qcRemark")) {
                        qcRemark = childObject.getString("qcRemark")
                    }
                    var qcResolved: Boolean = false
                    if (childObject.has("qcResolved")) {
                        qcResolved = childObject.getBoolean("qcResolved")
                    }
                    inputNumber(
                        placeholder,
                        label,
                        tableName,
                        childId,
                        fieldValidations,
                        skipLogic,
                        value,
                        qcRaised,
                        qcRemark,
                        qcResolved,
                        linearLayout = linearLayout
                    )
                }

                "TEXT_AREA" -> {
                    val label = childObject.getJSONObject("properties").getString("label")
                    val placeholder =
                        childObject.getJSONObject("properties").getString("placeholder")
                    val childId = childObject.getString("id")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    if (childObject.has("skipLogic")) {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    }
                    var value: String = ""
                    if (childObject.has("value")) {
                        value = childObject.getString("value")
                    }
                    var qcRaised: Boolean = false
                    if (childObject.has("qcRaised")) {
                        qcRaised = childObject.getBoolean("qcRaised")
                    }
                    var qcRemark = ""
                    if (childObject.has("qcRemark")) {
                        qcRemark = childObject.getString("qcRemark")
                    }
                    var qcResolved: Boolean = false
                    if (childObject.has("qcResolved")) {
                        qcResolved = childObject.getBoolean("qcResolved")
                    }
                    textArea(
                        placeholder,
                        label,
                        tableName,
                        childId,
                        fieldValidations,
                        skipLogic,
                        value,
                        qcRaised,
                        qcRemark,
                        qcResolved,
                        linearLayout = linearLayout
                    )
                }

                "DROPDOWN" -> {
                    val label = childObject.getJSONObject("properties").getString("label")
                    val placeholder =
                        childObject.getJSONObject("properties").getString("placeholder")

                    val childId = childObject.getString("id")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")

                    var optionsList: JSONArray? = null
                    var skipLogic: JSONArray? = null

                    if (childObject.has("skipLogic")) {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    }
                    var value: String = ""
                    if (childObject.has("value")) {
                        value = childObject.getString("value")
                    }
                    var qcRaised: Boolean = false
                    if (childObject.has("qcRaised")) {
                        qcRaised = childObject.getBoolean("qcRaised")
                    }
                    var qcRemark = ""
                    if (childObject.has("qcRemark")) {
                        qcRemark = childObject.getString("qcRemark")
                    }
                    var qcResolved: Boolean = false
                    if (childObject.has("qcResolved")) {
                        qcResolved = childObject.getBoolean("qcResolved")
                    }

                    val options = mutableMapOf<Int, String>()
                    options.clear()

                    var customOptionObject: JSONObject? = null
                    if (childObject.has("customOption")){
                        customOptionObject = childObject.getJSONObject("customOption")
                    }

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
                            skipLogic,
                            value,
                            qcRaised,
                            qcRemark,
                            qcResolved,
                            customOptionObject = customOptionObject,
                            linearLayout = linearLayout
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
                                value,
                                qcRaised,
                                qcRemark,
                                qcResolved,
                                relativeOptionsLogic,
                                customOptionObject = customOptionObject,
                                linearLayout = linearLayout
                            )
                        } else {
                            dropDown(
                                label,
                                options,
                                tableName,
                                childId,
                                fieldValidations,
                                placeholder,
                                skipLogic,
                                value,
                                qcRaised,
                                qcRemark,
                                qcResolved,
                                customOptionObject = customOptionObject,
                                linearLayout = linearLayout
                            )
                        }
                    }
                }

                "RADIO" -> {
                    val label = childObject.getJSONObject("properties").getString("label")
                    val childId = childObject.getString("id")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    val valueRequired = fieldValidations.getBoolean("valueRequired")

                    var qcRaised: Boolean = false
                    if (childObject.has("qcRaised")) {
                        qcRaised = childObject.getBoolean("qcRaised")
                    }
                    var qcRemark = ""
                    if (childObject.has("qcRemark")) {
                        qcRemark = childObject.getString("qcRemark")
                    }
                    var qcResolved: Boolean = false
                    if (childObject.has("qcResolved")) {
                        qcResolved = childObject.getBoolean("qcResolved")
                    }
// Create a LinearLayout
                    val linearLayoutLabel = LinearLayout(requireContext())
                    linearLayoutLabel.tag = "linerlayout_label_$childId"
                    var layout = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layout.setMargins(0, 25, 0, 0)
                    linearLayoutLabel.layoutParams = layout
                    linearLayoutLabel.orientation = LinearLayout.HORIZONTAL

                    val rg = RadioGroup(requireContext())
                    rg.tag = "radio_$childId"
                    val tvRadio = TextView(requireContext())
//                    var tvId=generateViewId("label_$childId")
                    tvRadio.tag = "label_$childId"
                    tvRadio.typeface = regular
//                    tvRadio.text = label
                    tvRadio.text = label + (if (qcRemark != "") " (" + qcRemark + ")" else "")
                    /*if (qcResolved) {
                        tvRadio.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.resolved_qc_color
                            )
                        );
                    }*/
                    tvRadio.textSize = 14f
                    val layoutParamsTV = LinearLayoutCompat.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                    )
//        layoutParamsTV.setMargins(0, 25, 0, 0)
                    tvRadio.layoutParams = layoutParamsTV
                    val checkBoxQc = CheckBox(requireContext())
                    val checkBoxLayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    checkBoxQc.isChecked = qcResolved
                    if (!qcResolved) {
                        if (qcRaised) {
                            saveQCResolve(childId, false)
                        }
                    }
                    checkBoxQc.setOnClickListener {
                        saveQCResolve(childId, checkBoxQc.isChecked)
                    }
                    linearLayoutLabel.addView(tvRadio)
                    linearLayoutLabel.addView(checkBoxQc, checkBoxLayoutParams)
                    linearLayout.addView(linearLayoutLabel)


                    if (valueRequired) {
                        val spannable = SpannableString("${tvRadio.text}*")
                        spannable.setSpan(
                            ForegroundColorSpan(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            ),
                            tvRadio.text.length,
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
                        var value: String = ""
                        if (childObject.has("value")) {
                            value = childObject.getString("value").toFloat().toInt().toString()
                        }
                        radioButton(optionsList, rg, tableName, childId, skipLogic, tvRadio, value, linearLayout = linearLayout)
                    }

                    var flag = initialTimeCheckSkipLogic(childId)
                    if (!qcRaised) {
                        linearLayoutLabel.visibility = View.GONE
                        rg.visibility = View.GONE
                    } else {

                        linearLayoutLabel.visibility = View.VISIBLE
                        rg.visibility = View.VISIBLE
                    }
                    linearLayout.addView(rg)
                }

                "UPLOAD_IMAGE", "CAPTURE_IMAGE" -> {
                    val childId = childObject.getString("id")
                    val label = childObject.getJSONObject("properties").getString("label")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    if (childObject.has("skipLogic")) {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    }
                    var value: String = ""
                    if (childObject.has("value")) {
                        value = childObject.getString("value")
                    }
                    var qcRaised: Boolean = false
                    if (childObject.has("qcRaised")) {
                        qcRaised = childObject.getBoolean("qcRaised")
                    }
                    var qcRemark = ""
                    if (childObject.has("qcRemark")) {
                        qcRemark = childObject.getString("qcRemark")
                    }
                    var qcResolved: Boolean = false
                    if (childObject.has("qcResolved")) {
                        qcResolved = childObject.getBoolean("qcResolved")
                    }
                    val linearLayoutRelativeOption = LinearLayout(requireContext())
                    linearLayoutRelativeOption.tag = "linearLayout_$childId"
                    val layoutParamsRO = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    linearLayoutRelativeOption.layoutParams = layoutParamsRO
                    linearLayoutRelativeOption.orientation = LinearLayout.VERTICAL
                    linearLayout.addView(linearLayoutRelativeOption)

                    var relativeOptionsLogic: String? = null
                    if (childObject.has("relativeOptionsLogic")) {
                        if (value.isNotEmpty()) {
                            val valueArray = JSONArray(value)

                            for (j in 0..valueArray.length() - 1) {
                                uploadImage(
                                    label,
                                    tableName,
                                    childId,
                                    fieldValidations,
                                    skipLogic,
                                    value,
                                    qcRaised,
                                    qcRemark,
                                    qcResolved,
                                    j,
                                    linearLayoutRelativeOption,
                                    linearLayout = linearLayout
                                )
                            }
                        }

                        relativeOptionsLogic = childObject.getString("relativeOptionsLogic")
                        val relativeView =
                            linearLayoutSection.findViewWithTag<EditText>("placeholder_$relativeOptionsLogic")
                        relativeView.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {

                                linearLayoutRelativeOption.removeAllViews()

                                relationImage = EditText(requireContext())
                                relationImage.tag = childId
                                relationImage.visibility = View.GONE
                                linearLayoutRelativeOption.addView(relationImage)

                                imageMutableArr = mutableMapOf()

                                relationImage.addTextChangedListener(object : TextWatcher {
                                    override fun afterTextChanged(s: Editable?) {
                                        saveValueRelativeOption(childId, imageMutableArr)
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

                                for (i in 0..s.toString().toInt() - 1) {
                                    uploadImage(
                                        label,
                                        tableName,
                                        childId,
                                        fieldValidations,
                                        skipLogic,
                                        value,
                                        qcRaised,
                                        qcRemark,
                                        qcResolved,
                                        i,
                                        linearLayoutRelativeOption,
                                        linearLayout = linearLayout
                                    )
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
                    } else {
                        uploadImage(
                            label,
                            tableName,
                            childId,
                            fieldValidations,
                            skipLogic,
                            value,
                            qcRaised,
                            qcRemark,
                            qcResolved,
                            linearLayoutRelativeOption = linearLayoutRelativeOption,
                            linearLayout = linearLayout
                        )
                    }
                }

                /* "CAPTURE_IMAGE" -> {
                     val childId = childObject.getString("id")
                     val label = childObject.getJSONObject("properties").getString("label")
                     val fieldValidations = childObject.getJSONObject("fieldValidations")
                     var skipLogic: JSONArray? = null
                     if (childObject.has("skipLogic")) {
                         skipLogic = childObject.getJSONArray("skipLogic")
                     }
                     var value: String = ""
                     if (childObject.has("value")) {
                         value = childObject.getString("value")
                     }
                     var qcRaised: Boolean = false
                     if (childObject.has("qcRaised")) {
                         qcRaised =childObject.getBoolean("qcRaised")
                     }
                     var qcRemark = ""
                     if (childObject.has("qcRemark")) {
                         qcRemark = childObject.getString("qcRemark")
                     }
                     var qcResolved: Boolean = false
                     if (childObject.has("qcResolved")) {
                         qcResolved = childObject.getBoolean("qcResolved")
                     }
                     captureImage(
                         label, tableName, childId, fieldValidations, skipLogic, value,
                         qcRaised,
                         qcRemark,
                         qcResolved
                     )
                 }*/

                "DATE" -> {
                    val childId = childObject.getString("id")
                    val label = childObject.getJSONObject("properties").getString("label")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    if (childObject.has("skipLogic")) {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    }
                    var value: String = ""
                    if (childObject.has("value")) {
                        value = childObject.getString("value")
                    }
                    var qcRaised: Boolean = false
                    if (childObject.has("qcRaised")) {
                        qcRaised = childObject.getBoolean("qcRaised")
                    }
                    var qcRemark = ""
                    if (childObject.has("qcRemark")) {
                        qcRemark = childObject.getString("qcRemark")
                    }
                    var qcResolved: Boolean = false
                    if (childObject.has("qcResolved")) {
                        qcResolved = childObject.getBoolean("qcResolved")
                    }
                    dateSelect(
                        label, tableName, childId, fieldValidations, skipLogic, value,
                        qcRaised,
                        qcRemark,
                        qcResolved,
                        linearLayout = linearLayout
                    )
                }

                "LOCATION" -> {
                    val childId = childObject.getString("id")
                    val label = childObject.getJSONObject("properties").getString("label")
                    val placeholder =
                        childObject.getJSONObject("properties").getString("placeholder")
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    if (childObject.has("skipLogic")) {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    }
                    var value: String = ""
                    if (childObject.has("value")) {
                        value = childObject.getString("value")
                    }
                    var qcRaised: Boolean = false
                    if (childObject.has("qcRaised")) {
                        qcRaised = childObject.getBoolean("qcRaised")
                    }
                    var qcRemark = ""
                    if (childObject.has("qcRemark")) {
                        qcRemark = childObject.getString("qcRemark")
                    }
                    var qcResolved: Boolean = false
                    if (childObject.has("qcResolved")) {
                        qcResolved = childObject.getBoolean("qcResolved")
                    }
                    getCurrentLocation(
                        label,
                        placeholder,
                        tableName,
                        childId,
                        fieldValidations,
                        skipLogic,
                        value,
                        qcRaised,
                        qcRemark,
                        qcResolved,
                        linearLayout = linearLayout
                    )
                }

                "ADDABLE" -> {
                    var addableDataArr = mutableMapOf<Int, MutableMap<Int, Any>>()
//                    addableDataArr.clear()

                    childObject.getJSONObject("fieldValidations")
                    var skipLogic: JSONArray? = null
                    if (childObject.has("skipLogic")) {
                        skipLogic = childObject.getJSONArray("skipLogic")
                    }
                    val childId = childObject.getString("id")
                    val linearLayoutAddable = LinearLayout(requireContext())
                    linearLayoutAddable.tag = "linearlayout_addable_$childId"
                    val layoutParamsLl = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    linearLayoutAddable.layoutParams = layoutParamsLl
                    linearLayoutAddable.orientation = LinearLayout.VERTICAL
                    linearLayout.addView(linearLayoutAddable, layoutParamsLl)

                    var qcRaised: Boolean = false
                    if (childObject.has("qcRaised")) {
                        qcRaised = childObject.getBoolean("qcRaised")
                    }
                    var qcRemark = ""
                    if (childObject.has("qcRemark")) {
                        qcRemark = childObject.getString("qcRemark")
                    }
                    var qcResolved: Boolean = false
                    if (childObject.has("qcResolved")) {
                        qcResolved = childObject.getBoolean("qcResolved")
                    }
                    var relativeOptionsLogic: String? = null
                    if (childObject.has("relativeOptionsLogic")) {

                        var valueJSONArrayRelative: JSONArray? = null
                        if (childObject.has("value") && !childObject.get("value").toString()
                                .isNullOrBlank()
                        ) {
                            valueJSONArrayRelative = childObject.getJSONArray("value")
                            for (i in 0 until valueJSONArrayRelative.length()) {
                                val value = valueJSONArrayRelative.getJSONObject(i)
                                addableRelative(
                                    childObject,
                                    linearLayoutAddable,
                                    i,
                                    addableDataArr,
                                    value = value,
                                    valueJSONArrayRelative.length()
                                )
                            }
                        }

                        relativeOptionsLogic = childObject.getString("relativeOptionsLogic")
                        val relativeView =
                            linearLayoutSection.findViewWithTag<EditText>("placeholder_$relativeOptionsLogic")
                        relativeView.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                                linearLayoutAddable.removeAllViews()
                                //linearLayoutRelativeOption.removeAllViews()

                                for (i in 0..s.toString().toInt() - 1) {
                                    addableRelative(
                                        childObject,
                                        linearLayoutAddable,
                                        i,
                                        addableDataArr,
                                        value = null,
                                        s.toString().toInt()
                                    )
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
                    } else {
                        var count = 0
                        var valueJSONArray: JSONArray? = null
                        if (childObject.has("value") && !childObject.get("value").toString()
                                .isNullOrBlank()
                        ) {
                            valueJSONArray = childObject.getJSONArray("value")
                            for (i in 0 until valueJSONArray.length()) {
                                val value = valueJSONArray.getJSONObject(i)
                                count = i
                                addable(
                                    childObject,
                                    bt,
                                    skipLogic,
                                    linearLayoutAddable,
                                    count,
                                    value,
                                    addableDataArr = addableDataArr,
                                    qcRaised,
                                    qcRemark,
                                    qcResolved
                                )
                            }
                        } else {

                            addable(
                                childObject,
                                bt,
                                skipLogic,
                                linearLayoutAddable,
                                count,
                                addableDataArr = addableDataArr,
                                qcRaised = qcRaised,
                                qcRemark = qcRemark,
                                qcResolved = qcResolved
                            )
                        }
                    }

                }

                "SECTION" -> {

                    val linearLayoutTitle = LinearLayout(requireContext())
                    linearLayoutTitle.orientation = LinearLayout.VERTICAL
                    val layoutParamsTitle = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParamsTitle.setMargins(0, 50, 0, 0)
                    linearLayoutTitle.setPadding(0, 0, 0, 20)
                    linearLayoutTitle.background = ContextCompat.getDrawable(requireContext(), R.drawable.layout_background)
                    linearLayoutTitle.layoutParams = layoutParamsTitle
                    linearLayout.addView(linearLayoutTitle)


                    val sectionTitle = childObject.getString("title")
                    val childSecAcronym = childObject.getString("acronym")
                    val secTv = TextView(requireContext())
                    secTv.text = sectionTitle
                    secTv.typeface = TabFragment.medium
                    secTv.textSize = 15f
                    secTv.setTextColor(R.color.sec_text_color)
                    //secTv.setTypeface(null, Typeface.BOLD)
                    secTv.setPadding(50, 30, 15, 30)

                    secTv.background = ContextCompat.getDrawable(requireContext(), R.drawable.title_background)
                    val layoutParamsTV = LinearLayout.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                    )
                    secTv.layoutParams = layoutParamsTV
                    linearLayoutTitle.addView(secTv)


                    val linearLayoutSec = LinearLayout(requireContext())
                    linearLayoutSec.orientation = LinearLayout.VERTICAL
                    linearLayoutSec.setPadding(25, 0, 25, 0)
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0, 20, 0, 0)
//                    linearLayoutSec.background = ContextCompat.getDrawable(requireContext(), R.drawable.layout_background)
                    linearLayoutSec.layoutParams = layoutParams
                    linearLayoutTitle.addView(linearLayoutSec)

                    val tabName = formAcronym + "_" + childSecAcronym

                    displaySectionData(childObject, "$indentation   ", bt, tabName, formAcronym, linearLayoutSec)

                }
            }
        }
    }

    private fun validationCheck(
        validationMutableList: MutableMap<String, Boolean>,
        focus: Boolean
    ) {
        if (sectionObject != null) {
            sectionValidationData(sectionObject!!, validationMutableList, focus)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun addableRelative(
        childObject: JSONObject,
        linearLayoutAddable: LinearLayout,
        relativeOption: Int,
        addableDataArr: MutableMap<Int, MutableMap<Int, Any>>,
        value: JSONObject? = null,
        selectOption: Int,
    ) {

        val dynamicObject = mutableMapOf<Int, Any>()
        /* addableDataArr[relativeOption] = dynamicObject*/

        val label = childObject.getJSONObject("properties").getString("label")
        val id = childObject.getString("id")

        val tv = TextView(requireContext())
        tv.text = "${relativeOption + 1} $label"
        /*if (relativeOption + 1 < 10) {
            tv.text = "0${relativeOption + 1} $label"
        } else {
            tv.text = "${relativeOption + 1} $label"
        }*/
        tv.textSize = 16f
        tv.typeface = TabFragment.medium
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 15, 0, 5)
        tv.setPadding(15, 15, 15, 15)
        tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.title_background)
        tv.layoutParams = params
        linearLayoutAddable.addView(tv)

        if (addableDataArr != null && addableDataArr.size > selectOption) {
            val arrSize = addableDataArr.size
            for (i in arrSize downTo selectOption) {
                addableDataArr.remove(i)

            }
        }

        val addableFormat = childObject.getJSONArray("addableFormat")

        for (k in 0 until addableFormat.length()) {

            val addableItem = addableFormat.getJSONObject(k)
            val addableType = addableItem.getString("type")
            val qNumber = addableItem.getString("qNumber")
            val localId = addableItem.getString("localId") //+ "_"+relativeOption
            val addableLabel = addableItem.getString("label")
            val addablePlaceholder = addableItem.getString("placeholder")
            val valueRequired = addableItem.getBoolean("valueRequired")
            var customValidationArr: JSONArray? = null
            if (addableItem.has("customValidation")) {
                customValidationArr = addableItem.getJSONArray("customValidation")
            }
            var defaultVisibility: Boolean = true
            if (addableItem.has("defaultVisibility")) {
                defaultVisibility = addableItem.getBoolean("defaultVisibility")
            }
            var storeData: String? = null
            if (value != null) {
                if (value.has(k.toString())) {
                    storeData = value.getString(k.toString())
                }
            } else {
                for ((pos, map) in addableDataArr.entries) {
                    if (pos == relativeOption) {
                        dynamicObject.putAll(map)
                        if (map[k] != null) {
                            storeData = map[k].toString()
                            dynamicObject.put(k, storeData)
                        }
                    }
                }
            }


            when (addableType) {
                "TEXT" -> {
                    inputTextAddable(
                        addablePlaceholder,
                        addableLabel,
                        linearLayoutAddable,
                        valueRequired,
                        dynamicObject,
                        k,
                        id,
                        storeData,
                        addableDataArr
                    )
                }

                "NUMBERS" -> {
                    inputNumberAddable(
                        addablePlaceholder,
                        addableLabel,
                        customValidationArr,
                        linearLayoutAddable,
                        valueRequired,
                        dynamicObject,
                        k,
                        id,
                        storeData,
                        addableDataArr
                    )
                }

                "DROPDOWN" -> {
                    val multiSelect = addableItem.getBoolean("multiSelect")
                    addableDropDown(
                        addableLabel,
                        addableItem,
                        linearLayoutAddable,
                        valueRequired,
                        dynamicObject,
                        k,
                        addablePlaceholder,
                        multiSelect,
                        id,
                        storeData,
                        addableDataArr
                    )
                }
            }


        }
    }


    fun sectionValidationData(
        sectionObject: JSONObject,
        validationMutableList: MutableMap<String, Boolean>,
        focus: Boolean
    ) {

        var focusSec = focus

        // Not work focus => DROPDOWN, RADIO, UPLOAD_IMAGE, CAPTURE_IMAGE, DATE, LOCATION, ADDABLE_DROPDOWN

        val childrenArray = sectionObject.getJSONArray("children")

        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            when (childObject.getString("type")) {
                "TEXT" -> {
                    val childId = childObject.getString("id")
                    validationMutableList[childId] = false

                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    val valueRequired = fieldValidations.getBoolean("valueRequired")

                    val flag = initialTimeCheckSkipLogic(childId)
                    val view =
                        linearLayoutSection.findViewWithTag<EditText>("placeholder_$childId")

                    if (flag) {
                        validationMutableList[childId] = true
                    } else {
                        if (valueRequired) {
                            if (view != null) {
                                if (view.text.toString() != "") {
                                    if (fieldValidations.has("minChar")) {
                                        val minChar = fieldValidations.getString("minChar")
                                        val maxChar = fieldValidations.getString("maxChar")
                                        if (validateText(
                                                view.text.toString(),
                                                minChar.toInt(),
                                                maxChar.toInt()
                                            )
                                        ) {
                                            validationMutableList[childId] = true
                                        } else {
                                            validationMutableList[childId] = false
                                            view.error =
                                                "Input must be between $minChar and $maxChar characters."
                                            if (!focusSec) {
                                                view.requestFocus()
                                                focusSec = true
                                            }
                                        }
                                    } else {
                                        validationMutableList[childId] = true
                                    }
                                } else {
                                    validationMutableList[childId] = false
                                    view.error = "Please enter data."
                                    if (!focusSec) {
                                        view.requestFocus()
                                        focusSec = true
                                    }
                                }
                            }
                        } else {
                            if (view != null) {
                                if (view.text.toString() != "") {
                                    if (fieldValidations.has("minChar")) {
                                        val minChar = fieldValidations.getString("minChar")
                                        val maxChar = fieldValidations.getString("maxChar")
                                        if (validateText(
                                                view.text.toString(),
                                                minChar.toInt(),
                                                maxChar.toInt()
                                            )
                                        ) {
                                            validationMutableList[childId] = true
                                        } else {
                                            validationMutableList[childId] = false
                                            view.error =
                                                "Input must be between $minChar and $maxChar characters."
                                            if (!focusSec) {
                                                view.requestFocus()
                                                focusSec = true
                                            }
                                        }
                                    } else {
                                        validationMutableList[childId] = true
                                    }
                                } else {
                                    validationMutableList[childId] = true
                                }
                            }
                        }

                    }

                }

                "NUMBERS" -> {
                    val childId = childObject.getString("id")
                    validationMutableList[childId] = false
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    val valueRequired = fieldValidations.getBoolean("valueRequired")

                    val flag = initialTimeCheckSkipLogic(childId)
                    val view =
                        linearLayoutSection.findViewWithTag<EditText>("placeholder_$childId")

                    if (flag) {
                        validationMutableList[childId] = true
                    } else {
                        if (valueRequired) {
                            if (view != null) {
                                if (view.text.toString() != "") {
                                    if (fieldValidations.has("minLimit")) {
                                        val minLimit =
                                            fieldValidations.getString("minLimit").toLong()
                                        val maxLimit =
                                            fieldValidations.getString("maxLimit").toLong()

                                        if (isValidNumber(
                                                view.text.toString(),
                                                minLimit,
                                                maxLimit
                                            )
                                        ) {
                                            validationMutableList[childId] = true
                                        } else {
                                            validationMutableList[childId] = false
                                            view.error =
                                                "Enter a valid number between $minLimit and $maxLimit"
                                            if (!focusSec) {
                                                view.requestFocus()
                                                focusSec = true
                                            }
                                        }
                                    } else {
                                        validationMutableList[childId] = true
                                    }

                                } else {
                                    validationMutableList[childId] = false
                                    view.error = "Please enter data."
                                    if (!focusSec) {
                                        view.requestFocus()
                                        focusSec = true
                                    }
                                }
                            }
                        } else {
                            if (view != null) {
                                if (view.text.toString() != "") {
                                    if (fieldValidations.has("minLimit")) {
                                        val minLimit =
                                            fieldValidations.getString("minLimit").toLong()
                                        val maxLimit =
                                            fieldValidations.getString("maxLimit").toLong()

                                        if (isValidNumber(
                                                view.text.toString(),
                                                minLimit,
                                                maxLimit
                                            )
                                        ) {
                                            validationMutableList[childId] = true
                                        } else {
                                            validationMutableList[childId] = false
                                            view.error =
                                                "Enter a valid number between $minLimit and $maxLimit"
                                            if (!focusSec) {
                                                view.requestFocus()
                                                focusSec = true
                                            }
                                        }
                                    } else {
                                        validationMutableList[childId] = true
                                    }
                                } else {
                                    validationMutableList[childId] = true
                                }
                            }
                        }

                    }

                }

                "TEXT_AREA" -> {
                    val childId = childObject.getString("id")
                    validationMutableList[childId] = false

                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    val valueRequired = fieldValidations.getBoolean("valueRequired")

                    val flag = initialTimeCheckSkipLogic(childId)
                    val view =
                        linearLayoutSection.findViewWithTag<EditText>("placeholder_$childId")

                    if (flag) {
                        validationMutableList[childId] = true
                    } else {
                        if (valueRequired) {
                            if (view != null) {
                                if (view.text.toString() != "") {
                                    if (fieldValidations.has("minChar")) {
                                        val minChar = fieldValidations.getString("minChar")
                                        val maxChar = fieldValidations.getString("maxChar")
                                        if (validateText(
                                                view.text.toString(),
                                                minChar.toInt(),
                                                maxChar.toInt()
                                            )
                                        ) {
                                            validationMutableList[childId] = true
                                        } else {
                                            validationMutableList[childId] = false
                                            view.error =
                                                "Input must be between $minChar and $maxChar characters."
                                            if (!focusSec) {
                                                view.requestFocus()
                                                focusSec = true
                                            }
                                        }
                                    } else {
                                        validationMutableList[childId] = true
                                    }
                                } else {
                                    validationMutableList[childId] = false
                                    view.error = "Please enter data."
                                    if (!focusSec) {
                                        view.requestFocus()
                                        focusSec = true
                                    }
                                }
                            }
                        } else {
                            if (view != null) {
                                if (view.text.toString() != "") {
                                    if (fieldValidations.has("minChar")) {
                                        val minChar = fieldValidations.getString("minChar")
                                        val maxChar = fieldValidations.getString("maxChar")
                                        if (validateText(
                                                view.text.toString(),
                                                minChar.toInt(),
                                                maxChar.toInt()
                                            )
                                        ) {
                                            validationMutableList[childId] = true
                                        } else {
                                            validationMutableList[childId] = false
                                            view.error =
                                                "Input must be between $minChar and $maxChar characters."
                                            if (!focusSec) {
                                                view.requestFocus()
                                                focusSec = true
                                            }
                                        }
                                    } else {
                                        validationMutableList[childId] = true

                                    }
                                } else {
                                    validationMutableList[childId] = true
                                }
                            }
                        }

                    }
                }

                "DROPDOWN" -> {
                    val childId = childObject.getString("id")
                    validationMutableList[childId] = false
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    val valueRequired = fieldValidations.getBoolean("valueRequired")

                    val flag = initialTimeCheckSkipLogic(childId)
                    val view =
                        linearLayoutSection.findViewWithTag<EditText>("placeholder_$childId")
                    val errorView =
                        linearLayoutSection.findViewWithTag<TextView>("error_$childId")

                    if (flag) {
                        if (errorView != null) {
                            errorView.visibility = View.GONE
                        }
                        validationMutableList[childId] = true
                    } else {
                        if (valueRequired) {
                            if (view != null) {
                                if (view.text.toString() != "") {
                                    if (errorView != null) {
                                        errorView.visibility = View.GONE
                                    }
                                    validationMutableList[childId] = true
                                } else {
                                    view.error = "Please enter data."

                                    if (errorView != null) {
                                        errorView.visibility = View.VISIBLE
                                    }
                                    /*if (!focusSec){
                                    errorView.requestFocus()
                                    focusSec = true
                                }*/
                                }
                            }

                        } else {
                            if (errorView != null) {
                                errorView.visibility = View.GONE
                            }
                            validationMutableList[childId] = true
                        }

                    }


                }

                "RADIO" -> {

                    val childId = childObject.getString("id")
                    validationMutableList[childId] = false
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    val valueRequired = fieldValidations.getBoolean("valueRequired")

                    val flag = initialTimeCheckSkipLogic(childId)
                    val view =
                        linearLayoutSection.findViewWithTag<RadioButton>("radioButton_$childId")
                    val errorView =
                        linearLayoutSection.findViewWithTag<TextView>("error_$childId")

                    if (view != null) {
                        view.setOnCheckedChangeListener { buttonView, isChecked ->
                            validationMutableList[childId] = true
                            if (errorView != null) {
                                errorView.visibility = View.GONE
                            }
                        }
                    }

                    var data = ""
                    getValue(childId) {
                        data = it
                    }

                    if (flag) {
                        if (errorView != null) {
                            errorView.visibility = View.GONE
                        }
                        validationMutableList[childId] = true
                    } else {
                        if (valueRequired) {
                            if (data != "") {
                                if (errorView != null) {
                                    errorView.visibility = View.GONE
                                }
                                validationMutableList[childId] = true
                            } else {
                                if (errorView != null) {
                                    errorView.visibility = View.VISIBLE
                                }
                            }
                        } else {
                            if (errorView != null) {
                                errorView.visibility = View.GONE
                            }
                            validationMutableList[childId] = true
                        }

                    }
                }

                "UPLOAD_IMAGE", "CAPTURE_IMAGE" -> {
                    val childId = childObject.getString("id")
                    validationMutableList[childId] = false
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    val valueRequired = fieldValidations.getBoolean("valueRequired")
                    Log.e("imagesvalid", fieldValidations.toString())
                    var relativeOptionsLogic: String? = null
                    if (childObject.has("relativeOptionsLogic")) {
                        validationMutableList[childId] = false
                        relativeOptionsLogic = childObject.getString("relativeOptionsLogic")
                        val relativeView =
                            linearLayoutSection.findViewWithTag<EditText>("placeholder_$relativeOptionsLogic")

                        val flag = initialTimeCheckSkipLogic(childId)
                        validationMutableList[childId] = flag


                        if (relativeView != null) {
                            val s = relativeView.text
                            if (s.toString() != "") {
                                val multiImgVal = mutableMapOf<String, Boolean>()

                                for (i in 0..s.toString().toInt() - 1) {
                                    Log.e("imageslength", s.toString())

                                    val flag = initialTimeCheckSkipLogic(childId)
                                    fieldValidations
                                    val errorView =
                                        linearLayoutSection.findViewWithTag<TextView>("error_$childId-$i")

                                    var data = ""
                                    getValue(childId) {
                                        data = it
                                    }
                                    Log.e("datalength", data.toString())

                                    if (flag) {
                                        if (errorView != null) {
                                            errorView.visibility = View.GONE
                                        }
                                        validationMutableList[childId] = true
                                    } else {
                                        if (valueRequired) {
                                            if (data != "") {
                                                val jsonArray = JSONArray(data)
                                                for (j in 0 until jsonArray.length()) {
                                                    if (i == j) {
                                                        if (jsonArray.get(j).toString() != "") {
                                                            multiImgVal["error_$childId-$i"] = true
                                                            if (errorView != null) {
                                                                errorView.visibility = View.GONE
                                                            }
                                                        } else {
                                                            multiImgVal["error_$childId-$i"] = false
                                                            if (errorView != null) {
                                                                errorView.visibility = View.VISIBLE
                                                            }
                                                        }
                                                    }
                                                }
                                                /* errorView.visibility = View.GONE
                                             validationMutableList[childId] = true*/
                                            } else {
                                                multiImgVal["error_$childId-$i"] = false
                                                validationMutableList[childId] = false
                                                if (errorView != null) {
                                                    errorView.visibility = View.VISIBLE
                                                }
                                            }
                                        } else {
                                            if (errorView != null) {
                                                errorView.visibility = View.GONE
                                            }
                                            validationMutableList[childId] = true
                                        }

                                    }
                                    //img.tag = "image_$childId-$relativeOption"
                                }

                                val multiImgTrueFalse = multiImgVal.all { it.value }

                                if (multiImgTrueFalse) {
                                    validationMutableList[childId] = true
                                } else {
                                    validationMutableList[childId] = false
                                }
                            }
                        }
                    } else {
                        val flag = initialTimeCheckSkipLogic(childId)
                        val errorView =
                            linearLayoutSection.findViewWithTag<TextView>("error_$childId")

                        var data = ""
                        getValue(childId) {
                            data = it
                        }

                        if (flag) {
                            if (errorView != null) {
                                errorView.visibility = View.GONE
                            }
                            validationMutableList[childId] = true
                        } else {
                            if (valueRequired) {
                                if (data != "") {
                                    if (errorView != null) {
                                        errorView.visibility = View.GONE
                                    }
                                    validationMutableList[childId] = true
                                } else {
                                    if (errorView != null) {
                                        errorView.visibility = View.VISIBLE
                                    }
                                    validationMutableList[childId] = false
                                }
                            } else {
                                if (errorView != null) {
                                    errorView.visibility = View.GONE
                                }
                                validationMutableList[childId] = true
                            }

                        }
                    }


                }

                "DATE" -> {
                    val childId = childObject.getString("id")
                    validationMutableList[childId] = false
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    val valueRequired = fieldValidations.getBoolean("valueRequired")

                    val flag = initialTimeCheckSkipLogic(childId)
                    val view =
                        linearLayoutSection.findViewWithTag<EditText>("placeholder_$childId")
                    val errorView =
                        linearLayoutSection.findViewWithTag<TextView>("error_$childId")

                    if (flag) {
                        if (errorView != null) {
                            errorView.visibility = View.GONE
                        }
                        validationMutableList[childId] = true
                    } else {
                        if (valueRequired) {
                            if (view != null) {
                                if (view.text.toString() != "") {
                                    if (errorView != null) {
                                        errorView.visibility = View.GONE
                                    }
                                    validationMutableList[childId] = true
                                } else {
                                    if (view != null) {
                                        view.error = "Please enter data."
                                    }
                                    if (errorView != null) {
                                        errorView.visibility = View.VISIBLE
                                    }
                                }
                            }
                        } else {
                            if (errorView != null) {
                                errorView.visibility = View.GONE
                            }
                            validationMutableList[childId] = true
                        }
                    }
                }

                "LOCATION" -> {
                    val childId = childObject.getString("id")
                    validationMutableList[childId] = false
                    val fieldValidations = childObject.getJSONObject("fieldValidations")
                    val valueRequired = fieldValidations.getBoolean("valueRequired")

                    val flag = initialTimeCheckSkipLogic(childId)
                    val view =
                        linearLayoutSection.findViewWithTag<EditText>("placeholder_$childId")
                    val errorView =
                        linearLayoutSection.findViewWithTag<TextView>("error_$childId")

                    if (flag) {
                        if (errorView != null) {
                            errorView.visibility = View.GONE
                        }
                        validationMutableList[childId] = true
                    } else {
                        if (valueRequired) {

                            /*if (view.text.toString() != "") {
                                if (fieldValidations.has("minChar")) {
                                    val minChar = fieldValidations.getString("minChar")
                                    val maxChar = fieldValidations.getString("maxChar")
                                    if (validateText(
                                            view.text.toString(),
                                            minChar.toInt(),
                                            maxChar.toInt()
                                        )
                                    ) {
                                        validationMutableList[childId] = true
                                    } else {
                                        view.error =
                                            "Input must be between $minChar and $maxChar characters."
                                    }
                                }
                            } else {
                                view.error = "Please enter data."
                            }*/
                            if (view != null) {
                                if (view.text.toString() != "") {
                                    if (errorView != null) {
                                        errorView.visibility = View.GONE
                                    }
                                    validationMutableList[childId] = true
                                } else {
                                    if (errorView != null) {
                                        errorView.visibility = View.VISIBLE
                                    }
                                }
                            }
                        } else {
                            if (errorView != null) {
                                errorView.visibility = View.GONE
                            }
                            validationMutableList[childId] = true
                        }
                    }

                }

                "ADDABLE" -> {
                    val childId = childObject.getString("id")
                    val addableFlag = initialTimeCheckSkipLogic(childId)
                    validationMutableList[childId] = addableFlag

                    val addableFormat = childObject.getJSONArray("addableFormat")

                    val addableCount = getPrefIntData(requireContext(), "count")
                    if (addableCount == 0) {
                        for (i in 1..1) {
                            for (k in 0 until addableFormat.length()) {

                                val addableItem = addableFormat.getJSONObject(k)

                                val localId = addableItem.getString("localId") + "_" + i
                                val addableType = addableItem.getString("type")
                                val valueRequired = addableItem.getBoolean("valueRequired")

                                //  validationMutableList[localId] = false
                                var relativeOptionsLogic: String? = null
                                if (childObject.has("relativeOptionsLogic")) {
                                    val localId = addableItem.getString("localId")
//                            validationMutableList[localId] = true
//                            validationMutableList[childId] = false
                                    relativeOptionsLogic =
                                        childObject.getString("relativeOptionsLogic")
                                    val relativeView =
                                        linearLayoutSection.findViewWithTag<EditText>("placeholder_$relativeOptionsLogic")

                                    /*val flag = initialTimeCheckSkipLogic(childId)
                                    validationMutableList[childId] = flag*/

                                    if (relativeView != null) {
                                        val s = relativeView.text
                                        if (s.toString() != "") {
                                            val multiAddableTextVal =
                                                mutableMapOf<String, Boolean>()

                                            if (s.toString().toInt() == 0) {
                                                validationMutableList[childId] = true
                                                multiAddableTextVal[localId] = true
                                            }

                                            if (addableFlag) {
                                                validationMutableList[childId] = true
                                                multiAddableTextVal[localId] = true
                                            } else {
                                                for (i in 0..s.toString().toInt() - 1) {
                                                    when (addableType) {
                                                        "TEXT" -> {
//                                            if (localId == "addable_name_dcg_sbb_one") {
                                                            multiAddableTextVal["$localId-$i"] =
                                                                false
//                                                if (!addableFlag) {
                                                            Log.d(TAG, "2: placeholder_$localId-$i")
                                                            val flag = initialTimeCheckVisibleLogic(
                                                                localId
                                                            )
                                                            val view =
                                                                linearLayoutSection.findViewWithTag<EditText>(
                                                                    "placeholder_$localId-$i"
                                                                )

                                                            if (flag) {
                                                                multiAddableTextVal["$localId-$i"] =
                                                                    true
                                                            } else {
                                                                if (valueRequired) {
                                                                    if (view != null) {
                                                                        if (view.text.toString() != "") {
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                true
                                                                        } else {
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                false
                                                                            view.error =
                                                                                "please enter data"
                                                                            if (!focusSec) {
                                                                                view.requestFocus()
                                                                                focusSec = true
                                                                            }
                                                                        }
                                                                    }
                                                                } else {
                                                                    multiAddableTextVal["$localId-$i"] =
                                                                        true
                                                                }
                                                            }
                                                            /* } else {
                                                            multiAddableTextVal["$localId-$i"] = true
                                                        }*/
//                                            }
                                                        }

                                                        //vf
                                                        "NUMBERS" -> {
//                                                if (!addableFlag) {
                                                            val flag = initialTimeCheckSkipLogic(
                                                                localId
                                                            )
                                                            val view =
                                                                linearLayoutSection.findViewWithTag<EditText>(
                                                                    "placeholder_$localId-$i"
                                                                )

                                                            if (flag) {
                                                                multiAddableTextVal["$localId-$i"] =
                                                                    true
                                                            } else {
                                                                if (valueRequired) {
                                                                    if (view != null) {
                                                                        if (view.text.toString() != "") {
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                true
                                                                        } else {
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                false
                                                                            view.error =
                                                                                "please enter data"
                                                                            if (!focusSec) {
                                                                                view.requestFocus()
                                                                                focusSec = true
                                                                            }
                                                                        }
                                                                    }
                                                                } else {
                                                                    multiAddableTextVal["$localId-$i"] =
                                                                        true
                                                                }
                                                            }
//                                                } else {
//                                                    multiAddableTextVal["$localId-$i"] = true
//                                                }
                                                        }

                                                        "DROPDOWN" -> {
//                                                if (!addableFlag) {
                                                            val flag = initialTimeCheckSkipLogic(
                                                                localId
                                                            )
                                                            val view =
                                                                linearLayoutSection.findViewWithTag<EditText>(
                                                                    "placeholder_$localId-$i"
                                                                )
                                                            val errorView =
                                                                linearLayoutSection.findViewWithTag<TextView>(
                                                                    "error_$localId-$i"
                                                                )

                                                            if (flag) {
                                                                if (errorView != null) {
                                                                    errorView.visibility = View.GONE
                                                                }
                                                                if (view != null) {
                                                                    view.error = null
                                                                }
                                                                multiAddableTextVal["$localId-$i"] =
                                                                    true
                                                            } else {
                                                                if (valueRequired) {
                                                                    if (view != null) {
                                                                        if (view.text.toString() != "") {
                                                                            view.error = null
                                                                            if (errorView != null) {
                                                                                errorView.visibility =
                                                                                    View.GONE
                                                                            }
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                true
                                                                        } else {
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                false
                                                                            view.error =
                                                                                "please enter data"
                                                                            if (errorView != null) {
                                                                                errorView.visibility =
                                                                                    View.VISIBLE
                                                                            }
                                                                        }
                                                                    }
                                                                } else {
                                                                    multiAddableTextVal["$localId-$i"] =
                                                                        true
                                                                }
                                                            }
                                                            /*  } else {
                                                            multiAddableTextVal["$localId-$i"] = true
                                                        }*/
                                                        }
                                                    }
                                                }
                                            }

                                            val addableTextTrueFalse =
                                                multiAddableTextVal.all { it.value }
                                            if (addableTextTrueFalse) {
                                                validationMutableList[localId] = true
                                                validationMutableList[childId] = true
                                            } else {
                                                validationMutableList[childId] = false
                                                validationMutableList[localId] = false
                                            }

                                        }
                                    }
                                } else {
                                    validationMutableList[childId] = true
                                    when (addableType) {
                                        "TEXT" -> {
                                            if (!addableFlag) {

                                                val flag = initialTimeCheckVisibleLogic(
                                                    localId
                                                )

                                                Log.d(TAG, "flag:$localId => $flag")
                                                /*val flagVisible = initialTimeCheckVisibleLogic(
                                                    localId
                                                )*/
                                                val view =
                                                    linearLayoutSection.findViewWithTag<EditText>("placeholder_$localId")

                                                if (!flag) {
                                                    validationMutableList[localId] = true
                                                } else {
                                                    if (valueRequired) {
                                                        if (view != null) {
                                                            if (view.text.toString() != "") {
                                                                validationMutableList[localId] =
                                                                    true
                                                            } else {
                                                                view.error = "please enter data"
                                                                if (!focusSec) {
                                                                    view.requestFocus()
                                                                    focusSec = true
                                                                }

                                                                validationMutableList[localId] =
                                                                    false
                                                            }
                                                        }
                                                    } else {
                                                        validationMutableList[localId] = true
                                                    }
                                                }


                                            } else {
                                                validationMutableList[localId] = true
                                            }
                                        }

                                        "NUMBERS" -> {
                                            if (!addableFlag) {
                                                val flag = initialTimeCheckSkipLogic(
                                                    localId
                                                )
                                                val view =
                                                    linearLayoutSection.findViewWithTag<EditText>("placeholder_$localId")

                                                if (flag) {
                                                    validationMutableList[localId] = true
                                                } else {
                                                    if (valueRequired) {
                                                        if (view != null) {
                                                            if (view.text.toString() != "") {
                                                                validationMutableList[localId] =
                                                                    true
                                                            } else {
                                                                view.error = "please enter data"
                                                                if (!focusSec) {
                                                                    view.requestFocus()
                                                                    focusSec = true
                                                                }
                                                                validationMutableList[localId] =
                                                                    false
                                                            }
                                                        }
                                                    } else {
                                                        validationMutableList[localId] = true
                                                    }
                                                }
                                            } else {
                                                validationMutableList[localId] = true
                                            }
                                        }

                                        "DROPDOWN" -> {
                                            if (!addableFlag) {
                                                val flag = initialTimeCheckSkipLogic(
                                                    localId
                                                )

                                                val view =
                                                    linearLayoutSection.findViewWithTag<EditText>("placeholder_$localId")

                                                val errorView =
                                                    linearLayoutSection.findViewWithTag<TextView>("error_$localId")

                                                if (flag) {
                                                    if (errorView != null) {
                                                        errorView.visibility = View.GONE
                                                    }
                                                    if (view != null) {
                                                        view.error = null
                                                    }
                                                    validationMutableList[localId] = true
                                                } else {
                                                    if (valueRequired) {
                                                        if (view != null) {
                                                            if (view.text.toString() != "") {
                                                                view.error = null
                                                                if (errorView != null) {
                                                                    errorView.visibility = View.GONE
                                                                }
                                                                validationMutableList[localId] =
                                                                    true
                                                            } else {
                                                                validationMutableList[localId] =
                                                                    false
                                                                view.error = "please enter data"
                                                                if (errorView != null) {
                                                                    errorView.visibility =
                                                                        View.VISIBLE
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        validationMutableList[localId] = true
                                                    }
                                                }
                                            } else {
                                                validationMutableList[localId] = true
                                            }
                                        }

                                        "UPLOAD_IMAGE" -> {
                                            val flag = initialTimeCheckSkipLogic(
                                                localId
                                            )
                                            /* if (!addableFlag) {
                                                 val flag = initialTimeCheckSkipLogic(
                                                     localId
                                                 )
                                                 val view =
                                                     linearLayoutSection.findViewWithTag<EditText>("placeholder_$localId")

                                                 if (flag) {
                                                     validationMutableList[localId] = true
                                                 } else {
                                                     if (valueRequired) {
                                                         if (view.text.toString() != "") {
                                                             validationMutableList[localId] = true
                                                         } else {
                                                             view.error = "please upload Image"
                                                             if (!focusSec) {
                                                                 view.requestFocus()
                                                                 focusSec = true
                                                             }
                                                             validationMutableList[localId] = false
                                                         }
                                                     } else {
                                                         validationMutableList[localId] = true
                                                     }
                                                 }
                                             } else {
                                                 validationMutableList[localId] = true
                                             }*/
                                        }
                                    }
                                }


                            }
                        }
                    } else {
                        for (i in 1..addableCount) {
                            for (k in 0 until addableFormat.length()) {

                                val addableItem = addableFormat.getJSONObject(k)

                                val localId = addableItem.getString("localId") + "_" + i
                                val addableType = addableItem.getString("type")
                                val valueRequired = addableItem.getBoolean("valueRequired")

                                //  validationMutableList[localId] = false
                                var relativeOptionsLogic: String? = null
                             /*   if (childObject.has("relativeOptionsLogic")) {
                                    val localId = addableItem.getString("localId")
//                            validationMutableList[localId] = true
//                            validationMutableList[childId] = false
                                    relativeOptionsLogic =
                                        childObject.getString("relativeOptionsLogic")
                                    val relativeView =
                                        linearLayoutSection.findViewWithTag<EditText>("placeholder_$relativeOptionsLogic")

                                    *//*val flag = initialTimeCheckSkipLogic(childId)
                                validationMutableList[childId] = flag*//*

                                    if (relativeView != null) {
                                        val s = relativeView.text
                                        if (s.toString() != "") {
                                            val multiAddableTextVal =
                                                mutableMapOf<String, Boolean>()

                                            if (s.toString().toInt() == 0) {
                                                validationMutableList[childId] = true
                                                multiAddableTextVal[localId] = true
                                            }

                                            if (addableFlag) {
                                                validationMutableList[childId] = true
                                                multiAddableTextVal[localId] = true
                                            } else {
                                                for (i in 0..s.toString().toInt() - 1) {
                                                    when (addableType) {
                                                        "TEXT" -> {
//                                            if (localId == "addable_name_dcg_sbb_one") {
                                                            multiAddableTextVal["$localId-$i"] =
                                                                false
//                                                if (!addableFlag) {
                                                            Log.d(TAG, "2: placeholder_$localId-$i")
                                                            val flag = initialTimeCheckVisibleLogic(
                                                                localId
                                                            )
                                                            val view =
                                                                linearLayoutSection.findViewWithTag<EditText>(
                                                                    "placeholder_$localId-$i"
                                                                )

                                                            if (flag) {
                                                                multiAddableTextVal["$localId-$i"] =
                                                                    true
                                                            } else {
                                                                if (valueRequired) {
                                                                    if (view != null) {
                                                                        if (view.text.toString() != "") {
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                true
                                                                        } else {
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                false
                                                                            view.error =
                                                                                "please enter data"
                                                                            if (!focusSec) {
                                                                                view.requestFocus()
                                                                                focusSec = true
                                                                            }
                                                                        }
                                                                    }
                                                                } else {
                                                                    multiAddableTextVal["$localId-$i"] =
                                                                        true
                                                                }
                                                            }
                                                            *//* } else {
                                                        multiAddableTextVal["$localId-$i"] = true
                                                    }*//*
//                                            }
                                                        }

                                                        //vf
                                                        "NUMBERS" -> {
//                                                if (!addableFlag) {
                                                            val flag = initialTimeCheckSkipLogic(
                                                                localId
                                                            )
                                                            val view =
                                                                linearLayoutSection.findViewWithTag<EditText>(
                                                                    "placeholder_$localId-$i"
                                                                )

                                                            if (flag) {
                                                                multiAddableTextVal["$localId-$i"] =
                                                                    true
                                                            } else {
                                                                if (valueRequired) {
                                                                    if (view != null) {
                                                                        if (view.text.toString() != "") {
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                true
                                                                        } else {
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                false
                                                                            view.error =
                                                                                "please enter data"
                                                                            if (!focusSec) {
                                                                                view.requestFocus()
                                                                                focusSec = true
                                                                            }
                                                                        }
                                                                    }
                                                                } else {
                                                                    multiAddableTextVal["$localId-$i"] =
                                                                        true
                                                                }
                                                            }
//                                                } else {
//                                                    multiAddableTextVal["$localId-$i"] = true
//                                                }
                                                        }

                                                        "DROPDOWN" -> {
//                                                if (!addableFlag) {
                                                            val flag = initialTimeCheckSkipLogic(
                                                                localId
                                                            )
                                                            val view =
                                                                linearLayoutSection.findViewWithTag<EditText>(
                                                                    "placeholder_$localId-$i"
                                                                )
                                                            val errorView =
                                                                linearLayoutSection.findViewWithTag<TextView>(
                                                                    "error_$localId-$i"
                                                                )

                                                            if (flag) {
                                                                if (errorView != null) {
                                                                    errorView.visibility = View.GONE
                                                                }
                                                                if (view != null) {
                                                                    view.error = null
                                                                }
                                                                multiAddableTextVal["$localId-$i"] =
                                                                    true
                                                            } else {
                                                                if (valueRequired) {
                                                                    if (view != null) {
                                                                        if (view.text.toString() != "") {
                                                                            view.error = null
                                                                            if (errorView != null) {
                                                                                errorView.visibility =
                                                                                    View.GONE
                                                                            }
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                true
                                                                        } else {
                                                                            multiAddableTextVal["$localId-$i"] =
                                                                                false
                                                                            view.error =
                                                                                "please enter data"
                                                                            if (errorView != null) {
                                                                                errorView.visibility =
                                                                                    View.VISIBLE
                                                                            }
                                                                        }
                                                                    }
                                                                } else {
                                                                    multiAddableTextVal["$localId-$i"] =
                                                                        true
                                                                }
                                                            }
                                                            *//*  } else {
                                                        multiAddableTextVal["$localId-$i"] = true
                                                    }*//*
                                                        }
                                                    }
                                                }
                                            }

                                            val addableTextTrueFalse =
                                                multiAddableTextVal.all { it.value }
                                            if (addableTextTrueFalse) {
                                                validationMutableList[localId] = true
                                                validationMutableList[childId] = true
                                            } else {
                                                validationMutableList[childId] = false
                                                validationMutableList[localId] = false
                                            }

                                        }
                                    }
                                } else*/
                                    // {
                                    validationMutableList[childId] = true
                                    when (addableType) {
                                        "TEXT" -> {
                                            if (!addableFlag) {

                                                val flag = initialTimeCheckVisibleLogic(
                                                    localId
                                                )

                                                Log.d(TAG, "flag:$localId => $flag")
                                                /*val flagVisible = initialTimeCheckVisibleLogic(
                                                localId
                                            )*/
                                                val view =
                                                    linearLayoutSection.findViewWithTag<EditText>("placeholder_$localId")

                                                if (!flag) {
                                                    validationMutableList[localId] = true
                                                } else {
                                                    if (valueRequired) {
                                                        if (view != null) {
                                                            if (view.text.toString() != "") {
                                                                validationMutableList[localId] =
                                                                    true
                                                            } else {
                                                                view.error = "please enter data"
                                                                if (!focusSec) {
                                                                    view.requestFocus()
                                                                    focusSec = true
                                                                }

                                                                validationMutableList[localId] =
                                                                    false
                                                            }
                                                        }
                                                    } else {
                                                        validationMutableList[localId] = true
                                                    }
                                                }


                                            } else {
                                                validationMutableList[localId] = true
                                            }
                                        }

                                        "NUMBERS" -> {
                                            if (!addableFlag) {
                                                val flag = initialTimeCheckSkipLogic(
                                                    localId
                                                )
                                                val view =
                                                    linearLayoutSection.findViewWithTag<EditText>("placeholder_$localId")

                                                if (flag) {
                                                    validationMutableList[localId] = true
                                                } else {
                                                    if (valueRequired) {
                                                        if (view != null) {
                                                            if (view.text.toString() != "") {
                                                                validationMutableList[localId] =
                                                                    true
                                                            } else {
                                                                view.error = "please enter data"
                                                                if (!focusSec) {
                                                                    view.requestFocus()
                                                                    focusSec = true
                                                                }
                                                                validationMutableList[localId] =
                                                                    false
                                                            }
                                                        }
                                                    } else {
                                                        validationMutableList[localId] = true
                                                    }
                                                }
                                            } else {
                                                validationMutableList[localId] = true
                                            }
                                        }

                                        "DROPDOWN" -> {
                                            if (!addableFlag) {
                                                val flag = initialTimeCheckSkipLogic(
                                                    localId
                                                )

                                                val view =
                                                    linearLayoutSection.findViewWithTag<EditText>("placeholder_$localId")

                                                val errorView =
                                                    linearLayoutSection.findViewWithTag<TextView>("error_$localId")

                                                if (flag) {
                                                    if (errorView != null) {
                                                        errorView.visibility = View.GONE
                                                    }
                                                    if (view != null) {
                                                        view.error = null
                                                    }
                                                    validationMutableList[localId] = true
                                                } else {
                                                    if (valueRequired) {
                                                        if (view != null) {
                                                            if (view.text.toString() != "") {
                                                                view.error = null
                                                                if (errorView != null) {
                                                                    errorView.visibility = View.GONE
                                                                }
                                                                validationMutableList[localId] =
                                                                    true
                                                            } else {
                                                                validationMutableList[localId] =
                                                                    false
                                                                view.error = "please enter data"
                                                                if (errorView != null) {
                                                                    errorView.visibility =
                                                                        View.VISIBLE
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        validationMutableList[localId] = true
                                                    }
                                                }
                                            } else {
                                                validationMutableList[localId] = true
                                            }
                                        }

                                        "UPLOAD_IMAGE" -> {
                                            val flag = initialTimeCheckSkipLogic(
                                                localId
                                            )
                                            /* if (!addableFlag) {
                                             val flag = initialTimeCheckSkipLogic(
                                                 localId
                                             )
                                             val view =
                                                 linearLayoutSection.findViewWithTag<EditText>("placeholder_$localId")

                                             if (flag) {
                                                 validationMutableList[localId] = true
                                             } else {
                                                 if (valueRequired) {
                                                     if (view.text.toString() != "") {
                                                         validationMutableList[localId] = true
                                                     } else {
                                                         view.error = "please upload Image"
                                                         if (!focusSec) {
                                                             view.requestFocus()
                                                             focusSec = true
                                                         }
                                                         validationMutableList[localId] = false
                                                     }
                                                 } else {
                                                     validationMutableList[localId] = true
                                                 }
                                             }
                                         } else {
                                             validationMutableList[localId] = true
                                         }*/
                                        }
                                    }
                              //  }


                            }
                        }
                    }

                }

                "SECTION" -> {
                    sectionValidationData(
                        childObject,
                        validationMutableList,
                        focusSec
                    )
                }
            }
        }
    }
    private fun inputText(
        placeholder: String,
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?,
        value: String,
        qcRaised: Boolean,
        qcRemark: String,
        qcResolved: Boolean,
        linearLayout: LinearLayout
    ) {
        // Create a LinearLayout
        val linearLayoutLabel = LinearLayout(requireContext())
        linearLayoutLabel.tag = "linerlayout_label_$childId"
        var layout = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.setMargins(0, 25, 0, 0)
        linearLayoutLabel.layoutParams = layout
        linearLayoutLabel.orientation = LinearLayout.HORIZONTAL

        val tv = TextView(requireContext())
        tv.tag = "label_$childId"
        tv.text = label + (if (qcRemark != null) "(" + qcRemark + ")" else "")
        /*if (qcResolved) {
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.resolved_qc_color));
        }*/
        tv.typeface = regular
        tv.textSize = 14f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
//        layoutParamsTV.setMargins(0, 25, 0, 0)
        tv.layoutParams = layoutParamsTV

        val checkBoxQc = CheckBox(requireContext())
        val checkBoxLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        checkBoxQc.isChecked = qcResolved
        if (!qcResolved) {
            if (qcRaised) {
                saveQCResolve(childId, false)
            }
        }
        checkBoxQc.setOnClickListener {
            saveQCResolve(childId, checkBoxQc.isChecked)
        }
        linearLayoutLabel.addView(tv)
        linearLayoutLabel.addView(checkBoxQc, checkBoxLayoutParams)
        linearLayout.addView(linearLayoutLabel)

        val etInputText = EditText(requireContext())
//        var etId=generateViewId("placeholder_$childId")
//        Log.d(TAG, "inputText: ${etId}")
        etInputText.tag = "placeholder_$childId"
        etInputText.hint = placeholder
        etInputText.typeface = regular
        etInputText.textSize = 14f
        etInputText.inputType = InputType.TYPE_CLASS_TEXT
        etInputText.setBackgroundResource(R.drawable.et_back)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        etInputText.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        linearLayout.addView(etInputText)

//        val textView = view?.findViewById<TextView>(tvId)
//        Log.d(TAG, "inputText_Value: "+textView?.text.toString())

//        viewDataSetFromDbText(tableName, childId, etInputText)

        etInputText.setText(value)


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
        val flag = initialTimeCheckSkipLogic(childId)
        if (!qcRaised) {
            linearLayoutLabel.visibility = View.GONE
            etInputText.visibility = View.GONE
        } else {
            linearLayoutLabel.visibility = View.VISIBLE
            etInputText.visibility = View.VISIBLE
        }

        EditTextDebounce(etInputText)
            .watch {
                saveValue(childId, it.toString())
                checkSkipLogic(childId, it.toString())
            }
        //inputTextET(etInputText, tableName, childId)
    }

    @SuppressLint("Range")
    private fun inputNumber(
        placeholder: String,
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?,
        value: String,
        qcRaised: Boolean,
        qcRemark: String,
        qcResolved: Boolean,
        linearLayout: LinearLayout
    ) {
        // Create a LinearLayout
        val linearLayoutLabel = LinearLayout(requireContext())
        linearLayoutLabel.tag = "linerlayout_label_$childId"
        var layout = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.setMargins(0, 25, 0, 0)
        linearLayoutLabel.layoutParams = layout
        linearLayoutLabel.orientation = LinearLayout.HORIZONTAL

        val tvInputNumber = TextView(requireContext())
//        var tvId=generateViewId("label_$childId")
        tvInputNumber.tag = "label_$childId"
//        tvInputNumber.text = label
        tvInputNumber.text = label + (if (qcRemark != "") " (" + qcRemark + ")" else "")
        /* if (qcResolved) {
             tvInputNumber.setTextColor(
                 ContextCompat.getColor(
                     requireContext(),
                     R.color.resolved_qc_color
                 )
             );
         }*/
        tvInputNumber.typeface = regular
        tvInputNumber.textSize = 14f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
//        layoutParamsTV.setMargins(0, 25, 0, 0)
        tvInputNumber.layoutParams = layoutParamsTV

        val checkBoxQc = CheckBox(requireContext())
        val checkBoxLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        checkBoxQc.isChecked = qcResolved
        if (!qcResolved) {
            if (qcRaised) {
                saveQCResolve(childId, false)
            }
        }
        checkBoxQc.setOnClickListener {
            saveQCResolve(childId, checkBoxQc.isChecked)
        }
        linearLayoutLabel.addView(tvInputNumber)
        linearLayoutLabel.addView(checkBoxQc, checkBoxLayoutParams)
        linearLayout.addView(linearLayoutLabel)

        val etInputNumber = EditText(requireContext())
//        var etId=generateViewId("placeholder_$childId")
        etInputNumber.tag = "placeholder_$childId"
//        etInputNumber.tag = childId
        etInputNumber.hint = placeholder
        etInputNumber.textSize = 12f
        etInputNumber.typeface = regular
        etInputNumber.inputType = InputType.TYPE_CLASS_NUMBER
        etInputNumber.setBackgroundResource(R.drawable.et_back)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        etInputNumber.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        linearLayout.addView(etInputNumber)

//        viewDataSetFromDbNumber(tableName, childId, etInputNumber)

        etInputNumber.setText(value)


        val valueRequired = fieldValidations.getBoolean("valueRequired")

        if (valueRequired) {
            val spannable = SpannableString("${tvInputNumber.text}*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                tvInputNumber.text.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tvInputNumber.text = spannable
            try {
                etInputNumber.setOnFocusChangeListener { e, hasFocus ->
                    try {
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
        var flag = initialTimeCheckSkipLogic(childId)
        if (!qcRaised) {
            linearLayoutLabel.visibility = View.GONE
            etInputNumber.visibility = View.GONE
        } else {
            linearLayoutLabel.visibility = View.VISIBLE
            etInputNumber.visibility = View.VISIBLE
        }
        EditTextDebounce(etInputNumber)
            .watch {
                saveValue(childId, it.toString())
                checkSkipLogic(childId, it.toString())
            }
//        inputNumberET(etInputNumber, tableName, childId, fieldValidations)
    }

    private fun textArea(
        placeholder: String,
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?,
        value: String,
        qcRaised: Boolean,
        qcRemark: String,
        qcResolved: Boolean,
        linearLayout: LinearLayout
    ) {
        // Create a LinearLayout
        val linearLayoutLabel = LinearLayout(requireContext())
        linearLayoutLabel.tag = "linerlayout_label_$childId"
        var layout = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.setMargins(0, 25, 0, 0)
        linearLayoutLabel.layoutParams = layout
        linearLayoutLabel.orientation = LinearLayout.HORIZONTAL

        val textAreaTV = TextView(requireContext())
//        var tvId=generateViewId("label_$childId")
        textAreaTV.tag = "label_$childId"
        textAreaTV.text = label + (if (qcRemark != "") " (" + qcRemark + ")" else "")
        /*if (qcResolved) {
            textAreaTV.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.resolved_qc_color
                )
            );
        }*/
        textAreaTV.typeface = regular
        textAreaTV.textSize = 14f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
//        layoutParamsTV.setMargins(0, 25, 0, 0)
        textAreaTV.layoutParams = layoutParamsTV

        val checkBoxQc = CheckBox(requireContext())
        val checkBoxLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        checkBoxQc.isChecked = qcResolved
        if (!qcResolved) {
            if (qcRaised) {
                saveQCResolve(childId, false)
            }
        }
        checkBoxQc.setOnClickListener {
            saveQCResolve(childId, checkBoxQc.isChecked)
        }
        linearLayoutLabel.addView(textAreaTV)
        linearLayoutLabel.addView(checkBoxQc, checkBoxLayoutParams)
        linearLayout.addView(linearLayoutLabel)

        val textAreaEt = EditText(requireContext())
//        var etId=generateViewId("placeholder_$childId")
        textAreaEt.tag = "placeholder_$childId"
        textAreaEt.hint = placeholder
        textAreaEt.typeface = regular
        textAreaEt.textSize = 12f
        textAreaEt.inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
        textAreaEt.setBackgroundResource(R.drawable.et_back)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        textAreaEt.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)

//        viewDataSetFromDbText(tableName, childId, textAreaEt)

        textAreaEt.setText(value)


        val valueRequired = fieldValidations.getBoolean("valueRequired")

        if (valueRequired) {
            val spannable = SpannableString("${textAreaTV.text}*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                textAreaTV.text.length,
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
        var flag = initialTimeCheckSkipLogic(childId)
        if (!qcRaised) {
            linearLayoutLabel.visibility = View.GONE
            textAreaEt.visibility = View.GONE
        } else {
            linearLayoutLabel.visibility = View.VISIBLE
            textAreaEt.visibility = View.VISIBLE
        }
        EditTextDebounce(textAreaEt)
            .watch {
                saveValue(childId, it.toString())
                checkSkipLogic(childId, it.toString())
            }
//        inputTextAreaET(textAreaEt, tableName, childId)

        linearLayout.addView(textAreaEt)
    }

    private fun radioButton(
        optionsList: JSONArray,
        rg: RadioGroup,
        tableName: String,
        childId: String,
        skipLogic: JSONArray?,
        tvRadio: TextView,
        value: String,
        linearLayout: LinearLayout
    ) {

//        val optionArr = optionsList
        for (i in 0 until optionsList.length()) {
            val radioButton = RadioButton(requireContext())
            radioButton.typeface = regular
            radioButton.text = optionsList.getJSONObject(i).getString("value")
//            radioButton.tag = "radio_$childId"
//            radioButton.tag = optionsList.getJSONObject(i).getString("value")
            radioButton.textSize = 12f
//            radioButton.id = View.generateViewId()
            rg.addView(radioButton)

            if (value != "" && value != "NULL") {
                if (optionsList.getJSONObject(i).getString("id").toString() == value) {
                    radioButton.isChecked = true
                }
            }
            radioButtonET(
                radioButton,
                tableName,
                childId,
                optionsList.getJSONObject(i).getString("id")
            )
        }

        val radioError = TextView(requireContext())
        radioError.tag = "error_$childId"
        radioError.visibility = View.GONE
        radioError.typeface = TabFragment.regular
        radioError.setTextColor(Color.RED)
        radioError.textSize = 10f
        val layoutParamsErr = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        radioError.gravity = Gravity.END
        radioError.layoutParams = layoutParamsErr
        radioError.text = "Please Select Value"
        linearLayout.addView(radioError)

    }

    @SuppressLint("Range")
    private fun radioButtonET(
        radioButton: RadioButton,
        tableName: String,
        childId: String,
        radioId: String
    ) {
        radioButton.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                saveValue(childId, radioId.toString())
                checkSkipLogic(childId, radioId.toString())
            } else {

            }
        }
    }

    private fun uploadImage(
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?,
        value: String,
        qcRaised: Boolean,
        qcRemark: String,
        qcResolved: Boolean,
        relativeOption: Int? = null,
        linearLayoutRelativeOption: LinearLayout,
        linearLayout: LinearLayout
    ) {

        val linearLayoutLabel = LinearLayout(requireContext())
        linearLayoutLabel.tag = "linerlayout_label_$childId"
        var layout = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.setMargins(0, 25, 0, 0)
        linearLayoutLabel.layoutParams = layout
        linearLayoutLabel.orientation = LinearLayout.HORIZONTAL


        val tv = TextView(requireContext())
        if (relativeOption != null) {
            tv.tag = "label_$childId-$relativeOption"
        } else {
            tv.tag = "label_$childId"
        }

//        tv.text = label
        tv.text = label + (if (qcRemark != "") " (" + qcRemark + ")" else "")
        /*if (qcResolved) {
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.resolved_qc_color));
        }*/
        tv.typeface = regular
        tv.textSize = 14f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
//        layoutParamsTV.setMargins(0, 25, 0, 0)
        tv.layoutParams = layoutParamsTV
        val checkBoxQc = CheckBox(requireContext())
        val checkBoxLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        checkBoxQc.isChecked = qcResolved
        if (!qcResolved) {
            if (qcRaised) {
                saveQCResolve(childId, false)
            }
        }
        checkBoxQc.setOnClickListener {
            saveQCResolve(childId, checkBoxQc.isChecked)
        }
        linearLayoutLabel.addView(tv)
        linearLayoutLabel.addView(checkBoxQc, checkBoxLayoutParams)
//        linearLayoutSection.addView(linearLayoutLabel)
        linearLayout.addView(linearLayoutLabel)

        val img = ImageView(requireContext())
        img.id = View.generateViewId()
        if (relativeOption != null) {
            img.tag = "image_$childId-$relativeOption"
        } else {
            img.tag = "image_$childId"
        }
        img.setImageResource(R.drawable.ic_select)
        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.image_width_300dp),
            resources.getDimensionPixelSize(R.dimen.image_height_200dp)
        )
        img.layoutParams = layoutParams
        img.setOnClickListener {
            if (relativeOption != null) {
                lastClickedImageViewTag = "image_$childId-$relativeOption"
                lastClickedImageViewTagUri = "$childId$relativeOption"
                if (::imageMutableArr.isInitialized) {
                    imageMutableArr["$childId$relativeOption"] = ""
                }
            } else {
                lastClickedImageViewTag = "image_$childId"
                lastClickedImageViewTagUri = childId
            }

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

            someActivityResultLauncher?.launch(chooserIntent)
        }

        linearLayoutRelativeOption.addView(img)

        val imgError = TextView(requireContext())
        if (relativeOption != null) {
            imgError.tag = "error_$childId-$relativeOption"
        } else {
            imgError.tag = "error_$childId"
        }
        imgError.visibility = View.GONE
        imgError.typeface = TabFragment.regular
        imgError.setTextColor(Color.RED)
        imgError.textSize = 10f
        val layoutParamsErr = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        imgError.gravity = Gravity.END
        imgError.layoutParams = layoutParamsErr
        imgError.text = "Please upload image"
        linearLayoutRelativeOption.addView(imgError)

        if (value != "" && value != "NULL") {
            if (isJsonArray(value)) {
                var valueArray = JSONArray(value)
                Log.d(TAG, "uploadImage: $valueArray")
                if (relativeOption != null) {
                    if (valueArray.length() > relativeOption!!) {
//                        if (valueArray.getString(relativeOption).contains("/storage")) {
                        if (isLocalFilePath(valueArray.getString(relativeOption))) {
                            var bitmapImage = pathToBitmap(valueArray.getString(relativeOption))
                            Log.d(TAG, "bitmapImage1111: " + bitmapImage)
                            img?.setImageBitmap(bitmapImage)
                        } else {
                            Glide.with(requireContext())
                                .load(
                                    ApiConstants.BASE_IMAGE_URL + valueArray.getString(
                                        relativeOption
                                    )
                                )
//                                .circleCrop()
//                                .placeholder(R.drawable.ic_default)
                                .into(img)
                        }
                    }
                } else {
//                    if (valueArray.getString(0).contains("/storage")) {
                    if (isLocalFilePath(valueArray.getString(0))) {
                        var bitmapImage = pathToBitmap(valueArray.getString(0))
                        Log.d(TAG, "bitmapImage2222: " + bitmapImage)
                        img?.setImageBitmap(bitmapImage)
                    } else {
                        Glide.with(requireContext())
                            .load(ApiConstants.BASE_IMAGE_URL + valueArray.getString(0))
                            .into(img)
                    }
                }
            } else {
//                if (value.contains("/storage")) {
                if (isLocalFilePath(value)) {
                    var bitmapImage = pathToBitmap(value)
                    Log.d(TAG, "bitmapImage3333: " + bitmapImage)
                    img?.setImageBitmap(bitmapImage)
                } else {
                    Glide.with(requireContext())
                        .load(ApiConstants.BASE_IMAGE_URL + value)
                        .into(img)
                }
            }
        }


        val valueRequired = fieldValidations.getBoolean("valueRequired")
        if (valueRequired) {
            val spannable = SpannableString("${tv.text}*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                tv.text.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = spannable
        }
        val flag = initialTimeCheckSkipLogic(childId)
        if (!qcRaised) {
            linearLayoutLabel.visibility = View.GONE
            img.visibility = View.GONE
        } else {
            linearLayoutLabel.visibility = View.VISIBLE
            img.visibility = View.VISIBLE
        }

    }

    private fun captureImage(
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?,
        value: String,
        qcRaised: Boolean,
        qcRemark: String,
        qcResolved: Boolean
    ) {
        val tv = TextView(requireContext())
        tv.tag = "label_$childId"
//        tv.text = label
        tv.text = label + (if (qcRemark != "") " (" + qcRemark + ")" else "")
        /*if (qcResolved) {
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.resolved_qc_color));
        }*/
        tv.typeface = regular
        tv.textSize = 18f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 25, 0, 0)
        tv.layoutParams = layoutParamsTV
        linearLayoutSection.addView(tv)

        val imgCapture = ImageView(requireContext())
        imgCapture.id = View.generateViewId()
        imgCapture.tag = "image_$childId"
        imgCapture.setImageResource(R.drawable.ic_capture)
        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.image_width_250dp),
            resources.getDimensionPixelSize(R.dimen.image_height_250dp)
        )
        imgCapture.layoutParams = layoutParams
        imgCapture.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivityForResult(takePictureIntent, SectionFragment.REQUEST_IMAGE_CAPTURE)
            }
        }
        linearLayoutSection.addView(imgCapture)
        /*etImgCapture = EditText(requireContext())
        etImgCapture.visibility = EditText.GONE
        ll.addView(etImgCapture)*/

        if (value != "" && value != "NULL") {
            value?.let {
                val stringToBitmap = stringToBitmap(it)
                imgCapture.setImageBitmap(stringToBitmap)
            }
        }

        val valueRequired = fieldValidations.getBoolean("valueRequired")
        if (valueRequired) {
            val spannable = SpannableString("${tv.text}*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                tv.text.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = spannable
        }
        var flag = initialTimeCheckSkipLogic(childId)
        if (!qcRaised) {
            tv.visibility = View.GONE
            imgCapture.visibility = View.GONE
        } else {
            tv.visibility = View.VISIBLE
            imgCapture.visibility = View.VISIBLE
        }
        /*etImgCapture.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
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
                }
            }
        })*/
    }

    private fun dateSelect(
        label: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?,
        value: String,
        qcRaised: Boolean,
        qcRemark: String,
        qcResolved: Boolean,
        linearLayout: LinearLayout
    ) {
        val linearLayoutLabel = LinearLayout(requireContext())
        linearLayoutLabel.tag = "linerlayout_label_$childId"
        var layout = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.setMargins(0, 25, 0, 0)
        linearLayoutLabel.layoutParams = layout
        linearLayoutLabel.orientation = LinearLayout.HORIZONTAL

        val tv = TextView(requireContext())
        tv.tag = "label_$childId"
//        tv.text = label
        tv.text = label + (if (qcRemark != "") " (" + qcRemark + ")" else "")
        /*if (qcResolved) {
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.resolved_qc_color));
        }*/
        tv.typeface = regular
        tv.textSize = 14f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
//        layoutParamsTV.setMargins(0, 25, 0, 0)
        tv.layoutParams = layoutParamsTV

        val checkBoxQc = CheckBox(requireContext())
        val checkBoxLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        checkBoxQc.isChecked = qcResolved
        if (!qcResolved) {
            if (qcRaised) {
                saveQCResolve(childId, false)
            }
        }
        checkBoxQc.setOnClickListener {
            saveQCResolve(childId, checkBoxQc.isChecked)
        }
        linearLayoutLabel.addView(tv)
        linearLayoutLabel.addView(checkBoxQc, checkBoxLayoutParams)
        linearLayout.addView(linearLayoutLabel)

        val textDateEt = EditText(requireContext())
        textDateEt.tag = "placeholder_$childId"
        textDateEt.hint = "Click and select date"
        textDateEt.textSize = 12f
        textDateEt.typeface = regular
        textDateEt.isFocusable = false
        textDateEt.isClickable = true
        textDateEt.setBackgroundResource(R.drawable.et_back)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        textDateEt.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        linearLayout.addView(textDateEt)

        val dateError = TextView(requireContext())
        dateError.tag = "error_$childId"
        dateError.visibility = View.GONE
        dateError.typeface = regular
        dateError.setTextColor(Color.RED)
        dateError.textSize = 10f
        val layoutParamsErr = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        dateError.gravity = Gravity.END
        dateError.layoutParams = layoutParamsErr
        dateError.text = "Please Select Value"
        linearLayout.addView(dateError)
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

//        viewDataSetFromDbText(tableName, childId, textDateEt)

        if (value != "" && value != "NULL") {
            textDateEt.setText(changeDateTimeFormat(value, "yyyy-MM-dd", "dd/MM/yyyy"))
        }

        if (valueRequired) {
            val spannable = SpannableString("${tv.text}*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                tv.text.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = spannable
        }

        var flag = initialTimeCheckSkipLogic(childId)
        if (!qcRaised) {
            linearLayoutLabel.visibility = View.GONE
            textDateEt.visibility = View.GONE
        } else {
            linearLayoutLabel.visibility = View.VISIBLE
            textDateEt.visibility = View.VISIBLE
        }
        EditTextDebounce(textDateEt)
            .watch {
                saveValue(childId, changeDateTimeFormat(it, "dd/MM/yyyy", "yyyy-MM-dd"))
                checkSkipLogic(childId, it.toString())
            }
    }

    private fun showDatePickerDialog(
        valueRequired: Boolean,
        tableName: String,
        childId: String,
        textDateEt: EditText,
        dateError: TextView,
        fieldValidations: JSONObject
    ) {
        var selectedDate = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->

                //YYYYMMDD
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val formattedDate = sdf.format(selectedDate.time)
                textDateEt.setText(formattedDate)

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

    private fun dateCheckValidation(
        valueRequired: Boolean,
        tableName: String,
        childId: String,
        textSelect: EditText,
        dateError: TextView
    ) {
        if (valueRequired) {
            var value = textSelect.text.toString()
            if (!value.isNullOrBlank() && value != "NULL") {
                textSelect.error = null
                dateError.visibility = View.GONE
            } else {
                textSelect.error = "Please Select Value"
                dateError.visibility = View.VISIBLE
            }
        }
    }

    private fun getCurrentLocation(
        label: String,
        placeholder: String,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        skipLogic: JSONArray?,
        value: String,
        qcRaised: Boolean,
        qcRemark: String,
        qcResolved: Boolean,
        linearLayout: LinearLayout
    ) {
        val linearLayoutLabel = LinearLayout(requireContext())
        linearLayoutLabel.tag = "linerlayout_label_$childId"
        var layout = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.setMargins(0, 25, 0, 0)
        linearLayoutLabel.layoutParams = layout
        linearLayoutLabel.orientation = LinearLayout.HORIZONTAL

        val tv = TextView(requireContext())
        tv.tag = "label_$childId"
//        tv.text = label
        tv.text = label + (if (qcRemark != "") " (" + qcRemark + ")" else "")
        /* if (qcResolved) {
             tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.resolved_qc_color));
         }*/
        tv.typeface = regular
        tv.textSize = 14f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
//        layoutParamsTV.setMargins(0, 25, 0, 0)
        tv.layoutParams = layoutParamsTV
        val checkBoxQc = CheckBox(requireContext())
        val checkBoxLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        checkBoxQc.isChecked = qcResolved
        if (!qcResolved) {
            if (qcRaised) {
                saveQCResolve(childId, false)
            }
        }
        checkBoxQc.setOnClickListener {
            saveQCResolve(childId, checkBoxQc.isChecked)
        }
        linearLayoutLabel.addView(tv)
        linearLayoutLabel.addView(checkBoxQc, checkBoxLayoutParams)
        linearLayout.addView(linearLayoutLabel)

        val conLayout = ConstraintLayout(requireContext())
        linearLayout.addView(conLayout)
        val btnLoc = Button(requireContext())
        btnLoc.tag = "button_$childId"

        val textLocationEt = EditText(requireContext())
        textLocationEt.tag = "placeholder_$childId"
        textLocationEt.hint = placeholder
        /*textLocationEt.maxLines = 2
        textLocationEt.minLines = 2*/
        textLocationEt.textSize = 12f
        textLocationEt.typeface = regular
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
        textLocationEt.setText(value)

        btnLoc.text = "LIVE"
        btnLoc.textSize = 10f
        btnLoc.typeface = regular
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        btnLoc.setTextColor(textColor)
        btnLoc.setBackgroundResource(R.drawable.btn_back)
        val paramsButton = ConstraintLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.location_live_width),
            resources.getDimensionPixelSize(R.dimen.location_live_height)
        )
        paramsButton.startToEnd = textLocationEt.id
        paramsButton.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        paramsButton.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        paramsButton.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

        btnLoc.layoutParams = paramsButton
        btnLoc.setOnClickListener {
            checkLocationPermission(label, placeholder, textLocationEt)
        }
        conLayout.addView(btnLoc)

        val locationError = TextView(requireContext())
        locationError.visibility = View.GONE
        locationError.tag = "error_$childId"
        locationError.setTextColor(Color.RED)
        locationError.textSize = 10f
        locationError.typeface = TabFragment.regular
        val layoutParamsErr = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        locationError.gravity = Gravity.END
        locationError.layoutParams = layoutParamsErr
        locationError.text = "Please Select Value"
        linearLayout.addView(locationError)

        val valueRequired = fieldValidations.getBoolean("valueRequired")
        if (valueRequired) {
            val spannable = SpannableString("${tv.text}*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                tv.text.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = spannable
        }
        var flag = initialTimeCheckSkipLogic(childId)
        if (!qcRaised) {
            linearLayoutLabel.visibility = View.GONE
            textLocationEt.visibility = View.GONE
            btnLoc.visibility = View.GONE
        } else {
            linearLayoutLabel.visibility = View.VISIBLE
            textLocationEt.visibility = View.VISIBLE
            btnLoc.visibility = View.VISIBLE
        }
        EditTextDebounce(textLocationEt)
            .watch {
                saveValue(childId, it.toString())
                checkSkipLogic(childId, it.toString())
            }
    }

    private fun checkLocationPermission(
        label: String,
        placeholder: String,
        textLocationEt: EditText
    ) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            liveLocation(label, placeholder, textLocationEt)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                SectionFragment.LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun liveLocation(label: String, placeholder: String, textLocationEt: EditText) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                it?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    textLocationEt.setText("$latitude, $longitude")
                    //Log.d("TAG", "liveLocation: $latitude + $longitude")
                }
            }.addOnFailureListener {
                Log.d(TAG, "liveLocation: Exception==${it.toString()}")
            }
    }

    private fun addableUploadImage(
        label: String,
        localId: String,
        addableType: String,
        value: String,
        countAddable: Int,
        valueRequired: Boolean,
        linearLayoutAddable: LinearLayout,
        dynamicObject: MutableMap<Int, Any>? = null,
        addableDataArr: MutableMap<Int, MutableMap<Int, Any>>? = null,
        k: Int,
        id: String
    ) {
        Log.e("addk", countAddable.toString())
        var addableActivityResultLauncher: ActivityResultLauncher<Intent>? = null

        val tv = TextView(requireContext())
        //  tv.tag = "label_$localId-$countAddable"

        tv.text = label
        tv.typeface = TabFragment.regular
        tv.textSize = 14f
        val layoutParamsView1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsView1.setMargins(25, 0, 25, 0)
        tv.layoutParams = layoutParamsView1
        linearLayoutAddable.addView(tv)

        val img = ImageView(requireContext())
        img.id = View.generateViewId()
        // img.tag = "image_$localId-$countAddable"

        img.setImageResource(R.drawable.ic_select)
        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.image_width_300dp),
            resources.getDimensionPixelSize(R.dimen.image_height_200dp)
        )
        layoutParams.setMargins(25, 0, 25, 0)
        img.layoutParams = layoutParams
        lastClickedImageViewTag = "image_$label-$countAddable"
        //       lastClickedImageViewTagUri = "$localId-$countAddable"
        Log.e("AddrPrev -$countAddable", label.toString())

        Log.e(
            "ckedimages",
            lastClickedImageViewTag.toString() + " " + lastClickedImageViewTagUri
        )


        img.setOnClickListener {
            // openBottomSheet(

            lastClickedImageViewTag = "image-$countAddable"
            lastClickedImageViewTagUri = "$label-$countAddable"

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
            addableActivityResultLauncher?.launch(chooserIntent)

            Log.e("BottomIntent", chooserIntent.toString())

        }
        var newImageAdd: Uri? = null
        var oldImageAdd: Uri? = null

        if (addableActivityResultLauncher == null) {
            addableActivityResultLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val data: Intent? = result.data
                        var imgUri2: Uri? = null
                        if (data != null) {
                            // Access intent values here
                            // For example:
                            val selectedImageUri: Uri? = data.data

                            if (data.extras != null && data.extras!!.containsKey("data")) {

                                val imageBitmap = data.extras!!.get("data") as Bitmap
                                imgUri2 = imageBitmap.saveImage(requireContext())
                            } else if (selectedImageUri != null) {

                                imgUri2 = selectedImageUri

                            }
                            if (newImageAdd == null) {
                                newImageAdd = imgUri2
                            } else {
                                // Otherwise, set the new image URI
                                oldImageAdd = newImageAdd
                                newImageAdd = imgUri2
                            }

                            if (oldImageAdd != null) {
                                deleteLastUploadedImages(
                                    requireContext(),
                                    folderName,
                                    1,
                                    folderfiles
                                )
                            }
                            var base64Encoded = getPathFromUri(requireContext(), newImageAdd!!)
                            Log.e("adding value", base64Encoded.toString())

                            val jsonArray = JSONArray()
                            if (base64Encoded == null) jsonArray.put(newImageAdd?.path!!) else
                                jsonArray.put(base64Encoded)

                            if (base64Encoded.isNullOrEmpty()) {
                                dynamicObject?.set(k, newImageAdd?.path.toString())
                                if (addableDataArr != null) {
                                    Log.e("data64Encoded", newImageAdd?.path.toString())
                                    saveValueAny(id, addableDataArr)
                                }
                            } else {
                                dynamicObject?.set(k, base64Encoded!!)
                                if (addableDataArr != null) {
                                    Log.e("addabledata", newImageAdd?.path.toString())
                                    saveValueAny(id, addableDataArr)
                                }
                            }

                            Log.e("selectedURI", selectedImageUri.toString())
                            img.setImageURI(imgUri2)
                        }
                    }
                }
        }else{
            addableActivityResultLauncher = null
        }
        linearLayoutAddable.addView(img)

        val imgError = TextView(requireContext())

        imgError.tag = "error_$label-$countAddable"

        imgError.visibility = View.GONE
        imgError.typeface = TabFragment.regular
        imgError.setTextColor(Color.RED)
        imgError.textSize = 10f
        val layoutParamsErr = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        imgError.gravity = Gravity.END
        imgError.layoutParams = layoutParamsErr
        imgError.text = "Please upload image"
        linearLayoutAddable.addView(imgError)

        if (value != "" && value != "NULL") {
            if (isJsonArray(value)) {
                var valueArray = JSONArray(value)
                Log.d(TAG, "uploadImage: $valueArray")
                var bitmapImage = pathToBitmap(valueArray.getString(0))
                Log.d(TAG, "bitmapImage1: " + bitmapImage)
                img?.setImageBitmap(bitmapImage)
            }
        } else {
            var bitmapImage = pathToBitmap(value)
            Log.d(TAG, "bitmapImage: " + bitmapImage)
            img?.setImageBitmap(bitmapImage)
        }

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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addable(
        childObject: JSONObject,
        bt: Button,
        skipLogic: JSONArray?,
        linearLayoutAddableMain: LinearLayout,
        count: Int,
        value: JSONObject? = null,
        addableDataArr: MutableMap<Int, MutableMap<Int, Any>>,
        qcRaised: Boolean = false,
        qcRemark: String = "",
        qcResolved: Boolean = false
    ) {
        val countAddable = count + 1

        val dynamicObject = mutableMapOf<Int, Any>()
        addableDataArr[countAddable] = dynamicObject

        val label = childObject.getJSONObject("properties").getString("label")
        val id = childObject.getString("id")

        val relativeLayout = RelativeLayout(requireContext())
        val linearLayoutAddable = LinearLayout(requireContext())
        val layoutParamsLl = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsLl.setMargins(0,0,0,30)
        linearLayoutAddable.layoutParams = layoutParamsLl
        linearLayoutAddable.orientation = LinearLayout.VERTICAL
        linearLayoutAddable.setPadding(0,0,0,30)
        linearLayoutAddable.background = ContextCompat.getDrawable(requireContext(), R.drawable.layout_background)
        linearLayoutAddableMain.addView(linearLayoutAddable, layoutParamsLl)

        textViewWithDelete(
            label,
            qcRaised,
            qcRemark,
            qcResolved,
            relativeLayout,
            linearLayoutAddable,
            linearLayoutAddableMain,
            countAddable,
            id,
            addableDataArr
        )

        val btn = Button(requireContext())

        val addableFormat = childObject.getJSONArray("addableFormat")

        for (k in 0 until addableFormat.length()) {

            val addableItem = addableFormat.getJSONObject(k)
            val addableType = addableItem.getString("type")
            val localId = addableItem.getString("localId") + "_" + countAddable
            val addableLabel = addableItem.getString("label")
            val addablePlaceholder = addableItem.getString("placeholder")
            val valueRequired = addableItem.getBoolean("valueRequired")
            var customValidationArr: JSONArray? = null
            if (addableItem.has("customValidation")) {
                customValidationArr = addableItem.getJSONArray("customValidation")
            }
            var storeData: String? = null
            if (value != null) {
                if (value.has(k.toString())) {
                    storeData = value.getString(k.toString())
                }
            }
            when (addableType) {
                "TEXT" -> {
                    //val fieldValidations = childObject.getJSONObject("fieldValidations")
                    inputTextAddable(
                        addablePlaceholder,
                        addableLabel,
                        linearLayoutAddable,
                        valueRequired,
                        dynamicObject,
                        k,
                        id,
                        storeData,
                        addableDataArr
                    )
                }

                "NUMBERS" -> {
                    //val fieldValidations = childObject.getJSONObject("fieldValidations")
                    inputNumberAddable(
                        addablePlaceholder,
                        addableLabel,
                        customValidationArr,
                        linearLayoutAddable,
                        valueRequired,
                        dynamicObject,
                        k,
                        id,
                        storeData,
                        addableDataArr
                    )
                }

                "DROPDOWN" -> {
                    val multiSelect = addableItem.getBoolean("multiSelect")
                    addableDropDown(
                        addableLabel,
                        addableItem,
                        linearLayoutAddable,
                        valueRequired,
                        dynamicObject,
                        k,
                        addablePlaceholder,
                        multiSelect,
                        id,
                        storeData,
                        addableDataArr
                    )
                }

                "UPLOAD_IMAGE" -> {
                    addableUploadImage(
                        addableLabel,
                        localId,
                        addableType,
                        storeData.toString(),
                        countAddable,
                        valueRequired,
                        linearLayoutAddable,
                        dynamicObject,
                        addableDataArr,
                        k,
                        id
                    )
                }
            }


        }
        val addButtonTitle = childObject.getString("addButtonTitle")

        setPrefIntData(requireContext(), "count", countAddable)

        val x = linearLayoutSection.findViewWithTag<Button>("button_addable_${id}")
        if (x == null) {
            buttonView(
                btn,
                addButtonTitle,
                childObject,
                bt,
                linearLayoutAddableMain,
                addableDataArr,
                qcRaised,
                qcRemark,
                qcResolved,
            )
        }

        val flag = initialTimeCheckSkipLogic(id)
        if (!qcRaised) {
            linearLayoutAddable.visibility = View.GONE
            btn.visibility = View.GONE
        } else {
            linearLayoutAddable.visibility = View.VISIBLE
            btn.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun textViewWithDelete(
        label: String,
        qcRaised: Boolean = false,
        qcRemark: String = "",
        qcResolved: Boolean = false,
        relativeLayout: RelativeLayout,
        linearLayoutAddable: LinearLayout,
        linearLayoutAddableMain: LinearLayout,
        countAddable: Int,
        id: String,
        addableDataArr: MutableMap<Int, MutableMap<Int, Any>>
    ) {
        val layoutParamsLl = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsLl.setMargins(0, 0, 0, 20)
        layoutParamsLl.addRule(RelativeLayout.CENTER_VERTICAL)
        relativeLayout.layoutParams = layoutParamsLl
        relativeLayout.setPadding(15, 15, 15, 15)
        relativeLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.title_background)
        linearLayoutAddable.addView(relativeLayout, layoutParamsLl)


        val imageView = ImageView(requireContext())
        imageView.tag = "imageViewDelete"
        imageView.id = View.generateViewId()
        val layoutParamsImageView = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParamsImageView.addRule(RelativeLayout.ALIGN_PARENT_END)
        layoutParamsImageView.addRule(RelativeLayout.CENTER_VERTICAL)
        imageView.layoutParams = layoutParamsImageView
        imageView.setImageResource(R.drawable.ic_delete)
        val outValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
        val selectableBackgroundResourceId = outValue.resourceId
        val selectableBackground = ContextCompat.getDrawable(requireContext(), selectableBackgroundResourceId)
        val layers = arrayOf(selectableBackground, ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete))
        val layerDrawable = LayerDrawable(layers)
        imageView.setImageDrawable(layerDrawable)
        relativeLayout.addView(imageView, layoutParamsImageView)

        val checkBoxQc = CheckBox(requireContext())
        checkBoxQc.tag = "checkbox"
        checkBoxQc.id = View.generateViewId()
        val checkBoxLayoutParams = RelativeLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        checkBoxLayoutParams.addRule(
            RelativeLayout.START_OF,
            relativeLayout.findViewWithTag<View>("imageViewDelete").id
        )
        checkBoxQc.isChecked = qcResolved
        if (!qcResolved) {
            if (qcRaised) {
                saveQCResolve(id, false)
            }
        }
        checkBoxQc.setOnClickListener {
            saveQCResolve(id, checkBoxQc.isChecked)
        }
        relativeLayout.addView(checkBoxQc, checkBoxLayoutParams)

        val tv = TextView(requireContext())
        tv.text = "$label ($qcRemark)"
        /*if (countAddable < 10) {
            tv.text = "0$countAddable $label ($qcRemark)"
        } else {
            tv.text = "$countAddable $label ($qcRemark)"
        }*/
        tv.textSize = 15f
        tv.typeface = medium
        //tv.setTypeface(null, Typeface.BOLD)
        val layoutParamsTV = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.addRule(RelativeLayout.ALIGN_PARENT_START)
        layoutParamsTV.addRule(
            RelativeLayout.START_OF,
            relativeLayout.findViewWithTag<View>("checkbox").id
        )
        tv.layoutParams = layoutParamsTV

        relativeLayout.addView(tv, layoutParamsTV)

        imageView.setOnClickListener {
            if (countAddable > 1) {
                try {
                    addableDataArr.remove(countAddable)
                    saveValueAny(id, addableDataArr)
                    linearLayoutAddableMain.removeView(linearLayoutAddable)
                } catch (e: Exception) {
                    Log.d(TAG, "textViewWithDelete: ${e.message}")
                }
            }
        }
    }

    private fun inputTextAddable(
        placeholder: String,
        label: String,
        linearLayoutAddable: LinearLayout,
        valueRequired: Boolean,
        dynamicObject: MutableMap<Int, Any>,
        k: Int,
        id: String,
        storeData: String? = null,
        addableDataArr: MutableMap<Int, MutableMap<Int, Any>>
    ) {
        val tv = TextView(requireContext())
        tv.text = label
        tv.textSize = 14f
        tv.typeface = regular
        val layoutParamsView1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsView1.setMargins(25, 0, 25, 0)
        tv.layoutParams = layoutParamsView1
        linearLayoutAddable.addView(tv)
        val etInputTextAddable = EditText(requireContext())
        etInputTextAddable.hint = placeholder
        etInputTextAddable.typeface = regular
        etInputTextAddable.textSize = 12f
        etInputTextAddable.inputType = InputType.TYPE_CLASS_TEXT
        etInputTextAddable.id = View.generateViewId()
        val layoutParamsView2 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsView2.setMargins(25, 0, 25, 0)
        etInputTextAddable.layoutParams = layoutParamsView2
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

        if (storeData != null) {
            etInputTextAddable.setText(storeData)
        }

        if (etInputTextAddable.text.toString() != "") {
            dynamicObject[k] = etInputTextAddable.text.toString()
            saveValueAny(id, addableDataArr)
        }

        etInputTextAddable.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dynamicObject[k] = s.toString()
                saveValueAny(id, addableDataArr)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun inputNumberAddable(
        placeholder: String,
        label: String,
        customValidationArr: JSONArray? = null,
        linearLayoutAddable: LinearLayout,
        valueRequired: Boolean,
        dynamicObject: MutableMap<Int, Any>,
        k: Int,
        id: String,
        storeData: String? = null,
        addableDataArr: MutableMap<Int, MutableMap<Int, Any>>
    ) {
        val tv = TextView(requireContext())
        tv.text = label
        tv.textSize = 14f
        tv.typeface = TabFragment.regular
        val layoutParamsView1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsView1.setMargins(25, 0, 25, 0)
        tv.layoutParams = layoutParamsView1
        linearLayoutAddable.addView(tv)

        val etInputTextAddable = EditText(requireContext())
        etInputTextAddable.hint = placeholder
        etInputTextAddable.typeface = TabFragment.regular
        etInputTextAddable.textSize = 12f
        etInputTextAddable.inputType = InputType.TYPE_CLASS_TEXT
        etInputTextAddable.id = View.generateViewId()
        val layoutParamsView2 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsView2.setMargins(25, 0, 25, 0)
        etInputTextAddable.layoutParams = layoutParamsView2
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

        if (storeData != null) {
            etInputTextAddable.setText(storeData)
        }

        if (etInputTextAddable.text.toString() != "") {
            dynamicObject[k] = etInputTextAddable.text.toString()
            saveValueAny(id, addableDataArr)
        }

        etInputTextAddable.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dynamicObject[k] = s.toString()
                saveValueAny(id, addableDataArr)
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
        if (customValidationArr != null) {
            var customValidation = customValidationArr.getJSONObject(0)
//            var globalTargetId=customValidation.getString("worker_dob")
//            var localTargetId=customValidation.getString("addable_relation")
            var matchesFollowing = customValidation.getJSONObject("matchesFollowing")
            var matchesData = matchesFollowing.getJSONArray("data")
            var localTargetValue = dynamicObject[1]
            for (j in 0 until matchesData.length()) {
                val dataObject = matchesData.getJSONObject(j)
                var operand = dataObject.getJSONArray("operand")

                for (k in 0 until operand.length()) {
                    var operandValue = operand.getString(k)
                    if (localTargetValue == operandValue) {
                        var maxActualValue = 0
                        var minActualValue = 0
                        var maxLimit = dataObject.getString("maxLimit")
                        var minLimit = dataObject.getString("minLimit")

                        var maxLimitValue = maxLimit.toIntOrNull()
                        if (maxLimitValue != null) {
                            println("It's an Integer")
                            maxActualValue = maxLimitValue
                        } else if (maxLimit is String) {
                            println("It's a String")
                            getValue(maxLimit) {
                                if (it.isNullOrBlank()) {
                                    maxActualValue = calculateAge(it)
                                }
                            }
                        } else {
                            println("It's neither an Integer nor a String")
                        }

                        var minLimitValue = minLimit.toIntOrNull()
                        if (minLimitValue != null) {
                            println("It's an Integer")
                            minActualValue = minLimitValue
                        } else if (minLimit is String) {
                            println("It's a String")
                            getValue(minLimit) {
                                if (it.isNullOrBlank()) {
                                    minActualValue = calculateAge(it)
                                }
                            }
                        } else {
                            println("It's neither an Integer nor a String")
                        }

                        setMinMaxValues(etInputTextAddable, minActualValue, maxActualValue)
                    }
                }

            }
        }
    }

    private fun addableDropDown(
        addableLabel: String,
        addableItem: JSONObject,
        linearLayoutAddable: LinearLayout,
        valueRequired: Boolean,
        dynamicObject: MutableMap<Int, Any>,
        k: Int,
        addablePlaceholder: String,
        multiSelect: Boolean,
        id: String,
        storeData: String?,
        addableDataArr: MutableMap<Int, MutableMap<Int, Any>>
    ) {
        val tvAddableDropDown = TextView(requireContext())
        tvAddableDropDown.text = addableLabel
        tvAddableDropDown.textSize = 14f
        tvAddableDropDown.typeface = regular
        val layoutParamsView1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsView1.setMargins(25, 0, 25, 0)
        tvAddableDropDown.layoutParams = layoutParamsView1
        linearLayoutAddable.addView(tvAddableDropDown)

        val textSelect = EditText(requireContext())
        textSelect.hint = addablePlaceholder
        textSelect.textSize = 12f
        textSelect.typeface = regular
        textSelect.isFocusable = false
        textSelect.isClickable = true
        val layoutParamsView2 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParamsView2.setMargins(25, 0, 25, 0)
        textSelect.layoutParams = layoutParamsView2
        textSelect.setBackgroundResource(R.drawable.et_back)
        textSelect.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        textSelect.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        linearLayoutAddable.addView(textSelect)

        val dropDownError = TextView(requireContext())
        dropDownError.visibility = View.GONE
        dropDownError.setTextColor(Color.RED)
        dropDownError.textSize = 10f
        dropDownError.typeface = regular
        val layoutParamsErr = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        dropDownError.gravity = Gravity.END
        dropDownError.layoutParams = layoutParamsErr
        dropDownError.text = "Please Select Value"
        linearLayoutAddable.addView(dropDownError)

        val textSelectId = EditText(requireContext())
        textSelectId.tag = k.toString() //childId
        textSelectId.visibility = View.GONE
        linearLayoutAddable.addView(textSelectId)

        val addableOptionArr = addableItem.getJSONArray("options")

        if (storeData != null && storeData != "") {
            if (multiSelect){
                val retrievedList = storeData.split(", ")
                var dataValue: String? = null
                for (i in 0 until retrievedList.size) {
                    for (j in 0 until addableOptionArr.length()) {
                        if (j == retrievedList.get(i).toString().toInt()) {
                            if (dataValue == null) {
                                dataValue = addableOptionArr.getString(j)
                            } else {
                                dataValue = "$dataValue, ${addableOptionArr.getString(j)}"
                            }

                        }
                    }
                }
                textSelectId.setText(storeData)
                textSelect.setText(dataValue)

            }else {
                val optionName = addableOptionArr.getString(storeData.toInt())
                textSelect.setText(optionName)
                textSelectId.setText(storeData)
            }
        }

        if (textSelectId.text.toString() != "") {
            dynamicObject[k] = textSelectId.text.toString()
            saveValueAny(id, addableDataArr)
        }

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
            addableDropDownDialog(
                addableOptionArr,
                addablePlaceholder,
                multiSelect,
                valueRequired,
                textSelect,
                textSelectId,
                k,
                dynamicObject,
                id,
                addableDataArr
            )
        }
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
    }

    @SuppressLint("Range")
    private fun addableDropDownDialog(
        addableOptionArr: JSONArray,
        addablePlaceholder: String,
        multiSelect: Boolean,
        valueRequired: Boolean,
        textSelect: EditText,
        textSelectId: EditText,
        k: Int,
        dynamicObject: MutableMap<Int, Any>,
        id: String,
        addableDataArr: MutableMap<Int, MutableMap<Int, Any>>
    ) {
        val options = mutableMapOf<Int, String>()

        for (i in 0 until addableOptionArr.length()) {
            val optionId = i
            val optionName = addableOptionArr.getString(i)
            options[optionId] = optionName
        }
        val selectedData = BooleanArray(options.size)

        val builder = AlertDialog.Builder(requireContext())
        val titleView = TextView(requireContext())
        titleView.text = addablePlaceholder
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)
        titleView.typeface = medium
        titleView.setTextColor(Color.BLACK)
        titleView.gravity = Gravity.CENTER
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 10, 0, 10)
        titleView.layoutParams = layoutParams
        builder.setCustomTitle(titleView)
        builder.setCancelable(false)
        var selectedIndex = 0


        if (multiSelect) {
            showDropDownMultiSelect(options, selectedData,  textSelectId.text.toString())
            builder.setMultiChoiceItems(
                options.values.toTypedArray(),
                selectedData
            ) { _, which, isChecked ->
                selectedData[which] = isChecked
            }
            // Handle multi-select logic
        } else {
            val selPos = textSelectId.text.toString()
            if (selPos != "") {
                selectedIndex = selPos.toInt()
            } else {
                selectedIndex = 0
            }
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
            }else{
                for ((index, value) in options.values.withIndex()) {
                    if (selectedData[index]) {
                        selectedItems.add(value)
                        selectedItemsId.add(index)
                    }
                    textSelect.setText(selectedItems.joinToString(", "))
                    textSelectId.setText(selectedItemsId.joinToString(", "))
                }
            }
            val selectedValue = selectedItems.joinToString(", ")
            dynamicObject[k] = textSelectId.text.toString()

            saveValueAny(id, addableDataArr)
        }

        builder.setNegativeButton("Cancel") { _, _ ->
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            val alertDialog = it as AlertDialog
            val listView = alertDialog.listView
            listView?.let {
                for (i in 0 until it.childCount) {
                    val textView = it.getChildAt(i) as TextView
                    textView.typeface = regular
                }
            }
        }
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buttonView(
        btn: Button,
        label: String,
        childObject: JSONObject,
        bt: Button,
        linearLayoutAddableMain: LinearLayout,
        addableDataArr: MutableMap<Int, MutableMap<Int, Any>>,
        qcRaised: Boolean = false,
        qcRemark: String = "",
        qcResolved: Boolean = false
    ) {
        val id = childObject.getString("id")
        btn.tag = "button_addable_${id}"
        btn.text = label
        btn.textSize = 14f
        btn.typeface = medium
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        btn.setTextColor(textColor)
        btn.setBackgroundResource(R.color.green)
        val layoutParamsBtn = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsBtn.setMargins(25, 80, 25, 20)
        btn.layoutParams = layoutParamsBtn
        btn.setOnClickListener {
            val pos = getPrefIntData(requireContext(), "count")
            linearLayoutAddableMain.removeView(btn)
            /*if (bt.parent != null) {
                (bt.parent as? ViewGroup)?.removeView(bt)
            }*/
            addable(
                childObject,
                bt,
                null,
                linearLayoutAddableMain,
                pos,
                addableDataArr = addableDataArr,
                qcRaised = qcRaised,
                qcRemark = qcRemark,
                qcResolved = qcResolved
            )
        }
        /*val viewSubmitButton = linearLayoutSection.findViewWithTag<View>("button_submit")
        var isSubmitButton=false
        if (viewSubmitButton != null) {
            linearLayoutSection.removeView(viewSubmitButton)
            isSubmitButton=true
        }*/
        linearLayoutAddableMain.addView(btn)
        /*if (isSubmitButton){
            linearLayoutSection.addView(viewSubmitButton)
        }*/
    }

    @SuppressLint("Range")
    private fun dropDown(
        label: String,
        options: MutableMap<Int, String>,
        tableName: String,
        childId: String,
        fieldValidations: JSONObject,
        placeholder: String,
        skipLogic: JSONArray?,
        valueMain: String,
        qcRaised: Boolean,
        qcRemark: String,
        qcResolved: Boolean,
        relativeOptionsLogic: String? = null,
        customOptionObject: JSONObject? = null,
        linearLayout: LinearLayout
    ) {
        val value = valueMain

        // Create a LinearLayout
        val linearLayoutLabel = LinearLayout(requireContext())
        linearLayoutLabel.tag = "linerlayout_label_$childId"
        var layout = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layout.setMargins(0, 25, 0, 0)
        linearLayoutLabel.layoutParams = layout
        linearLayoutLabel.orientation = LinearLayout.HORIZONTAL

        val tvDropDown = TextView(requireContext())
        tvDropDown.tag = "label_$childId"
//        tvDropDown.text = label
        tvDropDown.text = label + (if (qcRemark != "") " (" + qcRemark + ")" else "")
        /*if (qcResolved) {
            tvDropDown.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.resolved_qc_color
                )
            );
        }*/
        tvDropDown.textSize = 14f
        tvDropDown.typeface = regular
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
//        layoutParamsTV.setMargins(0, 25, 0, 0)
        tvDropDown.layoutParams = layoutParamsTV

        val checkBoxQc = CheckBox(requireContext())
        val checkBoxLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        checkBoxQc.isChecked = qcResolved
        if (!qcResolved) {
            if (qcRaised) {
                saveQCResolve(childId, false)
            }
        }
        checkBoxQc.setOnClickListener {
            saveQCResolve(childId, checkBoxQc.isChecked)
        }
        linearLayoutLabel.addView(tvDropDown, layoutParamsTV)
        linearLayoutLabel.addView(checkBoxQc, checkBoxLayoutParams)
        linearLayout.addView(linearLayoutLabel)

        val textSelect = EditText(requireContext())
        textSelect.tag = "placeholder_$childId"
        textSelect.hint = placeholder
        textSelect.textSize = 12f
        textSelect.typeface = regular
        textSelect.isFocusable = false
        textSelect.isClickable = true
        textSelect.setBackgroundResource(R.drawable.et_back)
        textSelect.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0)
        val paddingInPxSE = resources.getDimensionPixelSize(R.dimen.edit_text_padding_SE)
        val paddingInPxTB = resources.getDimensionPixelSize(R.dimen.edit_text_padding_TB)
        textSelect.setPadding(paddingInPxSE, paddingInPxTB, paddingInPxSE, paddingInPxTB)
        linearLayout.addView(textSelect)

        val dropDownError = TextView(requireContext())
        dropDownError.tag = "error_$childId"
        dropDownError.visibility = View.GONE
        dropDownError.typeface = regular
        dropDownError.setTextColor(Color.RED)
        dropDownError.textSize = 10f
        val layoutParamsErr = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        dropDownError.gravity = Gravity.END
        dropDownError.layoutParams = layoutParamsErr
        dropDownError.text = "Please Select Value"
        linearLayout.addView(dropDownError)

        val textSelectId = EditText(requireContext())
        textSelectId.tag = childId
        textSelectId.visibility = View.GONE
        linearLayout.addView(textSelectId)

        val valueRequired = fieldValidations.getBoolean("valueRequired")
        val multiSelect = fieldValidations.getBoolean("multiSelect")

        if (options != null && options.size > 0) {
            if (!value.isNullOrBlank() && value != "NULL") {
                val retrievedList = JSONArray(value) /*.split(", ")*/
                var dataValue: String? = null
                for (i in 0 until retrievedList.length()) {
                    for ((index, data) in options) {
                        if (index == retrievedList.get(i).toString().toInt()) {
                            if (dataValue == null) {
                                dataValue = data
                            } else {
                                dataValue = "$dataValue, $data"
                            }

                        }
                    }
                    if (i == 0) {
                        checkSkipLogic(childId, retrievedList.get(i).toString(), false)
                    }
                }
                textSelectId.setText(value)
                textSelect.setText(dataValue)

            }
        } else {
            if (fieldValidations.has("access")){
                val access = fieldValidations.getString("access")
                if (access == "state") {
                    var dataValue: String? = null
                    if (!value.isNullOrBlank() && value != "NULL") {
                        val retrievedList = JSONArray(value)

                        for (i in 0 until retrievedList.length()) {
                            var cursor = dbHelper.getAllRecordsWithCondition(
                                MasterDBHelper.USER_STATE_TABLE_NAME,
                                "${MasterDBHelper.ID}=${
                                    retrievedList.get(i).toString().toInt()
                                }  ORDER BY ${MasterDBHelper.NAME} ASC"
                            )
                            if (cursor.moveToFirst()) {
                                do {
                                    if (dataValue == null) {
                                        dataValue = cursor.getString(
                                            cursor.getColumnIndex(
                                                MasterDBHelper.NAME
                                            )
                                        )
                                    } else {
                                        dataValue = "$dataValue, ${
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        }"
                                    }

                                } while (cursor.moveToNext())
                            }
                        }
                        textSelectId.setText(value)
                        textSelect.setText(dataValue)
                    }

                } else if (access == "district") {
                    var dataValue: String? = null
                    if (!value.isNullOrBlank() && value != "NULL") {
                        val retrievedList = JSONArray(value)

                        for (i in 0 until retrievedList.length()) {
                            var cursor = dbHelper.getAllRecordsWithCondition(
                                MasterDBHelper.USER_DISTRICT_TABLE_NAME,
                                "${MasterDBHelper.ID}=${
                                    retrievedList.get(i).toString().toInt()
                                }  ORDER BY ${MasterDBHelper.NAME} ASC"
                            )
                            if (cursor.moveToFirst()) {
                                do {
                                    if (dataValue == null) {
                                        dataValue = cursor.getString(
                                            cursor.getColumnIndex(
                                                MasterDBHelper.NAME
                                            )
                                        )
                                    } else {
                                        dataValue = "$dataValue, ${
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        }"
                                    }

                                } while (cursor.moveToNext())
                            }
                        }
                        textSelectId.setText(value)
                        textSelect.setText(dataValue)
                    }
                } else if (access == "city") {
                    var dataValue: String? = null
                    if (!value.isNullOrBlank() && value != "NULL") {
                        val retrievedList = JSONArray(value)

                        for (i in 0 until retrievedList.length()) {
                            var cursor = dbHelper.getAllRecordsWithCondition(
                                MasterDBHelper.USER_CITY_TABLE_NAME,
                                "${MasterDBHelper.ID}=${
                                    retrievedList.get(i).toString().toInt()
                                }  ORDER BY ${MasterDBHelper.NAME} ASC"
                            )
                            if (cursor.moveToFirst()) {
                                do {
                                    if (dataValue == null) {
                                        dataValue = cursor.getString(
                                            cursor.getColumnIndex(
                                                MasterDBHelper.NAME
                                            )
                                        )
                                    } else {
                                        dataValue = "$dataValue, ${
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        }"
                                    }

                                } while (cursor.moveToNext())
                            }
                        }
                        textSelectId.setText(value)
                        textSelect.setText(dataValue)
                    }
                }
            }else {
                if (fieldValidations.has("dbTable") && fieldValidations.getString("dbTable") != "null") {
                    val dbTable = fieldValidations.getString("dbTable")
                    if (dbTable == "state") {
                        var dataValue: String? = null
                        if (!value.isNullOrBlank() && value != "NULL") {
                            val retrievedList = JSONArray(value)

                            for (i in 0 until retrievedList.length()) {
                                var cursor = dbHelper.getAllRecordsWithCondition(
                                    MasterDBHelper.STATE_MASTER_TABLE_NAME,
                                    "${MasterDBHelper.ID}=${
                                        retrievedList.get(i).toString().toInt()
                                    }  ORDER BY ${MasterDBHelper.NAME} ASC"
                                )
                                if (cursor.moveToFirst()) {
                                    do {
                                        if (dataValue == null) {
                                            dataValue = cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        } else {
                                            dataValue = "$dataValue, ${
                                                cursor.getString(
                                                    cursor.getColumnIndex(
                                                        MasterDBHelper.NAME
                                                    )
                                                )
                                            }"
                                        }

                                    } while (cursor.moveToNext())
                                }
                            }
                            textSelectId.setText(value)
                            textSelect.setText(dataValue)
                        }

                    } else if (dbTable == "district") {
                        var dataValue: String? = null
                        if (!value.isNullOrBlank() && value != "NULL") {
                            val retrievedList = JSONArray(value)

                            for (i in 0 until retrievedList.length()) {
                                var cursor = dbHelper.getAllRecordsWithCondition(
                                    MasterDBHelper.DISTRICT_MASTER_TABLE_NAME,
                                    "${MasterDBHelper.ID}=${
                                        retrievedList.get(i).toString().toInt()
                                    }  ORDER BY ${MasterDBHelper.NAME} ASC"
                                )
                                if (cursor.moveToFirst()) {
                                    do {
                                        if (dataValue == null) {
                                            dataValue = cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        } else {
                                            dataValue = "$dataValue, ${
                                                cursor.getString(
                                                    cursor.getColumnIndex(
                                                        MasterDBHelper.NAME
                                                    )
                                                )
                                            }"
                                        }

                                    } while (cursor.moveToNext())
                                }
                            }
                            textSelectId.setText(value)
                            textSelect.setText(dataValue)
                        }
                    } else if (dbTable == "city") {
                        var dataValue: String? = null
                        if (!value.isNullOrBlank() && value != "NULL") {
                            val retrievedList = JSONArray(value)

                            for (i in 0 until retrievedList.length()) {
                                var cursor = dbHelper.getAllRecordsWithCondition(
                                    MasterDBHelper.CITY_MASTER_TABLE_NAME,
                                    "${MasterDBHelper.ID}=${
                                        retrievedList.get(i).toString().toInt()
                                    }  ORDER BY ${MasterDBHelper.NAME} ASC"
                                )
                                if (cursor.moveToFirst()) {
                                    do {
                                        if (dataValue == null) {
                                            dataValue = cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        } else {
                                            dataValue = "$dataValue, ${
                                                cursor.getString(
                                                    cursor.getColumnIndex(
                                                        MasterDBHelper.NAME
                                                    )
                                                )
                                            }"
                                        }

                                    } while (cursor.moveToNext())
                                }
                            }
                            textSelectId.setText(value)
                            textSelect.setText(dataValue)
                        }
                    } else if (dbTable == "user_state") {
                        var dataValue: String? = null
                        if (!value.isNullOrBlank() && value != "NULL") {
                            val retrievedList = JSONArray(value)

                            for (i in 0 until retrievedList.length()) {
                                var cursor = dbHelper.getAllRecordsWithCondition(
                                    MasterDBHelper.USER_STATE_TABLE_NAME,
                                    "${MasterDBHelper.ID}=${
                                        retrievedList.get(i).toString().toInt()
                                    }  ORDER BY ${MasterDBHelper.NAME} ASC"
                                )
                                if (cursor.moveToFirst()) {
                                    do {
                                        if (dataValue == null) {
                                            dataValue = cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        } else {
                                            dataValue = "$dataValue, ${
                                                cursor.getString(
                                                    cursor.getColumnIndex(
                                                        MasterDBHelper.NAME
                                                    )
                                                )
                                            }"
                                        }

                                    } while (cursor.moveToNext())
                                }
                            }
                            textSelectId.setText(value)
                            textSelect.setText(dataValue)
                        }

                    } else if (dbTable == "user_district") {
                        var dataValue: String? = null
                        if (!value.isNullOrBlank() && value != "NULL") {
                            val retrievedList = JSONArray(value)

                            for (i in 0 until retrievedList.length()) {
                                var cursor = dbHelper.getAllRecordsWithCondition(
                                    MasterDBHelper.USER_DISTRICT_TABLE_NAME,
                                    "${MasterDBHelper.ID}=${
                                        retrievedList.get(i).toString().toInt()
                                    }  ORDER BY ${MasterDBHelper.NAME} ASC"
                                )
                                if (cursor.moveToFirst()) {
                                    do {
                                        if (dataValue == null) {
                                            dataValue = cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        } else {
                                            dataValue = "$dataValue, ${
                                                cursor.getString(
                                                    cursor.getColumnIndex(
                                                        MasterDBHelper.NAME
                                                    )
                                                )
                                            }"
                                        }

                                    } while (cursor.moveToNext())
                                }
                            }
                            textSelectId.setText(value)
                            textSelect.setText(dataValue)
                        }
                    } else if (dbTable == "user_city") {
                        var dataValue: String? = null
                        if (!value.isNullOrBlank() && value != "NULL") {
                            val retrievedList = JSONArray(value)

                            for (i in 0 until retrievedList.length()) {
                                var cursor = dbHelper.getAllRecordsWithCondition(
                                    MasterDBHelper.USER_CITY_TABLE_NAME,
                                    "${MasterDBHelper.ID}=${
                                        retrievedList.get(i).toString().toInt()
                                    }  ORDER BY ${MasterDBHelper.NAME} ASC"
                                )
                                if (cursor.moveToFirst()) {
                                    do {
                                        if (dataValue == null) {
                                            dataValue = cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        } else {
                                            dataValue = "$dataValue, ${
                                                cursor.getString(
                                                    cursor.getColumnIndex(
                                                        MasterDBHelper.NAME
                                                    )
                                                )
                                            }"
                                        }

                                    } while (cursor.moveToNext())
                                }
                            }
                            textSelectId.setText(value)
                            textSelect.setText(dataValue)
                        }
                    }
                }
            }
        }


        if (valueRequired) {
            val spannable = SpannableString("${tvDropDown.text}*")
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)),
                tvDropDown.text.length,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tvDropDown.text = spannable
        }
        var flag = initialTimeCheckSkipLogic(childId)
        if (!qcRaised) {
            linearLayoutLabel.visibility = View.GONE
            textSelect.visibility = View.GONE
        } else {
            linearLayoutLabel.visibility = View.VISIBLE
            textSelect.visibility = View.VISIBLE
        }
        textSelect.setOnClickListener {
            if (fieldValidations.has("access")){
                val access = fieldValidations.getString("access")
                if (access == "state") {
                    var dataValue: String? = null
                    if (!value.isNullOrBlank() && value != "NULL") {
                        val retrievedList = JSONArray(value)

                        for (i in 0 until retrievedList.length()) {
                            var cursor = dbHelper.getAllRecordsWithCondition(
                                MasterDBHelper.STATE_MASTER_TABLE_NAME,
                                "${MasterDBHelper.ID}=${
                                    retrievedList.get(i).toString().toInt()
                                }  ORDER BY ${MasterDBHelper.NAME} ASC"
                            )
                            if (cursor.moveToFirst()) {
                                do {
                                    if (dataValue == null) {
                                        dataValue = cursor.getString(
                                            cursor.getColumnIndex(
                                                MasterDBHelper.NAME
                                            )
                                        )
                                    } else {
                                        dataValue = "$dataValue, ${
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        }"
                                    }

                                } while (cursor.moveToNext())
                            }
                        }
                        textSelectId.setText(value)
                        textSelect.setText(dataValue)
                    }

                } else if (access == "district") {
                    var dataValue: String? = null
                    if (!value.isNullOrBlank() && value != "NULL") {
                        val retrievedList = JSONArray(value)

                        for (i in 0 until retrievedList.length()) {
                            var cursor = dbHelper.getAllRecordsWithCondition(
                                MasterDBHelper.USER_DISTRICT_TABLE_NAME,
                                "${MasterDBHelper.ID}=${
                                    retrievedList.get(i).toString().toInt()
                                }  ORDER BY ${MasterDBHelper.NAME} ASC"
                            )
                            if (cursor.moveToFirst()) {
                                do {
                                    if (dataValue == null) {
                                        dataValue = cursor.getString(
                                            cursor.getColumnIndex(
                                                MasterDBHelper.NAME
                                            )
                                        )
                                    } else {
                                        dataValue = "$dataValue, ${
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        }"
                                    }

                                } while (cursor.moveToNext())
                            }
                        }
                        textSelectId.setText(value)
                        textSelect.setText(dataValue)
                    }
                } else if (access == "city") {
                    var dataValue: String? = null
                    if (!value.isNullOrBlank() && value != "NULL") {
                        val retrievedList = JSONArray(value)

                        for (i in 0 until retrievedList.length()) {
                            var cursor = dbHelper.getAllRecordsWithCondition(
                                MasterDBHelper.USER_CITY_TABLE_NAME,
                                "${MasterDBHelper.ID}=${
                                    retrievedList.get(i).toString().toInt()
                                }  ORDER BY ${MasterDBHelper.NAME} ASC"
                            )
                            if (cursor.moveToFirst()) {
                                do {
                                    if (dataValue == null) {
                                        dataValue = cursor.getString(
                                            cursor.getColumnIndex(
                                                MasterDBHelper.NAME
                                            )
                                        )
                                    } else {
                                        dataValue = "$dataValue, ${
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                        }"
                                    }

                                } while (cursor.moveToNext())
                            }
                        }
                        textSelectId.setText(value)
                        textSelect.setText(dataValue)
                    }
                }
            }else {
                if (fieldValidations.has("dbTable") && fieldValidations.getString("dbTable") != "null") {
                    options.clear()
                    val dbTable = fieldValidations.getString("dbTable")
                    if (dbTable == "state") {
//                    Log.d(TAG, "dropDown: $childId")
                        var cursor = dbHelper.getAllRecordsWithCondition(
                            MasterDBHelper.STATE_MASTER_TABLE_NAME,
                            "${MasterDBHelper.ID}!=0 ORDER BY ${MasterDBHelper.NAME} ASC"
                        )
                        if (cursor.moveToFirst()) {
                            do {
                                options[cursor.getInt(cursor.getColumnIndex(MasterDBHelper.ID))] =
                                    cursor.getString(
                                        cursor.getColumnIndex(
                                            MasterDBHelper.NAME
                                        )
                                    )
                            } while (cursor.moveToNext())
                        }
                        /*QCFormDataActivity.statesList.forEachIndexed { index, state ->
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
                            dropDownError,
                            textSelectId.text.toString()
                        )
                    } else if (dbTable == "district") {
                        var stateSelectedId = 0
                        if (relativeOptionsLogic != null) {
//                        val stateValue = getValue(relativeOptionsLogic)
                            var stateValue = ""
                            getValue(relativeOptionsLogic) { it ->
                                stateValue = it
                            }
                            Log.d(TAG, "dropDown: $relativeOptionsLogic + $stateValue")
                            if (stateValue != "" && stateValue != null) {
                                var jsonArr = JSONArray(stateValue)
                                stateSelectedId = jsonArr.getInt(jsonArr.length() - 1)
//                            dataArr.add(jsonArr.getInt(jsonArr.length()-1))
                            }
                            if (stateSelectedId != 0) {
                                var cursor = dbHelper.getAllRecordsWithCondition(
                                    MasterDBHelper.DISTRICT_MASTER_TABLE_NAME,
                                    "${MasterDBHelper.STATE_ID}=$stateSelectedId  ORDER BY ${MasterDBHelper.NAME} ASC"
                                )
                                if (cursor.moveToFirst()) {
                                    do {
                                        options[cursor.getInt(cursor.getColumnIndex(MasterDBHelper.ID))] =
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                    } while (cursor.moveToNext())
                                    Log.d(TAG, "dropDown: $options")
                                    showDropDownDialog(
                                        multiSelect,
                                        options,
                                        tableName,
                                        childId,
                                        textSelect,
                                        placeholder,
                                        textSelectId,
                                        valueRequired,
                                        dropDownError,
                                        textSelectId.text.toString()
                                    )
                                }

                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "First select state",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    } else if (dbTable == "city") {
                        val dataArr = ArrayList<Int>()
                        var districtSelectedId = 0
                        if (relativeOptionsLogic != null) {
//                        val stateValue = getValue(relativeOptionsLogic)
                            var stateValue = ""
                            getValue(relativeOptionsLogic) { it ->
                                stateValue = it
                            }
                            if (stateValue != "" && stateValue != null) {
                                var jsonArr = JSONArray(stateValue)
                                districtSelectedId = jsonArr.getInt(jsonArr.length() - 1)
//                            dataArr.add(jsonArr.getInt(jsonArr.length()-1))
//                            dataArr.add(stateValue.toInt())
                            }
                            if (districtSelectedId != 0) {
                                var cursor = dbHelper.getAllRecordsWithCondition(
                                    MasterDBHelper.CITY_MASTER_TABLE_NAME,
                                    "${MasterDBHelper.DISTRICT_ID}=$districtSelectedId  ORDER BY ${MasterDBHelper.NAME} ASC"
                                )
                                if (cursor.moveToFirst()) {
                                    do {
                                        options[cursor.getInt(cursor.getColumnIndex(MasterDBHelper.ID))] =
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                    } while (cursor.moveToNext())
                                    showDropDownDialog(
                                        multiSelect,
                                        options,
                                        tableName,
                                        childId,
                                        textSelect,
                                        placeholder,
                                        textSelectId,
                                        valueRequired,
                                        dropDownError,
                                        textSelectId.text.toString()
                                    )
                                }

                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "First select district",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        }

                    } else if (dbTable == "user_state") {
//                    Log.d(TAG, "dropDown: $childId")
                        var cursor = dbHelper.getAllRecordsWithCondition(
                            MasterDBHelper.USER_STATE_TABLE_NAME,
                            "${MasterDBHelper.ID}!=0 ORDER BY ${MasterDBHelper.NAME} ASC"
                        )
                        if (cursor.moveToFirst()) {
                            do {
                                options[cursor.getInt(cursor.getColumnIndex(MasterDBHelper.ID))] =
                                    cursor.getString(
                                        cursor.getColumnIndex(
                                            MasterDBHelper.NAME
                                        )
                                    )
                            } while (cursor.moveToNext())
                        }
                        /*QCFormDataActivity.statesList.forEachIndexed { index, state ->
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
                            dropDownError,
                            textSelectId.text.toString()
                        )
                    } else if (dbTable == "user_district") {
                        var stateSelectedId = 0
                        if (relativeOptionsLogic != null) {
//                        val stateValue = getValue(relativeOptionsLogic)
                            var stateValue = ""
                            getValue(relativeOptionsLogic) { it ->
                                stateValue = it
                            }
                            Log.d(TAG, "dropDown: $relativeOptionsLogic + $stateValue")
                            if (stateValue != "" && stateValue != null) {
                                var jsonArr = JSONArray(stateValue)
                                stateSelectedId = jsonArr.getInt(jsonArr.length() - 1)
//                            dataArr.add(jsonArr.getInt(jsonArr.length()-1))
                            }
                            if (stateSelectedId != 0) {
                                var cursor = dbHelper.getAllRecordsWithCondition(
                                    MasterDBHelper.USER_DISTRICT_TABLE_NAME,
                                    "${MasterDBHelper.STATE_ID}=$stateSelectedId  ORDER BY ${MasterDBHelper.NAME} ASC"
                                )
                                if (cursor.moveToFirst()) {
                                    do {
                                        options[cursor.getInt(cursor.getColumnIndex(MasterDBHelper.ID))] =
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                    } while (cursor.moveToNext())
                                    Log.d(TAG, "dropDown: $options")
                                    showDropDownDialog(
                                        multiSelect,
                                        options,
                                        tableName,
                                        childId,
                                        textSelect,
                                        placeholder,
                                        textSelectId,
                                        valueRequired,
                                        dropDownError,
                                        textSelectId.text.toString()
                                    )
                                }

                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "First select state",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    } else if (dbTable == "user_city") {
                        val dataArr = ArrayList<Int>()
                        var districtSelectedId = 0
                        if (relativeOptionsLogic != null) {
//                        val stateValue = getValue(relativeOptionsLogic)
                            var stateValue = ""
                            getValue(relativeOptionsLogic) { it ->
                                stateValue = it
                            }
                            if (stateValue != "" && stateValue != null) {
                                var jsonArr = JSONArray(stateValue)
                                districtSelectedId = jsonArr.getInt(jsonArr.length() - 1)
//                            dataArr.add(jsonArr.getInt(jsonArr.length()-1))
//                            dataArr.add(stateValue.toInt())
                            }
                            if (districtSelectedId != 0) {
                                var cursor = dbHelper.getAllRecordsWithCondition(
                                    MasterDBHelper.USER_CITY_TABLE_NAME,
                                    "${MasterDBHelper.DISTRICT_ID}=$districtSelectedId  ORDER BY ${MasterDBHelper.NAME} ASC"
                                )
                                if (cursor.moveToFirst()) {
                                    do {
                                        options[cursor.getInt(cursor.getColumnIndex(MasterDBHelper.ID))] =
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    MasterDBHelper.NAME
                                                )
                                            )
                                    } while (cursor.moveToNext())
                                    showDropDownDialog(
                                        multiSelect,
                                        options,
                                        tableName,
                                        childId,
                                        textSelect,
                                        placeholder,
                                        textSelectId,
                                        valueRequired,
                                        dropDownError,
                                        textSelectId.text.toString()
                                    )
                                }

                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "First select district",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


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
                        dropDownError,
                        textSelectId.text.toString(),
                        customOptionObject = customOptionObject
                    )
                }
            }

        }
    }


    private fun showDropDownDialog(
        multiSelect: Boolean,
        options: Map<Int, String>,
        tableName: String,
        childId: String,
        textSelect: EditText,
        placeholder: String,
        textSelectId: EditText,
        valueRequired: Boolean,
        dropDownError: TextView,
        valueMain: String,
        customOptionObject: JSONObject? = null
    ) {

        val optionsDropDown = mutableMapOf<Int, String>()

        for ((i, value)in options) {
            if (customOptionObject != null) {
                var cusArr = customOptionObject.getJSONArray(value)
                var customObject = cusArr.getJSONObject(0)
                var localTargetId = customObject.getString("localTargetId")
                var valueIn = customObject.getJSONArray("valueIn")

                var data = ""
                getValue(localTargetId){
                    data = it
                }
                if (data != ""){
                    val valueArray: List<Int> = Gson().fromJson(data, Array<Int>::class.java).toList()
                    for (j in 0 until valueArray.size){
                        var x = valueArray.get(j)
                        for (m in 0 until valueIn.length()) {
                            val optionCustomName = valueIn.getString(m)
                            if (x.toString() == optionCustomName) {
                                optionsDropDown[i] = value
                            }
                        }
                    }
                }else{
                    optionsDropDown[i] = value
                }
            } else {
                optionsDropDown[i] = value
            }
        }

        var value = valueMain
        val selectedData = BooleanArray(optionsDropDown.size)
        val builder = AlertDialog.Builder(requireContext())
        val titleView = TextView(requireContext())
        titleView.text = placeholder
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)
        titleView.typeface = medium
        titleView.setTextColor(Color.BLACK)
        titleView.gravity = Gravity.CENTER
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 10, 0, 10)
        titleView.layoutParams = layoutParams
        builder.setCustomTitle(titleView)
        builder.setCancelable(false)
        var selectedIndex = 0

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_single_choice,
            optionsDropDown.values.toTypedArray()
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as CheckedTextView
                view.typeface = regular // Apply font family
                return view
            }
        }

        if (multiSelect) {
            showDropDownMultiSelect(optionsDropDown, selectedData, value)
            builder.setMultiChoiceItems(
                optionsDropDown.values.toTypedArray(),
                selectedData
            ) { _, which, isChecked ->
                selectedData[which] = isChecked
            }
        } else {
            var selectedValueKey = 0
            if (value.isNotEmpty()) {
                if (isJsonArray(value)) {
                    var valueArr = JSONArray(value)
                    selectedValueKey = valueArr.getInt(valueArr.length() - 1)
                        ?: 0
                } else {
                    selectedValueKey = value?.takeIf { !it.isNullOrBlank() }?.toInt()
                        ?: 0
                }
            }
            optionsDropDown.keys.forEachIndexed { index, s ->
                if (selectedValueKey == s) {
                    selectedIndex = index
                }
            }
            builder.setSingleChoiceItems(optionsDropDown.values.toTypedArray(), selectedIndex) { _, which ->
                selectedIndex = which
            }
        }

        builder.setPositiveButton("OK") { _, _ ->
            val selectedItems = mutableListOf<String>()
            val selectedItemsId = mutableListOf<Int>()

            if (multiSelect) {
                for ((index, value) in optionsDropDown.values.withIndex()) {
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
                    selectedItems.add(optionsDropDown.values.elementAt(selectedIndex))
                    selectedItemsId.add(optionsDropDown.keys.elementAt(selectedIndex))
                    //   }
                }

//                SectionFragment.demoMap.put(childId, selectedItemsId.toString())
                textSelect.setText(selectedItems.joinToString(", "))
                textSelectId.setText(selectedItemsId.joinToString(", "))
            }
            val selectedValue = selectedItems.joinToString(", ")
            var jsonArray = JSONArray()
            selectedItemsId.forEachIndexed { index, item ->
                jsonArray.put(item)
            }
            saveValue(childId, jsonArray)
            value = jsonArray.toString()
            checkSkipLogic(childId, textSelectId.text.toString())


//            checkSkipLogic(childId, textSelectId.text.toString())
//            Log.d(TAG, "showDropDownDialog: JSON="+AddFormDataActivity.formJsonData)

            dropDownCheckValidation(
                valueRequired,
                tableName,
                childId,
                textSelect,
                dropDownError,
                value
            )
        }
        builder.setNegativeButton("Cancel") { _, _ ->
            dropDownCheckValidation(
                valueRequired,
                tableName,
                childId,
                textSelect,
                dropDownError,
                value
            )

        }
        val dialog = builder.create()

        dialog.setOnShowListener {
            val alertDialog = it as AlertDialog
            val listView = alertDialog.listView
            listView?.let {
                for (i in 0 until it.childCount) {
                    val textView = it.getChildAt(i) as TextView
                    textView.typeface = regular
                }
            }
        }

        dialog.show()


    }

    private fun checkSkipLogic(childId: String, value: String, isVisibleLogic: Boolean = true) {
        if (QCFormDataActivity.allIds.contains(childId)) {
            if (checkIfValueMatched(QCFormDataActivity.formSkipLogics, childId, value)) {
                //Log.d(TAG, "Change detected in: $childId")
                computeFlagNRefreshSL()
                /*if (isVisibleLogic)
                    updateFieldVisibility()*/
                //  Log.d(TAG,"Flag computation and field visibility updated!")
            }
        }
        checkRelativeOptionsLogic(childId)
    }

    private fun updateFieldVisibility() {
        QCFormDataActivity.formSkipLogics.forEach { item ->
            manageViewVisibility(item.skipLogicQ!!, item.flag!!)
        }
    }

    private fun manageViewVisibility(skipLogicQ: String, flag: Boolean) {
        val viewLabel = linearLayoutSection.findViewWithTag<View>("label_${skipLogicQ}")
        val viewlinearLayoutLabel =
            linearLayoutSection.findViewWithTag<LinearLayout>("linerlayout_label_${skipLogicQ}")
        val viewError = linearLayoutSection.findViewWithTag<View>("error_${skipLogicQ}")
        val viewPlaceholder =
            linearLayoutSection.findViewWithTag<View>("placeholder_${skipLogicQ}")
        val viewRadio = linearLayoutSection.findViewWithTag<View>("radio_${skipLogicQ}")
        val viewImage = linearLayoutSection.findViewWithTag<View>("image_${skipLogicQ}")
        val viewButton = linearLayoutSection.findViewWithTag<View>("button_${skipLogicQ}")
        val viewLinearLayout =
            linearLayoutSection.findViewWithTag<View>("linearLayout_${skipLogicQ}")
        val viewButtonAddable =
            linearLayoutSection.findViewWithTag<View>("button_addable_${skipLogicQ}")
        val viewLinearLayoutAddable =
            linearLayoutSection.findViewWithTag<View>("linearlayout_addable_${skipLogicQ}")
        if (flag == true) {
            if (viewLabel != null) {
                viewLabel.visibility = View.GONE
            }
            if (viewlinearLayoutLabel != null) {
                viewlinearLayoutLabel.visibility = View.GONE
            }
            if (viewError != null) {
                viewError.visibility = View.GONE
            }
            if (viewPlaceholder != null) {
                viewPlaceholder.visibility = View.GONE
            }
            if (viewRadio != null) {
                viewRadio.visibility = View.GONE
            }
            if (viewImage != null) {
                viewImage.visibility = View.GONE
            }
            if (viewButton != null) {
                viewButton.visibility = View.GONE
            }
            if (viewLinearLayout != null) {
                viewLinearLayout.visibility = View.GONE
            }
            if (viewButtonAddable != null) {
                viewButtonAddable.visibility = View.GONE
            }
            if (viewLinearLayoutAddable != null) {
                viewLinearLayoutAddable.visibility = View.GONE
            }
        } else {
            if (viewLabel != null) {
                viewLabel.visibility = View.VISIBLE
            }
            if (viewlinearLayoutLabel != null) {
                viewlinearLayoutLabel.visibility = View.VISIBLE
            }
            if (viewPlaceholder != null) {
                viewPlaceholder.visibility = View.VISIBLE
            }
            if (viewRadio != null) {
                viewRadio.visibility = View.VISIBLE
            }
            if (viewImage != null) {
                viewImage.visibility = View.VISIBLE
            }
            if (viewButton != null) {
                viewButton.visibility = View.VISIBLE
            }
            if (viewLinearLayout != null) {
                viewLinearLayout.visibility = View.VISIBLE
            }
            if (viewButtonAddable != null) {
                viewButtonAddable.visibility = View.VISIBLE
            }
            if (viewLinearLayoutAddable != null) {
                viewLinearLayoutAddable.visibility = View.VISIBLE
            }
        }
    }

    private fun checkRelativeOptionsLogic(childId: String) {
        val jsonObjectOld = JSONObject(QCFormDataActivity.formJsonData)

        val dataArray = jsonObjectOld.getJSONArray("data")

        val sectionLength = dataArray.length()

        for (i in 0 until sectionLength) {
            val sectionObject = dataArray.getJSONObject(i)
            checkSectionForRelativeOptionsLogic(sectionObject, childId)
        }

        QCFormDataActivity.formJsonData = jsonObjectOld.toString()
    }

    private fun checkSectionForRelativeOptionsLogic(
        sectionObject: JSONObject,
        childId: String
    ) {

        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            if (childObject.getString("type") == "SECTION") {
                checkSectionForRelativeOptionsLogic(childObject, childId)
            } else {

                if (childObject.has("relativeOptionsLogic")) {
                    val columnNm = childObject.getString("id")
                    val relativeOptionsLogicColumnNm = childObject.getString("relativeOptionsLogic")

                    if (childId == relativeOptionsLogicColumnNm) {
//                    childObject.put("value", checkValue)
                        manageViewVisibility(columnNm, false)
                    }
                }
            }
        }

    }

    fun saveValueAny(childId: String, checkValue: MutableMap<Int, MutableMap<Int, Any>>) {
        val jsonObjectOld = JSONObject(QCFormDataActivity.formJsonData)

        val dataArray = jsonObjectOld.getJSONArray("data")

        val sectionLength = dataArray.length()

        for (i in 0 until sectionLength) {
            val sectionObject = dataArray.getJSONObject(i)
            checkSectionForSaveValueAny(sectionObject, childId, checkValue)
        }

        QCFormDataActivity.formJsonData = jsonObjectOld.toString()
    }

    private fun checkSectionForSaveValueAny(
        sectionObject: JSONObject,
        childId: String,
        checkValue: MutableMap<Int, MutableMap<Int, Any>>
    ) {

        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            if (childObject.getString("type") == "SECTION") {
                checkSectionForSaveValueAny(childObject, childId, checkValue)
            } else {
                val columnNm = childObject.getString("id")

                if (childId == columnNm) {
                    val jsonArray = JSONArray()
                    for ((_, map) in checkValue.entries) {
                        val jsonObject = JSONObject()
                        for ((key, value) in map) {
                            jsonObject.put(key.toString(), value)
                        }
                        jsonArray.put(jsonObject)
                    }
                    // childObject.put("value", checkValue)
                    childObject.put("value", jsonArray)
                }
            }
        }

    }

    private fun saveValueRelativeOption(childId: String, checkValue: MutableMap<String, String>) {
        val jsonObjectOld = JSONObject(QCFormDataActivity.formJsonData)

        val dataArray = jsonObjectOld.getJSONArray("data")

        val sectionLength = dataArray.length()

        for (i in 0 until sectionLength) {
            val sectionObject = dataArray.getJSONObject(i)
            checkSectionForSaveValueRelativeOption(sectionObject, childId, checkValue)
        }

        QCFormDataActivity.formJsonData = jsonObjectOld.toString()
    }


    private fun checkSectionForSaveValueRelativeOption(
        sectionObject: JSONObject,
        childId: String,
        checkValue: MutableMap<String, String>
    ) {
        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            if (childObject.getString("type") == "SECTION") {
                checkSectionForSaveValueRelativeOption(childObject, childId, checkValue)
            } else {
                val columnNm = childObject.getString("id")

                if (childId == columnNm) {
                    val jsonArray = JSONArray()
                    checkValue.forEach {
                        jsonArray.put(it.value)
                        /* if (value == null){
                             value = it.value
                         }else{
                             value = "$value, " + it.value
                         }*/
                    }
                    childObject.put("value", jsonArray)
                }
            }
        }

    }

    private fun saveQCResolve(childId: String, checkValue: Boolean) {
        val jsonObjectOld = JSONObject(QCFormDataActivity.formJsonData)

        val dataArray = jsonObjectOld.getJSONArray("data")

        val sectionLength = dataArray.length()

        for (i in 0 until sectionLength) {
            val sectionObject = dataArray.getJSONObject(i)
            checkSectionForSaveQCResolve(sectionObject, childId, checkValue)
        }

        QCFormDataActivity.formJsonData = jsonObjectOld.toString()
    }


    private fun checkSectionForSaveQCResolve(
        sectionObject: JSONObject,
        childId: String,
        checkValue: Boolean
    ) {

        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            if (childObject.getString("type") == "SECTION") {
                checkSectionForSaveQCResolve(childObject, childId, checkValue)
            } else {
                val columnNm = childObject.getString("id")

                if (childId == columnNm) {
                    childObject.put("qcResolved", checkValue)
                }
            }
        }

    }

    private fun saveValue(childId: String, checkValue: Any) {
        val jsonObjectOld = JSONObject(QCFormDataActivity.formJsonData)

        val dataArray = jsonObjectOld.getJSONArray("data")

        val sectionLength = dataArray.length()

        for (i in 0 until sectionLength) {
            val sectionObject = dataArray.getJSONObject(i)
            checkSectionForSaveValue(sectionObject, childId, checkValue)
        }

        QCFormDataActivity.formJsonData = jsonObjectOld.toString()
    }


    private fun checkSectionForSaveValue(
        sectionObject: JSONObject,
        childId: String,
        checkValue: Any
    ) {

        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            if (childObject.getString("type") == "SECTION") {
                checkSectionForSaveValue(childObject, childId, checkValue)
            } else {
                val columnNm = childObject.getString("id")

                if (childId == columnNm) {
                    childObject.put("value", checkValue)
                    childObject.put("qcInteract", true)
                }
                var relativeOptionsLogicID = ""
                if (childObject.has("relativeOptionsLogic")) {
                    relativeOptionsLogicID = childObject.getString("relativeOptionsLogic")

                    if (childId == relativeOptionsLogicID) {
                        childObject.put("value", "")
                        clearValue(columnNm)
                        manageRelativeValue(sectionObject, columnNm)
                    }
                }
            }
        }

    }

    private fun manageRelativeValue(sectionObject: JSONObject, childId: String) {
        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            if (childObject.getString("type") == "SECTION") {
                manageRelativeValue(childObject, childId)
            } else {
                val columnNm = childObject.getString("id")

                var relativeOptionsLogicID = ""
                if (childObject.has("relativeOptionsLogic")) {
                    relativeOptionsLogicID = childObject.getString("relativeOptionsLogic")

                    if (childId == relativeOptionsLogicID) {
                        childObject.put("value", "")
                        clearValue(columnNm)
                        manageRelativeValue(sectionObject, columnNm)
                    }
                }
            }
        }
    }

    private fun clearValue(id: String) {
        val viewPlaceholder =
            linearLayoutSection.findViewWithTag<EditText>("placeholder_${id}")
        if (viewPlaceholder != null) {
            viewPlaceholder.setText("")
        }
    }

    private fun getValue(childId: String, onGetValue: (String) -> Unit) {
        val jsonObjectOld = JSONObject(QCFormDataActivity.formJsonData)

        val dataArray = jsonObjectOld.getJSONArray("data")

        val sectionLength = dataArray.length()

        for (i in 0 until sectionLength) {
            val sectionObject = dataArray.getJSONObject(i)
            checkSectionForGetValue(sectionObject, childId) { it ->
                onGetValue.invoke(it)
            }
        }
    }

    private fun checkSectionForGetValue(
        sectionObject: JSONObject,
        childId: String,
        onGetValue: (String) -> Unit
    ) {
        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            if (childObject.getString("type") == "SECTION") {
                checkSectionForGetValue(childObject, childId) { it ->
                    onGetValue.invoke(it)
                }
            } else {
                val columnNm = childObject.getString("id")
                if (childId == columnNm) {
                    if (childObject.has("value")) {
                        onGetValue.invoke(childObject.getString("value"))
                    }
                }
            }
        }
    }

    lateinit var jsonRequestArray: ArrayList<JSONRequest>
    private fun getValueForRequest() {
        jsonRequestArray = ArrayList()
        val jsonObjectOld = JSONObject(QCFormDataActivity.formJsonData)

        val dataArray = jsonObjectOld.getJSONArray("data")

        val sectionLength = dataArray.length()

        for (i in 0 until sectionLength) {
            val sectionObject = dataArray.getJSONObject(i)
            checkSectionForGetValueForRequest(sectionObject)
        }


    }


    private fun checkSectionForGetValueForRequest(
        sectionObject: JSONObject
    ) {

        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            if (childObject.getString("type") == "SECTION") {
                checkSectionForGetValueForRequest(childObject)
            } else {
                val id = childObject.getString("id")
                var value: Any? = null
                if (childObject.has("value")) {
                    value = childObject.get("value")
                }

                /* var jsonRequest=JSONObject()
                 jsonRequest.put("id",id)
                 if (value!=null){
                     if (isJsonArray(value.toString())){
                         jsonRequest.put("value", value.toString())
                     }else{
                         jsonRequest.put("value", value)
                     }
                 }else{
                     jsonRequest.put("value", "")
                 }

                 jsonRequest.put("flag",initialTimeCheckSkipLogic(id))
                 jsonRequestArray.put(jsonRequest)*/

                var jsonRequest = JSONRequest()
                jsonRequest.id = id
                if (value != null) {
                    if (isJsonArray(value.toString())) {
                        jsonRequest.value = value.toString()
                    } else {
                        jsonRequest.value = value
                    }
                } else {
                    jsonRequest.value = ""
                }

                jsonRequest.flag = initialTimeCheckSkipLogic(id)
                jsonRequestArray.add(jsonRequest)
            }
        }
    }
    /* private fun checkSkipLogic(childId: String, checkValue: String,isCheckValue : Boolean=true) {
         val jsonObjectOld = JSONObject(AddFormDataActivity.formJsonData)

         val dataArray = jsonObjectOld.getJSONArray("data")

         val sectionLength = dataArray.length()

         for (i in 0 until sectionLength) {
             val sectionObject = dataArray.getJSONObject(i)
             checkSectionSkipLogic(sectionObject, childId, checkValue,isCheckValue)
 //            val sectionTitle = sectionObject.getString("title")
 //            Log.d(TAG, "checkSectionSkipLogic: "+sectionTitle.toString())
 //            Log.d(TAG, "checkSectionSkipLogic: "+sectionObject.toString())
         }

         AddFormDataActivity.formJsonData = jsonObjectOld.toString()
     }

     private fun checkSectionSkipLogic(
         sectionObject: JSONObject,
         childId: String,
         checkValue: String,
         isCheckValue : Boolean=true
     ) {

         val childrenArray = sectionObject.getJSONArray("children")
         for (j in 0 until childrenArray.length()) {
             val childObject = childrenArray.getJSONObject(j)
             if (childObject.getString("type") == "SECTION") {
                 checkSectionSkipLogic(childObject, childId, checkValue,isCheckValue)
             } else {
                 val viewId = childObject.getString("id")
                 val viewType = childObject.getString("type")
                 var skipLogic: JSONArray? = null
                 if (childObject.has("skipLogic")) {
                     skipLogic = childObject.getJSONArray("skipLogic")
                 }
                 if (skipLogic != null) {
                     for (i in 0 until skipLogic.length()) {
                         val skipLogicObject = skipLogic.getJSONObject(i)
                         val relation = skipLogicObject.getString("relation")
                         val flag = skipLogicObject.getBoolean("flag")
                         var flagOr = false
                         var flagAnd = true
                         var viewskipLogicQ: String = ""
                         val skipLogicDataArray = skipLogicObject.getJSONArray("data")
                         for (k in 0 until skipLogicDataArray.length()) {
                             val skipLogicDataObject = skipLogicDataArray.getJSONObject(k)
                             val skipLogicQ = skipLogicDataObject.getString("skipLogicQ")
                             val skipLogicVal = skipLogicDataObject.getString("skipLogicVal")
                             val flag = skipLogicDataObject.getBoolean("flag")

                             if (skipLogicQ == childId) {
                                 viewskipLogicQ = skipLogicQ
                             }
                             if (isCheckValue) {
                                 if (flag == true && skipLogicQ != childId) {
 //                                Log.d(TAG, "checkSectionSkipLogic: TRUE=" + childId)
                                     if (relation == "or") {
                                         flagOr = true
                                     }
                                 } else {
                                     if (skipLogicQ == childId && checkValue.contains(skipLogicVal)) {
 //                                    Log.d(TAG, "checkSectionSkipLogic: TRUE=" + childId + "==" + skipLogicVal)
                                         skipLogicDataObject.put("flag", true)
                                         if (relation == "or") {
                                             flagOr = true
                                         }
                                     } else {
 //                                    Log.d(TAG, "checkSectionSkipLogic: FALSE=" + childId + "==" + skipLogicQ + "==" + skipLogicVal)
                                         skipLogicDataObject.put("flag", false)
                                         if (relation == "and") {
                                             flagAnd = false
                                         }
                                     }
                                 }
                             }else{
                                 if (skipLogicQ==childId){
                                     Log.d(TAG, "checkSectionSkipLogic: DEPEND=" + childId )
                                      flagOr = true
                                      flagAnd = true
                                     break
                                 }
                             }
                         }
                         val viewLabel = linearLayoutSection.findViewWithTag<View>("label_$viewId")
                         val viewPlaceholder =
                             linearLayoutSection.findViewWithTag<View>("placeholder_$viewId")
                         val viewRadio = linearLayoutSection.findViewWithTag<View>("radio_$viewId")
                         val viewImage = linearLayoutSection.findViewWithTag<View>("image_$viewId")
                         val viewButton = linearLayoutSection.findViewWithTag<View>("button_$viewId")
                         val viewButtonAddable =
                             linearLayoutSection.findViewWithTag<View>("button_addable")
                         val viewLinearLayoutAddable =
                             linearLayoutSection.findViewWithTag<View>("linearlayout_addable")
                         if (childId == viewskipLogicQ) {

                             if (relation == "or" && flagOr) {

                                 if (viewLabel != null) {
                                     viewLabel.visibility = View.GONE
                                 }
                                 if (viewPlaceholder != null) {
                                     viewPlaceholder.visibility = View.GONE
                                 }
                                 if (viewRadio != null) {
                                     viewRadio.visibility = View.GONE
                                 }
                                 if (viewImage != null) {
                                     viewImage.visibility = View.GONE
                                 }
                                 if (viewButton != null) {
                                     viewButton.visibility = View.GONE
                                 }
                                 if (viewButtonAddable != null) {
                                     viewButtonAddable.visibility = View.GONE
                                 }
                                 if (viewLinearLayoutAddable != null) {
                                     viewLinearLayoutAddable.visibility = View.GONE
                                 }
                                 skipLogicObject.put("flag", true)
                                 checkSkipLogic(viewId, "",false)
                             } else if (relation == "and" && flagAnd) {

                                 if (viewLabel != null) {
                                     viewLabel.visibility = View.GONE
                                 }
                                 if (viewPlaceholder != null) {
                                     viewPlaceholder.visibility = View.GONE
                                 }
                                 if (viewRadio != null) {
                                     viewRadio.visibility = View.GONE
                                 }
                                 if (viewImage != null) {
                                     viewImage.visibility = View.GONE
                                 }
                                 if (viewButton != null) {
                                     viewButton.visibility = View.GONE
                                 }
                                 if (viewButtonAddable != null) {
                                     viewButtonAddable.visibility = View.GONE
                                 }
                                 if (viewLinearLayoutAddable != null) {
                                     viewLinearLayoutAddable.visibility = View.GONE
                                 }
                                 skipLogicObject.put("flag", true)
                                 checkSkipLogic(viewId, "",false)
                             } else {

                                 if (viewLabel != null) {
                                     viewLabel.visibility = View.VISIBLE
                                 }
                                 if (viewPlaceholder != null) {
                                     viewPlaceholder.visibility = View.VISIBLE
                                 }
                                 if (viewRadio != null) {
                                     viewRadio.visibility = View.VISIBLE
                                 }
                                 if (viewImage != null) {
                                     viewImage.visibility = View.VISIBLE
                                 }
                                 if (viewButton != null) {
                                     viewButton.visibility = View.VISIBLE
                                 }
                                 if (viewButtonAddable != null) {
                                     viewButtonAddable.visibility = View.VISIBLE
                                 }
                                 if (viewLinearLayoutAddable != null) {
                                     viewLinearLayoutAddable.visibility = View.VISIBLE
                                 }
                                 skipLogicObject.put("flag", false)
                             }
                         }
                     }
                 }
             }
         }

     }*/

    @SuppressLint("Range")
    private fun dropDownCheckValidation(
        valueRequired: Boolean,
        tableName: String,
        childId: String,
        textSelect: EditText,
        dropDownError: TextView,
        value: String
    ) {
        if (valueRequired) {

            if (!value.isNullOrBlank() && value != "NULL") {
                textSelect.error = null
                dropDownError.visibility = View.GONE
            } else {
                textSelect.error = "Please Select Value"
                dropDownError.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("Range")
    private fun showDropDownMultiSelect(
        options: Map<Int, String>,
        selectedData: BooleanArray,
        value: String
    ) {
        if (options != null) {
            if (!value.isNullOrBlank() && value != "NULL") {
                if (value.isNotEmpty()) {
                    if (isJsonArray(value)) {
                        val valueArr = JSONArray(value)/*.split(", ")*/
//                for (arr in valueArr) {
                        for (j in 0 until valueArr.length()) {
                            selectedData[valueArr.getInt(j).toInt()] = true
                        }
                    } else {
                        val valueArr = value.split(", ")
                        for (arr in valueArr) {
                            selectedData[arr.toInt()] = true
                        }
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun buttonViewSubmit(
        bt: Button
    ) {
        bt.text = "Submit"
        bt.textSize = 16f
        bt.typeface = medium
        bt.gravity = Gravity.CENTER_HORIZONTAL
        bt.textAlignment = Button.TEXT_ALIGNMENT_CENTER
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        bt.setTextColor(textColor)
        bt.setBackgroundResource(R.color.blue)
        val layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(25, 200, 25, 100)
        bt.layoutParams = layoutParams
        if (bt.parent != null) {
            (bt.parent as? ViewGroup)?.removeView(bt)
        }
        linearLayoutSection.addView(bt)

        val validationMutableList = mutableMapOf<String, Boolean>()
        bt.setOnClickListener {
            validationMutableList.clear()
            val focus = false
            validationCheck(validationMutableList, focus)
            Log.d(TAG, "onViewCreated: $validationMutableList")
            validationTrueFalse = validationMutableList.all { it.value }
            Log.d(TAG, "onViewCreated2: $validationTrueFalse")
            if (validationTrueFalse) {
                dbHelper.updateQCTableSchema(
                    QCFormDataActivity.qvTableName,
                    subId,
                    MasterDBHelper.QCRESOLVED,
                    QCFormDataActivity.formJsonData
                )

                Toast.makeText(requireContext(), "Data saved successfully", Toast.LENGTH_LONG)
                    .show()
                requireActivity().finish()
//            Log.d(TAG, "getValueForRequest: $jsonRequestArray")
                /* val token = getPrefStringData(requireContext(), "token")
             showLoader(requireContext(), "Survey Syncing...")
             sectionViewModel.callSurveyDataSubmit(token.toString(),formId,jsonRequestArray).observe(viewLifecycleOwner) { resource ->
                 when (resource.status) {
                     Status.SUCCESS -> {
                         Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                         Log.d(TAG, "buttonViewSubmit: " + resource)
                         requireActivity().finish()
                         val data = resource.data?.body()
                         Log.d(TAG, "buttonViewSubmit BODY: " + data)
 //                        if (data != null) {
 //
 //                        }
                     }

                     Status.ERROR -> {
                         Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                         Log.d("TAG", "Error: ${resource.message}")
                     }

                     Status.LOADING -> {
                         hideLoader()
                     }
                 }
             }*/
            }
        }
    }

    private fun sectionFun(
        sectionObject: JSONObject,
        values: ContentValues
    ) {
        val childrenArray = sectionObject.getJSONArray("children")

        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            val type = childObject.getString("type")
            when (type) {

                "SECTION" -> {
                    sectionFun(childObject, values)
                }

                "ADDABLE" -> {}

                else -> {
                    var value: Any? = null
                    if (childObject.has("value")) {
                        value = childObject.get("value")
                    }
                    if (childObject.has("primaryView")) {
                        val primaryView = childObject.getBoolean("primaryView")
                        val secondaryView = childObject.getBoolean("secondaryView")
                        val viewIndex = childObject.getInt("viewIndex")
                        val id = childObject.getString("id")
                        val label = childObject.getJSONObject("properties").getString("label")

                        if (value != null) {
                            if (type == "UPLOAD_IMAGE" || type == "CAPTURE_IMAGE") {
                                val uploadImageArr = value as JSONArray
                                var uploadImageValue: String? = null
                                if (uploadImageArr.length() > 0) {
                                    for (i in 0 until uploadImageArr.length()) {
                                        if (uploadImageValue == null) {
                                            uploadImageValue =
                                                uploadImageArr.getString(i)
                                        } else {
                                            uploadImageValue =
                                                uploadImageValue + ", " + uploadImageArr.getString(i)
                                        }

                                    }
                                }
                                values.put(id, uploadImageValue)

                            } else if (type == "DROPDOWN") {
                                val dropDownArr: JSONArray = value as JSONArray
                                var dropDownValue: String? = null
                                if (dropDownArr.length() > 0) {
                                    for (i in 0 until dropDownArr.length()) {
                                        if (dropDownValue == null) {

                                            dropDownValue = dropDownArr.getInt(i).toString()
                                        } else {
                                            dropDownValue =
                                                "$dropDownValue, ${dropDownArr.getInt(i)}"
                                        }
                                        /* if (childObject.has("options")) {
                                             val optionsList = childObject.getJSONArray("options")
                                             if (optionsList.length() > 0) {
                                                 for (j in 0 until optionsList.length()) {
                                                     val id = optionsList.getJSONObject(j).getInt("id")
                                                     val value = optionsList.getJSONObject(j).getString("value")
                                                     if (dropDownArr.getInt(i) == id){

                                                     }
                                                 }
                                             }
                                         }*/
                                    }
                                }
                                values.put(id, dropDownValue)
                            } else {
                                values.put(id, value.toString())
                            }
                        }
                    }
                }

            }
        }

    }

    private fun stringToBitmap(encodedString: String): Bitmap {
        val decodedByteArray = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
    }
    // Visible Logic Functions
    private fun initialTimeCheckVisibleLogic(childId: String): Boolean {
        var fild = QCFormDataActivity.formVisibleLogics.find { it.visibleLogicQ == childId }
        if (fild != null) {
            return fild.flag == true
        } else {
            return false
        }
    }
    // Skip Logic Functions
    private fun initialTimeCheckSkipLogic(childId: String): Boolean {
        var fild = QCFormDataActivity.formSkipLogics.find { it.skipLogicQ == childId }
        if (fild != null) {
            return fild.flag == true
        } else {
            return false
        }
    }

    fun checkIfValueMatched(
        data: ArrayList<SkipLogicModel>,
        targetId: String,
        value: Any,
        matched: Boolean = false
    ): Boolean {
        var matchedResult = matched
        data.forEach { item ->
            if (!item.data.isNullOrEmpty()) {
                val valReturned = checkIfValueMatched(item.data!!, targetId, value, matchedResult)
                if (valReturned) {
                    matchedResult = true
                }
            }
            if (item.skipLogicQ == targetId) {
                //  Log.d(TAG, "Checking if value following values match or not")
                //     Log.d(TAG, "Item's value: ${item.skipLogicVal}")
                //  Log.d(TAG, "Value to be checked: $value")
                if (item.skipLogicVal == value) {
                    item.flag = true;
                    //    Log.d(TAG, "Match Found! IF");
                } else if (item.skipLogicVal == null && getFlagById(
                        item.skipLogicQ.toString(),
                        QCFormDataActivity.formSkipLogics
                    )
                ) {
                    item.flag = true;
                    Log.d(TAG, "Match Found! IF ELSE");
                } else {
                    item.flag = false;
                }
                matchedResult = true
            }
        }
        return matchedResult
    }

    fun getFlagById(targetId: String, data: ArrayList<SkipLogicModel>): Boolean {
        data.forEach { item ->
            if (item.skipLogicQ == targetId) {
                return item.flag == true;
            }
            if (!item.data.isNullOrEmpty()) {
                return this.getFlagById(targetId, item.data!!);
            }
        }
        return false
    }

    fun computeFlagNRefreshSL() {
        val previousParentFlags = QCFormDataActivity.formSkipLogics.map { item ->
            SkipLogicModel(
                flag = item.flag,
                skipLogicQ = item.skipLogicQ
            )
        }

        flagComputation(QCFormDataActivity.formSkipLogics)

        val newParentFlags = QCFormDataActivity.formSkipLogics.map { item ->
            SkipLogicModel(
                flag = item.flag,
                skipLogicQ = item.skipLogicQ
            )
        }

        for (i in QCFormDataActivity.formSkipLogics.indices) {
            if (previousParentFlags[i].flag != newParentFlags[i].flag ||
                previousParentFlags[i].skipLogicQ != newParentFlags[i].skipLogicQ
            ) {
                updateFlagById(
                    newParentFlags[i].skipLogicQ.toString(),
                    newParentFlags[i].flag == true, QCFormDataActivity.formSkipLogics
                )

                flagComputation(QCFormDataActivity.formSkipLogics)
            }
        }
    }

    fun flagComputation(data: ArrayList<SkipLogicModel>) {
        data.forEach { item ->
            val flagBeforeProcessing = item.flag
            if (item.relation == "or") {
                if (!item.data.isNullOrEmpty()) {
                    this.flagComputation(item.data!!);
                    item.flag = item.data!!.any { child -> child.flag == true };
                }
            } else if (item.relation == "and") {
                if (!item.data.isNullOrEmpty()) {
                    this.flagComputation(item.data!!);
                }
                item.flag = item.data!!.all { child -> child.flag == true };
            }
        }
    }

    fun updateFlagById(targetId: String, newFlag: Boolean, data: ArrayList<SkipLogicModel>) {
        data.forEach { item ->
            if (item.skipLogicQ == targetId) {
                item.flag = newFlag
                updateFieldVisibility()
            }
            if (!item.data.isNullOrEmpty()) {
                updateFlagById(targetId, newFlag, item.data!!)
            }
        }
    }
}