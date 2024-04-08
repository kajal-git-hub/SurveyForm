package com.umcbms.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.umcbms.app.api.AuthenticationException
import com.umcbms.app.api.Resource
import com.umcbms.app.api.RetrofitInstance
import com.umcbms.app.api.request.LoginRequest
import com.umcbms.app.api.respose.CitiesResponse
import com.umcbms.app.api.respose.DataSyncDataResponse
import com.umcbms.app.api.respose.DataSyncList
import com.umcbms.app.api.respose.LoginData
import com.umcbms.app.api.respose.LogoutResponse
import com.umcbms.app.api.respose.SchemaCode
import com.umcbms.app.api.respose.ShowSchemaResponse
import com.umcbms.app.api.respose.StatesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LoginViewModel : ViewModel() {
    fun loginApi(loginRequest: LoginRequest) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getLoginData(loginRequest)))
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

    private suspend fun getLoginData(loginRequest: LoginRequest):  Response<LoginData> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().login(loginRequest)
        }
    }

    fun logoutApi(token: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getLogoutData(token)))
        } catch (exceptions: UnknownHostException) {
            emit(Resource.error(data = null, msg = "Please check internet connection"))
        } catch (exceptions: ConnectException) {
            emit(Resource.error(data = null, msg = "Connection Error"))
        } catch (exceptions: SocketTimeoutException) {
            emit(Resource.error(data = null, msg = "Please try again"))
        } catch (exceptions: AuthenticationException) {
            emit(Resource.error(data = null, msg = "Authentication expired"))
        }  catch (e: Exception) {
            emit(Resource.error(data = null, msg = e.message ?: "Error Occurred! in Login"))
        }
    }

    private suspend fun getLogoutData(token: String): Response<LogoutResponse> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().logout("Bearer $token")
        }
    }

    fun getSchemaIndex(token: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getSchemaIndexData(token)))
        } catch (exceptions: UnknownHostException) {
            emit(Resource.error(data = null, msg = "Please check internet connection $exceptions"))
        } catch (exceptions: ConnectException) {
            emit(Resource.error(data = null, msg = "Connection Error"))
        } catch (exceptions: SocketTimeoutException) {
            emit(Resource.error(data = null, msg = "Please try again"))
        } catch (e: Exception) {
            emit(Resource.error(data = null, msg = e.message ?: "Error Occurred!"))
        }
    }

    private suspend fun getSchemaIndexData(token: String): Response<SchemaCode> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().getSchemaIndex(token = "Bearer $token")
        }
    }

    fun getSchema(token: String, id: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getSchemaData(token, id)))
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

    private suspend fun getSchemaData(token: String, id: Int): Response<ShowSchemaResponse> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().getSchema("Bearer $token", id)
        }
    }

    fun getSyncedList(token: String, id: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getSyncedListData(token, id)))
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

    private suspend fun getSyncedListData(token: String, id: Int): Response<DataSyncList> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().getSyncedList("Bearer $token", id)
        }
    }

    fun getSyncedData(token: String, formId: Int, resposeId: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getSyncedDataData(token, formId, resposeId)))
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

    private suspend fun getSyncedDataData(
        token: String,
        formId: Int,
        resposeId: String
    ): Response<DataSyncDataResponse> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().getSyncedDataData("Bearer $token", formId, resposeId)
        }
    }

    fun getStateMaster(token: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getStateMasterData(token)))
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

    private suspend fun getStateMasterData(token: String): Response<StatesResponse> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().getStateMaster("Bearer $token")
        }
    }

    fun getDistrictMaster(token: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getDistrictMasterData(token)))
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

    private suspend fun getDistrictMasterData(token: String): Response<StatesResponse> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().getDistrictMaster("Bearer $token")
        }
    }

    fun getCityMaster(token: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getCityMasterData(token)))
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

    private suspend fun getCityMasterData(token: String): Response<StatesResponse> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().getCityMaster("Bearer $token")
        }
    }

    fun getUserCities(token: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = getUserCitiesData(token)))
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

    private suspend fun getUserCitiesData(token: String): Response<CitiesResponse> {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.getApiService().getUserCities("Bearer $token")
        }
    }
}