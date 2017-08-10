package org.mukdongjeil.mjchurch.models;

/**
 * Created by gradler on 26/07/2017.
 */

public class ChatMessage {

    public String id;
    public String body;
    public String imgUrl;
    public String email;
    public long timeStamp;

    public ChatMessage() {
        timeStamp = System.currentTimeMillis();
    }

    public ChatMessage(String email, String body) {
        this.email = email;
        this.body = body;
        timeStamp = System.currentTimeMillis();
    }
}
