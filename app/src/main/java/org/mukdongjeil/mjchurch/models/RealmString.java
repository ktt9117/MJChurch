package org.mukdongjeil.mjchurch.models;

import io.realm.RealmObject;

/**
 * Created by gradler on 08/06/2017.
 */

public class RealmString extends RealmObject {
    private String val;

    public RealmString() {

    }

    public RealmString(String value) {
        this.val = value;
    }

    public String getValue() {
        return val;
    }

    public void setValue(String value) {
        this.val = value;
    }
}