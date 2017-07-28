package org.mukdongjeil.mjchurch.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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
import org.mukdongjeil.mjchurch.utils.Logger;
import org.mukdongjeil.mjchurch.models.Message;
import org.mukdongjeil.mjchurch.models.User;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends Fragment implements View.OnClickListener, ChildEventListener, View.OnFocusChangeListener {
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;

    private ChatRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private FirebaseAuth mAuth;
    private User me;

    private EditText edtMessage;
    private Button btnSend;

    private List<Message> mMessages;
    private boolean isDialogShowing = false;

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
        edtMessage = (EditText) view.findViewById(R.id.edt_message);
        edtMessage.setOnFocusChangeListener(this);
        btnSend = (Button) view.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);

        ((MainActivity) getActivity()).showLoadingDialog();
        isDialogShowing = true;

        setupRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view_message));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        databaseReference.child("message").limitToLast(20).addChildEventListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        databaseReference.child("message").removeEventListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
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
                if (edtMessage == null) return;
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtMessage.getWindowToken(), 0);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }

    private void setUserInformation(FirebaseUser user) {
        mAdapter.setUser(user);
        if (user != null) {
            me = new User();
            String name = user.getDisplayName();
            String email = user.getEmail();
            if (TextUtils.isEmpty(name)) {
                me.name = email;
            } else {
                me.name = name;
            }

            me.email = email;
            if (user.getPhotoUrl() != null) {
                me.photoUrl = user.getPhotoUrl().toString();
            }
        } else {
            me = null;
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

        String text = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }


        btnSend.setEnabled(false);

        final Message message = new Message(me, text);
        databaseReference.child("message").push().setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                btnSend.setEnabled(true);
                if (databaseError != null) {
                    Toast.makeText(getActivity(), databaseError.getDetails(), Toast.LENGTH_LONG).show();
                    return;
                }

                edtMessage.setText("");
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

        if (isDialogShowing == true) {
            ((MainActivity) getActivity()).hideLoadingDialog();
            isDialogShowing = false;
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