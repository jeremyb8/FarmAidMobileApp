package com.uct.jeremy.farmaid;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that stores messages which is accessed when populating UI
 */

public class ChatArrayAdapter extends ArrayAdapter<ChatMessage>{
    private final String TAG = this.getClass().getSimpleName();
    private List<ChatMessage> MessageList = new ArrayList<ChatMessage>();
    Context context;
    Activity mActivity;

    private TextView chatText;
    private TextView timeText;
    private LinearLayout messageLayout;
    private LinearLayout messageContainer;


    public ChatArrayAdapter(Activity activity, Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context=context;
        this.mActivity=activity;
    }

    public void superAdd(ChatMessage object) {
        super.add(object);
    }

    public void add(final ChatMessage object) {
        Log.i(TAG, ">>>>> ChatArrayAdapter: trying new thread");
        Thread thread = new Thread() {
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MessageList.add(object);
                        superAdd(object);
                        Log.i(TAG, ">>>>> ChatArrayAdapter: added ChatMessage\n"+object.message);
                    }
                });
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, ">>>>> ChatArrayAdapter: InterruptedException "+e);
        }
    }

    public int getCount() {return this.MessageList.size();}

    public ChatMessage getItem(int index){
        return this.MessageList.get(index);
    }

    public View getView(int position,View ConvertView, ViewGroup parent){
        View v = ConvertView;
        if(v==null){
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v =inflater.inflate(R.layout.chat_messages, parent,false);
        }

        messageLayout = (LinearLayout)v.findViewById(R.id.HorizontalMessageLayout);
        ChatMessage messageobj = getItem(position);
        timeText =(TextView)v.findViewById(R.id.MessageTimeText);
        timeText.setText(messageobj.time);
        chatText =(TextView)v.findViewById(R.id.MessageText);
        chatText.setText(messageobj.message);

        messageContainer = (LinearLayout)v.findViewById(R.id.MessageContainer);
        messageContainer.setBackgroundResource(messageobj.left ? R.drawable.message_left :R.drawable.message_right);
        messageLayout.setGravity(messageobj.left?Gravity.LEFT:Gravity.RIGHT);

        return v;
    }
}

