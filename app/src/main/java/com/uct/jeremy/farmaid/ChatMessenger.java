package com.uct.jeremy.farmaid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by jeremy on 2017/09/06.
 * Adapted from https://github.com/vad-zuev/ImageDownloader
 */

class ChatMessenger {
    private final String TAG = this.getClass().getSimpleName();
    private Activity activity;
    private File messageLog;
    private ChatArrayAdapter adp;
    String email;

    ChatMessenger(Activity activity, File messageLog, ChatArrayAdapter adp, String email){
        this.activity = activity;
        this.messageLog = messageLog;
        this.adp = adp;
        this.email = email;
    }

    /**
     * Send/receive message(s).
     * @param message Message to be sent (or null if receiving)
     * @param type T = sending message, F = receiving
     */
    void handleMessage(final String message, final Boolean type) {
    // boolean type reflects whether a message is being sent (T), or a call to check for new messages is being made (F)
        Log.i(TAG, ">>>>> ChatMessenger handleMessage email:\n"+email);

        new AsyncTask<Void, Integer, String>() {

            private BasicImageDownloader.ImageError error;
            HttpURLConnection connection = null;

            @Override
            protected void onCancelled() {
                connection.disconnect();
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
            }

            @Override
            protected String doInBackground(Void... params) {
                String serverUrl = "https://farmaid.cs.uct.ac.za";
                String result = "";
                Log.i(TAG, ">>>>> ChatMessenger ABOUT TO TRY");

                try {
                    // Compose http post message and set desired method
                    String postData;
                    if(type){ // case where user is sending message
                        serverUrl += "/android/send_message";
//                        postData = URLEncoder.encode(email+"<>"+message, "UTF-8");
                        postData = email+"<>"+message;
                    }else{ // case where user is receiving message(s)
                        serverUrl += "/android/update_messages";
//                        postData = URLEncoder.encode("email="+email, "UTF-8");
                        postData = email;
                    }

                    // Setup connection
                    URL url = new URL (serverUrl);
                    Log.i(TAG, ">>>>> ChatMessenger CREATED URL");
                    connection = (HttpURLConnection)url.openConnection();
                    Log.i(TAG, ">>>>> ChatMessenger HTTP CONN OPENED");

                    connection.setRequestMethod("POST");

                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    Log.i(TAG, ">>>>> ChatMessenger HTTP SETS COMPLETE");
                    OutputStream opStream = connection.getOutputStream();
                    Log.i(TAG, ">>>>> ChatMessenger GOT OP STREAM");
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(opStream, "UTF-8"));
                    Log.i(TAG, ">>>>> ChatMessenger CREATED I/O OBJECTS");

                    // Send POST request
                    bufferedWriter.write(postData);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    opStream.close();

                    // Receive result of POST request
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String line;
                    while ((line = bufferedReader.readLine()) != null){
                        result += line;
                    }
                    bufferedReader.close();
                    inputStream.close();
                    connection.disconnect();

                    Log.i(TAG, ">>>>> ChatMessenger CLOSED OUTPUT OBJECTS");

                    Log.i(TAG, ">>>>> ChatMessenger Result: \n"+result);

                    return result;
                } catch (MalformedURLException e) {
                    Log.e("com.uct.jeremy.farmaid", "exception", e);
                    Log.i(TAG, ">>>>> ChatMessenger MalformedURLException");
                    connection.disconnect();
                } catch (UnknownHostException e) {
                Log.e(TAG, ">>>>> UPDATE: UnknownHostException");
                Log.e(TAG, "UnknownHostException", e);
                new Thread() {
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, "No network access. Please enable mobile data or connect to a WiFi network.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }.start();
                this.cancel(true);
                return result;
                } catch (IOException e) {
                    Log.e("com.uct.jeremy.farmaid", "exception", e);
                    Log.i(TAG, ">>>>> ChatMessenger IOException");
                    connection.disconnect();
                }
                Log.i(TAG, ">>>>> ChatMessenger returning here");
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                Log.i(TAG, ">>>>> ChatMessenger onPostExecute Result: \n"+result);
                if(result.startsWith("Error:") && Character.isDigit(result.charAt(result.length()-1))){
                    int errorCode = Character.getNumericValue(result.charAt(result.length()-1));
                    Log.i(TAG, ">>>>> ChatMessenger errorCode = "+errorCode);
                    String errorMessage = "";
                    switch (errorCode){
                        case 1: errorMessage = "Incorrect login details";
                            Log.i(TAG, ">>>>> ChatMessenger email:\n"+email);
                            break;
                        case 2: errorMessage = "No new files to download"; break;
                        case 3: errorMessage = "No new messages to download"; break;
                        case 4: errorMessage = "Message not sent. Please try later"; break;
                    }
                    Log.e(TAG, ">>>>> ChatMessenger: ERROR ENCOUNTERED: "+errorMessage);
                    this.cancel(true);
                }
                else if (result.equals("")) {
                    Log.e(TAG, "ChatMessenger: Download returned a null result");
                    this.cancel(true);
                }
                else {
                    FileWriter fw = null;
                    BufferedWriter bw = null;
                    try {
                        fw = new FileWriter(messageLog.getAbsoluteFile(), true);
                        bw = new BufferedWriter(fw);

                        String[] splitMsgs = splitMessages(result);
                        String[] splitTimes = splitTimes(result);
                        // Handle server response based on type
                        if (splitMsgs!=null && splitTimes!=null) {
                            if (type) { // Sent message
                                adp.add(new ChatMessage(false, splitMsgs[0], splitTimes[0]));
                                Log.i(TAG, ">>>>> ChatMessenger: added send message");
                            } else { // Receiving messages
                                ChatMessage chatMessage;
                                String msg;
                                String time;
                                for (int i =0; i<splitMsgs.length; i++) {
                                    msg = splitMsgs[i];
                                    time = splitTimes[i];
                                    chatMessage = new ChatMessage(true, msg, time);
                                    adp.add(chatMessage);
                                    Log.i(TAG, ">>>>> ChatMessenger: received message\n"+msg);
                                }
                            }
                            Log.i(TAG, ">>>>> ChatMessenger: about to write to file. Result=\n"+result);
                            bw.write(result);
                            Log.i(TAG, ">>>>> ChatMessenger: wrote to file");
                        }
                        else{
                            Log.i(TAG, ">>>>> ChatMessenger no messages received/sent. Result=\n"+result);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (bw != null) bw.close();
                            if (fw != null) fw.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                System.gc();
            }
        }.execute();
    }

    /**
    * Takes a string representing the response from the server (messagesText), indexes according to each
    * message's start, and returns a string array of the text in the messages
    * */
    private String[] splitMessages (String messagesText) throws IOException{
        String[] messages = null;
        ArrayList<Integer> indexes = new ArrayList<>();
        int i = messagesText.indexOf("Message:");
        while(i >= 0) {
            indexes.add(i);
            i = messagesText.indexOf("Message:", i+1);
        }
        if (indexes.size()>0){
            messages = new String[indexes.size()];
            for (int j = 0; j<indexes.size(); j++){
                if (j+1 == indexes.size()){
                    messages[j]=messagesText.substring(indexes.get(j));
                    messages[j] = messages[j].substring(8, messages[j].indexOf("<br/>"));
                }
                else if (j+1 < indexes.size()){
                    messages[j]=messagesText.substring(indexes.get(j), indexes.get(j+1));
                    messages[j] = messages[j].substring(8, messages[j].indexOf("<br/>"));
                }
            }
        }
        return messages;
    }

    /**
    * Takes a string representing the response from the server (messagesText), indexes according to each
    * time stamp's start, and returns a string array of the times in the messages
    * */
    private String[] splitTimes (String messagesText) throws IOException{
        String[] times = null;
        ArrayList<Integer> indexes = new ArrayList<>();
        int i = messagesText.indexOf("Time:");
        while(i >= 0) {
            indexes.add(i);
            i = messagesText.indexOf("Time:", i+1);
        }
        if (indexes.size()>0){
            times = new String[indexes.size()];
            for (int j = 0; j<indexes.size(); j++){
                if (j+1 == indexes.size()){
                    times[j]=messagesText.substring(indexes.get(j));
                    times[j] = times[j].substring(5, times[j].indexOf("<br/>")).replace(" UTC", "");
                    times[j] = times[j].substring(0, times[j].length()-3);
                }
                else if (j+1 < indexes.size()){
                    times[j]=messagesText.substring(indexes.get(j), indexes.get(j+1));
                    times[j] = times[j].substring(5, times[j].indexOf("<br/>")).replace(" UTC", "");
                    times[j] = times[j].substring(0, times[j].length()-3);
                }
            }
        }
        return times;
    }

    /**
    * Takes a string representing the response from the server (messagesText), indexes according to each
    * user name's start, and returns a string array of the users in the messages
    * */
    private Boolean[] splitUsers (String messagesText) throws IOException{
        String[] users;
        Boolean[] usersBoolean = null;
        ArrayList<Integer> indexes = new ArrayList<>();
        int i = messagesText.indexOf("From:");
        while(i >= 0) {
            indexes.add(i);
            i = messagesText.indexOf("From:", i+1);
        }
        if (indexes.size()>0){
            users = new String[indexes.size()];
            for (int j = 0; j<indexes.size(); j++){
                if (j+1 == indexes.size()){
                    users[j]=messagesText.substring(indexes.get(j));
                    users[j] = users[j].substring(5, users[j].indexOf("<br/>"));
                    Log.i(TAG, ">>>>> ChatMessenger: users[j] = "+users[j]);
                }
                else if (j+1 < indexes.size()){
                    users[j]=messagesText.substring(indexes.get(j), indexes.get(j+1));
                    users[j] = users[j].substring(5, users[j].indexOf("<br/>"));
                    Log.i(TAG, ">>>>> ChatMessenger: users[j] = "+users[j]);
                }
            }

            usersBoolean = new Boolean[users.length];
            for (int j = 0; j<users.length; j++){
                if (users[j].startsWith("user")){
                    usersBoolean[j] = false;
                }
                else if (users[j].startsWith("super")){
                    usersBoolean[j] = true;
                }
            }
        }


        return usersBoolean;
    }

    /**
     * Updates the UI to display the message history associated with the device, pulling all
     * messages from the MessageLog.txt file held in the phone's internal memory
     */
    void displayHistory(){
        String line;
        String logText = "";
        try {
            FileReader fileReader = new FileReader(messageLog);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) {
                logText+=line;
            }
            bufferedReader.close();
            fileReader.close();
            Log.i(TAG, ">>>>> ChatMessenger: logText =\n"+logText);

            String[] splitMsgs = splitMessages(logText);
            String[] splitTimes = splitTimes(logText);
            Boolean[] splitUsers = splitUsers (logText);

            if (splitMsgs!=null && splitTimes!=null && splitUsers!=null) {
                Log.i(TAG, ">>>>> ChatMessenger: adding messages from messageLog to adp");
                ChatMessage chatMessage;
                String msg;
                String time;
                Boolean user;
                for (int i =0; i<splitMsgs.length; i++) {
                    msg = splitMsgs[i];
                    time = splitTimes[i];
                    user = splitUsers[i];
                    chatMessage = new ChatMessage(user, msg, time);
                    adp.add(chatMessage);
                    Log.i(TAG, ">>>>> ChatMessenger: added message: "+msg+"\nTime: "+time+"\nFrom: "+user);
                }
            }
            else{
                Log.i(TAG, ">>>>> ChatMessenger no messages in messageLog.");
            }
        }
        catch(FileNotFoundException ex) {
            Log.i(TAG, "Unable to open file " + messageLog.getName());
        }
        catch(IOException ex) {
            Log.i(TAG, "Error reading file " + messageLog.getName());
        }
    }

}
