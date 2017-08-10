package org.mukdongjeil.mjchurch.services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.models.User;
import org.mukdongjeil.mjchurch.utils.Logger;
import org.mukdongjeil.mjchurch.utils.StringUtils;

/**
 * Created by gradler on 09/08/2017.
 */

public class FirebaseDataHelper {
    private static final String TAG = FirebaseDataHelper.class.getSimpleName();

    public interface OnUserQueryListener {
        void onResult(User user);
    }

    public static void getUserByEmail(DatabaseReference ref, String email, final OnUserQueryListener listener) {
        Logger.i(TAG, "getUserByEmail : " + email);
        if (ref == null || email == null || listener == null) {
            Logger.e(TAG, "getUser failed. caused by there is an invalid param");
            return;
        }

        ref.child(Const.FIRE_DATA_USER_CHILD).child(StringUtils.convertHashCode(email))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Logger.e(TAG, "dataSnapshot : " + dataSnapshot.toString());
                }

                if (dataSnapshot != null && dataSnapshot.getChildrenCount() > 0) {
                    User user = dataSnapshot.getValue(User.class);
                    listener.onResult(user);
                } else {
                    listener.onResult(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Logger.e(TAG, "databaseError occured : " + databaseError.getMessage());
                listener.onResult(null);
            }
        });
    }

    public static void createOrUpdateUser(DatabaseReference ref, User user) {
        if (ref == null || user == null) {
            Logger.e(TAG, "createOrUpdateUser failed. caused by there is an invalid param");
            return;
        }

        ref.child(Const.FIRE_DATA_USER_CHILD).child(StringUtils.convertHashCode(user.email)).setValue(user);
    }
}
