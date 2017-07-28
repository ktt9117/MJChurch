package org.mukdongjeil.mjchurch.models;

/**
 * Created by gradler on 26/07/2017.
 */

public class User {
    public String email;
    public String name;
    public String photoUrl;

    public User() {}

    public User(String name) {
        this.name = name;
    }

    public User(String name, String photoUrl) {
        this.name = name;
        this.photoUrl = photoUrl;
    }
}
