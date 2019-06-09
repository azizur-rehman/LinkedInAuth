package com.aziz.linkedinauth

import android.content.Context
import android.content.Intent
import android.os.Build
import org.jetbrains.anko.intentFor

class LinkedInIntent {

    class Builder(private var context: Context) {


        /**This is the public api key of our application*/
        private var app_key: String = ""

        /** This is the private api key of our application*/
        private var app_secret: String = ""
        /**
        //This is any string we want to use. This will be used for avoiding CSRF attacks. You can generate one here: http://strongpasswordgenerator.com/
         */
        private var state: String = ""

        private var scope: String = ""

        /** This is the url that LinkedIn Auth process will redirect to. Use the same url that you put it into the console.
         */
        private var redirect_url: String = ""

        fun setAppKey(app_key: String): Builder {
            this.app_key = app_key
            return this
        }

        fun setAppSecret(app_secret: String): Builder {
            this.app_secret = app_secret
            return this
        }

        fun setState(state: String  ): Builder {
            this.state = state
            return this
        }

        fun setScope(scope: String ): Builder {
            this.scope = scope
            return this
        }

        fun setRedirectURL(url:String):Builder{
            this.redirect_url = url
            return this
        }

        fun build(): Intent {

            return context.intentFor<MainActivity>(
                "app_key" to app_key,
                "app_secret" to app_secret,
                "scope" to if(scope.isEmpty()) "scope=r_liteprofile%20r_emailaddress%20w_member_social" else scope,
                "state" to if(state.isEmpty()) "E3ZYKC1T6H2yP4z" else state,
                "url" to redirect_url
            )
        }

    }

    class UserDetail(private var intent:Intent?){

         val data:LinkedInResponse?
        get() = intent?.getSerializableExtra("data") as LinkedInResponse
    }
}