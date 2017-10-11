package com.uct.jeremy.farmaid;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;

/**
 * Messaging activity
 */

public class ChatActivity extends AppCompatActivity{
    private final String TAG = this.getClass().getSimpleName();
    private ChatArrayAdapter adp;
    private ListView list;
    private EditText chatText;
    private Button send;
    String email = "";
    String farmName = "";
//    String email = "robbie@gmail.com"; //Predefined email for debugging purposes
    ChatMessenger chatMessenger;
    File messageLog;
    Toolbar myToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat_main);
            myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);
            this.setTitle("Messaging");

            if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();
                if(extras != null) {
                    email = extras.getString("email");
                    farmName = extras.getString("farmName");
                }
            } else {
                email = (String) savedInstanceState.getSerializable("email");
                farmName = (String) savedInstanceState.getSerializable("farmName");
            }
            Log.i(TAG, ">>>>> CHATACTIVITY onCreate email=\n"+email);

            send = (Button) findViewById(R.id.btn);
            list = (ListView) findViewById(R.id.listview);
            adp = new ChatArrayAdapter(ChatActivity.this, getApplicationContext(), R.layout.chat_messages);
            list.setAdapter(adp);
            chatText = (EditText) findViewById(R.id.chat_text);
            chatText.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode ==
                            KeyEvent.KEYCODE_ENTER)) {
                        return sendChatMessage();
                    }
                    return false;
                }
            });
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    sendChatMessage();
                }
            });
            list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            list.setAdapter(adp);
            adp.registerDataSetObserver(new DataSetObserver() {
                public void OnChanged() {
                    super.onChanged();
                    list.setSelection(adp.getCount() - 1);
                }
            });
            messageLog = new File (this.getFilesDir().getAbsolutePath()+"/messageLog.txt");

            if (!messageLog.exists()) {
                Log.i(TAG, ">>>>> ChatActivity: messageLog doesn't exist");
                try {
                    messageLog.getParentFile().mkdirs();
                    messageLog.createNewFile();
                }catch (IOException e) {
                    Log.e("farmaid", "exception", e);
                }
            }
            else{ Log.i(TAG, ">>>>> ChatActivity: messageLog exists");}

            chatMessenger = new ChatMessenger(ChatActivity.this, messageLog, adp, email);
        // Check for any new messages since last opened
            chatMessenger.displayHistory();
            chatMessenger.handleMessage(null, false);
        }catch (Error e) {
            Log.e("farmaid", "exception", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem item = myToolbar.getMenu().findItem(R.id.message_action);
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
                intent = new Intent(ChatActivity.this, LoginActivity.class);
                break;
            case R.id.message_action:
                intent = new Intent(ChatActivity.this, ChatActivity.class);
                break;
            case R.id.home_action:
                intent = new Intent(ChatActivity.this, MainActivity.class);
                break;
            default: break;
        }
        if (intent!=null){
            intent.putExtra("email", email);
            intent.putExtra("farmName", farmName);
            if (id==R.id.logout_action){
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        }
        startActivity(intent);
        finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Call the assigned chatMessenger to send the text held in the chatText TextView,
     * first checking for any new messages
     * @return true
     */
    private boolean sendChatMessage(){
        try{
            receiveMessages();
            chatMessenger.handleMessage(chatText.getText().toString(), true);
            chatText.setText("");
        }catch (Error error){
            Log.e(TAG, error.toString());
        }
        return true;
    }

    /**
     * Call the assigned chatMessenger to receive all new messages on the server
     * @return true
     */
    private boolean receiveMessages(){
        try{
            chatMessenger.handleMessage(null, false);
        }catch (Error error){
            Log.e(TAG, error.toString());
        }
        return true;
    }
}