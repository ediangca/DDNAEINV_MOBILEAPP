package com.ddn.peedo.project.aeinv.services

import com.ddn.peedo.project.aeinv.model.ItemResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("Report/GetItemsByQRCode")
    suspend fun getItemsByQRCode(  @Query("qrCode") qrCode: String ): retrofit2.Response<List<ItemResponse>>
}
