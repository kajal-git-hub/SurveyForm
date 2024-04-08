package com.umcbms.app.Home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.gson.Gson
import com.umcbms.app.Home.FormData.FormDataActivity
import com.umcbms.app.JSONModel.JSONFormDataModel
import com.umcbms.app.MasterDB.FormDataID
import com.umcbms.app.MasterDB.FormJsonDataModel
import com.umcbms.app.MasterDB.FormListModel
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.R

class FormAdapter(
    private var context: Context,
    private var formList: ArrayList<FormListModel>,
    private var formJsonData: ArrayList<FormJsonDataModel>,
    private var formJsonId: ArrayList<FormDataID>,
    var dbHelper: MasterDBHelper
) : RecyclerView.Adapter<FormAdapter.FormViewHolder>() {

    class FormViewHolder(view: View) : ViewHolder(view) {
        var formTv: TextView = view.findViewById(R.id.formTv)
        var formAcronymTv: TextView = view.findViewById(R.id.formAcronymTv)
        var buttonOpenForm: TextView = view.findViewById(R.id.buttonOpenForm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.form_list, parent, false)
        return FormViewHolder(view)
    }

    override fun getItemCount(): Int {
        return formList.size
    }

    @SuppressLint("Range")
    override fun onBindViewHolder(holder: FormViewHolder, position: Int) {
        val currentForm = formList[position].formName
        holder.formTv.text = currentForm
        val jsonData = Gson().fromJson(formJsonData[position].formSchema, JSONFormDataModel::class.java)
        holder.formAcronymTv.text = jsonData.acronym


        holder.buttonOpenForm.setOnClickListener {
            Log.e("getformname",currentForm)

            try {
                var permissions=""
                //val jsonData: JSONFormDataModel = Gson().fromJson(formJsonData[position].formSchema, JSONFormDataModel::class.java)
                var cursor = dbHelper.getAllRecordsWithCondition(MasterDBHelper.ACCESS_PERMISSION_TABLE_NAME,  "${MasterDBHelper.SUBUNIT_ID}=${formJsonId[position].id}")
                if (cursor != null && cursor.moveToFirst()) {
                    permissions=cursor.getString(cursor.getColumnIndex(MasterDBHelper.PERMISSIONS))
                }

                val intent = Intent(context, FormDataActivity::class.java)
                intent.putExtra("formJsonData", formJsonData[position].formSchema)
                intent.putExtra("formId", formJsonId[position].id)
                intent.putExtra("permissions", permissions)
                intent.putExtra("currentForm", currentForm)
                /*intent.putExtra("subId",subId[position].id)*/
                context.startActivity(intent)

            } catch (e: Exception) {
                Log.e("TAG", "JsonSyntaxException: ${e.message}")
            }
        }

    }
    /*private fun getSubmissionTableData(): ArrayList<FormSubmissionData> {
        val formData = dbHelper.getSubmissionTable()

        val formListDB = ArrayList<FormSubmissionData>()

        formListDB.addAll(formData)

        return formListDB
    }*/
}