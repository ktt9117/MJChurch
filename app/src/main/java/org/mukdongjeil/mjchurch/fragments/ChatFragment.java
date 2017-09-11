package org.mukdongjeil.mjchurch.fragments;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.activities.ProfileMainActivity;
import org.mukdongjeil.mjchurch.activities.SignInActivity;
import org.mukdongjeil.mjchurch.models.ChatMessage;
import org.mukdongjeil.mjchurch.models.User;
import org.mukdongjeil.mjchurch.services.FirebaseDataHelper;
import org.mukdongjeil.mjchurch.utils.ExHandler;
import org.mukdongjeil.mjchurch.utils.Logger;
import org.mukdongjeil.mjchurch.utils.PreferenceUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import me.himanshusoni.chatmessageview.ChatMessageView;

public class ChatFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    private static final int RC_SIGN_IN = 100;
    private static final int RC_IMAGE_PICK = 200;

    public static boolean IS_CHATROOM_FOREGROUND = false;

    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1;

    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<ChatMessage, MessageHolder> mFirebaseAdapter;

    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().getRoot();
    private FirebaseAuth mAuth;
    private User mUserMySelf;

    private EditText mMessageField;
    private Button mBtnSend;
    private ImageView mBtnAddImage;

    private NotificationManager mNotiManager;
    private InputMethodManager mInputMethodManager;
    private RequestManager mGlide;

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

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_SIGN_IN) {
                Toast.makeText(getActivity(), R.string.login_success, Toast.LENGTH_LONG).show();
                setUserInformation(mAuth.getCurrentUser());
                FirebaseDataHelper.createOrUpdateUser(mRef, mUserMySelf);

            } else if (requestCode == RC_IMAGE_PICK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Logger.d(TAG, "Uri: " + uri.toString());

                    final ChatMessage tempChatMessage = new ChatMessage();
                    tempChatMessage.email = mUserMySelf.email;
                    tempChatMessage.imgUrl = LOADING_IMAGE_URL;

                    mRef.child(Const.FIRE_DATA_MESSAGE_CHILD).push()
                        .setValue(tempChatMessage, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError,
                                                   DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    String key = databaseReference.getKey();
                                    StorageReference storageReference =
                                            FirebaseStorage.getInstance()
                                                    .getReference(mAuth.getCurrentUser().getUid())
                                                    .child(key)
                                                    .child(uri.getLastPathSegment());

                                    putImageInStorage(storageReference, uri, key);
                                } else {
                                    Log.w(TAG, "Unable to write message to database.",
                                            databaseError.toException());
                                }
                            }
                        });
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
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(Const.MIME_TYPE_IMAGES);
                startActivityForResult(intent, RC_IMAGE_PICK);
                break;
            }
        }
    }

    private void setupRecyclerView(final RecyclerView view) {
        // Set the adapter
        Context context = view.getContext();
        mRecyclerView = view;
        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setStackFromEnd(true);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, MessageHolder> (
                ChatMessage.class,
                R.layout.row_chat_other,
                MessageHolder.class,
                mRef.child(Const.FIRE_DATA_MESSAGE_CHILD)) {

            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }

            @Override
            public int getItemViewType(int position) {
                ChatMessage item = getItem(position);
                if (item == null) {
                    return super.getItemViewType(position);
                }

                String email = item.email;
                if (mUserMySelf != null && !TextUtils.isEmpty(email) && email.equals(mUserMySelf.email)) {
                    return MY_MESSAGE;
                } else {
                    return OTHER_MESSAGE;
                }
            }

            @Override
            public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                if (viewType == MY_MESSAGE) {
                    return new MessageHolder(LayoutInflater.from(getActivity()).inflate(R.layout.row_chat_mine, parent, false));
                } else {
                    return new MessageHolder(LayoutInflater.from(getActivity()).inflate(R.layout.row_chat_other, parent, false));
                }
            }

            @Override
            protected ChatMessage parseSnapshot(DataSnapshot snapshot) {
                Logger.i(TAG, "parseSnapshot");
                ChatMessage friendlyChatMessage = super.parseSnapshot(snapshot);
                if (friendlyChatMessage != null) {
                    friendlyChatMessage.id = snapshot.getKey();
                }

                return friendlyChatMessage;
            }

            @Override
            protected void populateViewHolder(final MessageHolder viewHolder, final ChatMessage chatMessage, int position) {
                Logger.i(TAG, "populateViewHolder");
                closeLoadingDialog();
                mHandler.removeMessages(MSG_WHAT_LOGOUT);
                Logger.i(TAG, "remove message MSG_WHAT_LOGOUT on populateViewHolder");
                viewHolder.containerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mMessageField == null) return;
                        mInputMethodManager.hideSoftInputFromWindow(mMessageField.getWindowToken(), 0);
                    }
                });

                if (!TextUtils.isEmpty(chatMessage.body)) {
                    viewHolder.tvMessage.setText(chatMessage.body);
                    viewHolder.tvMessage.setVisibility(TextView.VISIBLE);
                } else {
                    viewHolder.tvMessage.setVisibility(TextView.GONE);
                }

                String date = new SimpleDateFormat("aa hh:mm", Locale.KOREA).format(new Date(chatMessage.timeStamp));
                viewHolder.tvTime.setText(date);

                if (!TextUtils.isEmpty(chatMessage.imgUrl)) {
                    String imageUrl = chatMessage.imgUrl;
                    if (imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(viewHolder.ivImage.getContext())
                                                    .load(downloadUrl)
                                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                    .into(viewHolder.ivImage);
                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    } else {
                        Glide.with(viewHolder.ivImage.getContext())
                                .load(chatMessage.imgUrl)
                                .error(R.drawable.ic_account_circle_black_36dp)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(viewHolder.ivImage);
                    }

                    viewHolder.ivImage.setVisibility(ImageView.VISIBLE);

                } else {
                    viewHolder.ivImage.setVisibility(ImageView.GONE);
                }

                if (!isMyMessage(chatMessage)) {
                    FirebaseDataHelper.getUserByEmail(mRef, chatMessage.email,
                            new FirebaseDataHelper.OnUserQueryListener() {
                                @Override
                                public void onResult(User user) {
                                    if (user != null) {
                                        viewHolder.tvWriter.setText(user.name);
                                        mGlide.load(user.photoUrl)
                                                .placeholder(R.drawable.ic_account_circle_black_36dp)
                                                .error(R.drawable.ic_account_circle_black_36dp)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .into(viewHolder.avatarView);

                                    } else {
                                        viewHolder.tvWriter.setText(chatMessage.email);
                                        mGlide.load(R.drawable.ic_account_circle_black_36dp).into(viewHolder.avatarView);
                                    }
                                }
                            });
                }
        }};

        mFirebaseAdapter.setHasStableIds(true);

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                Logger.i(TAG, "onItemRangeInserted");
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        if (mRecyclerView.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);

        mRef.child(Const.FIRE_DATA_MESSAGE_CHILD).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null) {
                    Logger.e(TAG, "dataSnapshot is null");
                    return;
                }

                if (dataSnapshot.exists() == false || dataSnapshot.getChildrenCount() < 1) {
                    closeLoadingDialog();
                    mHandler.removeMessages(MSG_WHAT_LOGOUT);
                    Logger.i(TAG, "remove message MSG_WHAT_LOGOUT on onDataChange");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // The user has been authenticated but the user has not yet been created.
        if (mAuth.getCurrentUser() != null) {
            FirebaseDataHelper.getUserByEmail(mRef, mAuth.getCurrentUser().getEmail(),
                    new FirebaseDataHelper.OnUserQueryListener() {
                @Override
                public void onResult(User user) {
                    if (user == null) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        User me = new User();
                        me.email = firebaseUser.getEmail();
                        me.name = firebaseUser.getDisplayName();
                        if (TextUtils.isEmpty(me.name)) {
                            me.name = firebaseUser.getEmail();
                        }

                        if (firebaseUser.getPhotoUrl() != null) {
                            me.photoUrl = firebaseUser.getPhotoUrl().toString();
                        }

                        FirebaseDataHelper.createOrUpdateUser(mRef, me);
                    }
                }
            });
        }
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.email = mUserMySelf.email;
                    //noinspection VisibleForTests
                    chatMessage.imgUrl = task.getResult().getDownloadUrl().toString();
                    mRef.child(Const.FIRE_DATA_MESSAGE_CHILD).child(key)
                            .setValue(chatMessage);
                } else {
                    Log.w(TAG, "Image upload task was not successful.",
                            task.getException());
                }
            }
        });
    }

    private void setUserInformation(FirebaseUser user) {
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

        final ChatMessage chatMessage = new ChatMessage(mUserMySelf.email, text);
        mRef.child(Const.FIRE_DATA_MESSAGE_CHILD).push().setValue(chatMessage,
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

    private void showProfileActivity() {
        Intent profileIntent = new Intent(getActivity(), ProfileMainActivity.class);
        startActivity(profileIntent);
    }

    private boolean isMyMessage(ChatMessage chatMessage) {
        if (mUserMySelf == null || TextUtils.isEmpty(mUserMySelf.email)) {
            return false;
        }

        if (chatMessage == null) {
            return false;
        }

        return chatMessage.email.equals(mUserMySelf.email);
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

    public static class MessageHolder extends RecyclerView.ViewHolder {
        LinearLayout containerView;
        CircleImageView avatarView;
        TextView tvMessage, tvTime, tvWriter;
        ImageView ivImage;
        ChatMessageView chatMessageView;

        MessageHolder(View itemView) {
            super(itemView);
            containerView = (LinearLayout) itemView.findViewById(R.id.messageRowContainerView);
            avatarView = (CircleImageView) itemView.findViewById(R.id.chat_avatar_view);
            chatMessageView = (ChatMessageView) itemView.findViewById(R.id.chat_message_view);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            tvWriter = (TextView) itemView.findViewById(R.id.tv_writer);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_image);
        }
    }

    private void doLogout() {
        mAuth.signOut();
        getActivity().invalidateOptionsMenu();
    }

}