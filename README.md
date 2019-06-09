# LinkedInAuth
A Lightweight android library for outh2 based LinkedIn Sign In


Prerequisite:
  - Create your developer account from https://developer.linkedin.com
  - Retrieve your api_key & app_secret


Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  
  
  Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.azizur-rehman:LinkedInAuth:master-SNAPSHOT'
	}
  
  
  Implementation
  
  1. Create an intent builder
  
         val intent = LinkedInIntent.Builder(this)
            .setAppKey("YOUR_KEY")
            .setAppSecret("YOUR_APP_SECRET")
            .setRedirectURL("YOUR_REDIRECT_URL") 
            .setScope("LINKED_IN_PROVIDED_SCOPE") //optional
            .setState("LINKEDIN_PROVIDED_STATE") //optional
            .build()
            
 2. Start activity
 
        startActivityForResult(intent, RC_LINKEDIN)
    
 3. Override onActivityResult()
 
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	
        val userDetail = LinkedInIntent.UserDetail(data).data
        Log.d("SampleActivity", "onActivityResult: $userDetail")
	
        super.onActivityResult(requestCode, resultCode, data)
        }
  
