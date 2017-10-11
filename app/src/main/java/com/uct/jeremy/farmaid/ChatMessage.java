package com.uct.jeremy.farmaid;

/**
 * A simple object that holds a message's contents
 */

public class ChatMessage {
    public boolean left;
    public String message;
    public String time;

    public ChatMessage(boolean left , String message, String time) {
        super();
        this.left=left;
        this.message = message;
        this.time = time.substring(0, time.length()-6);
    }
}