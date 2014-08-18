package com.locallygrownstudios.texttag;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import static android.provider.ContactsContract.CommonDataKinds.*;


public class NewTag extends Activity implements View.OnClickListener{

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "710073189384";
    static final String TAG = "GCM Services";
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    static Context context;

    private List<NewTagBean> list = new ArrayList<NewTagBean>();
    final static int listSize = 5;
    int position1 = -1, position2 = -1, position3 = -1, position4 = -1, position5 = -1;
    String contactName, contactNumber, regid, selectedNumber;
    Button newTag;
    ListView listView;


    public static Context getAppContext()
    {
        // Return the value of cntxContactImporter

        return context;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tag);
        newTag = (Button) findViewById(R.id.btn_send_tag);
        newTag.setOnClickListener(this);
        getContacts();
        listView = (ListView) findViewById(R.id.list);
        NewTagAdapter adapter = new NewTagAdapter(this, R.layout.listitem_new_tag, list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);
        listView.setItemChecked(0, true);
        context = getApplicationContext();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView textView = (TextView) view.findViewById(R.id.txt_new_tag_number);
                selectedNumber = textView.getText().toString();
                Log.e("List Selection", "Position " + String.valueOf(position) + " " + "Number " + selectedNumber);

            }
        });



        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
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
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */

    private void storeRegistrationId(Context context, String regId) {

        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion + " " + regId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();

    }


    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

                FirstRunCheck appFirstRun = new FirstRunCheck();
                appFirstRun.appFirstRun();

                if (appFirstRun.appHasNotRun()) {

                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    String number = tm.getLine1Number();

                    DbService.writeToUsers(context, Helpers.formatPhoneNumber(number), regid);

                    appFirstRun.setAppHasRun();

                }


            }
        }.execute(null, null, null);
    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    /**
     * @return Application's {@code SharedPreferences}.
     */

    private SharedPreferences getGcmPreferences(Context context) {

        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.

        return getSharedPreferences(NewTag.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }


    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */

    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }


    public void getContacts() {

        int currentPosition;

        for (int i = 0; i < listSize; i++) {

            NewTagBean newTagBean = new NewTagBean();
            Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, null, null, null, null);
            currentPosition = Helpers.randInt(0, cursor.getCount() - 1
            );
            cursor.moveToPosition(currentPosition);

            if (currentPosition != position1 && currentPosition != position2 && currentPosition != position3 && currentPosition != position4 && currentPosition != position5) {

                if (cursor.getString(cursor.getColumnIndex(Phone.TYPE)) != null) {

                    int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));

                    if (phoneType == 2) {

                        contactName = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
                        contactNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));

                        if (contactName.length() > 10) {

                            newTagBean.Nameset(contactName.substring(0, 10) + "..");

                        }
                        else {

                            newTagBean.Nameset(contactName);

                        }
                        contactNumber = Helpers.stripNumberFormatiing(contactNumber);
                        newTagBean.PhoneNoset(Helpers.formatPhoneNumber(contactNumber));
                        list.add(newTagBean);
                        position5 = position4;
                        position4 = position3;
                        position3 = position2;
                        position2 = position1;
                        position1 = currentPosition;
                    }
                    else {
                        i--;
                    }
                }
                else {
                    i--;
                }
            }
            else {
                i--;
            }

        }

    }


    @Override
    public void onClick(View v) {

        DbService.writeToTags(this);
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";

                try {



                    Bundle data = new Bundle();
                    data.putString("my_message", "Hello World");
                    data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
                    String id = Integer.toString(msgId.incrementAndGet());
                    gcm.send(SENDER_ID + "@gcm.googleapis.com:5236", id, data);
                    msg = "Sent message";


                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }

        }.execute(null, null, null);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_tag, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
