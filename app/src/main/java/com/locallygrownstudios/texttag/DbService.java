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
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DbService extends IntentService {

    private static final String writeToDb = "com.locallygrownstudios.texttag.action.WRITE_TO_DB";
    private static final String readFromDb = "com.locallygrownstudios.texttag.action.READ_FROM_DB";

    int code;
    String result = null, line = null, thisCountry, thisAdminArea;
    InputStream is;
    Long currentTime;


    public static void writeToDb(Context context) {
        Intent intent = new Intent(context, DbService.class);
        intent.setAction(writeToDb);
        context.startService(intent);
    }


    public static void readFromDb(Context context) {
        Intent intent = new Intent(context, DbService.class);
        intent.setAction(readFromDb);
        context.startService(intent);
    }


    public DbService() {
        super("DbService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (writeToDb.equals(action)) {
                handleWriteToDb();
            } else if (readFromDb.equals(action)) {
                handleReadFromDb();
            }
        }
    }


    private void handleWriteToDb() {

        insertDB();

    }


    private void handleReadFromDb() {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public void insertDB() {

        currentTime = System.currentTimeMillis();
        String Time = currentTime.toString();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("CountriesTagged", "0"));
        nameValuePairs.add(new BasicNameValuePair("StatesTagged", "0"));
        nameValuePairs.add(new BasicNameValuePair("PeopleTagged", "1"));
        nameValuePairs.add(new BasicNameValuePair("TimeAlive", "0"));
        nameValuePairs.add(new BasicNameValuePair("TimeSent", Time));
        nameValuePairs.add(new BasicNameValuePair("_id",null));

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://locallygrownstudios.com/webservice/insert_tagdetails.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.e("pass 1", "connection success ");
        } catch (Exception e) {
            Log.e("Fail 1", e.toString());
        }

        try {
            BufferedReader reader = new BufferedReader (new InputStreamReader (is, "iso-8859-1"), 16);
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
            }
            else {
                Log.e("fail 3", "connection fail ");
            }
        } catch (Exception e) {
            Log.e("Fail 3", e.toString());
        }
    }


    public void updateDB() {

        currentTime = System.currentTimeMillis();
        String Time = currentTime.toString();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("TimeAlive", "This is just a test"));
        nameValuePairs.add(new BasicNameValuePair("TimeSent", Time));
        nameValuePairs.add(new BasicNameValuePair("_id",null));

        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://locallygrownstudios.com/webservice/update_tagdetails.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.e("pass 1", "connection success ");
        }
        catch(Exception e)
        {
            Log.e("Fail 1", e.toString());
        }

        try
        {
            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.e("pass 2", "connection success ");
        }
        catch(Exception e)
        {
            Log.e("Fail 2", e.toString());
        }

        try
        {
            JSONObject json_data = new JSONObject(result);
            code=(json_data.getInt("code"));

            if(code==1)
            {
                Log.e("pass 3", "connection success ");
            }
            else
            {
                Log.e("Fail 3", "connection fail " );
            }
        }
        catch(Exception e)
        {
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
                    if(thisCountry.contains("United States")) {

                        thisAdminArea = listAddresses.get(0).getAdminArea();
                    }
                    else{
                        thisAdminArea = null;
                    }

                    Log.e("Location", thisLocation );
                    Log.e("Location", thisCountry );
                    Log.e("Location", thisAdminArea );
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Location", "Location Failed" );
            }

        }
    }

}
