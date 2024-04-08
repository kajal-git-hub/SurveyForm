package com.umcbms.app.Home.FormData.fragment

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.umcbms.app.Home.FormData.EditFormDataActivity
import com.umcbms.app.Home.FormData.FormDataActivity
import com.umcbms.app.Home.FormData.FormDataAdapter
import com.umcbms.app.Home.FormData.viewModel.SectionViewModel
import com.umcbms.app.JSONModel.Data
import com.umcbms.app.JSONModel.JSONFormDataModel
import com.umcbms.app.JSONModel.SkipLogicModel
import com.umcbms.app.MasterDB.FormSubmissionData
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.R
import com.umcbms.app.api.Status
import com.umcbms.app.api.request.JSONRequest
import com.umcbms.app.api.request.SyncedJSONRequest
import com.umcbms.app.getPathToBase64String
import com.umcbms.app.getPrefStringData
import com.umcbms.app.hideLoader
import com.umcbms.app.isJsonArray
import com.umcbms.app.isLocalFilePath
import com.umcbms.app.showLoader
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "IndexFragment"

class IndexFragment : Fragment() {

    private lateinit var rvFormData: RecyclerView
    private lateinit var rvFormSync: RecyclerView
    private lateinit var rvFormPending: RecyclerView
    private lateinit var tvDataNotFound: TextView
    private lateinit var tvSync: TextView
    private lateinit var tvPendingSync: TextView
    private lateinit var llFirst: LinearLayout
    private lateinit var llready_Readysynced: LinearLayout
    private lateinit var lltsync: LinearLayout
    private lateinit var dbHelper: MasterDBHelper
    private var selectedValue: String? = null
    private lateinit var sectionViewModel: SectionViewModel

    companion object {
        var formSkipLogics: ArrayList<SkipLogicModel> = arrayListOf()
        var allIds: ArrayList<String> = arrayListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedValue = getString(R.string.all)
      // val imgFilter = view?.findViewById<ImageView>(R.id.imgFilter)
        sectionViewModel = ViewModelProvider(this).get(SectionViewModel::class.java)




        /*FormDataAdapter.ivBack.setOnClickListener {
            finish()
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_index, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llFirst = view.findViewById(R.id.llFirst)
        llready_Readysynced = view.findViewById(R.id.llready_Readysynced)
        lltsync = view.findViewById(R.id.lltsync)
        rvFormData = view.findViewById(R.id.rvFormDataDraft)
        tvSync = view.findViewById(R.id.tvsync)
        tvPendingSync = view.findViewById(R.id.tv_Readysynced)
        rvFormPending = view.findViewById(R.id.rv_Readysynced)
        rvFormSync = view.findViewById(R.id.rvFormDatasync)
        tvDataNotFound = view.findViewById(R.id.tvDataNotFound)
        dbHelper = MasterDBHelper(requireContext())
//        FormDataActivity.imgFilter.setOnClickListener {
//            showFilterOptions()
//            FormDataActivity
//        }



    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onResume() {
        super.onResume()
        Log.e("Onceddstatus",selectedValue.toString())
        //Draft
        val getDraftData = getSummeryTablesByStatus(getString(R.string.draft))
        Log.d("Summary", "Summary us $getDraftData")

        if (getDraftData.isEmpty()) {
            llFirst.visibility = View.GONE
          /*  tvDataNotFound.text = "$selectedValue ${getString(R.string.dataNotFound)}"
            tvDataNotFound.visibility = View.VISIBLE*/
        } else {
            llFirst.visibility = View.VISIBLE
            tvDataNotFound.visibility = View.GONE
        }
        rvFormData.layoutManager = LinearLayoutManager(requireContext())
        val draftAdapter = FormDataAdapter(dbHelper, requireContext(), getDraftData, { item ->
            val intentAdd = Intent(requireContext(), EditFormDataActivity::class.java)
            intentAdd.putExtra("summeryId", item["id"]?.toInt())
            intentAdd.putExtra("formId", item["master_form_id"]?.toInt())
            intentAdd.putExtra(
                "subId",
                if (item["form_sub_id"] == "") 0 else item["form_sub_id"]?.toInt()
            )
            intentAdd.putExtra(
                "responseId",
                if (item["response_id"] == "") 0 else item["response_id"]?.toInt()
            )
            intentAdd.putExtra("status", item["status"])
            startActivity(intentAdd)
        }, { item ->
            //Sync Click
            syncDataToServer(item)
        })
        rvFormData.adapter = draftAdapter

