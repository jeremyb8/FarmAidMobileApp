package com.uct.jeremy.farmaid;

/**
 * The Home Page for FarmAid, as well as a progress screen that is shown when the user selects to update
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    File mainDir;
    String farmName = "farm_Bfarm";
    String email = "robbie@gmail.com";
    Toolbar myToolbar;
    private View contentView;
    private View mProgressView;
    private TextView progressStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, TAG+"\nonCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainDir = getFilesDir();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                farmName = extras.getString("farmName");
                email = extras.getString("email");
            }
        } else {
            farmName = (String) savedInstanceState.getSerializable("farmName");
            email = (String) savedInstanceState.getSerializable("email");
        }
        this.setTitle("Welcome to FarmAid");
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        mProgressView = findViewById(R.id.login_progress);
        contentView = findViewById(R.id.content_view);
        progressStatus = (TextView)findViewById(R.id.progress_status);
        
//        File fileToBeDeleted = new File (mainDir.getAbsolutePath()+"/"+farmName);
//        if (fileToBeDeleted.exists()){
//            deleteDir(fileToBeDeleted);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem item = myToolbar.getMenu().findItem(R.id.home_action);
        item.setEnabled(false);
        item.setVisible(false);
        item = myToolbar.getMenu().findItem(R.id.message_action);
        item.setEnabled(false);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        switch (id){
            case R.id.logout_action:
                intent = new Intent(MainActivity.this, LoginActivity.class);
                break;
            case R.id.message_action:
                intent = new Intent(MainActivity.this, ChatActivity.class);
                break;
            case R.id.home_action:
                intent = new Intent(MainActivity.this, MainActivity.class);
                break;
            default: break;
        }
        if (intent!=null){
            intent.putExtra("email", email);
            if (id==R.id.logout_action){
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        }
        startActivity(intent);
        finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows the progress UI and hides the home page.
     * This method copied and adapted from Android studio's template login activity
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            contentView.setVisibility(show ? View.GONE : View.VISIBLE);
            contentView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    contentView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            contentView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Called when the user taps the Crops button, moves the intent of the app to CropsDisplayActivity
     */
    public void displayCrops(View view) {
        Intent intent = new Intent(this, CropsDisplayActivity.class);
        String filePath = mainDir.getAbsolutePath()+"/"+farmName+"/crops";
        intent.putExtra("dirPath", filePath);
        intent.putExtra("email", email);
        intent.putExtra("farmName", farmName);
        Log.i(TAG, ">>>>> MAINACTIVITY crop DirPath=\n"+filePath);
        startActivity(intent);
    }

    /**
     * Called when the user taps the Messaging button, moves the intent of the app to ChatActivity
     * @param view
     */
    public void message(View view){
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("farmName", farmName);
        startActivity(intent);
    }

    /**
     * Called when the user taps the Update button, hides the Home Page layout and shows the
     * progress screen while an Async Task is run to handle network connectivity
     */
    public void update(View view) {
        Log.i(TAG, TAG+"\nUpdate");
        final String serverURL = "https://farmaid.cs.uct.ac.za/";
        final ArrayList<String> downloadPaths = new ArrayList<>();
        final BasicTextDownloader textDownloader = new BasicTextDownloader();
        final String mEmail = email;
        final String[] toastMessage = {""};

        final BasicImageDownloader imageDownloader = new BasicImageDownloader(
                new BasicImageDownloader.OnImageLoaderListener() {

            @Override
            public void onError(BasicImageDownloader.ImageError error) {
                if (error.getErrorCode() != BasicImageDownloader.ImageError.ERROR_FILE_EXISTS){
//                    Toast.makeText(MainActivity.this, "Error code " + error.getErrorCode() + ": " +
//                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
                error.printStackTrace();
//            imgDisplay.setImageResource(RES_ERROR);
//            tvPercent.setVisibility(View.GONE);
//            pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onProgressChange(int percent) {
//            pbLoading.setProgress(percent);
//            tvPercent.setText(percent + "%");
            }

            @Override
            public void onComplete(Bitmap result, String imageURL) {

                String imagePath = imageURL.substring(serverURL.length());
//                Log.i(TAG, ">>>>> UPDATE: IN OnCOMPLETE imagePath =\n"+imagePath);

                if(imagePath.contains(".jpeg") || imagePath.contains(".jpg") || imagePath.contains(".png")){
                    imagePath = imagePath.substring(0, imagePath.lastIndexOf('.'));
                }
                if (imagePath.contains("uploads/")){
                    imagePath = imagePath.replace("uploads/", "");
                }
//                Log.i(TAG, ">>>>> UPDATE: IN OnCOMPLETE imagePath NOW =\n"+imagePath);
//                Toast.makeText(MainActivity.this, "Downloading image: "+imagePath, Toast.LENGTH_LONG).show();

                /* save the image */
                final Bitmap.CompressFormat mFormat = Bitmap.CompressFormat.PNG;
                final File myImageFile = new File(mainDir.getAbsolutePath() + File.separator + imagePath + "." + mFormat.name().toLowerCase());
                BasicImageDownloader.writeToDisk(myImageFile, result, new BasicImageDownloader.OnBitmapSaveListener() {
                    @Override
                    public void onBitmapSaved() {
//                        Toast.makeText(MainActivity.this, "Image saved as: " + myImageFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onBitmapSaveError(BasicImageDownloader.ImageError error) {
//                        Toast.makeText(MainActivity.this, "Error code " + error.getErrorCode() + ": " +
//                                error.getMessage(), Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }

                }, mFormat, false);

//            tvPercent.setVisibility(View.GONE);
//            pbLoading.setVisibility(View.GONE);
//            imgDisplay.setImageBitmap(result);
//            imgDisplay.startAnimation(AnimationUtils.loadAnimation(ImageActivity.this, android.R.anim.fade_in));
            }
        });

            new AsyncTask<Void, Integer, String>() {

                @Override
                protected void onPreExecute() {
                    showProgress(true);
                    progressStatus.setText("Checking for updates");
                }

                @Override
                protected void onCancelled() {
                    showProgress(false);
                    this.cancel(true);
                }

                @Override
                protected String doInBackground(Void... params) {
                    Log.i(TAG, ">>>>> UPDATE: doInBackground");
                    HttpURLConnection connection = null;
                    InputStream is = null;
                    ByteArrayOutputStream out = null;
                    String result = "";
                    try {
                        URL url = new URL(serverURL + "android/update");
                        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                        httpConn.setRequestMethod("POST");
                        httpConn.setDoOutput(true);
                        httpConn.setDoInput(true);
                        OutputStream opStream = httpConn.getOutputStream();
                        BufferedWriter bufferedWriter = new BufferedWriter(
                                new OutputStreamWriter(opStream, "UTF-8"));
                        String postData = "email=" + mEmail;

                        bufferedWriter.write(postData);
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        opStream.close();

                        // Receive result of post message
                        InputStream inputStream = httpConn.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(inputStream, "iso-8859-1"));
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            result += line;
                        }
                        bufferedReader.close();
                        inputStream.close();
                        httpConn.disconnect();
                        Log.i(TAG, ">>>>> UPDATE: RESULT = \n" + result);

                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File Not Found exception", e);
                        new Thread() {
                            public void run() {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Unable to connect to server," +
                                                " please try again later",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }.start();
                        this.cancel(true);
                        return result;
                    } catch (UnknownHostException e) {
                        Log.e(TAG, "UnknownHostException", e);
                        new Thread() {
                            public void run() {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "No network access. " +
                                                "Please enable mobile data or connect to a WiFi network.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }.start();
                        this.cancel(true);
                        return result;
                    } catch (Throwable e) {
                        if (!this.isCancelled()) {
                            Log.e(TAG, "exception", e);
                            Log.e(TAG, "exceptionType:\n"+e.toString());
                            new Thread() {
                                public void run() {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "An error occurred, " +
                                                    "please try again later", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }.start();
                            this.cancel(true);
                            return result;
                        }
                    } finally {
                        try {
                            if (connection != null)
                                connection.disconnect();
                            if (out != null) {
                                out.flush();
                                out.close();
                            }
                            if (is != null)
                                is.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return result;
                        }
                    }
                    return result;
                }

                @Override
                protected void onPostExecute(String result) {
                    Log.i(TAG, ">>>>> UPDATE: onPostExecute. Result =\n"+result);
                    if (result.startsWith("Error:") && Character.isDigit(result.charAt(result.length() - 1))) {
                        int errorCode = (int) result.charAt(result.length() - 1);
                        String errorMessage;
                        switch (errorCode) {
                            case 1:
                                errorMessage = "Incorrect login details";
                                break;
                            case 2:
                                errorMessage = "No new files to download";
                                break;
                            case 3:
                                errorMessage = "No new messages to download";
                                break;
                            case 4:
                                errorMessage = "Message not sent. Please try later";
                                break;
                            default:
                                errorMessage = "Error encountered. Please try later";
                        }
                        Log.e(TAG, ">>>>> UPDATE: ERROR ENCOUNTERED: " + errorMessage);
                        toastMessage[0] = errorMessage;
                        this.cancel(true);
                    } else if (result == "") {
                        Log.e(TAG, "UPDATE: Download returned a null result");
                        toastMessage[0] = "App up to date!";
                        this.cancel(true);
                    } else {
                        for (String path : result.split("<br>")) {
                            if (!path.equals(null) && !path.equals("")) {
                                path = path.replace("\n", "").replace("\r", "");
                                path = path.substring(1);
                                downloadPaths.add(path);
                                Log.i(TAG, ">>>>> UPDATE: ADD PATH =\n" + path);
                            }
                        }
                        progressStatus.setText("Downloading resources");
                        for (String path : downloadPaths) {
                            // Download images
                            if (path.endsWith("jpeg") || path.endsWith("jpg") || path.endsWith("png")) {
                                imageDownloader.download(serverURL, path, true);
                                Log.i(TAG, ">>>>> UPDATE: calling imageDownloader.download ON " + path);
                            //Download text
                            } else if (path.endsWith("txt")) {
                                Log.i(TAG, ">>>>> UPDATE: calling textDownloader.download ON " + path);
                                textDownloader.download(mainDir, serverURL, path);
                            }
                        }
                        toastMessage[0] = "Resources downloaded";
                    }
                    showProgress(false);

                    System.gc();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Toast.makeText(MainActivity.this, "Update complete. " + toastMessage[0], Toast.LENGTH_LONG).show();
    }

    /**
     * Helper method for clearing phone's internal memory contents during debugging
     * @param file Folder/file to be deleted
     */
    void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

}
