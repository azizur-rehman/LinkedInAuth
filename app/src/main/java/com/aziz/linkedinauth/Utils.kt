package com.aziz.linkedinauth

import android.content.Intent
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

object Utils {

    val linkedInClient: IRetrofitApis
        get() = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl( "https://api.linkedin.com/v2/")
            .build().create(IRetrofitApis::class.java)

    interface IRetrofitApis{
//        @Headers(Constant.headerJSON)
        @GET("clientAwareMemberHandles?q=members&projection=(elements*(primary,type,handle~))")
        fun linkedInGetEmail(@Header("Authorization") accessToken:String,
                             @Header("cache-control") cacheControl:String = "no-cache",
                             @Header("X-Restli-Protocol-Version") protocol:String = "2.0.0"): Call<ResponseBody>

//        @Headers(Constant.headerJSON)
        @GET("me")
        fun linkedInGetDetail(@Header("Authorization") accessToken:String,
                              @Header("cache-control") cacheControl:String = "no-cache",
                              @Header("X-Restli-Protocol-Version") protocol:String = "2.0.0"):Call<ResponseBody>    }

    class IntentBuilderValues(private val intent: Intent){

        val api_key:String
        get() = intent.getStringExtra("app_key")

        val api_secret:String
        get() = intent.getStringExtra("app_secret")

        val scope:String
        get() = intent.getStringExtra("scope")

        val state:String
        get() = intent.getStringExtra("state")

        val redirectURL:String
        get() = intent.getStringExtra("url")

    }

}