        //Sync

        val getSyncData = getSummeryTablesByStatus(getString(R.string.synced))
        Log.e("getSyncData",getSyncData.toString())

        if (getSyncData.isEmpty()) {
            lltsync.visibility = View.GONE
          /*  tvDataNotFound.text = "$selectedValue ${getString(R.string.dataNotFound)}"
            tvDataNotFound.visibility = View.VISIBLE*/
        } else {
            lltsync.visibility = View.VISIBLE
            tvDataNotFound.visibility = View.GONE
        }

        rvFormSync.layoutManager = LinearLayoutManager(requireContext())
        val syncAdapter = FormDataAdapter(dbHelper, requireContext(), getSyncData, { item ->
            Log.e("ccheckitemb",item.keys.toString()+item.values)

            val intentAdd = Intent(requireContext(), EditFormDataActivity::class.java)
            intentAdd.putExtra("summeryId", item["id"]?.toInt())
            intentAdd.putExtra("formId", item["master_form_id"]?.toInt())
            intentAdd.putExtra(
                "subId",
                if (item["form_sub_id"] == "") 0 else item["form_sub_id"]?.toInt()
            )
            intentAdd.putExtra(
                "responseId",
                if (item["response_id"] == "") 0 else item["response_id"]?.toInt()
            )
            intentAdd.putExtra("status", item["status"])
            startActivity(intentAdd)
        }, { item ->
            //Sync Click
            syncDataToServer(item)
        })
        rvFormSync.adapter = syncAdapter


        //Pending
        val getPendingData = getSummeryTablesByStatus(getString(R.string.saved))
        Log.d("Summary", "Summary us $getPendingData")

        if (getPendingData.isEmpty()) {
            llready_Readysynced.visibility = View.GONE
           /* tvDataNotFound.text = "$selectedValue ${getString(R.string.dataNotFound)}"
            tvDataNotFound.visibility = View.VISIBLE*/
        } else {
            llready_Readysynced.visibility = View.VISIBLE
            tvDataNotFound.visibility = View.GONE
        }
        rvFormPending.layoutManager = LinearLayoutManager(requireContext())
        val pendingAdapter = FormDataAdapter(dbHelper, requireContext(), getPendingData, { item ->
            val intentAdd = Intent(requireContext(), EditFormDataActivity::class.java)
            intentAdd.putExtra("summeryId", item["id"]?.toInt())
            intentAdd.putExtra("formId", item["master_form_id"]?.toInt())
            intentAdd.putExtra(
                "subId",
                if (item["form_sub_id"] == "") 0 else item["form_sub_id"]?.toInt()
            )
            intentAdd.putExtra(
                "responseId",
                if (item["response_id"] == "") 0 else item["response_id"]?.toInt()
            )
            intentAdd.putExtra("status", item["status"])
            startActivity(intentAdd)
        }, { item ->
            //Sync Click
            syncDataToServer(item)
        })
        rvFormPending.adapter = pendingAdapter


