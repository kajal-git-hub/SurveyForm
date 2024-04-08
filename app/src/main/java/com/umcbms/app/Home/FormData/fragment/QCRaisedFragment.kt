package com.umcbms.app.Home.FormData.fragment

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umcbms.app.Home.FormData.FormDataActivity
import com.umcbms.app.Home.FormData.QCFormDataActivity
import com.umcbms.app.Home.FormData.QCListAdapter
import com.umcbms.app.Home.FormData.viewModel.SectionViewModel
import com.umcbms.app.JSONModel.Data
import com.umcbms.app.JSONModel.JSONFormDataModel
import com.umcbms.app.JSONModel.SkipLogicModel
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.R
import com.umcbms.app.api.Status
import com.umcbms.app.api.request.QCJSONRequest
import com.umcbms.app.api.respose.QCData
import com.umcbms.app.getPrefStringData
import com.umcbms.app.hideLoader
import com.umcbms.app.isJsonArray
import com.umcbms.app.showLoader
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

private val TAG = "QCRaisedFragment"
@SuppressLint("Range")
class QCRaisedFragment : Fragment() {

    private lateinit var dbHelper: MasterDBHelper
    private lateinit var sectionViewModel: SectionViewModel
    private lateinit var recyclerViewQCList: RecyclerView
    private lateinit var tvDataNotFound: TextView

    private var clickPosition=0
    companion object {
        var formSkipLogics: ArrayList<SkipLogicModel> = arrayListOf()
        var allIds: ArrayList<String> = arrayListOf()
    }

    private val adapter by lazy {
        QCListAdapter( requireContext(),
            dbHelper,
            { position: Int, item: QCData ->
            // Download
                       getQcSchema(position,item)

        },{ position: Int, item: QCData ->
            clickPosition=position
            // QC
            val intentAdd = Intent(requireContext(), QCFormDataActivity::class.java)
            intentAdd.putExtra("qcId", item.id?.toInt())
            intentAdd.putExtra("formId", FormDataActivity.formId)
            intentAdd.putExtra("qvTableName",FormDataActivity.qcTableName)

            startActivity(intentAdd)
        },{ position: Int, item: QCData ->
            // Sync
            syncDataToServer(item,position)

        })

    }

