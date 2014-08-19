package com.locallygrownstudios.texttag;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DbService extends IntentService {

    private static final String writeToTags = "com.locallygrownstudios.texttag.action.WRITE_TO_TAGS";
    private static final String writeToUsers = "com.locallygrownstudios.texttag.action.WRITE_TO_USERS";
    private static final String readFromUsers = "com.locallygrownstudios.texttag.action.READ_FROM_USERS";


    private static final String EXTRA_USER_NUMBER = "com.locallygrownstudios.texttag.extra.USER_NUMBER";
    private static final String EXTRA_TAGGED_NUMBER = "com.locallygrownstudios.texttag.extra.TAGGED_NUMBER";
    private static final String EXTRA_REGID = "com.locallygrownstudios.texttag.extra.REGID";


    int code;
    String result = null, line = null,  thisCountry, thisAdminArea;
    InputStream is;
    Long currentTime;


    public DbService() {
        super("DbService");
    }


    public static void writeToTags(Context context) {
        Intent intent = new Intent(context, DbService.class);
        intent.setAction(writeToTags);
        context.startService(intent);
    }


    public static void writeToUsers(Context context, String userNumber, String regID) {

        Intent intent = new Intent(context, DbService.class);
        intent.setAction(writeToUsers);
        intent.putExtra(EXTRA_USER_NUMBER, userNumber);
        intent.putExtra(EXTRA_REGID, regID);
        context.startService(intent);
    }


    public static void readFromUsers(Context context, String taggedNumber) {

        Intent intent = new Intent(context, DbService.class);
        intent.setAction(readFromUsers);
        intent.putExtra(EXTRA_TAGGED_NUMBER, taggedNumber);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            final String action = intent.getAction();

            if (writeToTags.equals(action)) {

                handleWriteToTags();

            } else if (writeToUsers.equals(action)) {

                final String userNumber = intent.getStringExtra(EXTRA_USER_NUMBER);
                final String regID = intent.getStringExtra(EXTRA_REGID);
                handleWriteToUsers(userNumber, regID);

            } else if (readFromUsers.equals(action)) {

                final String taggedNumber = intent.getStringExtra(EXTRA_TAGGED_NUMBER);
                handleReadFromUsers(taggedNumber);
            }
        }
    }


    private void handleWriteToTags() {

        HttpPost httpPost = new HttpPost("http://locallygrownstudios.com/webservice/insert_tagdetails.php");
        currentTime = System.currentTimeMillis();
        String Time = currentTime.toString();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("CountriesTagged", "0"));
        nameValuePairs.add(new BasicNameValuePair("StatesTagged", "0"));
        nameValuePairs.add(new BasicNameValuePair("PeopleTagged", "1"));
        nameValuePairs.add(new BasicNameValuePair("TimeAlive", "0"));
        nameValuePairs.add(new BasicNameValuePair("TimeSent", Time));
        nameValuePairs.add(new BasicNameValuePair("_id", null));

        insertDB(httpPost, nameValuePairs);

    }


    private void handleWriteToUsers(String selectedNumber, String regID) {

        HttpPost httpPost = new HttpPost("http://locallygrownstudios.com/webservice/insert_userdetails.php");
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("PhoneNumber", selectedNumber));
        nameValuePairs.add(new BasicNameValuePair("GCMKey", regID));

        insertDB(httpPost, nameValuePairs);

    }


    private void handleReadFromUsers(String taggedNumber) {

        taggedNumber = "6123662750";
        HttpPost httpost = new HttpPost("http://locallygrownstudios.com/webservice/select_userdetails.php");
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("PhoneNumber", taggedNumber));

        checkForTaggedGCM(httpost, nameValuePairs);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(NewTag.UserDataReceiver.GET_USER_DATA);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);


    }


    public void insertDB(HttpPost httppost, ArrayList<NameValuePair> nameValuePairs) {

        try {
            HttpClient httpclient = new DefaultHttpClient();
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.e("pass 1", "connection success ");

        } catch (Exception e) {

            Log.e("Fail 1", e.toString());
        }

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 16);
            StringBuilder sb = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            is.close();
            result = sb.toString();
            Log.e("pass 2", "connection success ");

        } catch (Exception e) {

            Log.e("Fail 2", e.toString());

        }

        try {

            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("code"));

            if (code == 1) {
                Log.e("pass 3", "connection success ");
            } else {
                Log.e("fail 3", "connection fail ");
            }
        } catch (Exception e) {
            Log.e("Fail 3", e.toString());
        }
    }


    public String checkForTaggedGCM(HttpPost httpPost, ArrayList<NameValuePair> nameValuePairs){

        String GCMKey = "Placeholder";

        try {

            HttpClient httpClient = new DefaultHttpClient();
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.e( "Pass 1" , "connection success");

        }
        catch (Exception e){

            Log.e( "Fail 1" , "Error in Connection " + e.toString());

        }

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 16);
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");

            }

            is.close();
            result = stringBuilder.toString();
            Log.e( "Pass 2" , "Result Converted");

        }
        catch (Exception e){

            Log.e( "Fail 2" , "Error Converting Result " + e.toString());

        }


        try {


            JSONArray jsonArray = new JSONArray(result);
            jsonArray.length();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Log.e("Pass 3", jsonObject.getString("GCMKey"));

            }
        }


        catch (Exception e){

            Log.e( "Fail 3" , "Error Parsing Data " + e.toString());

        }


        return GCMKey;
    }


    public void updateDB() {

        currentTime = System.currentTimeMillis();
        String Time = currentTime.toString();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("TimeAlive", "This is just a test"));
        nameValuePairs.add(new BasicNameValuePair("TimeSent", Time));
        nameValuePairs.add(new BasicNameValuePair("_id", null));

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://locallygrownstudios.com/webservice/update_tagdetails.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.e("pass 1", "connection success ");
        } catch (Exception e) {
            Log.e("Fail 1", e.toString());
        }

        try {
            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.e("pass 2", "connection success ");
        } catch (Exception e) {
            Log.e("Fail 2", e.toString());
        }

        try {
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("code"));

            if (code == 1) {
                Log.e("pass 3", "connection success ");
            } else {
                Log.e("Fail 3", "connection fail ");
            }
        } catch (Exception e) {
            Log.e("Fail 3", e.toString());
        }
    }


    private void getLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);

        Location locations = locationManager.getLastKnownLocation(provider);
        List<String> providerList = locationManager.getAllProviders();

        if (null != locations && null != providerList && providerList.size() > 0) {

            double longitude = locations.getLongitude();
            double latitude = locations.getLatitude();
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    String thisLocation = listAddresses.get(0).getAddressLine(0);
                    thisCountry = listAddresses.get(0).getCountryName();
                    if (thisCountry.contains("United States")) {

                        thisAdminArea = listAddresses.get(0).getAdminArea();
                    } else {
                        thisAdminArea = null;
                    }

                    Log.e("Location", thisLocation);
                    Log.e("Location", thisCountry);
                    Log.e("Location", thisAdminArea);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Location", "Location Failed");
            }

        }
    }

}
