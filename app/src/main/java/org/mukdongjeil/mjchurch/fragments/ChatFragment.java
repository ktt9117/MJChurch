package org.mukdongjeil.mjchurch.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.activities.ProfileMainActivity;
import org.mukdongjeil.mjchurch.adapters.ChatAdapter;
import org.mukdongjeil.mjchurch.models.ChatMessage;
import org.mukdongjeil.mjchurch.utils.ExHandler;
import org.mukdongjeil.mjchurch.utils.Logger;
import org.mukdongjeil.mjchurch.utils.PreferenceUtil;

import java.util.ArrayList;

public class ChatFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    private static final int RC_SIGN_IN = 100;
    private static final int RC_IMAGE_PICK = 200;

    public static boolean IS_CHATROOM_FOREGROUND = false;

    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;
    private ChatAdapter mChatAdapter;
    private ArrayList<ChatMessage> mMessageList;

    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().getRoot();
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;

    private EditText mMessageField;
    private Button mBtnSend;
    private ImageView mBtnAddImage;

    private NotificationManager mNotiManager;
    private InputMethodManager mInputMethodManager;
    private RequestManager mGlide;
    private boolean isFirstItemAdded = false;

    private static final int MSG_WHAT_LOGOUT = 100;
    private ExHandler<ChatFragment> mHandler = new ExHandler<ChatFragment>(this) {
        @Override
        protected void handleMessage(ChatFragment reference, android.os.Message msg) {
            Logger.i(TAG, "msg.what : " + msg.what);
            if (msg.what == MSG_WHAT_LOGOUT) {
                if (reference != null && reference.isLoadingDialogShowing()) {
                    Toast.makeText(reference.getActivity(), R.string.auth_problem_occured, Toast.LENGTH_LONG).show();
                    reference.closeLoadingDialog();
                    reference.doLogout();
                }
            }
        }
    };

    public ChatFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initializeGoogleSignIn();
        mAuth = FirebaseAuth.getInstance();
        mNotiManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mGlide = Glide.with(this);

        if (PreferenceUtil.allowChatNotification()) {
            FirebaseMessaging.getInstance().subscribeToTopic(Const.CHATROOM_TOPIC);
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Const.CHATROOM_TOPIC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(R.string.menu_chat);

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mMessageField = (EditText) view.findViewById(R.id.edt_message);
        mMessageField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mBtnSend.setEnabled(true);
                } else {
                    mBtnSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mBtnSend = (Button) view.findViewById(R.id.btn_send);
        mBtnAddImage = (ImageView) view.findViewById(R.id.btn_add_image);
        mBtnSend.setOnClickListener(this);
        mBtnAddImage.setOnClickListener(this);

        showLoadingDialog();
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_LOGOUT, 7000);
        Logger.i(TAG, "send handler message MSG_WHAT_LOGOUT. It will called after 7000");

        setupRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view_message));
        loadMessages();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setUserInformation(mAuth.getCurrentUser());
    }

    @Override
    public void onResume() {
        super.onResume();
        IS_CHATROOM_FOREGROUND = true;
        mNotiManager.cancelAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        IS_CHATROOM_FOREGROUND = false;
        mHandler.removeMessages(MSG_WHAT_LOGOUT);
        Logger.i(TAG, "remove message MSG_WHAT_LOGOUT on onPause");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.i(TAG, "onCreateOptionsMenu");
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            inflater.inflate(R.menu.menu_chat, menu);
            for (int i = 0 ; i < menu.size(); i++) {
                if (menu.getItem(i).getItemId() == R.id.action_checkbox_notify) {
                    Logger.i(TAG, "this menu item is notify checkbox");
                    MenuItem menuItem = menu.getItem(i);
                    boolean allowNotify = PreferenceUtil.allowChatNotification();
                    menuItem.setChecked(allowNotify);
                    menuItem.setTitle(allowNotify ? R.string.notification_on : R.string.notification_off);
                    menuItem.setIcon(allowNotify ? R.drawable.ic_notify_on : R.drawable.ic_notify_off);
                    break;
                }
            }
        } else {
            inflater.inflate(R.menu.menu_login, menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_checkbox_notify:
                changeNotificationOption();
                return true;
            case R.id.action_btn_profile:
                showProfileActivity();
                return true;
            case R.id.google_sign_in:
                Logger.e(TAG, "Trying to Google sign in");
                signInWithGoogle();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {
                Logger.e(TAG, "Login unsuccessful result status : " + result.getStatus());
                Toast.makeText(getContext(), R.string.login_failed, Toast.LENGTH_LONG);
            }

            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_IMAGE_PICK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Logger.d(TAG, "Uri: " + uri.toString());
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReference(mAuth.getCurrentUser().getUid())
                            .child(uri.getLastPathSegment());

                    showLoadingDialog();
                    storageReference.putFile(uri).addOnCompleteListener(getActivity(),
                            new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    closeLoadingDialog();
                                    if (task.isSuccessful()) {
                                        sendImageMessage(task.getResult().getDownloadUrl().toString());

                                    } else {
                                        Log.w(TAG, "Image upload task was not successful.",
                                                task.getException());
                                    }
                                }
                            });

