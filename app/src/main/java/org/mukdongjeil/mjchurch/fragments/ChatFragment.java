package org.mukdongjeil.mjchurch.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.activities.ProfileMainActivity;
import org.mukdongjeil.mjchurch.activities.SignInActivity;
import org.mukdongjeil.mjchurch.adapters.ChatRecyclerAdapter;
import org.mukdongjeil.mjchurch.models.Message;
import org.mukdongjeil.mjchurch.models.User;
import org.mukdongjeil.mjchurch.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends Fragment implements View.OnClickListener, ChildEventListener, View.OnFocusChangeListener {
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;

    private ChatRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference();

    private FirebaseAuth mAuth;
    private User mUserMySelf;

    private EditText mMessageField;
    private Button mBtnSend;

    private List<Message> mMessages;
    private boolean mIsDialogShowing = false;

    public ChatFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(R.string.menu_chat);

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mMessageField = (EditText) view.findViewById(R.id.edt_message);
        mMessageField.setOnFocusChangeListener(this);
        mBtnSend = (Button) view.findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);

//        ((MainActivity) getActivity()).showLoadingDialog();
//        mIsDialogShowing = true;

        setupRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view_message));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDatabaseReference.child("message").limitToLast(20).addChildEventListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDatabaseReference.child("message").removeEventListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        setUserInformation(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            inflater.inflate(R.menu.menu_chat, menu);
        } else {
            inflater.inflate(R.menu.menu_login, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_btn_logout:
                logout();
                return true;
            case R.id.action_btn_profile:
                showProfileActivity();
                return true;
            case R.id.action_btn_login:
                Intent signInIntent = new Intent(getActivity(), SignInActivity.class);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            Toast.makeText(getActivity(), "로그인 완료\n메시지를 전송할 수 있습니다.", Toast.LENGTH_LONG).show();
            setUserInformation(mAuth.getCurrentUser());
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_send: {
                sendMessage();
                break;
            }
        }
    }

    private void setupRecyclerView(RecyclerView view) {
        // Set the adapter
        Context context = view.getContext();
        mMessages = new ArrayList<>();
        mRecyclerView = view;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new ChatRecyclerAdapter(getActivity(), mMessages, new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(Message item) {
                if (mMessageField == null) return;
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mMessageField.getWindowToken(), 0);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }

    private void setUserInformation(FirebaseUser user) {
        mAdapter.setUser(user);
        if (user != null) {
            mUserMySelf = new User();
            String name = user.getDisplayName();
            String email = user.getEmail();
            if (TextUtils.isEmpty(name)) {
                mUserMySelf.name = email;
            } else {
                mUserMySelf.name = name;
            }

            mUserMySelf.email = email;
            if (user.getPhotoUrl() != null) {
                mUserMySelf.photoUrl = user.getPhotoUrl().toString();
            }
        } else {
            mUserMySelf = null;
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        getActivity().invalidateOptionsMenu();
    }

    private void sendMessage() {
        if (mAuth.getCurrentUser() == null) {
            Intent signInIntent = new Intent(getActivity(), SignInActivity.class);
            startActivityForResult(signInIntent, RC_SIGN_IN);
            Toast.makeText(getActivity(), R.string.login_required, Toast.LENGTH_LONG).show();
            return;
        }

        String text = mMessageField.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }


        mBtnSend.setEnabled(false);

        final Message message = new Message(mUserMySelf, text);
        mDatabaseReference.child("message").push().setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mBtnSend.setEnabled(true);
                if (databaseError != null) {
                    Toast.makeText(getActivity(), databaseError.getDetails(), Toast.LENGTH_LONG).show();
                    return;
                }

                mMessageField.setText("");
            }
        });
    }

    private void showProfileActivity() {
        Intent profileIntent = new Intent(getActivity(), ProfileMainActivity.class);
        startActivity(profileIntent);
    }

    private void logout() {
        mAuth.signOut();
        setUserInformation(null);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        mMessages.add(dataSnapshot.getValue(Message.class));
        int lastPosition = mMessages.size() - 1;

        if (mAdapter != null) {
            mAdapter.notifyItemInserted(lastPosition);
            mRecyclerView.scrollToPosition(lastPosition);
        }

        if (mIsDialogShowing == true) {
            ((MainActivity) getActivity()).hideLoadingDialog();
            mIsDialogShowing = false;
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Logger.d(TAG, "onChildChanged > s : " + s);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Logger.d(TAG, "onChildRemoved > dataSnapshot : " + dataSnapshot);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Logger.d(TAG, "onChildMoved > s : " + s);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        ((MainActivity) getActivity()).hideLoadingDialog();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Logger.d(TAG, "onFocusChange hasFocus : " + hasFocus);
        if (hasFocus) {
            if (mRecyclerView != null) {
                mRecyclerView.scrollToPosition(mMessages.size() - 1);
            }
        }
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Message item);
    }
}