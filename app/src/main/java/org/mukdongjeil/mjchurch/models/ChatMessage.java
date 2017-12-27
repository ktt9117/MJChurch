package org.mukdongjeil.mjchurch.models;

/**
 * Created by gradler on 26/07/2017.
 */

public class ChatMessage {

    public String name;
    public String id;
    public String body;
    public String imgUrl;
    public String email;
    public String avatarUrl;
    public long timeStamp;

    public ChatMessage() {
        timeStamp = System.currentTimeMillis();
    }

    public ChatMessage(String name, String email, String body, String avatarUrl) {
        this.name = name;
        this.email = email;
        this.body = body;
        this.avatarUrl = avatarUrl;
        timeStamp = System.currentTimeMillis();
    }
}
