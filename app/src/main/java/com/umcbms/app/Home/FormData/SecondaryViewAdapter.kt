package com.umcbms.app.Home.FormData

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.umcbms.app.MasterDB.ViewArrayModel
import com.umcbms.app.R
import com.umcbms.app.api.ApiConstants
import com.umcbms.app.changeDateTimeFormat
import com.umcbms.app.isLocalFilePath
import com.umcbms.app.pathToBitmap

private const val TAG = "SecondaryViewAdapter"

class SecondaryViewAdapter(val context: Context, private val sortedViewArr: List<ViewArrayModel>) :
    RecyclerView.Adapter<SecondaryViewAdapter.SecondaryViewHolder>() {
    class SecondaryViewHolder(view: View) : ViewHolder(view) {

        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val tvValue: TextView = view.findViewById(R.id.tvValue)
        val ivImage: ImageView = view.findViewById(R.id.ivImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SecondaryViewHolder {
        return SecondaryViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.secondary_view_recyclerview, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return sortedViewArr.size
    }

    override fun onBindViewHolder(holder: SecondaryViewHolder, position: Int) {
        val arr = sortedViewArr[position]
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
                            holder.tvLabel.text = arr.label.toString()
                            holder.tvValue.text = optionData
                        } else {
                            holder.tvLabel.text = arr.label.toString()
                            holder.tvValue.text = ""
                        }
                    } else {
                        holder.tvLabel.text = arr.label.toString()
                        holder.tvValue.text = ""
                    }
                }
            } else if (arr.type == "UPLOAD_IMAGE" || arr.type == "CAPTURE_IMAGE") {
//                val numbers = arr.value.split(", ").map { it.trim() }
                holder.ivImage.visibility = View.VISIBLE
                holder.tvLabel.text = arr.label.toString()
                holder.tvValue.visibility = View.GONE

                val valueArray = arr.value.split(", ").map { it.trim() }
                for (imageUrl in valueArray) {
//                for (j in 0..valueArray.length() - 1) {
//                    var imageUrl=valueArray.getString(j)
                    if (imageUrl == "") {
                        holder.ivImage.setImageResource(R.drawable.ic_select)
                    } else {
//                        if (imageUrl.contains("/storage")) {
                        if (isLocalFilePath(imageUrl)) {
                            val bitmapImage = pathToBitmap(imageUrl)
                            holder.ivImage.setImageBitmap(bitmapImage)
                        } else {
                            Glide.with(holder.ivImage.context)
                                .load(ApiConstants.BASE_IMAGE_URL + imageUrl)
                                .into(holder.ivImage)
                        }
                    }
                }
            } else if (arr.type == "RADIO") {
                if (arr.options != null) {
                    if (arr.value != "") {
                        val opsVal = arr.options[arr.value.toFloat().toInt()]
                        if (opsVal != null) {
                            holder.tvLabel.text = arr.label.toString()
                            holder.tvValue.text = opsVal
                        }
                    } else {
                        holder.tvLabel.text = arr.label.toString()
                        holder.tvValue.text = ""
                    }
                }
            } else if (arr.type == "NUMBERS") {
                if (arr.value != "") {
                    holder.tvLabel.text = arr.label.toString()
                    holder.tvValue.text = arr.value.toDouble().toLong().toString()
                } else {
                    holder.tvLabel.text = arr.label.toString()
                    holder.tvValue.text = ""
                }
            } else if (arr.type == "DATE") {
                val dateValue = changeDateTimeFormat(arr.value, "yyyy-MM-dd", "dd/MM/yyyy")
                holder.tvLabel.text = arr.label.toString()
                holder.tvValue.text = dateValue
            } else {
                holder.tvLabel.text = arr.label.toString()
                holder.tvValue.text = arr.value
            }
        }

    }
}