//                    final ChatMessage tempChatMessage = new ChatMessage();
//                    tempChatMessage.name = user.getDisplayName();
//                    tempChatMessage.email = user.getEmail();
//                    tempChatMessage.imgUrl = LOADING_IMAGE_URL;
//                    tempChatMessage.avatarUrl = user.getPhotoUrl().toString();
//
//                    mRef.child(Const.getMessageDatabaseUri()).push()
//                        .setValue(tempChatMessage, new DatabaseReference.CompletionListener() {
//                            @Override
//                            public void onComplete(DatabaseError databaseError,
//                                                   DatabaseReference databaseReference) {
//                                if (databaseError == null) {
//                                    String key = databaseReference.getKey();
//                                    StorageReference storageReference =
//                                            FirebaseStorage.getInstance()
//                                                    .getReference(mAuth.getCurrentUser().getUid())
//                                                    .child(key)
//                                                    .child(uri.getLastPathSegment());
//
//                                    putImageInStorage(storageReference, uri, key);
//                                } else {
//                                    Log.w(TAG, "Unable to write message to database.",
//                                            databaseError.toException());
//                                }
//                            }
//                        });
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_send: {
                sendMessage();
                break;
            }
            case R.id.btn_add_image: {
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(getActivity(), R.string.login_required, Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(Const.MIME_TYPE_IMAGES);
                startActivityForResult(intent, RC_IMAGE_PICK);
                break;
            }
        }
    }

    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getContext()).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }

    private void setupRecyclerView(final RecyclerView view) {
        // Set the adapter
        Context context = view.getContext();
        mRecyclerView = view;
        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setStackFromEnd(true);

        String emailAddress = null;
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            emailAddress = mAuth.getCurrentUser().getEmail();
        }

        mMessageList = new ArrayList<>();
        mChatAdapter = new ChatAdapter(getActivity(), mMessageList, mGlide, emailAddress,
                new ChatAdapter.OnRowItemClickedListener() {
                    @Override
                    public void onRowItemClicked(View view) {
                        if (mMessageField == null) return;
                        mInputMethodManager.hideSoftInputFromWindow(mMessageField.getWindowToken(), 0);
                    }
                }
        );

        mChatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                Logger.i(TAG, "onItemRangeInserted");
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = mChatAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mChatAdapter);
    }

    private void setUserInformation(FirebaseUser user) {
        if (user != null) {
            if (mChatAdapter != null) {
                mChatAdapter.setMyEmailAddress(mAuth.getCurrentUser().getEmail());
            }
        } else {
            if (mChatAdapter != null) {
                mChatAdapter.setMyEmailAddress(null);
            }
        }

        getActivity().invalidateOptionsMenu();
    }

    private void sendMessage() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getActivity(), R.string.login_required, Toast.LENGTH_LONG).show();
            return;
        }

        String text = mMessageField.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }

        mBtnSend.setEnabled(false);
        final FirebaseUser user = mAuth.getCurrentUser();
        final ChatMessage chatMessage = new ChatMessage(user.getDisplayName(), user.getEmail(),
                text, user.getPhotoUrl().toString());
        mRef.child(Const.getMessageDatabaseUri()).push().setValue(chatMessage,
                new DatabaseReference.CompletionListener() {
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

    private void sendImageMessage(String imageUrl) {
        showLoadingDialog();
        final FirebaseUser user = mAuth.getCurrentUser();
        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.name = user.getDisplayName();
        chatMessage.email = user.getEmail();
        chatMessage.imgUrl = imageUrl;
        chatMessage.avatarUrl = user.getPhotoUrl().toString();
        mRef.child(Const.getMessageDatabaseUri()).push().setValue(chatMessage,
            new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    closeLoadingDialog();
                    if (databaseError != null) {
                        Toast.makeText(getActivity(), databaseError.getDetails(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(getActivity(), "이미지 전송 완료", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showProfileActivity() {
        Intent profileIntent = new Intent(getActivity(), ProfileMainActivity.class);
        startActivity(profileIntent);
    }

    private void changeNotificationOption() {
        boolean currentNotificationOption = PreferenceUtil.allowChatNotification();
        boolean tobeOption = !currentNotificationOption;
        PreferenceUtil.setNotificationOption(tobeOption);
        getActivity().invalidateOptionsMenu();

        Toast.makeText(getActivity(),
                tobeOption ? R.string.message_notification_on : R.string.message_notification_off,
                Toast.LENGTH_SHORT)
                .show();

        if (tobeOption) {
            FirebaseMessaging.getInstance().subscribeToTopic(Const.CHATROOM_TOPIC);
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Const.CHATROOM_TOPIC);
        }
    }

    private void doLogout() {
        mAuth.signOut();
        getActivity().invalidateOptionsMenu();
    }

    private void signInWithGoogle() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity()) != ConnectionResult.SUCCESS) {
            showAlert(R.string.login_unavailable, R.string.google_play_unavailable);
            return;
        }

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Logger.e(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showLoadingDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        closeLoadingDialog();

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Logger.e(TAG, "signInWithCredential:success");
                            Toast.makeText(getContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
                            setUserInformation(mAuth.getCurrentUser());

                        } else {
                            // If sign in fails, display a message to the user.
                            Logger.e(TAG, "signInWithCredential:failure", task.getException());
                            showAlert(R.string.auth_failure, "인증 실패 : " + task.getException().getLocalizedMessage());
                        }

                    }
                });
    }

    private void showAlert(int title, int message) {
        showAlert(getString(title), getString(message));
    }

    private void showAlert(int title, String message) {
        showAlert(getString(title), message);
    }

    private void showAlert(String title, String message) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        dialog.show();
    }

    private void loadMessages() {
        Query query = mRef.child(Const.getMessageDatabaseUri()).limitToLast(100);
        query.addChildEventListener(mChildEventListener);
    }

    private ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Logger.e(TAG, "onChildAdded : " + dataSnapshot.getValue() + ", " + s);
            if (isFirstItemAdded == false) {
                closeLoadingDialog();
                mHandler.removeMessages(MSG_WHAT_LOGOUT);
                isFirstItemAdded = true;
            }

            mMessageList.add(dataSnapshot.getValue(ChatMessage.class));
            int index = mMessageList.size() - 1;
            if (index < 0) index = 0;
            mChatAdapter.notifyItemRangeInserted(index, 1);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Logger.e(TAG, "onChildChanged : " + dataSnapshot.getValue() + ", " + s);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Logger.e(TAG, "onChildRemoved : " + dataSnapshot.getValue());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Logger.e(TAG, "onChildMoved : " + dataSnapshot.getValue() + ", " + s);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Logger.e(TAG, "onCancelled : " + databaseError.getDetails());
        }
    };
}