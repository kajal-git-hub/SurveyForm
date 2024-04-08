package com.umcbms.app.Home.FormData

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
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
import com.bumptech.glide.Glide
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.MasterDB.MasterDBHelper.Companion.QCRESOLVED
import com.umcbms.app.MasterDB.QCViewArrayModel
import com.umcbms.app.R
import com.umcbms.app.api.ApiConstants
import com.umcbms.app.api.respose.QCData
import com.umcbms.app.changeDateTimeFormat
import com.umcbms.app.databinding.ItemQcListLayoutBinding
import com.umcbms.app.isLocalFilePath
import com.umcbms.app.pathToBitmap
import org.json.JSONObject

class QCListAdapter(
    val context: Context,
    val dbHelper: MasterDBHelper,
    val callBackDownload: (Int, QCData) -> Unit,
    val callBackQC: (Int, QCData) -> Unit,
    val callBackSync: (Int, QCData) -> Unit
) :
    RecyclerView.Adapter<QCListAdapter.FormViewHolder>() {

    var formList = ArrayList<QCData>()

    inner class FormViewHolder(val binding: ItemQcListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun Int.dpToPx(): Int {
            val scale = Resources.getSystem().displayMetrics.density
            return (this * scale + 0.5f).toInt()
        }

        fun addTextView(value: String, label: String) = with(binding) {

            val linearLayoutHorizontal = LinearLayout(linearLayoutQCView.context)
            linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL
            linearLayoutHorizontal.gravity = Gravity.CENTER_VERTICAL
            val layoutParamsLL = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            layoutParamsLL.setMargins(0, 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
            linearLayoutHorizontal.layoutParams = layoutParamsLL
            linearLayoutQCView.addView(linearLayoutHorizontal)

            val tv = TextView(linearLayoutQCView.context)
            tv.text = label
            tv.textSize = 13f
            tv.typeface = ResourcesCompat.getFont(
                linearLayoutQCView.context,
                R.font.medium
            )!! //ResourcesCompat.getFont(linearLayoutPrimaryView.context, R.font.medium)
            tv.setTextColor(ContextCompat.getColor(linearLayoutQCView.context, R.color.black))
            val layoutParamsTV = LinearLayoutCompat.LayoutParams(
                130.dpToPx(),
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            tv.layoutParams = layoutParamsTV
            linearLayoutHorizontal.addView(tv)

            val tv2 = TextView(linearLayoutQCView.context)
            if (value != "") {
                tv2.text = value
            } else {
                tv2.text = "-"
            }
            tv2.textSize = 12f
            tv2.typeface = ResourcesCompat.getFont(linearLayoutQCView.context, R.font.regular)
            tv2.setTextColor(
                ContextCompat.getColor(
                    linearLayoutQCView.context,
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

        fun showImageDialog(imageUrl: String) {
            val dialog = Dialog(binding.linearLayoutImg.context)
            dialog.setContentView(R.layout.dialog_image)
            val imageView = dialog.findViewById<ImageView>(R.id.imageView)
//            if (imageUrl.contains("/storage")) {
            if (isLocalFilePath(imageUrl)) {
                val bitmapImage = pathToBitmap(imageUrl)
                imageView.setImageBitmap(bitmapImage)
            } else {
                Glide.with(binding.linearLayoutImg.context)
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

        @SuppressLint("ResourceAsColor")
        fun bindData(item: QCData) = with(binding) {
            textViewId.text = item.id
            textViewStatus.text = item.status

            if (item.status == QCRESOLVED) {
                textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.green_bold))
            } else {
                textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
            }

            val jsonString = getJsonSchemaMasterTablesByID(FormDataActivity.formId.toString())

            val viewArray = ArrayList<QCViewArrayModel>()

            for ((key, value) in item.primary_view_map) {
                val jsonObject = jsonString?.let { JSONObject(it) }
                val dataArray = jsonObject?.getJSONArray("data")

                if (dataArray != null) {
                    for (j in 0 until dataArray.length()) {
                        val sectionObject = dataArray.getJSONObject(j)
                        sectionFun(sectionObject, key, viewArray, value)
                    }
                }
            }

            for (arr in viewArray) {
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
                                addTextView(optionData, arr.label.toString())
                            } else {
                                addTextView("", arr.label.toString())
                            }
                        } else {
                            addTextView("", arr.label.toString())
                        }
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
                } else if (arr.type == "DATE") {
                    val dateValue = changeDateTimeFormat(arr.value, "yyyy-MM-dd", "dd/MM/yyyy")
                    addTextView(dateValue, arr.label.toString())
                } else if (arr.type == "UPLOAD_IMAGE" || arr.type == "CAPTURE_IMAGE") {

                    if (arr.value.isNotEmpty()) {
//                        val valueArray = JSONArray(arr.value)/*.split(", ").map { it.trim() }*/

                        imageViewImg.visibility = View.VISIBLE
                        val valueArray = arr.value.split(", ").map { it.trim() }
                        for (imageUrl in valueArray) {
//                        for (j in 0..valueArray.length() - 1) {
//                            var imageUrl = valueArray.getString(j)
                            if (imageUrl == "") {
                                imageViewImg.setImageResource(R.drawable.ic_select)
                            } else {
//                            if (imageUrl.contains("/storage")) {
                                if (isLocalFilePath(imageUrl)) {
                                    val bitmapImage = pathToBitmap(imageUrl)
                                    imageViewImg.setImageBitmap(bitmapImage)
                                } else {
                                    Glide.with(imageViewImg.context)
                                        .load(ApiConstants.BASE_IMAGE_URL + imageUrl)
                                        .into(imageViewImg)
                                    // img.setImageBitmap(bitmapImage)
                                }
                                imageViewImg.setOnClickListener {
                                    showImageDialog(imageUrl)
                                }
                            }
                            // holder.addImageView(imageUrl, arr.label.toString())
                        }
                    }
                    // imageViewImg
                } else {
                    addTextView(arr.value, arr.label.toString())
                }

            }


            if (item.qcId == "0") {
                imageViewQCDownload.visibility = View.VISIBLE
            } else {
                imageViewQCDownload.visibility = View.GONE
                if (item.status == MasterDBHelper.QCRAISED) {
                    imageViewQC.visibility = View.VISIBLE
                    imageViewQCSync.visibility = View.GONE
                } else {
                    imageViewQC.visibility = View.GONE
                    imageViewQCSync.visibility = View.VISIBLE
                }
            }
            imageViewQCDownload.setOnClickListener {
                callBackDownload.invoke(adapterPosition, item)
            }
            imageViewQC.setOnClickListener {
                callBackQC.invoke(adapterPosition, item)
            }
            imageViewQCSync.setOnClickListener {
                callBackSync.invoke(adapterPosition, item)
            }
        }

        private fun getJsonSchemaMasterTablesByID(masterFormId: String): String? {
            val data = dbHelper.getFormsById(masterFormId.toInt())
            return data.formSchema
        }

        private fun sectionFun(
            sectionObject: JSONObject,
            key: String,
            viewArray: ArrayList<QCViewArrayModel>,
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
                        val id = childObject.getString("id")
                        val type = childObject.getString("type")
                        if (key == id) {
                            var labelView = ""
                            if (childObject.has("homeLabel")) {
                                labelView = childObject.getString("homeLabel")
                            } else {
                                labelView =
                                    childObject.getJSONObject("properties").getString("label")
                            }
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

                            viewArray.add(QCViewArrayModel(id, type, labelView, options, value))
                        }
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormViewHolder {
        return FormViewHolder(
            ItemQcListLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = formList.size

    override fun onBindViewHolder(holder: FormViewHolder, position: Int) {
        holder.bindData(formList[position])
    }
}