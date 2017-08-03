package org.mukdongjeil.mjchurch.models;

/**
 * Created by gradler on 26/07/2017.
 */

public class Message {

    public String id;
    public User writer;
    public String body;
    public String imgUrl;
    public long timeStamp;

    public Message() {
        timeStamp = System.currentTimeMillis();
    }

    public Message(User writer, String body) {
        this.writer = writer;
        this.body = body;
        timeStamp = System.currentTimeMillis();
    }
}
