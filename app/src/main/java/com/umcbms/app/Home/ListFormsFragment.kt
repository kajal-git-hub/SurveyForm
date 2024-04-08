package com.umcbms.app.Home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umcbms.app.MasterDB.FormData
import com.umcbms.app.MasterDB.FormDataID
import com.umcbms.app.MasterDB.FormJsonDataModel
import com.umcbms.app.MasterDB.FormListModel
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.R


class ListFormsFragment : Fragment() {

    private lateinit var recyclerViewForm: RecyclerView
    private lateinit var formList: ArrayList<FormData>
    private lateinit var masterDBHelper: MasterDBHelper
    private var formName = ArrayList<FormListModel>()
    private var formJsonData = ArrayList<FormJsonDataModel>()
    private var formId = ArrayList<FormDataID>()
    //private var subFormId = ArrayList<SubFormId>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_forms, container, false)
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAGB", "onResume: ListFormsFragment ${getFormList()}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        masterDBHelper = MasterDBHelper(requireContext())

        formList = ArrayList()

        val getFormList = getFormList()

        for (i in 0 until getFormList.size) {
            formName.add(FormListModel(getFormList[i].formName + "_" + getFormList[i].id))
            formJsonData.add(FormJsonDataModel(getFormList[i].formSchema!!))
            formId.add(FormDataID(getFormList[i].formId!!))
        }

        /*val getFormIDList = getSubmissionTableData()
        for (i in 0 until getSubmissionTableList.size){
            subFormId.add(SubFormId(getSubmissionTableList[i].formId))
        }*/

        recyclerViewForm = view.findViewById(R.id.recyclerViewForm)
        recyclerViewForm.layoutManager = LinearLayoutManager(requireContext())
        val formAdapter = FormAdapter(requireContext(), formName, formJsonData, formId, masterDBHelper)
        recyclerViewForm.adapter = formAdapter
    }

    fun getFormList(): ArrayList<FormData> {
        val formData = masterDBHelper.getAllForms()

        val formListDB = ArrayList<FormData>()

        formListDB.addAll(formData)

        return formListDB
    }


    /*private fun getSubmissionTableData(): ArrayList<FormSubmissionData> {
        val formData = masterDBHelper.getSubmissionTable()

        val formListDB = ArrayList<FormSubmissionData>()

        formListDB.addAll(formData)

        return formListDB
    }*/


}