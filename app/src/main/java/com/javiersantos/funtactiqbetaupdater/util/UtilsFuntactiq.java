package com.javiersantos.funtactiqbetaupdater.util;

import android.content.Context;
import android.content.pm.PackageManager;

import com.javiersantos.funtactiqbetaupdater.BuildConfig;
import com.javiersantos.funtactiqbetaupdater.object.Version;

public class UtilsFuntactiq {

    static String packageName = "com.ionicframework.ionicmaps310749";


    public static String getInstalledFuntactiqVersion(Context context) {
        String version = "";

        try {
            version = context.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    public static Boolean isFuntactiqInstalled(Context context) {
        Boolean res;

        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            res = true;
        } catch (PackageManager.NameNotFoundException e) {
            res = false;
        }

        return res;
    }

    public static Boolean isUpdateAvailable(String installedVersion, String latestVersion) {
        if (BuildConfig.DEBUG_MODE) {
            return false;
        } else {
            installedVersion = installedVersion.replace("-debug","");
            latestVersion = latestVersion.replace("-debug","");


            Version installed = new Version(installedVersion);
            Version latest = new Version(latestVersion);

            return installed.compareTo(latest) < 0;
        }
    }

}
