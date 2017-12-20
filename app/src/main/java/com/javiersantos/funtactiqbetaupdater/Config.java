package com.javiersantos.funtactiqbetaupdater;

public class Config {

    public static final String GITHUB_URL = "https://github.com/kaproslaci/WhatsAppBetaUpdater.git";
    public static final String GITHUB_TAGS = GITHUB_URL.concat("/tags");
    public static final String GITHUB_APK = GITHUB_URL.concat("/releases/download/");
    public static final String FUNTACTIQ_URL = "http://fundater.azurewebsites.net";
    //public static final String FUNTACTIQ_APK = FUNTACTIQ_URL.concat("/assets/releases/download/v1.0.0/com.funtactiq.apk");
    public static final String FUNTACTIQ_APK = "https://fundater.blob.core.windows.net/apks/com.funtactiq.apk";
    //public static final String FUNTACTIQ_APK = "https://fundater.blob.core.windows.net/apks/app-debug.apk";
    public static final String PAYPAL_DONATION = "donate@javiersantos.me";

    public static final String PATTERN_LATEST_VERSION = "<p class=\"version\" align=\"center\">Version";
    public static final String PATTERN_LATEST_VERSION_CDN = "<a class=\"button\" href=\"";

}
