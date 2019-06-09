package com.aziz.linkedinauth

import java.io.Serializable

data class LinkedInResponse (val raw:String?,
                             val statusCode:Int?,
                             val firstName:String? ,
                             val lastName:String?,
                             val email:String?,
                             val userID:String?,
                             val accessToken:String?):Serializable