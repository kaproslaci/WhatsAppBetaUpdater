package com.javiersantos.funtactiqbetaupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.javiersantos.funtactiqbetaupdater.activity.MainActivity;

/**
 * Created by ZoltanSzilvai on 2017. 08. 23..
 */
public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);  //MyActivity can be anything which you want to start on bootup...
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}