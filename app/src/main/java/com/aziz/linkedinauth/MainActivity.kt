package com.aziz.linkedinauth

import android.app.Activity
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import org.jetbrains.anko.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

    /*CONSTANT FOR THE AUTHORIZATION PROCESS*/

    //This is the public api key of our application
    private var app_key = ""
    //This is the private api key of our application
    private var app_secret = ""
    //This is any string we want to use. This will be used for avoiding CSRF attacks. You can generate one here: http://strongpasswordgenerator.com/
    private var state = ""
    //This is the url that LinkedIn Auth process will redirect to. We can put whatever we want that starts with http:// or https:// .
    //We use a made up url that we will intercept when redirecting. Avoid Uppercases.
    private var redirect_url = ""
    private var scope = ""

    //These are constants used for build the urls
    private val AUTHORIZATION_URL = "https://www.linkedin.com/oauth/v2/authorization"
    private val ACCESS_TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken"



    private val SECRET_KEY_PARAM = "client_secret"
    private val RESPONSE_TYPE_PARAM = "response_type"
    private val GRANT_TYPE_PARAM = "grant_type"
    private val GRANT_TYPE = "authorization_code"
    private val RESPONSE_TYPE_VALUE = "code"
    private val CLIENT_ID_PARAM = "client_id"
    private val STATE_PARAM = "state"
    private val REDIRECT_URI_PARAM = "redirect_uri"
    /*---------------------------------------*/
    private val QUESTION_MARK = "?"
    private val AMPERSAND = "&"
    private val EQUALS = "="

    lateinit var webView: WebView
    lateinit var pd: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        window.requestFeature(Window.FEATURE_NO_TITLE)

        //get the webView from the layout
        webView = linkedin_webview

        //Request focus for the webview
        webView.requestFocus(View.FOCUS_DOWN)


        //init values
        init()


        //Show a progress filterDialog to the user

        pd = Dialog(this, R.style.LibraryDialogSlideAnimStyle)
        with(pd){
            window?.requestFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.progress_bar_dialog)

            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            window?.setGravity(Gravity.TOP)
            setCancelable(false)
            window?.setDimAmount(0f)
            show()
        }

        //Set a custom web view client
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                //This method will be executed each time a page finished loading.
                //The only we do is dismiss the progressDialog, in case we are showing any.
                if (pd.isShowing) {
                    pd.dismiss()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, authorizationUrl: String): Boolean {
                //This method will be called when the Auth proccess redirect to our RedirectUri.
                //We will check the url looking for our RedirectUri.
                if (authorizationUrl.startsWith(redirect_url)) {
                    Log.i("LinkedIn", "")
                    val uri = Uri.parse(authorizationUrl)
                    //We take from the url the authorizationToken and the state token. We have to check that the state token returned by the Service is the same we sent.
                    //If not, that means the request may be a result of CSRF and must be rejected.
                    val stateToken = uri.getQueryParameter(STATE_PARAM)
                    if (stateToken == null || stateToken != state) {
                        Log.e("LinkedIn", "State token doesn't match")
                        return true
                    }

                    //If the user doesn't allow authorization to our application, the authorizationToken Will be null.
                    val authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE)
                    if (authorizationToken == null) {
                        Log.i("LinkedIn", "The user doesn't allow authorization.")
                        return true
                    }
//                    Log.i("LinkedIn", "Auth token received: $authorizationToken")




                    //Generate URL for requesting Access Token
                    val accessTokenUrl = getAccessTokenUrl(authorizationToken)
                    Log.d("LinkedInWebView", "shouldOverrideUrlLoading: access token url = $accessTokenUrl")
                    //We make the request in a AsyncTask
//                    PostRequestAsyncTask().execute(accessTokenUrl)
                    loadAuthURL(accessTokenUrl)

                } else {
                    //Default behaviour
                    Log.i("LinkedIn", "Redirecting to: $authorizationUrl")
                    webView.loadUrl(authorizationUrl)
                }
                return true
            }
        }




        //Get the authorization Url
        val authUrl = getAuthorizationUrl()
        Log.i("LinkedIn", "Loading Auth Url: $authUrl")
        //Load the authorization URL into the webView
        webView.loadUrl(authUrl)
    }

    private fun init() {
        val values = Utils.IntentBuilderValues(intent)
        app_key = values.api_key
        app_secret = values.api_secret
        scope = values.scope
        redirect_url = values.redirectURL
        state = values.state

        if(app_key.isEmpty()){
            throw IllegalArgumentException("App Key must not be empty")
        }

        if(app_secret.isEmpty()){
            throw IllegalArgumentException("App Secret must not be empty")
        }

        if(scope.isEmpty()){
            throw IllegalArgumentException("Scope must not be empty")
        }
        if(state.isEmpty()){
            throw IllegalArgumentException("State must not be empty")
        }

        if(redirect_url.isEmpty()){
            throw IllegalArgumentException("Redirection url must not be empty")
        }

    }


    private fun loadAuthURL(authURL : String){


        doAsync {

            runOnUiThread {


                pd.show()

                val url = URL(authURL)
                val connection = url.openConnection() as HttpsURLConnection


                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var response = ""
                var line: String?

                do {

                    line = reader.readLine()
                    response += line

                    if (line == null)

                        break

                    println(line)

                } while (true)

                reader.close()
                pd.dismiss()

                if (response.contains("access_token")) {
                    val json = JSONObject(response)
                    val accessToken = json.getString("access_token")
                    val expiresIn = json.getLong("expires_in")

//                Log.d("LinkedInWebView", "loadAuthURL: access token = $accessToken")
//                Log.d("LinkedInWebView", "loadAuthURL: expires in = $expiresIn")


                    pd.show()
                    Utils.linkedInClient.linkedInGetEmail(accessToken = "Bearer $accessToken")
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Log.d("LinkedInWebView", "onFailure: email error = ${t.message}")
                                toast("Failed to fetch user data")
                                finish()
                            }

                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                pd.dismiss()
                                val body = response.body()?.string()
                                Log.d("LinkedInWebView", "onResponse: email response = $body")
                                try {

                                    val rootJSON = JSONObject(body)
                                    val elementArray = rootJSON.getJSONArray("elements")
                                    val handleItem = JSONObject(elementArray[0].toString())
                                    val handleObject = handleItem.getString("handle~")
                                    val emailAddress = JSONObject(handleObject).getString("emailAddress")


                                    if (emailAddress.isEmpty()) {
                                        Log.e(
                                            "LinkedIn",
                                            "onResponse: User has either no email or has kept hidden from public"
                                        )
                                    }

                                    Utils.linkedInClient.linkedInGetDetail("Bearer $accessToken")
                                        .enqueue(object : Callback<ResponseBody> {
                                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                                Log.d("LinkedInWebView", "onFailure: detail error = ${t.message}")
                                                pd.dismiss()
                                            }

                                            override fun onResponse(
                                                call: Call<ResponseBody>,
                                                response: Response<ResponseBody>
                                            ) {
                                                val detailBody = response.body()?.string()
                                                pd.dismiss()

                                                val firstName = JSONObject(detailBody).getString("localizedFirstName")
                                                val lastName = JSONObject(detailBody).getString("localizedLastName")
                                                val id = JSONObject(detailBody).getString("id")

                                                val responseObject = LinkedInResponse(
                                                    detailBody, response.code(),
                                                    firstName, lastName, emailAddress, id,
                                                    accessToken
                                                )

                                                val intent = intentFor<Any>("data" to responseObject)
//                                            ("first" to firstName,
//                                                "last" to lastName,
//                                                "id" to id,
//                                                "email" to emailAddress)

                                                setResult(Activity.RESULT_OK, intent)
                                                finish()

                                                Log.d("LinkedInWebView", "onResponse: detail = $detailBody")
                                            }

                                        })

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    toast("Failed to fetch user data")
                                    finish()
                                    Log.e("LinkedIN", "LinkedInWebView: onResponse = ${e.message}")
                                }

                            }

                        })
                }
            }

        }
    }


    /**
     * Method that generates the url for get the access token from the Service
     * @return Url
     */
    private fun getAccessTokenUrl(authorizationToken: String): String {
        return (ACCESS_TOKEN_URL
                + QUESTION_MARK
                + GRANT_TYPE_PARAM + EQUALS + GRANT_TYPE
                + AMPERSAND
                + RESPONSE_TYPE_VALUE + EQUALS + authorizationToken
                + AMPERSAND
                + CLIENT_ID_PARAM + EQUALS + app_key
                + AMPERSAND
                + REDIRECT_URI_PARAM + EQUALS + redirect_url
                + AMPERSAND
                + SECRET_KEY_PARAM + EQUALS + app_secret
                + AMPERSAND + scope)
    }

    /**
     * Method that generates the url for get the authorization token from the Service
     * @return Url
     */
    private fun getAuthorizationUrl(): String {
        return (AUTHORIZATION_URL
                + QUESTION_MARK + RESPONSE_TYPE_PARAM + EQUALS + RESPONSE_TYPE_VALUE
                + AMPERSAND + CLIENT_ID_PARAM + EQUALS + app_key
                + AMPERSAND + STATE_PARAM + EQUALS + state
                + AMPERSAND + REDIRECT_URI_PARAM + EQUALS + redirect_url
                + AMPERSAND + scope)
    }


}
