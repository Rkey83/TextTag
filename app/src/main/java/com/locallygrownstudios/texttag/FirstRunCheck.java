package com.locallygrownstudios.texttag;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Nicholas on 8/17/2014.
 */
public class FirstRunCheck {

    SharedPreferences sprefFirstRun;
    Context context;

    public void appFirstRun() {

        context = NewTag.getAppContext();
        sprefFirstRun = context.getSharedPreferences("appPrefs", 0);

    }

    public boolean appHasNotRun(){

        return sprefFirstRun.getBoolean("contactFirstRun", true);

    }

    public void setAppHasRun(){

        SharedPreferences.Editor editContact = sprefFirstRun.edit().putBoolean("contactFirstRun", false);
        editContact.apply();

    }


}
