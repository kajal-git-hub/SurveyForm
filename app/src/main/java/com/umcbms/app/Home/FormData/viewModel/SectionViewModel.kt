package com.umcbms.app.Home.FormData.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.umcbms.app.api.Resource
import com.umcbms.app.api.RetrofitInstance
import com.umcbms.app.api.request.JSONRequest
import com.umcbms.app.api.request.QCJSONRequest
import com.umcbms.app.api.request.SyncedJSONRequest
import com.umcbms.app.api.respose.DataSyncDataResponse
import com.umcbms.app.api.respose.QCResponse
import com.umcbms.app.api.respose.SyncDataResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SectionViewModel : ViewModel() {
    fun callSurveyDataSubmit(token: String,formId: Int, formJson : ArrayList<JSONRequest>) = liveData(Dispatchers.IO) {

        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = callSurveyDataSubmitData(token,formId,formJson)))
        } catch (exceptions: UnknownHostException) {
            emit(Resource.error(data = null, msg = "Please check internet connection"))
        } catch (exceptions: ConnectException) {
            emit(Resource.error(data = null, msg = "Connection Error"))
        } catch (exceptions: SocketTimeoutException) {
            emit(Resource.error(data = null, msg = "Please try again"))
        } catch (e: Exception) {
            emit(Resource.error(data = null, msg = e.message ?: "Error Occurred! in Login"))
        }
    }

    private suspend fun callSurveyDataSubmitData(token: String,formId: Int, formJson : ArrayList<JSONRequest>): Response<SyncDataResponse> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().callSurveyDataSubmit(token="Bearer $token",id=formId, formJson =  formJson)
        }
    }
    fun callSyncedSurveyDataSubmit(token: String,formId: Int, formJson : SyncedJSONRequest) = liveData(Dispatchers.IO) {

        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = callSyncedSurveyDataSubmitData(token,formId,formJson)))
        } catch (exceptions: UnknownHostException) {
            emit(Resource.error(data = null, msg = "Please check internet connection"))
        } catch (exceptions: ConnectException) {
            emit(Resource.error(data = null, msg = "Connection Error"))
        } catch (exceptions: SocketTimeoutException) {
            emit(Resource.error(data = null, msg = "Please try again"))
        } catch (e: Exception) {
            emit(Resource.error(data = null, msg = e.message ?: "Error Occurred! in Login"))
        }
    }

    private suspend fun callSyncedSurveyDataSubmitData(token: String,formId: Int, formJson : SyncedJSONRequest): Response<SyncDataResponse> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().callSyncedSurveyDataSubmit(token="Bearer $token",id=formId, formJson =  formJson)
        }
    }


    fun getSyncedSchemaValue(token: String, formId: Int,resposeId : Int) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getSyncedSchemaValueData(token, formId,resposeId)))
        } catch (exceptions: UnknownHostException) {
            emit(Resource.error(data = null, msg = "Please check internet connection"))
        } catch (exceptions: ConnectException) {
            emit(Resource.error(data = null, msg = "Connection Error"))
        } catch (exceptions: SocketTimeoutException) {
            emit(Resource.error(data = null, msg = "Please try again"))
        } catch (e: Exception) {
            emit(Resource.error(data = null, msg = e.message ?: "Error Occurred!"))
        }
    }

    private suspend fun getSyncedSchemaValueData(token: String, formId: Int,resposeId: Int): Response<DataSyncDataResponse>{
        return withContext(Dispatchers.IO){
            RetrofitInstance.getApiService().
            getSyncedSchemaValue("Bearer $token", formId,resposeId)
        }
    }


    fun getQCRaisedList(token: String, formId: Int) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getQCRaisedListData(token, formId)))
        } catch (exceptions: UnknownHostException) {
            emit(Resource.error(data = null, msg = "Please check internet connection"))
        } catch (exceptions: ConnectException) {
            emit(Resource.error(data = null, msg = "Connection Error"))
        } catch (exceptions: SocketTimeoutException) {
            emit(Resource.error(data = null, msg = "Please try again"))
        } catch (e: Exception) {
            emit(Resource.error(data = null, msg = e.message ?: "Error Occurred!"))
        }
    }

    private suspend fun getQCRaisedListData(token: String, formId: Int): Response<QCResponse>{
        return withContext(Dispatchers.IO){
            RetrofitInstance.getApiService().
            getQCRaisedList("Bearer $token", formId)
        }
    }

    fun getQCRaisedSchemaValue(token: String, formId: Int,resposeId : Int) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getQCRaisedSchemaValueData(token, formId,resposeId)))
        } catch (exceptions: UnknownHostException) {
            emit(Resource.error(data = null, msg = "Please check internet connection"))
        } catch (exceptions: ConnectException) {
            emit(Resource.error(data = null, msg = "Connection Error"))
        } catch (exceptions: SocketTimeoutException) {
            emit(Resource.error(data = null, msg = "Please try again"))
        } catch (e: Exception) {
            emit(Resource.error(data = null, msg = e.message ?: "Error Occurred!"))
        }
    }

    private suspend fun getQCRaisedSchemaValueData(token: String, formId: Int,resposeId: Int): Response<DataSyncDataResponse>{
        return withContext(Dispatchers.IO){
            RetrofitInstance.getApiService().
            getQCRaisedSchemaValue("Bearer $token", formId,resposeId)
        }
    }

    fun surveyQCResolveSubmit(token: String,formId: Int,responseId: Int, formJson : ArrayList<QCJSONRequest>) = liveData(Dispatchers.IO) {

        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = surveyQCResolveSubmitData(token,formId,responseId,formJson)))
        } catch (exceptions: UnknownHostException) {
            emit(Resource.error(data = null, msg = "Please check internet connection"))
        } catch (exceptions: ConnectException) {
            emit(Resource.error(data = null, msg = "Connection Error"))
        } catch (exceptions: SocketTimeoutException) {
            emit(Resource.error(data = null, msg = "Please try again"))
        } catch (e: Exception) {
            emit(Resource.error(data = null, msg = e.message ?: "Error Occurred! in Login"))
        }
    }

    private suspend fun surveyQCResolveSubmitData(token: String,formId: Int,responseId: Int, formJson : ArrayList<QCJSONRequest>): Response<SyncDataResponse> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().surveyQCResolveSubmit(token="Bearer $token",id=formId,responseId=responseId, formJson =  formJson)
        }
    }
}