    private fun syncDataToServer(item: QCData, position: Int) {
        showLoader(requireContext(), "Survey Syncing...")
        var qcCursor= dbHelper.getAllRecordsWithCondition(FormDataActivity.qcTableName, "${MasterDBHelper.QC_ID}=${item.id}")
        qcCursor.moveToFirst()
        if (qcCursor.count > 0) {
            var qcSchema = qcCursor.getString(qcCursor.getColumnIndex(MasterDBHelper.QC_SCHEMA))
            var jsonData = Gson().fromJson(
                qcSchema,
                JSONFormDataModel::class.java
            )
            val skipLogicManager = SkipLogicManager()
            skipLogicManager.init(jsonData)
            var formResponseId=item.id?.toInt()
            getValueForRequest(qcSchema, formResponseId)
            val token = getPrefStringData(requireContext(), "token")

            if (formResponseId != 0 && formResponseId != null) {
//                Log.d(TAG, "syncDataToServer: JSONARRAY=="+jsonRequestArray)
                sectionViewModel.surveyQCResolveSubmit(
                    token.toString(),
                    item.formId.toInt(),
                    formResponseId,
                    jsonRequestArray
                ).observe(viewLifecycleOwner) { resource ->
                    Log.d(TAG, "SYNCData BODY: " + resource)

                    when (resource.status) {
                        Status.SUCCESS -> {
                            val data = resource.data?.body()
                            if (data?.code == 1) {
                                Toast.makeText(requireContext(), data?.message, Toast.LENGTH_LONG)
                                    .show()
                                Log.d(TAG, "SYNC BODY: " + data)
                                dbHelper.deleteRecordWithCondition(
                                    FormDataActivity.qcTableName,
                                    MasterDBHelper.QC_ID,
                                    item.id
                                )
                                FormDataActivity.qcRaisedList.removeAt(position)
                               adapter.notifyItemRemoved(position)
                            }
                        }

                        Status.ERROR -> {
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                            Log.d("TAG", "Error: ${resource.message}")
                        }

                        Status.LOADING -> {
                            hideLoader()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setRecycleView()
    }
    private fun getQcSchema(position: Int,item: QCData) {
        val token = getPrefStringData(requireContext(), "token")
        sectionViewModel.getQCRaisedSchemaValue(token.toString(), item.formId.toInt(), item.id.toInt())
            .observe(this) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
//                        Log.d(TAG, "getSyncedSchemaValue:="+response.data?.body())
                        if (response.data?.body()?.code == 1) {
                            val jsonString = Gson().toJson(response.data?.body()?.data?.schema)
                              val values = ContentValues()
                                        values.put(MasterDBHelper.QC_ID, item.id)
                                        values.put(MasterDBHelper.FORM_ID, item.formId)
                                        values.put(MasterDBHelper.STATUS, MasterDBHelper.QCRAISED)
                                        values.put(MasterDBHelper.PRIMARY_VIEW, item.primary_view_map.toString())
                                        values.put(MasterDBHelper.QC_SCHEMA, jsonString)
                                        var qcId=dbHelper.insertData(FormDataActivity.qcTableName,values)
                            val QCData = QCData(
                                item.id,
                                qcId.toString(),
                                item.formId.toString(),
                                MasterDBHelper.QCRAISED,
                                primary_view_map = item.primary_view_map,
                                        qc_schema=jsonString
                            )
                            adapter.formList[position]=QCData
                            adapter.notifyItemChanged(position)
                        }
                    }

                    Status.ERROR -> {
                        Log.d(TAG, "getSyncedSchemaValue: ${response.message}")
                        Toast.makeText(
                            requireContext(),
                            "getSyncedSchemaValue : ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    Status.LOADING -> {}
                }

            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHelper = MasterDBHelper(requireContext())
        sectionViewModel = ViewModelProvider(this).get(SectionViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qcraised, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewQCList = view.findViewById(R.id.recyclerViewQCList)
        tvDataNotFound = view.findViewById(R.id.tvDataNotFound)

        setRecycleView()
    }

    private fun setRecycleView() {
        recyclerViewQCList.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        adapter.formList = FormDataActivity.qcRaisedList
        recyclerViewQCList.adapter = adapter

        if (FormDataActivity.qcRaisedList.isEmpty()) {
            tvDataNotFound.text = "${getString(R.string.dataNotFound)}"
            tvDataNotFound.visibility = View.VISIBLE
        } else {
            tvDataNotFound.visibility = View.GONE
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

    lateinit var jsonRequestArray: ArrayList<QCJSONRequest>
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
                var qcResolve: Boolean = false
                if (childObject.has("qcResolved")) {
                    qcResolve = childObject.getBoolean("qcResolved")
                }
                var qcInteract: Boolean = false
                if (childObject.has("qcInteract")) {
                    qcInteract = childObject.getBoolean("qcInteract")
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

                var jsonRequest = QCJSONRequest()
                jsonRequest.id = id
                jsonRequest.qcResolve=qcResolve
                jsonRequest.qcInteract=qcInteract
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
                                    if (valueObject.contains("/storage")) {
//                                    if (isLocalFilePath(valueObject)) {
                                        valueDataArray.put(valueObject)
                                    } else {
                                        valueBase64DataArray.put(valueObject)
                                    }
                                }
                                jsonRequest.value = valueDataArray.toString()
                                jsonRequest.newBase64Values = valueBase64DataArray.toString()
                            } else {
                                jsonRequest.value = value.toString()
                            }
                        } else {
                            jsonRequest.value = value.toString()
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
}