package com.umcbms.app.Home.FormData

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.MasterDB.MasterDBHelper.Companion.MASTER_FORM_ID
import com.umcbms.app.MasterDB.MasterDBHelper.Companion.RESPONSE_ID
import com.umcbms.app.MasterDB.MasterDBHelper.Companion.STATUS
import com.umcbms.app.MasterDB.MasterDBHelper.Companion.SYNCED
import com.umcbms.app.MasterDB.ViewArrayModel
import com.umcbms.app.R
import com.umcbms.app.api.ApiConstants
import com.umcbms.app.changeDateTimeFormat
import com.umcbms.app.isLocalFilePath
import com.umcbms.app.pathToBitmap
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = "FormDataAdapter"
private  var currentTime: String? = null
private val timeMap = mutableMapOf<Int, String>()
class FormDataAdapter(
    private val dbHelper: MasterDBHelper,
    var context: Context,
    private val summeryDataList: MutableList<Map<String, String>>,
    private val onEditClick: (Map<String, String>) -> Unit,
    private val onSyncClick: (Map<String, String>) -> Unit
) :
    RecyclerView.Adapter<FormDataAdapter.FormDataViewHolder>() {

    class FormDataViewHolder(view: View) : ViewHolder(view) {
      //  var tvSaveDraft: TextView = view.findViewById(R.id.tvSaveDraft)
        var responseId: TextView = view.findViewById(R.id.responseId)
        var textViewPrimary: TextView = view.findViewById(R.id.textViewPrimary)
 //       var cvSub: CardView = view.findViewById(R.id.cvSub)
        var imgViewData: ImageView = view.findViewById(R.id.imgViewData)
        var imgEdit: ImageView = view.findViewById(R.id.imgEdit)
        var changedTime:TextView = view.findViewById(R.id.tv_data_changed)
        var imgSync: ImageView = view.findViewById(R.id.imgSync)
        var ivImage: CircleImageView = view.findViewById(R.id.ivImage)
        var linearLayoutPrimaryView: LinearLayout = view.findViewById(R.id.linearLayoutPrimaryView)

        private fun Int.dpToPx(): Int {
            val scale = Resources.getSystem().displayMetrics.density
            return (this * scale + 0.5f).toInt()
        }

        @SuppressLint("SetTextI18n", "ResourceAsColor")
        fun addTextView(value: String, label: String) {

            val linearLayoutHorizontal = LinearLayout(linearLayoutPrimaryView.context)
            linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL
            linearLayoutHorizontal.gravity = Gravity.CENTER_VERTICAL
            val layoutParamsLL = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            layoutParamsLL.setMargins(0, 6.dpToPx(), 3.dpToPx(), 6.dpToPx())
            linearLayoutHorizontal.layoutParams = layoutParamsLL
            linearLayoutPrimaryView.addView(linearLayoutHorizontal)

//            val tv = TextView(linearLayoutPrimaryView.context)
//            tv.text = linearLayoutHorizontal.context.getString(R.string.a)//label
//            tv.textSize = 15f//13f
//            tv.typeface = ResourcesCompat.getFont(
//                linearLayoutPrimaryView.context,
//                R.font.abc900
//            )!! //ResourcesCompat.getFont(linearLayoutPrimaryView.context, R.font.medium)
//            tv.setTextColor(ContextCompat.getColor(linearLayoutPrimaryView.context, R.color.black))
//            val layoutParamsTV = LinearLayoutCompat.LayoutParams(
//                20.dpToPx(),
//                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
//            )
//            tv.layoutParams = layoutParamsTV
//            linearLayoutHorizontal.addView(tv)

            val tv2 = TextView(linearLayoutPrimaryView.context)
            if (value != "") {
                tv2.text = value + " -"
            } else {
                tv2.text = "-"
            }
            tv2.textSize = 12f
            tv2.typeface = ResourcesCompat.getFont(linearLayoutPrimaryView.context, R.font.regular)
            tv2.setTextColor(
                ContextCompat.getColor(
                    linearLayoutPrimaryView.context,
                    R.color.dark_gray
                )
            )
            val layoutParamsTV2 = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            layoutParamsTV2.setMargins(7.dpToPx(), 0, 0, 0)
            tv2.layoutParams = layoutParamsTV2
            linearLayoutHorizontal.addView(tv2)
        }



        @SuppressLint("SetTextI18n")
        fun addImageView(value: String, label: String, ivImage: ImageView? = null) {
            //ivImage.id = View.generateViewId()
            /*if (value == "") {
                ivImage.setImageResource(R.drawable.ic_select)
            } else {
                if (value.contains("/storage")) {
                    Glide.with(ivImage.context)
                        .load(ApiConstants.BASE_IMAGE_URL + value)
                        .into(ivImage)
                } else {
                    val bitmapImage = Base64ToBitmap(value)
                    ivImage.setImageBitmap(bitmapImage)
                   // img.setImageBitmap(bitmapImage)
                }
            }*/

            val tv = TextView(linearLayoutPrimaryView.context)
            tv.text = "$label:"
            tv.textSize = 15f
            val layoutParamsTV = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            layoutParamsTV.setMargins(0, 1, 0, 1)
            tv.layoutParams = layoutParamsTV
            tv.typeface = ResourcesCompat.getFont(linearLayoutPrimaryView.context, R.font.medium)
            linearLayoutPrimaryView.addView(tv)

            val img = ImageView(linearLayoutPrimaryView.context)
            img.id = View.generateViewId()
            if (value == "") {
                img.setImageResource(R.drawable.ic_select)
            } else {
//                if (value.contains("/storage")) {
                if (isLocalFilePath(value)) {
                    val bitmapImage = pathToBitmap(value)
                    img.setImageBitmap(bitmapImage)
                } else {
                    Glide.with(img.context)
                        .load(ApiConstants.BASE_IMAGE_URL + value)
                        .into(img)
                }
            }
            val layoutParams = LinearLayout.LayoutParams(
                linearLayoutPrimaryView.context.resources.getDimensionPixelSize(R.dimen.image_width_100dp),
                linearLayoutPrimaryView.context.resources.getDimensionPixelSize(R.dimen.image_width_100dp)
            )
            layoutParams.setMargins(0, 2, 0, 3)
            img.layoutParams = layoutParams
            linearLayoutPrimaryView.addView(img)
        }

        @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
        fun addDropDownView(value: String, label: String) {

            val linearLayoutHorizontal = LinearLayout(linearLayoutPrimaryView.context)
            linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL
            linearLayoutHorizontal.gravity = Gravity.CENTER_VERTICAL
            val layoutParamsLL = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            layoutParamsLL.setMargins(0, 6.dpToPx(), 3.dpToPx(), 6.dpToPx())
            linearLayoutHorizontal.layoutParams = layoutParamsLL
            linearLayoutPrimaryView.addView(linearLayoutHorizontal)

            val tv = TextView(linearLayoutPrimaryView.context)
            tv.text = value//label
//            tv.text = linearLayoutHorizontal.context.getString(R.string.c)//label
            tv.textSize = 15f
            tv.typeface = ResourcesCompat.getFont(
                linearLayoutPrimaryView.context,
                R.font.regular
            )!!
           /* tv.typeface = ResourcesCompat.getFont(
                linearLayoutPrimaryView.context,
                R.font.abc900
            )!!*/ //ResourcesCompat.getFont(linearLayoutPrimaryView.context, R.font.medium)
            tv.setTextColor(ContextCompat.getColor(linearLayoutPrimaryView.context, R.color.black))
            val layoutParamsTV = LinearLayoutCompat.LayoutParams(
                20.dpToPx(),
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            tv.layoutParams = layoutParamsTV
            linearLayoutHorizontal.addView(tv)

            val tv2 = TextView(linearLayoutPrimaryView.context)
            if (value != "") {
                tv2.text = value
            } else {
                tv2.text = "-"
            }
            tv2.textSize = 12f
            tv2.typeface = ResourcesCompat.getFont(linearLayoutPrimaryView.context, R.font.regular)
            tv2.setTextColor(
                ContextCompat.getColor(
                    linearLayoutPrimaryView.context,
                    R.color.dark_gray
                )
            )
            val layoutParamsTV2 = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            layoutParamsTV2.setMargins(7.dpToPx(), 0, 0, 0)
            tv2.layoutParams = layoutParamsTV2
            linearLayoutHorizontal.addView(tv2)

            /*val tv = TextView(linearLayoutPrimaryView.context)
            tv.text = linearLayoutPrimaryView.context.getText(R.string.c)//label
            tv.textSize = 13f
            val layoutParamsTV = LinearLayoutCompat.LayoutParams(
                20.dpToPx(),
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            tv.layoutParams = layoutParamsTV
            tv.typeface = ResourcesCompat.getFont(linearLayoutPrimaryView.context, R.font.abc900)!! //ResourcesCompat.getFont(linearLayoutPrimaryView.context, R.font.medium)
            tv.setTextColor(ContextCompat.getColor(linearLayoutPrimaryView.context, R.color.black))
            linearLayoutHorizontal.addView(tv)*/

            /*val editText = EditText(linearLayoutPrimaryView.context)
            editText.background = null
            val layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(7.dpToPx(), 0, 0, 0)
            editText.layoutParams = layoutParams
            editText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_CLASS_TEXT
            editText.maxLines = 2
            editText.textSize = 12f
            editText.typeface =
                ResourcesCompat.getFont(linearLayoutPrimaryView.context, R.font.regular)
            editText.setTextColor(
                ContextCompat.getColor(
                    linearLayoutPrimaryView.context,
                    R.color.dark_gray
                )
            )
            editText.isVerticalScrollBarEnabled = true
            editText.isClickable = false
            editText.isFocusable = false
            editText.setOnTouchListener { view, event ->
                view.parent.requestDisallowInterceptTouchEvent(true)
                false
            }

            if (value != "") {
                editText.setText(value)
            } else {
                editText.setText("-")
            }
            linearLayoutHorizontal.addView(editText)*/

        }

        fun showImageDialog(imageUrl: String) {
            val dialog = Dialog(linearLayoutPrimaryView.context)
            dialog.setContentView(R.layout.dialog_image)
            val imageView = dialog.findViewById<ImageView>(R.id.imageView)
//            if (imageUrl.contains("/storage")) {
            if (isLocalFilePath(imageUrl)) {
                val bitmapImage = pathToBitmap(imageUrl)
                imageView.setImageBitmap(bitmapImage)
            } else {
                Glide.with(linearLayoutPrimaryView.context)
                    .load(ApiConstants.BASE_IMAGE_URL + imageUrl)
                    .into(imageView)
            }
            imageView.setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormDataViewHolder {
        return FormDataViewHolder(
            //drafts available in this
            LayoutInflater.from(context).inflate(R.layout.sub_status_date_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return summeryDataList.size
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBindViewHolder(holder: FormDataViewHolder, position: Int) {
//        holder.linearLayoutPrimaryView.removeAllViews()
        val item = summeryDataList[position]
        var mLastClickSync: Long = 0L
        var mLastClickEdit: Long = 0L
        var jsonString: String? = null
        val value = item[MASTER_FORM_ID]
        val responseId = item[RESPONSE_ID]
        if (responseId.toString() != "" && responseId != "NULL") {
            Log.d(TAG, "onBindViewHolder: +$responseId")
            Log.d(TAG, "Response ID: +$responseId")

//            holder.responseId.visibility = View.VISIBLE
//            holder.responseId.text = responseId.toString()
        } else {
 //           holder.responseId.visibility = View.GONE
        }
        Log.d(TAG, "onBindViewHolder: $responseId")
        jsonString = value?.let { getJsonSchemaMasterTablesByID(it) }

        val viewArray = ArrayList<ViewArrayModel>()

        for (itemValue in item) {
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

        for (arr in sortedViewArr) {
            if (arr.primaryView) {
                Log.d(TAG, "onBindViewHolder: ${arr.type}")
                if (arr.type == "DROPDOWN") {
                    val numbers = arr.value.split(", ").map { it.trim() }

//                    if (arr.)
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
                                if(holder.textViewPrimary.text == ""){
                                    holder.textViewPrimary.text = optionData
                                }else{
                                    holder.textViewPrimary.text = holder.textViewPrimary.text.toString() + " - " + optionData
                                }
//                                holder.addDropDownView(optionData, arr.label.toString())
                            } else {
                                if(holder.textViewPrimary.text == ""){
                                    holder.textViewPrimary.text = "-"
                                }else{
                                    holder.textViewPrimary.text = holder.textViewPrimary.text.toString() + " - " + ""
                                }
//                                holder.addDropDownView("", arr.label.toString())
                            }
                        } else {
                            if(holder.textViewPrimary.text == ""){
                                holder.textViewPrimary.text = "-"
                            }else{
                                holder.textViewPrimary.text = holder.textViewPrimary.text.toString() + " - " + ""
                            }
//                            holder.addDropDownView("", arr.label.toString())
                        }
                    }
                } else if (arr.type == "UPLOAD_IMAGE" || arr.type == "CAPTURE_IMAGE") {
                    if (arr.value.isNotEmpty()) {
//                        val valueArray = JSONArray(arr.value)/*.split(", ").map { it.trim() }*/

                        holder.ivImage.visibility = View.VISIBLE
                        val valueArray = arr.value.split(", ").map { it.trim() }
                        for (imageUrl in valueArray) {
//                        for (j in 0..valueArray.length() - 1) {
//                            var imageUrl = valueArray.getString(j)
                            if (imageUrl == "") {
                                holder.ivImage.setImageResource(R.drawable.ic_select)
                            } else {
//                            if (imageUrl.contains("/storage")) {
                                if (isLocalFilePath(imageUrl)) {
                                    val bitmapImage = pathToBitmap(imageUrl)
                                    holder.ivImage.setImageBitmap(bitmapImage)
                                } else {
                                    Glide.with(holder.ivImage.context)
                                        .load(ApiConstants.BASE_IMAGE_URL + imageUrl)
                                        .into(holder.ivImage)
                                    // img.setImageBitmap(bitmapImage)
                                }
                                holder.ivImage.setOnClickListener {
                                    holder.showImageDialog(imageUrl)
                                }
                            }
                            // holder.addImageView(imageUrl, arr.label.toString())
                        }
                    }
                } else if (arr.type == "RADIO") {
                    if (arr.options != null) {
                        if (arr.value != "") {
                            val opsVal = arr.options[arr.value.toFloat().toInt()]
                            if (opsVal != null) {
                                if(holder.textViewPrimary.text == ""){
                                    holder.textViewPrimary.text = opsVal
                                }else{
                                    holder.textViewPrimary.text = holder.textViewPrimary.text.toString() + " - " + opsVal
                                }
//                                holder.addTextView(opsVal, arr.label.toString())
                            }
                        } else {
                            if(holder.textViewPrimary.text == ""){
                                holder.textViewPrimary.text = "-"
                            }else{
                                holder.textViewPrimary.text = holder.textViewPrimary.text.toString() + " - " + ""
                            }
//                            holder.addTextView("", arr.label.toString())
                        }
                    }
                } else if (arr.type == "NUMBERS") {
                    if (arr.value != "") {
                        if(holder.textViewPrimary.text == ""){
                            holder.textViewPrimary.text = arr.value.toDouble().toLong().toString()
                        }else{
                            holder.textViewPrimary.text = holder.textViewPrimary.text.toString() + " - " + arr.value.toDouble().toLong().toString()
                        }
                        /*holder.addTextView(
                            arr.value.toDouble().toLong().toString(),
                            arr.label.toString()
                        )*/
                    } else {
                        if(holder.textViewPrimary.text == ""){
                            holder.textViewPrimary.text = ""
                        }else{
                            holder.textViewPrimary.text = holder.textViewPrimary.text.toString() + " - " + ""
                        }
//                        holder.addTextView("", arr.label.toString())
                    }
                } else if (arr.type == "DATE") {
                    val dateValue = changeDateTimeFormat(arr.value, "yyyy-MM-dd", "dd/MM/yyyy")
                    if(holder.textViewPrimary.text == ""){
                        holder.textViewPrimary.text = dateValue
                    }else{
                        holder.textViewPrimary.text = holder.textViewPrimary.text.toString() + " - " + dateValue
                    }
                //                    holder.addTextView(dateValue, arr.label.toString())
                } else {
                    if (arr.value == ""){
                        if(holder.textViewPrimary.text == ""){
                            holder.textViewPrimary.text = "-"
                        }else{
                            holder.textViewPrimary.text = holder.textViewPrimary.text.toString() + " - " + "-"
                        }
                    }else{
                        if(holder.textViewPrimary.text == ""){
                            holder.textViewPrimary.text = arr.value
                            Log.d(TAG, "onBindViewHolder 1: ${holder.textViewPrimary.text}")
                        }else{
                            holder.textViewPrimary.text = holder.textViewPrimary.text.toString() + " - " + arr.value
                            Log.d(TAG, "onBindViewHolder 2: ${holder.textViewPrimary.text}")
                        }
                    }
//                    holder.addTextView(arr.value, arr.label.toString())
                }
            }
        }

        for (i in item.keys) {
            if (i == STATUS) {
                if (item[i] == "Draft") {
                  //  holder.tvSaveDraft.text = "Draft Surveys"
                    holder.imgSync.visibility = View.GONE
                    if (currentTime == null) holder.changedTime.text = "Changed 04/04/2024" else
                    holder.changedTime.text = "Changed $currentTime"

                } else if (item[i] == SYNCED) {
               //     holder.tvSaveDraft.text = SYNCED + "Surveys"
                    holder.imgEdit.visibility = View.GONE

                    holder.imgViewData.visibility = View.VISIBLE
                } else {
                  //  holder.tvSaveDraft.text = "Sync Pending"
                    holder.imgSync.visibility = View.VISIBLE
                    holder.imgEdit.visibility = View.VISIBLE
                }
            }
        }

        holder.imgEdit.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickEdit < 1000) return@setOnClickListener
            mLastClickEdit = SystemClock.elapsedRealtime()
            val currentTime = getCurrentTime()
            timeMap[position] = currentTime
            notifyItemChanged(position)
            onEditClick.invoke(item)
            Log.d("CurrentTimeForItem", "Current time for item at position $position: $currentTime")
        }

        var  currentTimeForItem = getCurrentTimeForItem(position)
        if (currentTimeForItem!=null) {
            holder.changedTime.text = (" Last Updated On " + currentTimeForItem)
        }else holder.changedTime.text = ""
        holder.imgSync.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickSync < 1000) return@setOnClickListener
            mLastClickSync = SystemClock.elapsedRealtime()
            onSyncClick.invoke(item)
        }



        holder.imgViewData.setOnClickListener {
            val intent = Intent(context, SecondaryViewActivity::class.java)
            intent.putExtra("id", item["id"])
            context.startActivity(intent)
        }
    }
    // Function to get current time
    private fun getCurrentTime(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateTimeFormat.format(calendar.time)
    }

    // Function to get current time for a specific item position
    private fun getCurrentTimeForItem(position: Int): String? {
        return timeMap[position]
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
                        //val label =

                        if (key == id) {
                            val options = mutableMapOf<Int, String>()
                            options.clear()

                            if (childObject.has("options")) {
                                val optionsList = childObject.getJSONArray("options")
                                if (optionsList.length() > 0) {
                                    for (i in 0 until optionsList.length()) {
                                        val childId = optionsList.getJSONObject(i).getInt("id")
                                        val childValue =
                                            optionsList.getJSONObject(i).getString("value")
                                        options[childId] = childValue
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
}

