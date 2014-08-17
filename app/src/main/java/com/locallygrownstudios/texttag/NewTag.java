package com.locallygrownstudios.texttag;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.provider.ContactsContract.CommonDataKinds.*;


public class NewTag extends Activity implements View.OnClickListener{

    private List<NewTagBean> list = new ArrayList<NewTagBean>();
    final static int listSize = 5;
    int position1 = -1, position2 = -1, position3 = -1, position4 = -1, position5 = -1;
    String contactName, contactNumber;
    Button newTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tag);
        newTag = (Button) findViewById(R.id.btn_send_tag);
        newTag.setOnClickListener(this);
        getContacts();
        ListView listView = (ListView) findViewById(R.id.list);
        NewTagAdapter adapter = new NewTagAdapter(this, R.layout.listitem_new_tag, list);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);
        listView.setItemChecked(0, true);

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

        DbService.writeToDb(this);

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