        if (getDraftData.isEmpty() && getPendingData.isEmpty() && getSyncData.isEmpty()){
            tvDataNotFound.visibility = View.VISIBLE
        }else{
            tvDataNotFound.visibility = View.GONE
        }
//        if (selectedValue == getString(R.string.draft) || selectedValue == null) {
//           /* val getSubmissionTableList = getSubmissionTableByFormId(FormDataActivity.formId)
//            getSubmissionTableList.reverse()*/
//
//            val getSummeryData = getSummeryTables()
//
//            if (getSummeryData.isEmpty()) {
//                tvDataNotFound.visibility = View.VISIBLE
//                tvDataNotFound.text = getString(R.string.dataNotFound)
//            } else {
//                tvDataNotFound.visibility = View.GONE
//            }
//
//            rvFormData.layoutManager = LinearLayoutManager(requireContext())
//
//            val adapter = FormDataAdapter(dbHelper, requireContext(), getSummeryData, { item ->
//                Log.e("itemdata", item.keys.toString()+item.values)
//
//                val intentAdd = Intent(requireContext(), EditFormDataActivity::class.java)
//                intentAdd.putExtra("summeryId", item["id"]?.toInt())
//                intentAdd.putExtra("formId", item["master_form_id"]?.toInt())
//                intentAdd.putExtra(
//                    "subId",
//                    if (item["form_sub_id"] == "") 0 else item["form_sub_id"]?.toInt()
//                )
//                intentAdd.putExtra(
//                    "responseId",
//                    if (item["response_id"] == "") 0 else item["response_id"]?.toInt()
//                )
//                intentAdd.putExtra("status", item["status"])
//                startActivity(intentAdd)
//            }, { item ->
//                //Sync Click
//                Log.e("checkitemm",item.keys.toString()+item.values)
//
//                syncDataToServer(item)
//            })
//            rvFormData.adapter = adapter
//        } else {
//          /*  val getSubmissionTableList = getSubmissionTableByFormId(FormDataActivity.formId)
//            getSubmissionTableList.reverse()*/
//
//            val getSummeryData = getSummeryTablesByStatus(selectedValue!!)
//            Log.e("Synceddstatus",selectedValue.toString())
//
//            if (getSummeryData.isEmpty()) {
//                tvDataNotFound.text = "$selectedValue ${getString(R.string.dataNotFound)}"
//                tvDataNotFound.visibility = View.VISIBLE
//            } else {
//                tvDataNotFound.visibility = View.GONE
//            }
//
//            rvFormData.layoutManager = LinearLayoutManager(requireContext())
//
//
//            rvFormData.layoutManager = LinearLayoutManager(requireContext())
//            val adapter = FormDataAdapter(dbHelper, requireContext(), getSummeryData, { item ->
//                val intentAdd = Intent(requireContext(), EditFormDataActivity::class.java)
//                intentAdd.putExtra("summeryId", item["id"]?.toInt())
//                intentAdd.putExtra("formId", item["master_form_id"]?.toInt())
//                intentAdd.putExtra(
//                    "subId",
//                    if (item["form_sub_id"] == "") 0 else item["form_sub_id"]?.toInt()
//                )
//                intentAdd.putExtra(
//                    "responseId",
//                    if (item["response_id"] == "") 0 else item["response_id"]?.toInt()
//                )
//                intentAdd.putExtra("status", item["status"])
//                startActivity(intentAdd)
//            }, { item ->
//                //Sync Click
//                Log.e("ccheckitem",item.keys.toString()+item.values)
//                syncDataToServer(item)
//            })
//            rvFormData.adapter = adapter
//        }
    }

    private fun getSummeryTables(): MutableList<Map<String, String>> {
        val cursor = dbHelper.getSummeryTable(FormDataActivity.summeryTableName)

        val dataList = mutableListOf<Map<String, String>>()

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnCount = cursor.columnCount
                val columnNames = Array<String>(columnCount) { "" }

                for (i in 0 until columnCount) {
                    columnNames[i] = cursor.getColumnName(i)
                }

                do {
                    val rowData = mutableMapOf<String, String>()
                    for (i in 0 until columnCount) {
                        val columnId = cursor.getColumnName(i)
                        val data = cursor.getString(i)
                        rowData[columnId] = data ?: ""

                    }
                    dataList.add(rowData)
                } while (cursor.moveToNext())

            }
        }
        //Log.d(TAG, "Data : $dataList")
        return dataList

    }

    private fun getSummeryTablesByStatus(status: String): MutableList<Map<String, String>> {
        val cursor = dbHelper.getSummeryTableByStatus(FormDataActivity.summeryTableName, status)

        val dataList = mutableListOf<Map<String, String>>()

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnCount = cursor.columnCount
                val columnNames = Array<String>(columnCount) { "" }

                for (i in 0 until columnCount) {
                    columnNames[i] = cursor.getColumnName(i)
                }

                do {
                    val rowData = mutableMapOf<String, String>()
                    for (i in 0 until columnCount) {
                        val columnId = cursor.getColumnName(i)
                        val data = cursor.getString(i)
                        rowData[columnId] = data ?: ""

                    }
                    dataList.add(rowData)
                } while (cursor.moveToNext())

            }
        }
        return dataList

    }


    private fun getSubmissionTableByFormId(formId: Int): ArrayList<FormSubmissionData> {
        val formData = dbHelper.getSubmissionTableByFormId(formId)
        val formListDB = ArrayList<FormSubmissionData>()

        formListDB.addAll(formData)
        return formListDB
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingInflatedId", "ResourceAsColor", "SetTextI18n")
    private fun showFilterOptions() {

        val blueColor = ContextCompat.getColor(requireContext(), R.color.blue)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)

        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.filter_options, null)
        //val applyFilterButton = dialogView.findViewById<Button>(R.id.applyFilterButton)
        val tvAll = dialogView.findViewById<TextView>(R.id.tvAll)
        val tvDraft = dialogView.findViewById<TextView>(R.id.tvDraft)
        val tvSaved = dialogView.findViewById<TextView>(R.id.tvSaved)
        val tvSynced = dialogView.findViewById<TextView>(R.id.tvSynced)

        when (selectedValue != null) {
            (tvAll.text.toString() == selectedValue) -> {
                tvAll.setBackgroundColor(blueColor)
                tvAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvDraft.setBackgroundColor(whiteColor)
                tvDraft.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvSynced.setBackgroundColor(whiteColor)
                tvSynced.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvSaved.setBackgroundColor(whiteColor)
                tvSaved.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            (tvDraft.text.toString() == selectedValue) -> {
                tvAll.setBackgroundColor(whiteColor)
                tvAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvDraft.setBackgroundColor(blueColor)
                tvDraft.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvSynced.setBackgroundColor(whiteColor)
                tvSynced.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvSaved.setBackgroundColor(whiteColor)
                tvSaved.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            (tvSaved.text.toString() == selectedValue) -> {
                tvAll.setBackgroundColor(whiteColor)
                tvAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvDraft.setBackgroundColor(whiteColor)
                tvDraft.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvSynced.setBackgroundColor(whiteColor)
                tvSynced.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvSaved.setBackgroundColor(blueColor)
                tvSaved.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

            (tvSynced.text.toString() == selectedValue) -> {
                tvAll.setBackgroundColor(whiteColor)
                tvAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvDraft.setBackgroundColor(whiteColor)
                tvDraft.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvSynced.setBackgroundColor(blueColor)
                tvSynced.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvSaved.setBackgroundColor(whiteColor)
                tvSaved.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            else -> {}
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
        val titleView = TextView(requireContext())
        titleView.text = "Filter Options"
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)
        titleView.typeface = ResourcesCompat.getFont(requireContext(), R.font.regular)
        titleView.gravity = Gravity.CENTER
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 20, 0, 15)
        titleView.layoutParams = layoutParams
        dialogBuilder.setCustomTitle(titleView)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        /*applyFilterButton.setOnClickListener {
            alertDialog.dismiss()
        }*/
        tvAll.setOnClickListener {
            selectedValue = getString(R.string.all)

            /*val getSubmissionTableList = getSubmissionTableByFormId(FormDataActivity.formId)
            getSubmissionTableList.reverse()*/

            val getSummeryData = getSummeryTables()

            if (getSummeryData.isEmpty()) {
              /*  tvDataNotFound.text = "$selectedValue ${getString(R.string.dataNotFound)}"
                tvDataNotFound.visibility = View.VISIBLE*/
            } else {
                tvDataNotFound.visibility = View.GONE
            }

            rvFormData.layoutManager = LinearLayoutManager(requireContext())
            val adapter = FormDataAdapter(dbHelper, requireContext(), getSummeryData, { item ->
                val intentAdd = Intent(requireContext(), EditFormDataActivity::class.java)
                intentAdd.putExtra("summeryId", item["id"]?.toInt())
                intentAdd.putExtra("formId", item["master_form_id"]?.toInt())
                intentAdd.putExtra(
                    "subId",
                    if (item["form_sub_id"] == "") 0 else item["form_sub_id"]?.toInt()
                )
                intentAdd.putExtra(
                    "responseId",
                    if (item["response_id"] == "") 0 else item["response_id"]?.toInt()
                )
                intentAdd.putExtra("status", item["status"])
                startActivity(intentAdd)
            }, { item ->
                //Sync Click
                syncDataToServer(item)
            })
            rvFormData.adapter = adapter

            alertDialog.dismiss()
        }
        tvDraft.setOnClickListener {
            selectedValue = getString(R.string.draft)

            /*val getSubmissionTableList = getSubmissionTableByFormId(FormDataActivity.formId)
            getSubmissionTableList.reverse()*/

            val getSummeryData = getSummeryTablesByStatus(getString(R.string.draft))

            if (getSummeryData.isEmpty()) {
           /*     tvDataNotFound.text = "$selectedValue ${getString(R.string.dataNotFound)}"
                tvDataNotFound.visibility = View.VISIBLE*/
            } else {
                tvDataNotFound.visibility = View.GONE
            }

            rvFormData.layoutManager = LinearLayoutManager(requireContext())
            val adapter = FormDataAdapter(dbHelper, requireContext(), getSummeryData, { item ->
                val intentAdd = Intent(requireContext(), EditFormDataActivity::class.java)
                intentAdd.putExtra("summeryId", item["id"]?.toInt())
                intentAdd.putExtra("formId", item["master_form_id"]?.toInt())
                intentAdd.putExtra(
                    "subId",
                    if (item["form_sub_id"] == "") 0 else item["form_sub_id"]?.toInt()
                )
                intentAdd.putExtra(
                    "responseId",
                    if (item["response_id"] == "") 0 else item["response_id"]?.toInt()
                )
                intentAdd.putExtra("status", item["status"])
                startActivity(intentAdd)
            }, { item ->
                //Sync Click
                syncDataToServer(item)
            })
            rvFormData.adapter = adapter

            alertDialog.dismiss()
        }
            selectedValue = getString(R.string.saved)

           /* val getSubmissionTableList = getSubmissionTableByFormId(FormDataActivity.formId)
            getSubmissionTableList.reverse()*/

            val getSummeryData = getSummeryTablesByStatus(getString(R.string.saved))

            if (getSummeryData.isEmpty()) {
              /*  tvDataNotFound.text = "$selectedValue ${getString(R.string.dataNotFound)}"
                tvDataNotFound.visibility = View.VISIBLE*/
            } else {
                tvDataNotFound.visibility = View.GONE
            }

            rvFormData.layoutManager = LinearLayoutManager(requireContext())
            val adapter = FormDataAdapter(dbHelper, requireContext(), getSummeryData, { item ->
                val intentAdd = Intent(requireContext(), EditFormDataActivity::class.java)
                intentAdd.putExtra("summeryId", item["id"]?.toInt())
                intentAdd.putExtra("formId", item["master_form_id"]?.toInt())
                intentAdd.putExtra(
                    "subId",
                    if (item["form_sub_id"] == "") 0 else item["form_sub_id"]?.toInt()
                )
                intentAdd.putExtra(
                    "responseId",
                    if (item["response_id"] == "") 0 else item["response_id"]?.toInt()
                )
                intentAdd.putExtra("status", item["status"])
                startActivity(intentAdd)
            }, { item ->
                //Sync Click
                syncDataToServer(item)
            })
            rvFormData.adapter = adapter

        tvSynced.setOnClickListener {
            selectedValue = getString(R.string.synced)

            /*al getSubmissionTableList = getSubmissionTableByFormId(FormDataActivity.formId)
            getSubmissionTableList.reverse()*/

            val getSummeryData = getSummeryTablesByStatus(getString(R.string.synced))

            if (getSummeryData.isEmpty()) {
               /* tvDataNotFound.text = "$selectedValue ${getString(R.string.dataNotFound)}"
                tvDataNotFound.visibility = View.VISIBLE*/
            } else {
                tvDataNotFound.visibility = View.GONE
            }

            rvFormData.layoutManager = LinearLayoutManager(requireContext())
            val adapter = FormDataAdapter(dbHelper, requireContext(), getSummeryData, { item ->
                Log.e("ccheckitemb",item.keys.toString()+item.values)

                val intentAdd = Intent(requireContext(), EditFormDataActivity::class.java)
                intentAdd.putExtra("summeryId", item["id"]?.toInt())
                intentAdd.putExtra("formId", item["master_form_id"]?.toInt())
                intentAdd.putExtra(
                    "subId",
                    if (item["form_sub_id"] == "") 0 else item["form_sub_id"]?.toInt()
                )
                intentAdd.putExtra(
                    "responseId",
                    if (item["response_id"] == "") 0 else item["response_id"]?.toInt()
                )
                intentAdd.putExtra("status", item["status"])
                startActivity(intentAdd)
            }, { item ->
                //Sync Click
                syncDataToServer(item)
            })
            rvFormData.adapter = adapter

            alertDialog.dismiss()
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun syncDataToServer(item: Map<String, String>) {
        showLoader(requireContext(), "Survey Syncing...")
        var formSubmissionData =
            dbHelper.getSubmissionTableById(if (item["form_sub_id"] == "") 0 else item["form_sub_id"]!!.toInt())
        var jsonData = Gson().fromJson(
            formSubmissionData?.responseJson.toString(),
            JSONFormDataModel::class.java
        )
        val skipLogicManager = SkipLogicManager()
        skipLogicManager.init(jsonData)
        var formResponseId = if (item["response_id"] == "") 0 else item["response_id"]?.toInt()
        getValueForRequest(formSubmissionData?.responseJson.toString(), formResponseId)

        Log.d(TAG, "" +
                ": $jsonRequestArray")
        val token = getPrefStringData(requireContext(), "token")

        if (formResponseId != 0 && formResponseId != null) {
            var syncedJSONRequest = SyncedJSONRequest(
                form_response_id = formResponseId,
                data = jsonRequestArray
            )
            sectionViewModel.callSyncedSurveyDataSubmit(
                token.toString(),
                item["master_form_id"]!!.toInt(),
                syncedJSONRequest
            ).observe(viewLifecycleOwner) { resource ->
                Log.d(TAG, "SYNCupBODY: " + resource)

                when (resource.status) {
                    Status.SUCCESS -> {
                        hideLoader()
                        val data = resource.data?.body()
                        if (data?.code == 1) {
                            Toast.makeText(requireContext(), data?.message, Toast.LENGTH_LONG)
                                .show()
                            Log.d(TAG, "SYNC BODY: " + data)
                            dbHelper.deleteRecordById(
                                MasterDBHelper.FORM_SUBMISSION_TABLE_NAME,
                                if (item["form_sub_id"] == "") 0L else item["form_sub_id"]!!.toLong()
                            )

                            val values = ContentValues()
                            values.put(MasterDBHelper.FORM_SUB_ID, "")
                            values.put(MasterDBHelper.RESPONSE_ID, data?.data?.form_response_id)
                            values.put(MasterDBHelper.STATUS, MasterDBHelper.SYNCED)

                            val tableName = jsonData.acronym + MasterDBHelper.SUMMERY
                            if (values.size() != 0) {
                                dbHelper.updateSummeryData(
                                    tableName = tableName,
                                    values,
                                    id = item["id"]!!.toInt()
                                )
                            }
                            onResume()
                        }else{
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        }
                    }

                    Status.ERROR -> {
                        hideLoader()
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        Log.d("TAG", "Error: ${resource.message}")
                    }

                    Status.LOADING -> {

                    }
                }
            }
        } else {
            sectionViewModel.callSurveyDataSubmit(
                token.toString(),
                item["master_form_id"]!!.toInt(),
                jsonRequestArray
            ).observe(viewLifecycleOwner) { resource ->
                Log.d(TAG, "SYNCupBODY: " + resource)

                when (resource.status) {
                    Status.SUCCESS -> {
                        hideLoader()
                        val data = resource.data?.body()
                        if (data?.code == 1) {
                            Toast.makeText(requireContext(), data?.message, Toast.LENGTH_LONG)
                                .show()
                            Log.d(TAG, "SYNC BODY: " + data)
                            dbHelper.deleteRecordById(
                                MasterDBHelper.FORM_SUBMISSION_TABLE_NAME,
                                if (item["form_sub_id"] == "") 0L else item["form_sub_id"]!!.toLong()
                            )

                            val values = ContentValues()
                            values.put(MasterDBHelper.FORM_SUB_ID, "")
                            values.put(MasterDBHelper.RESPONSE_ID, data?.data?.form_response_id)
                            values.put(MasterDBHelper.STATUS, MasterDBHelper.SYNCED)

                            val tableName = jsonData.acronym + MasterDBHelper.SUMMERY
                            if (values.size() != 0) {
                                dbHelper.updateSummeryData(
                                    tableName = tableName,
                                    values,
                                    id = item["id"]!!.toInt()
                                )
                            }
                            onResume()
                        }else{
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        }
                    }

                    Status.ERROR -> {
                        hideLoader()
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        Log.d("TAG", "Error: ${resource.message}")
                    }

                    Status.LOADING -> {
                    }
                }
            }
        }
    }

    lateinit var jsonRequestArray: ArrayList<JSONRequest>
    private fun getValueForRequest(formJsonData: String, formResponseId: Int?) {
        jsonRequestArray = ArrayList()
        val jsonObjectOld = JSONObject(formJsonData)

        val dataArray = jsonObjectOld.getJSONArray("data")

        val sectionLength = dataArray.length()

        for (i in 0 until sectionLength) {
            val sectionObject = dataArray.getJSONObject(i)
            checkSectionForGetValueForRequest(sectionObject, formResponseId)
        }


    }


    private fun checkSectionForGetValueForRequest(
        sectionObject: JSONObject,
        formResponseId: Int?
    ) {

        val childrenArray = sectionObject.getJSONArray("children")
        for (j in 0 until childrenArray.length()) {
            val childObject = childrenArray.getJSONObject(j)
            if (childObject.getString("type") == "SECTION") {
                checkSectionForGetValueForRequest(childObject, formResponseId)
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
                        var valueArray = JSONArray(value.toString())
                        checkSkipLogic(id,valueArray.get(valueArray.length()-1).toString())
                        if (formResponseId != 0 && formResponseId != null) {
                            if (childObject.getString("type") == "UPLOAD_IMAGE" || childObject.getString("type") == "CAPTURE_IMAGE") {

                                var valueDataArray = JSONArray()
                                var valueBase64DataArray = JSONArray()
                                for (i in 0 until valueArray.length()) {
                                    val valueObject = valueArray.getString(i)
//                                    if (valueObject.contains("/storage")) {
                                    if (isLocalFilePath(valueObject)) {
                                        var base64String=getPathToBase64String(valueObject)
                                        valueBase64DataArray.put(base64String.toString())
                                    } else {
                                        valueDataArray.put(valueObject)
                                    }
                                }
                                jsonRequest.value = valueDataArray.toString()
                                jsonRequest.newBase64Values = valueBase64DataArray.toString()
                            } else {
                                jsonRequest.value = value.toString()
                            }
                        } else {
                            if (childObject.getString("type") == "UPLOAD_IMAGE" || childObject.getString("type") == "CAPTURE_IMAGE") {
                                var imageValue=  JSONArray()
                                for (i in 0 until valueArray.length()) {
                                    val valueObject = valueArray.getString(i)
                                    var base64String=getPathToBase64String(valueObject)
                                    imageValue.put(base64String.toString())
                                }
                                jsonRequest.value =imageValue.toString()
                            }else if (childObject.getString("type") == "ADDABLE"){
                                val addableFormat = childObject.getJSONArray("addableFormat")


                                    for (i in 0 until valueArray.length()) {
                                        for (k in 0 until addableFormat.length()) {
                                            val addableItem = addableFormat.getJSONObject(k)
                                            val addableType = addableItem.getString("type")
                                            if (addableType == "UPLOAD_IMAGE" || addableType == "CAPTURE_IMAGE") {

                                                var base64String=getPathToBase64String((valueArray.get(i) as JSONObject).get(k.toString()).toString())
                                                (valueArray.get(i) as JSONObject).put(k.toString(),base64String.toString())
                                            }
                                    }
                                        jsonRequest.value =valueArray.toString()
                                }

                            }else {
                                jsonRequest.value = value.toString()
                            }
                        }
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
    private fun checkSkipLogic(childId: String, value: String) {
        if (allIds.contains(childId)){
            if (checkIfValueMatched(formSkipLogics, childId, value)) {
                //Log.d(TAG, "Change detected in: $childId")
                computeFlagNRefreshSL()

                //  Log.d(TAG,"Flag computation and field visibility updated!")
            }
        }
    }
    fun checkIfValueMatched(data: ArrayList<SkipLogicModel>, targetId: String, value: Any, matched: Boolean = false): Boolean {
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
                } else if (item.skipLogicVal == null && getFlagById(item.skipLogicQ.toString(),
                        formSkipLogics
                    )) {
                    item.flag = true;
                    Log.d(TAG, "Match Found! IF ELSE");
                }
                else {
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
        val previousParentFlags = formSkipLogics.map { item ->
            SkipLogicModel(
                flag = item.flag,
                skipLogicQ = item.skipLogicQ
            )
        }

        flagComputation(formSkipLogics)

        val newParentFlags = formSkipLogics.map { item ->
            SkipLogicModel(
                flag = item.flag,
                skipLogicQ = item.skipLogicQ
            )
        }

        for (i in formSkipLogics.indices) {
            if (previousParentFlags[i].flag != newParentFlags[i].flag ||
                previousParentFlags[i].skipLogicQ != newParentFlags[i].skipLogicQ
            ) {
                updateFlagById(newParentFlags[i].skipLogicQ.toString(),
                    newParentFlags[i].flag == true, formSkipLogics
                )

                flagComputation(formSkipLogics)
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
                item.flag = item.data!!.all {child -> child.flag == true };
            }
        }
    }
    fun updateFlagById(targetId: String, newFlag: Boolean, data: ArrayList<SkipLogicModel>) {
        data.forEach { item ->
            if (item.skipLogicQ == targetId) {
                item.flag = newFlag
            }
            if (!item.data.isNullOrEmpty()) {
                updateFlagById(targetId, newFlag, item.data!!)
            }
        }
    }
    private fun initialTimeCheckSkipLogic(childId: String): Boolean {
        var fild = formSkipLogics.find { it.skipLogicQ == childId }
        if (fild != null) {
            return fild.flag == true
        } else {
            return false
        }
    }

    class SkipLogicManager {
        fun recursiveBuildSkipLogics(section: Data) {
            section.children?.forEach { field ->
                if (field.type != "SECTION") {
                    if (!field.skipLogic.isNullOrEmpty()) {
                        val skipLogicElement = SkipLogicModel(
                            skipLogicQ = field.id,
                            relation = "or",
                            flag = false,
                            data = arrayListOf()
                        )
                        field.skipLogic?.forEach { fieldSl ->
                            skipLogicElement.data?.add(fieldSl)
                        }
                        formSkipLogics.add(skipLogicElement)
                    }
                } else if (field.type == "SECTION") {
                    Log.d(TAG, field.title.toString())
                    var data = Data(
                        acronym = field.acronym,
                        children = field.children,
                        isActive = field.isActive,
                        title = field.title,
                        type = field.type
                    )
                    recursiveBuildSkipLogics(data)
                }
            }
        }

        fun buildSkipLogics(jsonData: JSONFormDataModel) {

            jsonData.data?.forEach { section ->
                recursiveBuildSkipLogics(section)
            }
            Log.d(TAG, "Form Skip Logics Data Structure: ")
//            Log.d(TAG, formSkipLogics.toString())
        }

        fun getAllIds(data: ArrayList<SkipLogicModel>): ArrayList<String> {
            val ids = arrayListOf<String>()
            data.forEach { item ->
                if (item.skipLogicQ != null) {
                    ids.add(item.skipLogicQ.toString())
                }
                if (!item.data.isNullOrEmpty()) {

                    ids.addAll(getAllIds(item.data!!))
                }
            }
            return ids
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

        fun checkIfValueMatched(
            data: ArrayList<SkipLogicModel>,
            targetId: String,
            value: Any,
            matched: Boolean = false
        ): Boolean {
            var matchedResult = matched
            data.forEach { item ->
                if (!item.data.isNullOrEmpty()) {
                    val valReturned =
                        checkIfValueMatched(item.data!!, targetId, value, matchedResult)
                    if (valReturned) {
                        matchedResult = true
                    }
                }
                if (item.skipLogicQ == targetId) {
                    Log.d(TAG, "Checking if value following values match or not")
                    Log.d(TAG, "Item's value: ${item.skipLogicVal}")
                    Log.d(TAG, "Value to be checked: $value")
                    if (item.skipLogicVal == value) {
                        item.flag = true;
                        Log.d(TAG, "Match Found! IF");
                    } else if (item.skipLogicVal == null && getFlagById(
                            item.skipLogicQ.toString(),
                            formSkipLogics
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

        fun computeFlagNRefreshSL() {
            val previousParentFlags = formSkipLogics.map { item ->
                SkipLogicModel(
                    flag = item.flag,
                    skipLogicQ = item.skipLogicQ
                )
            }

            flagComputation(formSkipLogics)

            val newParentFlags = formSkipLogics.map { item ->
                SkipLogicModel(
                    flag = item.flag,
                    skipLogicQ = item.skipLogicQ
                )
            }

            for (i in formSkipLogics.indices) {
                if (previousParentFlags[i].flag != newParentFlags[i].flag ||
                    previousParentFlags[i].skipLogicQ != newParentFlags[i].skipLogicQ
                ) {
                    updateFlagById(
                        newParentFlags[i].skipLogicQ.toString(),
                        newParentFlags[i].flag == true, formSkipLogics
                    )

                    flagComputation(formSkipLogics)
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
                }
                if (!item.data.isNullOrEmpty()) {
                    updateFlagById(targetId, newFlag, item.data!!)
                }
            }
        }

        fun init(jsonData: JSONFormDataModel) {
            allIds.clear()
            formSkipLogics.clear()
            Log.d(TAG, "Skip logic initialization started...")
            buildSkipLogics(jsonData)
            allIds = getAllIds(formSkipLogics)
            Log.d(TAG, "All ids: ${allIds}")
            /*allIds.forEach { id ->

                if (checkIfValueMatched(formSkipLogics, "permanent_address_same_current_address", "0")) {
                    Log.d(TAG, "Change detected in: $id")
                    computeFlagNRefreshSL();
                    Log.d(TAG,"Flag computation and field visibility updated!");
                }
            }
            Log.d(TAG, formSkipLogics.toString())*/
        }
    }
}