package org.mukdongjeil.mjchurch.models;

/**
 * Created by gradler on 26/07/2017.
 */

public class Message {

    public boolean isImage = false;
    public User writer;
    public String body;
    public long timeStamp;

    public Message() {}

    public Message(User writer, String body) {
        this.writer = writer;
        this.body = body;
        timeStamp = System.currentTimeMillis();
    }
}
