package com.javiersantos.funtactiqbetaupdater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.javiersantos.funtactiqbetaupdater.util.UtilsAsync;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new UtilsAsync.NotifyFuntactiqVersion(context, intent).execute();
    }

}
