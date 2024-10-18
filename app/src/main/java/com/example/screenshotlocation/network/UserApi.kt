package com.example.screenshotlocation.network

import com.example.screenshotlocation.dataModel.UserInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("create")
    suspend fun createRecord(
        @Body userInfo: UserInfo
    ): Response<Any>
}