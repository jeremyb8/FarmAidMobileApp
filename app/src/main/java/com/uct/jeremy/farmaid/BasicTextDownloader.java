package com.uct.jeremy.farmaid;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jeremy on 2017/09/06.
 * Adapted from https://github.com/vad-zuev/ImageDownloader
 */

public class BasicTextDownloader {

    private final String TAG = this.getClass().getSimpleName();

    public BasicTextDownloader(){};

    /**
     * Download text file from server and save to internal memory
     * @param directory the phone's internal storage directory
     * @param serverURL FarmAid server URL
     * @param path url of the file to be downloaded, as well as it's path to be saved on the phone
     */
    public void download(File directory, final String serverURL, @NonNull final String path) {
        final File mainDir = directory;

        new AsyncTask<Void, Integer, String>() {
            HttpURLConnection httpConn = null;

            @Override
            protected void onCancelled() {
                if (httpConn!=null){
                    httpConn.disconnect();
                }
                this.cancel(true);
            }

            @Override
            protected String doInBackground(Void... params) {
                String result = "";
                try {
                    URL url = new URL (serverURL+"android/get_file");
                    httpConn = (HttpURLConnection)url.openConnection();
                    httpConn.setRequestMethod("POST");
                    httpConn.setDoOutput(true);
                    httpConn.setDoInput(true);
                    OutputStream opStream = httpConn.getOutputStream();

                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(opStream, "UTF-8"));

                    // Compose http post message
                    String postData = path;
                    bufferedWriter.write(postData);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    opStream.close();

                    // Receive result of post message
                    InputStream inputStream = httpConn.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null){
                        result += line;
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpConn.disconnect();
                    Log.i(TAG, ">>>>> TextDownloader: FILE CONTENTS =\n"+result);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File Not Found exception", e);
                    this.cancel(true);
                } catch (MalformedURLException e){
                    Log.e(TAG, "MalformedURL exception", e);
                    this.cancel(true);
                } catch (Throwable e) {
                    if (!this.isCancelled()) {
                        Log.e(TAG, "exception", e);
                        this.cancel(true);
                    }
                } finally {
                    try {
                        if (httpConn != null)
                            httpConn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null || result.equals("")) {
                    Log.e(TAG, ">>>>> TEXTDOWNLOADER: File empty/null");
                    this.cancel(true);
                }
                else {
                    Log.i(TAG, ">>>>> TEXTDOWNLOADER: File contents:\n"+result);
                    String savePath = path;
                    if (path.contains("uploads/")){
                        savePath = path.replace("uploads/", "");
                    }

                    File file = new File(mainDir.getAbsolutePath().toString()+"/"+savePath);
                    File parent = file.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IllegalStateException("Couldn't create dir: " + parent);
                    }

                    if (file.exists()){
                        file.delete();
                        file = new File(mainDir.getAbsolutePath().toString()+"/"+savePath);
                    }
                    PrintWriter pwOut = null;
                    result = result.replace("<br>", "\n");
                    Log.i(TAG, ">>>>> TEXTDOWNLOADER: File contents after replacement:\n"+result);
                    try {
                        pwOut = new PrintWriter(file.getAbsolutePath());
                        pwOut.println(result);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        pwOut.close();
                    }
                }
                System.gc();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


}
