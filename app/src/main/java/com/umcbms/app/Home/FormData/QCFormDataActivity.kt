package com.umcbms.app.Home.FormData

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.umcbms.app.Home.FormData.viewModel.SectionViewModel
import com.umcbms.app.JSONModel.Data
import com.umcbms.app.JSONModel.JSONFormDataModel
import com.umcbms.app.JSONModel.SkipLogicModel
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.R
import com.umcbms.app.getPrefStringData
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.umcbms.app.JSONModel.AddableFormat
import com.umcbms.app.JSONModel.VisibleLogicModel
import org.json.JSONArray
import org.json.JSONObject

private val TAG = "QCFormDataActivity"

class QCFormDataActivity : AppCompatActivity() {

    private lateinit var dynamicContainerLl: LinearLayout
    private lateinit var txtTitleName: TextView
    private lateinit var ivBack: ImageView
    private lateinit var jsonData: JSONFormDataModel
    private lateinit var dbHelper: MasterDBHelper
    private lateinit var tbLayout: CustomTabLayout
    private var subId: Int = 0
    private var responseId: Int = 0
    private var formId: Int = 0

    private lateinit var sectionViewModel: SectionViewModel

    companion object {
        lateinit var viewPager: NonSwipeableViewPager
        var formJsonData = ""
        var status = ""
        var qvTableName = ""
        var summeryId: Int = 0
        var qcId: Int = 0
        var formSkipLogics: ArrayList<SkipLogicModel> = arrayListOf()
        var allIds: ArrayList<String> = arrayListOf()

        val visibleLogicManager = VisibleLogicManager()

        var formVisibleLogics: ArrayList<VisibleLogicModel> = arrayListOf()
        var visibleLogicAllIds: ArrayList<String> = arrayListOf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        super.onPause()
        dbHelper.updateQCTableSchema(qvTableName,qcId, formJsonData)
        /*if (status!= MasterDBHelper.SYNCED) {
            var insertedId = 0L
            var cursor = dbHelper.getSingleRecord(MasterDBHelper.FORM_SUBMISSION_TABLE_NAME, subId)
            if (cursor.count == 0) {
                val currentDateTime = getCurrentDateTime()
                insertedId = dbHelper.insertFormSubmissionData(
                    FormSubmissionData(
                        formId = formId.toInt(),
                        status = MasterDBHelper.DRAFT,
                        responseJson = formJsonData,
                        createdBy = null,
                        createdAt = currentDateTime,
                        updateAt = null,
                        isDeleted = null,
                        deletedAt = null
                    )
                )
                subId = insertedId.toInt()
            } else {
                dbHelper.updateSubmissionTable(subId, formJsonData)
            }

            val jsonString = formJsonData
            val jsonObject = JSONObject(jsonString)
            val dataArray = jsonObject.getJSONArray("data")

            val values = ContentValues()

            for (j in 0 until dataArray.length()) {
                val sectionObject = dataArray.getJSONObject(j)
                sectionFun(sectionObject, values)
            }

            val tableName = jsonObject.getString("acronym") + MasterDBHelper.SUMMERY

            if (values.size() != 0) {
                dbHelper.updateSummeryData(tableName = tableName, values, id = summeryId)
            }
        }*/
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
                            if (type == "UPLOAD_IMAGE" || type=="CAPTURE_IMAGE") {
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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility", "Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_form_data)
        dbHelper = MasterDBHelper(this)
        txtTitleName = findViewById(R.id.txtTitleName_Home)
        ivBack = findViewById(R.id.ivBack)

        ivBack.setOnClickListener {
            finish()
        }

        val userName = getPrefStringData(this, "userName")
        txtTitleName.text = userName

        sectionViewModel = ViewModelProvider(this).get(SectionViewModel::class.java)

//        try {

        qcId = intent.getIntExtra("qcId", 0)

        formId = intent.getIntExtra("formId", 0)
        qvTableName = intent.getStringExtra("qvTableName").toString()

        //formJsonData = intent.getStringExtra("formJsonData").toString()
        Log.d(TAG, "onCreate: =="+qcId+"=="+formId+"=="+qvTableName)
        var cursor = dbHelper.getAllRecordsWithCondition(qvTableName, "${MasterDBHelper.QC_ID}=$qcId")

        cursor.moveToFirst()
        if (cursor.count > 0) {
            status = cursor.getString(cursor.getColumnIndex(MasterDBHelper.STATUS))
            formJsonData = cursor.getString(cursor.getColumnIndex(MasterDBHelper.QC_SCHEMA))

            jsonData = Gson().fromJson(formJsonData, JSONFormDataModel::class.java)

            val skipLogicManager = SkipLogicManager()
            skipLogicManager.init(jsonData)

            dynamicContainerLl = findViewById(R.id.dynamicContainerLl)

            val hs = HorizontalScrollView(this)
            val layoutParamsHs = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            hs.layoutParams = layoutParamsHs
            dynamicContainerLl.addView(hs, layoutParamsHs)

            tbLayout = CustomTabLayout(this)
            val layoutParamsTbLayout = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.MATCH_PARENT
            )
            tbLayout.layoutParams = layoutParamsTbLayout
            tbLayout.tabGravity = TabLayout.GRAVITY_CENTER
            tbLayout.tabMode = TabLayout.MODE_FIXED


            hs.addView(tbLayout, layoutParamsTbLayout)

            viewPager = NonSwipeableViewPager(this)
            viewPager.id = View.generateViewId()

            viewPager.setOffscreenPageLimit(1)
            val layoutParamsViewPager = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            viewPager.layoutParams = layoutParamsViewPager
            dynamicContainerLl.addView(viewPager, layoutParamsViewPager)

            val adapter = ViewPagerAdapter(
                supportFragmentManager,
                jsonData.data,
                jsonData,
                formJsonData,
                qcId,
                formId,
                this
            )

            viewPager.adapter = adapter


            tbLayout.setupWithViewPager(viewPager)


            tbLayout.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                @SuppressLint("SuspiciousIndentation")
                override fun onTabSelected(tab: TabLayout.Tab) {

                    val currentFragment =
                        adapter.getItem(tab.position) as QCTabFragment
                    if (currentFragment.isOnCreatedView) {
                        currentFragment.onTabChanged(tab.position)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
//        jsonData = response.data?.body()?.data?.schema!!
//        formJsonData = Gson().toJson(jsonData)



        /*val token = getPrefStringData(this, "token")
        sectionViewModel.getQCRaisedSchemaValue(token.toString(), formId, qcId)
            .observe(this) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
//                        Log.d(TAG, "getSyncedSchemaValue:="+response.data?.body())
                        if (response.data?.body()?.success == true) {
                            jsonData = response.data?.body()?.data?.schema!!
                            formJsonData = Gson().toJson(jsonData)
                            val skipLogicManager = SkipLogicManager()
                            skipLogicManager.init(jsonData)

                            dynamicContainerLl = findViewById(R.id.dynamicContainerLl)

                            val hs = HorizontalScrollView(this)
                            val layoutParamsHs = LinearLayoutCompat.LayoutParams(
                                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                            )
                            hs.layoutParams = layoutParamsHs
                            dynamicContainerLl.addView(hs, layoutParamsHs)

                            tbLayout = CustomTabLayout(this)
                            val layoutParamsTbLayout = LinearLayoutCompat.LayoutParams(
                                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                LinearLayoutCompat.LayoutParams.MATCH_PARENT
                            )
                            tbLayout.layoutParams = layoutParamsTbLayout
                            tbLayout.tabGravity = TabLayout.GRAVITY_CENTER
                            tbLayout.tabMode = TabLayout.MODE_FIXED


                            hs.addView(tbLayout, layoutParamsTbLayout)

                            val viewPager = NonSwipeableViewPager(this)
                            viewPager.id = View.generateViewId()

                            viewPager.setOffscreenPageLimit(1)
                            val layoutParamsViewPager = LinearLayoutCompat.LayoutParams(
                                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                            )
                            viewPager.layoutParams = layoutParamsViewPager
                            dynamicContainerLl.addView(viewPager, layoutParamsViewPager)

                            val adapter = ViewPagerAdapter(
                                supportFragmentManager,
                                jsonData.data,
                                jsonData,
                                formJsonData,
                                subId,
                                formId,
                                this
                            )

                            viewPager.adapter = adapter


                            tbLayout.setupWithViewPager(viewPager)


                            tbLayout.addOnTabSelectedListener(object :
                                TabLayout.OnTabSelectedListener {
                                @SuppressLint("SuspiciousIndentation")
                                override fun onTabSelected(tab: TabLayout.Tab) {

                                    val currentFragment =
                                        adapter.getItem(tab.position) as QCTabFragment
                                    if (currentFragment.isOnCreatedView) {
                                        currentFragment.onTabChanged(tab.position)
                                    }
                                }

                                override fun onTabUnselected(tab: TabLayout.Tab) {}

                                override fun onTabReselected(tab: TabLayout.Tab) {}
                            })
                        }
                    }

                    Status.ERROR -> {
                        Log.d(TAG, "getSyncedSchemaValue: ${response.message}")
                        Toast.makeText(
                            this,
                            "getSyncedSchemaValue : ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    Status.LOADING -> {}
                }

            }*/

        /* jsonData = Gson().fromJson(formJsonData, JSONFormDataModel::class.java)
         val skipLogicManager = SkipLogicManager()
         skipLogicManager.init(jsonData)

         dbHelper = MasterDBHelper(this)

         dynamicContainerLl = findViewById(R.id.dynamicContainerLl)

         val hs = HorizontalScrollView(this)
         val layoutParamsHs = LinearLayoutCompat.LayoutParams(
             LinearLayoutCompat.LayoutParams.MATCH_PARENT,
             LinearLayoutCompat.LayoutParams.WRAP_CONTENT
         )
         hs.layoutParams = layoutParamsHs
         dynamicContainerLl.addView(hs, layoutParamsHs)

         tbLayout = CustomTabLayout(this)
         val layoutParamsTbLayout = LinearLayoutCompat.LayoutParams(
             LinearLayoutCompat.LayoutParams.MATCH_PARENT,
             LinearLayoutCompat.LayoutParams.MATCH_PARENT
         )
         tbLayout.layoutParams = layoutParamsTbLayout
         tbLayout.tabGravity = TabLayout.GRAVITY_CENTER
         tbLayout.tabMode = TabLayout.MODE_FIXED


         hs.addView(tbLayout, layoutParamsTbLayout)

         val viewPager = NonSwipeableViewPager(this)
         viewPager.id = View.generateViewId()

         viewPager.setOffscreenPageLimit(1)
         val layoutParamsViewPager = LinearLayoutCompat.LayoutParams(
             LinearLayoutCompat.LayoutParams.MATCH_PARENT,
             LinearLayoutCompat.LayoutParams.WRAP_CONTENT
         )
         viewPager.layoutParams = layoutParamsViewPager
         dynamicContainerLl.addView(viewPager, layoutParamsViewPager)

         val adapter = ViewPagerAdapter(
             supportFragmentManager,
             jsonData.data,
             jsonData,
             formJsonData,
             subId,
             formId,
             this
         )

         viewPager.adapter = adapter


         tbLayout.setupWithViewPager(viewPager)


         tbLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
             @SuppressLint("SuspiciousIndentation")
             override fun onTabSelected(tab: TabLayout.Tab) {

                 val currentFragment = adapter.getItem(tab.position) as TabFragment
                 if (currentFragment.isOnCreatedView) {
                     currentFragment.onTabChanged(tab.position)
                 }
             }

             override fun onTabUnselected(tab: TabLayout.Tab) {}

             override fun onTabReselected(tab: TabLayout.Tab) {}
         })*/
//        } catch (e: Exception) {
//            Log.d(TAG, "Exception_171: ${e.message}")
//        }


    }


    class ViewPagerAdapter(
        fm: FragmentManager,
        val data: List<Data>?,
        private val jsonData: JSONFormDataModel,
        private val formJsonData: String?,
        private var subId: Int,
        private var formId: Int,
        private val context: Context
    ) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList: MutableList<Fragment> = mutableListOf()

        init {
            // Initialize the fragment list with your fragments
            jsonData.data?.forEachIndexed { index, data ->
                fragmentList.add(QCTabFragment())
            }

            // Add more fragments if needed
        }

        override fun getItem(position: Int): Fragment {
//            var fragment = SectionFragment()
            val bundle = Bundle()
            bundle.putInt("sectionPos", position)
//            bundle.putParcelable("section", data?.get(position))
            jsonData.data?.size?.let { bundle.putInt("endSection", it) }
            bundle.putInt("subId", subId.toInt())
            bundle.putInt("formId", formId.toInt())
            fragmentList[position].arguments = bundle
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return data!!.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return data?.get(position)?.title.toString()
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
//                            flag = field.skipLogic?.all{  child -> child.flag == true },
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
            Log.d(TAG, "Skip logic initialization started...")
            buildSkipLogics(jsonData)
            allIds = getAllIds(formSkipLogics)
            Log.d(TAG, "All ids: $allIds")
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

    class VisibleLogicManager {
        fun setAddableLocalIds(section: List<AddableFormat>, countAddable : Int): List<AddableFormat> {
            section.forEach { field ->
                field.localId=field.localId+"_"+countAddable
                if (!field.visibleLogic.isNullOrEmpty()) {
                    setAddableVisibleLogicLocalIds(field.visibleLogic!!,countAddable) {
                        field.visibleLogic = it
                    }
                }
            }
            return section
        }
        fun setAddableVisibleLogicLocalIds(visibleLogic: List<VisibleLogicModel>,countAddable : Int, onClickBack: (List<VisibleLogicModel>) -> Unit) {
            visibleLogic.forEach { field ->
                if (!field.data.isNullOrEmpty()) {
                    setAddableVisibleLogicDataLocalIds(field.data!!,countAddable) {
                        onClickBack.invoke(visibleLogic)
                    }
                }else{

                }
            }

        }
        fun setAddableVisibleLogicDataLocalIds(visibleLogicData: List<VisibleLogicModel>,countAddable : Int, onClickBack: (List<VisibleLogicModel>) -> Unit) {
            visibleLogicData.forEach { field ->
                if (!field.data.isNullOrEmpty()) {
                    setAddableVisibleLogicDataLocalIds(field.data!!,countAddable) {
                        onClickBack.invoke(it)
                    }
                }else{
                    field.visibleLogicQ=field.visibleLogicQ+"_"+countAddable
                }
            }
        }
        fun recursiveBuildVisibleLogicsForAddable(section: List<AddableFormat>) {
            section.forEach { field ->
                if (!field.visibleLogic.isNullOrEmpty()) {
                    val visibleLogicElement = VisibleLogicModel(
                        visibleLogicQ = field.localId,
                        relation = "or",
                        flag = false,
                        data = arrayListOf()
                    )
                    field.visibleLogic?.forEach { fieldSl ->
                        visibleLogicElement.data?.add(fieldSl)
                    }
                    formVisibleLogics.add(visibleLogicElement)
                }
            }
        }
        fun recursiveBuildVisibleLogics(section: Data) {
            section.children?.forEach { field ->
                if (field.type != "SECTION") {
                    if (field.type!="ADDABLE") {
                        if (!field.visibleLogic.isNullOrEmpty()) {
                            val visibleLogicElement = VisibleLogicModel(
                                visibleLogicQ = field.id,
                                relation = "or",
                                flag = false,
                                data = arrayListOf()
                            )
                            field.visibleLogic?.forEach { fieldSl ->
                                visibleLogicElement.data?.add(fieldSl)
                            }
                            formVisibleLogics.add(visibleLogicElement)
                        }
                    }else{
                        if (!field.visibleLogic.isNullOrEmpty()) {
                            val visibleLogicElement = VisibleLogicModel(
                                visibleLogicQ = field.id,
                                relation = "or",
                                flag = false,
                                data = arrayListOf()
                            )
                            field.visibleLogic?.forEach { fieldSl ->
                                visibleLogicElement.data?.add(fieldSl)
                            }
                            formVisibleLogics.add(visibleLogicElement)
                        }
                        /*field.addableFormat?.forEach {
                            if (!it.visibleLogic.isNullOrEmpty()) {
                                val visibleLogicElement = VisibleLogicModel(
                                    visibleLogicQ = it.localId,
                                    relation = "or",
                                    flag = false,
                                    data = arrayListOf()
                                )
                                it.visibleLogic?.forEach { fieldSl ->
                                    visibleLogicElement.data?.add(fieldSl)
                                }
                                formVisibleLogics.add(visibleLogicElement)
                            }

                        }*/
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
                    recursiveBuildVisibleLogics(data)
                }
            }
        }

        fun buildVisibleLogics(jsonData: JSONFormDataModel) {

            jsonData.data?.forEach { section ->
                recursiveBuildVisibleLogics(section)
            }
            Log.d(TAG, "Form Visible Logics Data Structure: ")
//            Log.d(TAG, formVisibleLogics.toString())
        }

        fun getAllIds(data: ArrayList<VisibleLogicModel>): ArrayList<String> {
            val ids = arrayListOf<String>()
            data.forEach { item ->
                if (item.visibleLogicQ != null) {
                    ids.add(item.visibleLogicQ.toString())
                }
                if (!item.data.isNullOrEmpty()) {

                    ids.addAll(getAllIds(item.data!!))
                }
            }
            return ids
        }

        fun getFlagById(targetId: String, data: ArrayList<VisibleLogicModel>): Boolean {
            data.forEach { item ->
                if (item.visibleLogicQ == targetId) {
                    return item.flag == true;
                }
                if (!item.data.isNullOrEmpty()) {
                    return this.getFlagById(targetId, item.data!!);
                }
            }
            return false
        }

        fun checkIfValueMatched(
            data: ArrayList<VisibleLogicModel>,
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
                if (item.visibleLogicQ == targetId) {
                    Log.d(TAG, "Checking if value following values match or not")
                    Log.d(TAG, "Item's value: ${item.visibleLogicVal}")
                    Log.d(TAG, "Value to be checked: $value")
                    if (item.visibleLogicVal == value) {
                        item.flag = true;
                        Log.d(TAG, "Match Found! IF");
                    } else if (item.visibleLogicVal == null && getFlagById(
                            item.visibleLogicQ.toString(),
                            formVisibleLogics
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
            val previousParentFlags = formVisibleLogics.map { item ->
                VisibleLogicModel(
                    flag = item.flag,
                    visibleLogicQ = item.visibleLogicQ
                )
            }

            flagComputation(formVisibleLogics)

            val newParentFlags = formVisibleLogics.map { item ->
                VisibleLogicModel(
                    flag = item.flag,
                    visibleLogicQ = item.visibleLogicQ
                )
            }

            for (i in formVisibleLogics.indices) {
                if (previousParentFlags[i].flag != newParentFlags[i].flag ||
                    previousParentFlags[i].visibleLogicQ != newParentFlags[i].visibleLogicQ
                ) {
                    updateFlagById(
                        newParentFlags[i].visibleLogicQ.toString(),
                        newParentFlags[i].flag == true, formVisibleLogics
                    )

                    flagComputation(formVisibleLogics)
                }
            }
        }

        fun flagComputation(data: ArrayList<VisibleLogicModel>) {
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

        fun updateFlagById(targetId: String, newFlag: Boolean, data: ArrayList<VisibleLogicModel>) {
            data.forEach { item ->
                if (item.visibleLogicQ == targetId) {
                    item.flag = newFlag
                }
                if (!item.data.isNullOrEmpty()) {
                    updateFlagById(targetId, newFlag, item.data!!)
                }
            }
        }

        fun init(jsonData: JSONFormDataModel) {
            Log.d(TAG, "Visible logic initialization started...")
            buildVisibleLogics(jsonData)
            visibleLogicAllIds = getAllIds(formVisibleLogics)
            Log.d(TAG, "All VisibleLogic ids: ${visibleLogicAllIds}")
            /*allIds.forEach { id ->

                if (checkIfValueMatched(formVisibleLogics, "permanent_address_same_current_address", "0")) {
                    Log.d(TAG, "Change detected in: $id")
                    computeFlagNRefreshSL();
                    Log.d(TAG,"Flag computation and field visibility updated!");
                }
            }
            Log.d(TAG, formVisibleLogics.toString())*/
        }
    }
}