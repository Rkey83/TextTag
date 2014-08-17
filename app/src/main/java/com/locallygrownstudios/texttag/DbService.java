package com.locallygrownstudios.texttag;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.Button;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;


public class DbService extends IntentService {

    private static final String writeToDb = "com.locallygrownstudios.texttag.action.WRITE_TO_DB";
    private static final String readFromDb = "com.locallygrownstudios.texttag.action.READ_FROM_DB";

    int code;
    String result = null, line = null;
    InputStream is;
    Date timeSent;
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
      //  updateDB();

    }


    private void handleReadFromDb() {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public void insertDB() {

        currentTime = System.currentTimeMillis();
        String Time = currentTime.toString();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();


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
            }
            else {
                Log.e("fail 3", "connection fail ");
            }
        } catch (Exception e) {
            Log.e("Fail 3", e.toString());
        }
    }



    public void updateDB()
    {

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
}
