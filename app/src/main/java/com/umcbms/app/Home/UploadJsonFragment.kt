package com.umcbms.app.Home

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.umcbms.app.JSONModel.JSONFormDataModel
import com.umcbms.app.MasterDB.FormData
import com.umcbms.app.MasterDB.MasterDBHelper
import com.umcbms.app.R
import com.umcbms.app.getCurrentDateTime
import com.google.gson.Gson

class UploadJsonFragment : Fragment() {

    private lateinit var jsonUploadImg: ImageView
    private var jsonContent: String? = null
    private lateinit var masterDBHelper: MasterDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_json, container, false)
    }

    @SuppressLint("IntentReset")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        masterDBHelper = MasterDBHelper(requireContext())


        jsonUploadImg = view.findViewById(R.id.jsonUploadImg)

        jsonUploadImg.setOnClickListener {
            openFilePicker()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/json"
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    @SuppressLint("Recycle")
    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {

            data?.data?.let { uri ->
                val inputStream = requireActivity().contentResolver.openInputStream(uri)
                jsonContent = inputStream?.bufferedReader().use { it?.readText() }

                val jsonData: JSONFormDataModel =
                    Gson().fromJson(jsonContent, JSONFormDataModel::class.java)

                if (jsonContent.toString() != "") {

                    try {
                        val currentDateTime = getCurrentDateTime()
                        val rowInsert = masterDBHelper.insertForm(
                            FormData(
                                formId = jsonData.form_id!!.toInt(),
                                formName = jsonData.form_name.toString(),
                                version = jsonData.version.toString(),
                                formSchema = jsonContent!!,
                                createdBy = null,
                                createdAt = currentDateTime,
                                updateAt = null,
                                isDeleted = null,
                                deletedAt = null
                            )
                        )
                        // Log.d("TAG", "rowInsert: $rowInsert")
                        if (rowInsert != -1L) {
                            val fileName = getFileName(requireContext().contentResolver, uri)
                            Toast.makeText(
                                requireContext(),
                                "$fileName JSON Uploaded Successful ",
                                Toast.LENGTH_LONG
                            ).show()


                        } else {
                            Toast.makeText(
                                requireContext(),
                                "JSON Uploaded Unsuccessful",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "thi JSON not uploaded",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        Log.d("TAG", "onActivityResult: ${e.message}")

                    }
                } else {
                    Log.d("TAG", "onActivityResult2: ${jsonContent}")
                }
            }
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            return cursor.getString(nameIndex)
        }
        return null
    }

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 123
    }

}