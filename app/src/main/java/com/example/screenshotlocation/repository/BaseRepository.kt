package com.example.screenshotlocation.repository

import com.example.screenshotlocation.dataModel.UserInfo
import com.example.screenshotlocation.network.UserApi
import retrofit2.Retrofit

class BaseRepository (retrofit: Retrofit) {
    private var userApi : UserApi = retrofit.create(UserApi::class.java)
    suspend fun createRecord(userInfo: UserInfo) = userApi.createRecord(userInfo)
}