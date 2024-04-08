package com.umcbms.app.Home.FormData

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.MasterDB.MasterDBHelper.Companion.MASTER_FORM_ID
import com.umcbms.app.MasterDB.ViewArrayModel
import com.umcbms.app.R
import com.umcbms.app.api.ApiConstants
import com.umcbms.app.changeDateTimeFormat
import com.umcbms.app.isLocalFilePath
import com.umcbms.app.pathToBitmap
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

private const val TAG = "SecondaryViewActivity"

class SecondaryViewActivity : AppCompatActivity() {
    private lateinit var dbHelper: MasterDBHelper
    private lateinit var linearLayoutSecondaryView: LinearLayout
    private lateinit var linearLayoutPrimaryView: LinearLayout

    // private lateinit var tlSecondaryView: TableLayout
    private lateinit var ivBack: ImageView
    private lateinit var ivImagePrimary: CircleImageView
    private lateinit var rvSecondary: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secondary_view)

        linearLayoutSecondaryView = findViewById(R.id.linearLayoutSecondaryView)
        linearLayoutPrimaryView = findViewById(R.id.linearLayoutPrimaryView)
        // tlSecondaryView = findViewById(R.id.tlSecondaryView)
        ivBack = findViewById(R.id.ivBack)
        ivImagePrimary = findViewById(R.id.ivImagePrimary)
        rvSecondary = findViewById(R.id.rvSecondary)

        dbHelper = MasterDBHelper(this)

        ivBack.setOnClickListener {
            finish()
        }

        val id = intent.getStringExtra("id")
        if (id != null) {
            val data = getSummeryDataById(id)
            val masterFormId = data[MASTER_FORM_ID]
            var jsonString: String? = null

            jsonString = masterFormId?.let { getJsonSchemaMasterTablesByID(it) }

            val viewArray = ArrayList<ViewArrayModel>()

            for (itemValue in data) {
                val key = itemValue.key
                val value = itemValue.value

                val jsonObject = jsonString?.let { JSONObject(it) }
                val dataArray = jsonObject?.getJSONArray("data")

                if (dataArray != null) {
                    for (j in 0 until dataArray.length()) {
                        val sectionObject = dataArray.getJSONObject(j)
                        sectionFun(sectionObject, itemValue.key, viewArray, value)
                    }
                }

            }

            val sortedViewArr = viewArray.sortedBy { it.viewIndex }

            //PrimaryView
            for (arr in sortedViewArr) {
                if (arr.primaryView) {
                    if (arr.type == "DROPDOWN") {
                        val numbers = arr.value?.split(", ")?.map { it.trim() }

                        if (numbers != null) {
                            for (dropDownId in numbers) {
                                if (dropDownId != "") {
                                    var optionData: String? = null
                                    for (i in 0 until arr.options?.size!!) {
                                        if (i == dropDownId.toFloat().toInt()) {
                                            val opsVal = arr.options.get(i)
                                            if (opsVal != null) {
                                                if (optionData == null) {
                                                    optionData = opsVal
                                                } else {
                                                    optionData = "$optionData, $opsVal"
                                                }
                                            }
                                        }
                                    }
                                    if (optionData != null) {
                                        displayText(optionData)
                                    }
                                }
                            }
                        }
                    } else if (arr.type == "UPLOAD_IMAGE" || arr.type == "CAPTURE_IMAGE") {
//                        val valueArray = JSONArray(arr.value)/*.split(", ").map { it.trim() }*/
                        val valueArray = arr.value.split(", ").map { it.trim() }
                        for (imageUrl in valueArray) {
                          /*  for (j in 0..valueArray.length() - 1) {
                                var imageUrl=valueArray.getString(j)*/

                            if (imageUrl == "") {
                                ivImagePrimary.setImageResource(R.drawable.ic_select)
                            } else {
                                ivImagePrimary.visibility = View.VISIBLE
//                                if (imageUrl.contains("/storage")) {
                                    if (isLocalFilePath(imageUrl)) {
                                        val bitmapImage = pathToBitmap(imageUrl)
                                        ivImagePrimary.setImageBitmap(bitmapImage)
                                } else {
                                        Glide.with(this)
                                            .load(ApiConstants.BASE_IMAGE_URL + imageUrl)
                                            .circleCrop()
                                            .into(ivImagePrimary)
                                }
                            }
                        }
                    } else if (arr.type == "RADIO") {
                        if (arr.options != null) {
                            if (arr.value != "") {
                                val opsVal = arr.options[arr.value?.toFloat()?.toInt()]
                                if (opsVal != null) {
                                    displayText(opsVal)
                                }
                            }
                        }
                    } else if (arr.type == "NUMBERS") {
                        if (arr.value != "") {
                            displayText(arr.value?.toDouble()?.toLong().toString())
                        }
                    } else if (arr.type == "DATE") {
                        val dateValue = arr.value?.let { changeDateTimeFormat(it, "yyyy-MM-dd", "dd/MM/yyyy") }
                        if (dateValue != "") {
                                displayText(dateValue.toString())
                        }
                    } else {
                        if (arr.value != "") {
                            displayText(arr.value.toString())
                        }
                    }
                }
            }

            //SecondaryView
            rvSecondary.layoutManager = LinearLayoutManager(this)
            val adapter = SecondaryViewAdapter(this, sortedViewArr)
            rvSecondary.adapter = adapter
            /*for (arr in sortedViewArr) {
                if (arr.secondaryView) {
                    if (arr.type == "DROPDOWN") {
                        val numbers = arr.value.split(", ").map { it.trim() }

                        for (dropDownId in numbers) {
                            if (dropDownId != "") {
                                var optionData: String? = null
                                for (i in 0 until arr.options?.size!!) {
                                    if (i == dropDownId.toFloat().toInt()) {
                                        val opsVal = arr.options.get(i)
                                        if (opsVal != null) {
                                            if (optionData == null) {
                                                optionData = opsVal
                                            } else {
                                                optionData = "$optionData, $opsVal"
                                            }
                                        }
                                    }
                                }
                                if (optionData != null) {
                                    //addDropDownView(optionData, arr.label.toString())
                                } else {
                                    //addDropDownView("", arr.label.toString())
                                }
                            } else {
                                //addDropDownView("", arr.label.toString())
                            }
                        }
                    } else if (arr.type == "UPLOAD_IMAGE") {
                        val numbers = arr.value.split(", ").map { it.trim() }
                        for (imageUrl in numbers) {
                            //addImageView(imageUrl, arr.label.toString())
                        }
                    } else if (arr.type == "RADIO") {
                        if (arr.options != null) {
                            if (arr.value != "") {
                                val opsVal = arr.options[arr.value.toFloat().toInt()]
                                if (opsVal != null) {
                                    addTextView(opsVal, arr.label.toString())
                                }
                            } else {
                                addTextView("", arr.label.toString())
                            }
                        }
                    } else if (arr.type == "NUMBERS") {
                        if (arr.value != "") {
                            addTextView(
                                arr.value.toDouble().toLong().toString(),
                                arr.label.toString()
                            )
                        } else {
                            addTextView("", arr.label.toString())
                        }
                    } else if (arr.type == "DATE") {
                        val dateValue = changeDateTimeFormat(arr.value, "yyyy-MM-dd", "dd/MM/yyyy")
                        addTextView(dateValue, arr.label.toString())
                    } else {
                        addTextView(arr.value, arr.label.toString())
                    }
                }
            }*/
        }

    }

    private fun getSummeryDataById(id: String): MutableMap<String, String> {
        val cursor = dbHelper.getSingleRecord(FormDataActivity.summeryTableName, id.toInt())
        val rowData = mutableMapOf<String, String>()
        if (cursor != null && cursor.moveToFirst()) {
            val columnCount = cursor.columnCount
            val columnNames = Array<String>(columnCount) { "" }

            for (i in 0 until columnCount) {
                columnNames[i] = cursor.getColumnName(i)
            }

            for (i in 0 until columnCount) {
                val columnId = cursor.getColumnName(i)
                val data = cursor.getString(i)
                rowData[columnId] = data ?: ""
            }
        }
        return rowData
    }

    private fun getJsonSchemaMasterTablesByID(masterFormId: String): String? {
        val data = dbHelper.getFormsById(masterFormId.toInt())
        return data.formSchema
    }

    private fun sectionFun(
        sectionObject: JSONObject,
        key: String,
        viewArray: ArrayList<ViewArrayModel>,
        value: String
    ) {
        val childrenArray = sectionObject.getJSONArray("children")

        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            when (childObject.getString("type")) {

                "SECTION" -> {
                    sectionFun(childObject, key, viewArray, value)
                }

                "ADDABLE" -> {}

                else -> {
                    if (childObject.has("primaryView")) {
                        val primaryView = childObject.getBoolean("primaryView")
                        val secondaryView = childObject.getBoolean("secondaryView")
                        val viewIndex = childObject.getInt("viewIndex")
                        val type = childObject.getString("type")
                        val id = childObject.getString("id")
                        var labelView = ""
                        if (childObject.has("homeLabel")) {
                            labelView = childObject.getString("homeLabel")
                        } else {
                            labelView = childObject.getJSONObject("properties").getString("label")
                        }

                        if (key == id) {
                            val options = mutableMapOf<Int, String>()
                            options.clear()

                            if (childObject.has("options")) {
                                val optionsList = childObject.getJSONArray("options")
                                if (optionsList.length() > 0) {
                                    for (i in 0 until optionsList.length()) {
                                        val id = optionsList.getJSONObject(i).getInt("id")
                                        val value = optionsList.getJSONObject(i).getString("value")
                                        options.put(id, value)
                                    }
                                }
                            }

                            viewArray.add(
                                ViewArrayModel(
                                    id,
                                    type,
                                    labelView,
                                    viewIndex,
                                    options,
                                    primaryView,
                                    secondaryView,
                                    value
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addDropDownView(value: String, label: String) {
        val tv = TextView(this)
        tv.text = "$label:"
        tv.textSize = 18f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 3, 0, 1)
        tv.layoutParams = layoutParamsTV
        tv.typeface = ResourcesCompat.getFont(this, R.font.medium)
        linearLayoutSecondaryView.addView(tv)

        val editText = EditText(this)
        editText.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        editText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_CLASS_TEXT
        editText.maxLines = 2
        editText.textSize = 16f
        editText.typeface = ResourcesCompat.getFont(this, R.font.regular)
        editText.isVerticalScrollBarEnabled = true
        editText.isClickable = false
        editText.isFocusable = false
        editText.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        editText.setText(value)
        linearLayoutSecondaryView.addView(editText)

    }

    fun addImageView(value: String, label: String) {
        val tv = TextView(this)
        tv.text = "$label:"
        tv.textSize = 18f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 3, 0, 1)
        tv.layoutParams = layoutParamsTV
        tv.typeface = ResourcesCompat.getFont(this, R.font.medium)
        linearLayoutSecondaryView.addView(tv)

        val img = ImageView(this)
        img.id = View.generateViewId()
        if (value == "") {
            img.setImageResource(R.drawable.ic_select)
        } else {
//            if (value.contains("/storage")) {
            if (isLocalFilePath(value)) {
                val bitmapImage = pathToBitmap(value)
                Log.d(TAG, "addImageView: $bitmapImage")
                img.setImageBitmap(bitmapImage)
            } else {
                Glide.with(img.context)
                    .load(ApiConstants.BASE_IMAGE_URL + value)
                    .into(img)
            }
        }
        val layoutParams = LinearLayout.LayoutParams(
            this.resources.getDimensionPixelSize(R.dimen.image_width_150dp),
            this.resources.getDimensionPixelSize(R.dimen.image_width_150dp)
        )
        img.layoutParams = layoutParams
        linearLayoutSecondaryView.addView(img)
    }

    /*@SuppressLint("SetTextI18n")
    fun addTextView(value: String, label: String) {
        val tableRow = TableRow(this)
        tableRow.setBackgroundResource(R.drawable.table_row_border)
        tlSecondaryView.addView(tableRow)

        val tv = TextView(this)
        tv.text = "$label:"
        tv.textSize = 18f
        val layoutParamsTV = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 3, 0, 1)
        tv.layoutParams = layoutParamsTV
        tv.typeface = ResourcesCompat.getFont(this, R.font.medium)
        tableRow.addView(tv)

        val lineView = View(this)
        lineView.setBackgroundResource(R.drawable.vertical_line)
        val layoutParamsLine = TableRow.LayoutParams(1, TableRow.LayoutParams.MATCH_PARENT)
        lineView.layoutParams = layoutParamsLine
        tableRow.addView(lineView)

        val tv2 = TextView(this)
        tv2.text = value
        tv2.textSize = 16f
        val layoutParamsTV2 = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV2.setMargins(0, 2, 0, 3) // Use layoutParamsTV2 here
        tv2.layoutParams = layoutParamsTV2
        tv2.typeface = ResourcesCompat.getFont(this, R.font.regular)
        tableRow.addView(tv2)

    }*/

    @SuppressLint("SetTextI18n")
    fun addTextView(value: String, label: String) {
        val tv = TextView(this)
        tv.text = "$label:"
        tv.textSize = 18f
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 3, 0, 1)
        tv.layoutParams = layoutParamsTV
        tv.typeface = ResourcesCompat.getFont(this, R.font.medium)
        linearLayoutSecondaryView.addView(tv)

        val tv2 = TextView(this)
        tv2.text = value
        tv2.textSize = 16f
        val layoutParamsTV2 = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 2, 0, 3)
        tv2.layoutParams = layoutParamsTV2
        tv2.typeface = ResourcesCompat.getFont(this, R.font.regular)
        linearLayoutSecondaryView.addView(tv2)
    }


    private fun displayText(value: String) {
        val tv = TextView(this)
        tv.text = value
        tv.textSize = 13f
        tv.gravity = Gravity.CENTER_HORIZONTAL
        val layoutParamsTV = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        )
        layoutParamsTV.setMargins(0, 0, 0, 5)
        tv.layoutParams = layoutParamsTV
        tv.typeface = ResourcesCompat.getFont(this, R.font.regular)
        linearLayoutPrimaryView.addView(tv)
    }

}