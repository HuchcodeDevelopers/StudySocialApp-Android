package com.programmingtechniques.gcmdemo;
 
import java.util.concurrent.atomic.AtomicInteger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
 
public class MainActivity extends Activity {
  
 private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
 public static final String EXTRA_MESSAGE = "message";
 public static final String PROPERTY_REG_ID = "reg_id";
 private static final String PROPERTY_APP_VERSION = "1";
 private static final String TAG = "GCMRelated";
 GoogleCloudMessaging gcm;
 AtomicInteger msgId = new AtomicInteger();
 String regid;
 
 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_main);
  final Button button = (Button) findViewById(R.id.register);
   
  if (checkPlayServices()) {
      gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            regid = getRegistrationId(getApplicationContext());
            if(!regid.isEmpty()){
             button.setEnabled(false);
            }else{
             button.setEnabled(true);
            }
  }
   
  button.setOnClickListener(new View.OnClickListener() {
    
   @Override
   public void onClick(View view) {
    // Check device for Play Services APK.
       if (checkPlayServices()) {
        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
              regid = getRegistrationId(getApplicationContext());
              Log.i(TAG, regid);
               
              if (regid.isEmpty()) {
               button.setEnabled(false);
                  new RegisterApp(getApplicationContext(), gcm, getAppVersion(getApplicationContext())).execute();
              }else{
               Toast.makeText(getApplicationContext(), "Device already Registered", Toast.LENGTH_SHORT).show();
              }
       } else {
              Log.i(TAG, "No valid Google Play Services APK found.");
       }
   }
  });   
 }
 
 	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
  
 /**
  * Check the device to make sure it has the Google Play Services APK. If
  * it doesn't, display a dialog that allows users to download the APK from
  * the Google Play Store or enable it in the device's system settings.
  */
  
 private boolean checkPlayServices() {
     int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
     if (resultCode != ConnectionResult.SUCCESS) {
         if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
             GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                     PLAY_SERVICES_RESOLUTION_REQUEST).show();
         } else {
             Log.i(TAG, "This device is not supported.");
             finish();
         }
         return false;
     }
     return true;
 }
  
 /**
  * Gets the current registration ID for application on GCM service.
  * <p>
  * If result is empty, the app needs to register.
  *
  * @return registration ID, or empty string if there is no existing
  *         registration ID.
  */
 private String getRegistrationId(Context context) {
     final SharedPreferences prefs = getGCMPreferences(context);
     String registrationId = prefs.getString(PROPERTY_REG_ID, "");
     if (registrationId.isEmpty()) {
         Log.i(TAG, "Registration not found.");
         return "";
     }
     // Check if app was updated; if so, it must clear the registration ID
     // since the existing regID is not guaranteed to work with the new
     // app version.
     int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
     int currentVersion = getAppVersion(getApplicationContext());
     if (registeredVersion != currentVersion) {
         Log.i(TAG, "App version changed.");
         return "";
     }
     return registrationId;
 }
  
 /**
  * @return Application's {@code SharedPreferences}.
  */
 private SharedPreferences getGCMPreferences(Context context) {
  // This sample app persists the registration ID in shared preferences, but
     // how you store the regID in your app is up to you.
     return getSharedPreferences(MainActivity.class.getSimpleName(),
             Context.MODE_PRIVATE);
 }
  
 /**
  * @return Application's version code from the {@code PackageManager}.
  */
 private static int getAppVersion(Context context) {
     try {
         PackageInfo packageInfo = context.getPackageManager()
                 .getPackageInfo(context.getPackageName(), 0);
         return packageInfo.versionCode;
     } catch (NameNotFoundException e) {
         // should never happen
         throw new RuntimeException("Could not get package name: " + e);
     }
 }
}