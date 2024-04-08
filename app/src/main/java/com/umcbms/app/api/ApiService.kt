package com.umcbms.app.api

import com.umcbms.app.api.request.DistrictIdsRequest
import com.umcbms.app.api.request.JSONRequest
import com.umcbms.app.api.request.LoginRequest
import com.umcbms.app.api.request.QCJSONRequest
import com.umcbms.app.api.request.StateIdsRequest
import com.umcbms.app.api.request.SyncedJSONRequest
import com.umcbms.app.api.respose.CitiesResponse
import com.umcbms.app.api.respose.DataSyncDataResponse
import com.umcbms.app.api.respose.DataSyncList
import com.umcbms.app.api.respose.LoginData
import com.umcbms.app.api.respose.LogoutResponse
import com.umcbms.app.api.respose.QCResponse
import com.umcbms.app.api.respose.SchemaCode
import com.umcbms.app.api.respose.ShowSchemaResponse
import com.umcbms.app.api.respose.StatesResponse
import com.umcbms.app.api.respose.SyncDataResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET(ApiConstants.states)
    suspend fun getStateList(): Response<StatesResponse>

    @POST(ApiConstants.districts)
    suspend fun getDistrictList(
        @Body request: StateIdsRequest
    ): Response<StatesResponse>

    @POST(ApiConstants.cities)
    suspend fun getCitiesList(
        @Body request: DistrictIdsRequest
    ): Response<StatesResponse>

    @POST(ApiConstants.login)
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginData>

    @POST(ApiConstants.logout)
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>

    @GET(ApiConstants.schemaIndex)
    suspend fun getSchemaIndex(
        @Header("Authorization") token: String
    ): Response<SchemaCode>

    @GET("schema/{id}")
    suspend fun getSchema(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ShowSchemaResponse>
    @GET("form/survey/{id}/response")
    suspend fun getSyncedList(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<DataSyncList>
    @GET("form/survey/{id}/response/{responseId}")
    suspend fun getSyncedDataData(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Path("responseId") responseId: String
    ): Response<DataSyncDataResponse>

    @POST("form/survey/{id}")
    suspend fun callSurveyDataSubmit(
        @Header("Accept") accept : String= "application/json",
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body formJson: ArrayList<JSONRequest>
    ): Response<SyncDataResponse>

    @PATCH("form/survey/{id}")
    suspend fun callSyncedSurveyDataSubmit(
        @Header("Accept") accept : String= "application/json",
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body formJson: SyncedJSONRequest
    ): Response<SyncDataResponse>

    @GET("form/survey/{id}/response/{responseId}/schema-value")
    suspend fun getSyncedSchemaValue(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Path("responseId") responseId: Int
    ): Response<DataSyncDataResponse>

    @GET("form/survey/{id}/qc")
    suspend fun getQCRaisedList(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<QCResponse>
    @GET("form/survey/{id}/qc/{responseId}")
    suspend fun getQCRaisedSchemaValue(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Path("responseId") responseId: Int
    ): Response<DataSyncDataResponse>
    @POST("form/survey/{id}/qc/{responseId}")
    suspend fun surveyQCResolveSubmit(
        @Header("Accept") accept : String= "application/json",
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Path("responseId") responseId: Int,
        @Body formJson: ArrayList<QCJSONRequest>
    ): Response<SyncDataResponse>
    @GET("states")
    suspend fun getStateMaster(
        @Header("Authorization") token: String
    ): Response<StatesResponse>
    @GET("districts")
    suspend fun getDistrictMaster(
        @Header("Authorization") token: String
    ): Response<StatesResponse>
    @GET("cities")
    suspend fun getCityMaster(
        @Header("Authorization") token: String
    ): Response<StatesResponse>
    @GET("user/cities")
    suspend fun getUserCities(
        @Header("Authorization") token: String
    ): Response<CitiesResponse>
}