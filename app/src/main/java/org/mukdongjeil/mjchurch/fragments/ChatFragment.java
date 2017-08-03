package org.mukdongjeil.mjchurch.fragments;

import android.content.Context;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.activities.ProfileMainActivity;
import org.mukdongjeil.mjchurch.activities.SignInActivity;
import org.mukdongjeil.mjchurch.models.Message;
import org.mukdongjeil.mjchurch.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import agency.tango.android.avatarview.views.AvatarView;
import agency.tango.android.avatarviewglide.GlideLoader;
import me.himanshusoni.chatmessageview.ChatMessageView;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final String MESSAGE_CHILD = "message";
    private static final int RC_SIGN_IN = 100;
    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1;

    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Message, MessageHolder> mFirebaseAdapter;

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference();

    private FirebaseAuth mAuth;
    private User mUserMySelf;

    private EditText mMessageField;
    private Button mBtnSend;

    public static class MessageHolder extends RecyclerView.ViewHolder {
        LinearLayout containerView;
        AvatarView avatarView;
        TextView tvMessage, tvTime, tvWriter;
        ImageView ivImage;
        ChatMessageView chatMessageView;

        MessageHolder(View itemView) {
            super(itemView);
            containerView = (LinearLayout) itemView.findViewById(R.id.messageRowContainerView);
            avatarView = (AvatarView) itemView.findViewById(R.id.chatAvatarView);
            chatMessageView = (ChatMessageView) itemView.findViewById(R.id.chatMessageView);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            tvWriter = (TextView) itemView.findViewById(R.id.tv_writer);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_image);
        }
    }

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
        mBtnSend.setOnClickListener(this);

        showLoadingDialog();
        setupRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view_message));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setUserInformation(mAuth.getCurrentUser());
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
        mRecyclerView = view;
        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setStackFromEnd(true);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageHolder> (
                Message.class,
                R.layout.row_chat_other,
                MessageHolder.class,
                mDatabaseReference.child(MESSAGE_CHILD)) {

            @Override
            public int getItemViewType(int position) {
                Message item = getItem(position);
                if (item == null) {
                    return super.getItemViewType(position);
                }

                String email = item.writer.email;
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
            protected Message parseSnapshot(DataSnapshot snapshot) {
                Message friendlyMessage = super.parseSnapshot(snapshot);
                if (friendlyMessage != null) {
                    friendlyMessage.id = snapshot.getKey();
                }

                return friendlyMessage;
            }

            @Override
            protected void populateViewHolder(final MessageHolder viewHolder, Message message, int position) {
                closeLoadingDialog();
                if (!TextUtils.isEmpty(message.body)) {
                    viewHolder.tvMessage.setText(message.body);
                    viewHolder.tvMessage.setVisibility(TextView.VISIBLE);
                } else {
                    viewHolder.tvMessage.setVisibility(TextView.GONE);
                }

                String date = new SimpleDateFormat("aa hh:mm", Locale.KOREA).format(new Date(message.timeStamp));
                viewHolder.tvTime.setText(date);

                if (!TextUtils.isEmpty(message.imgUrl)) {
                    String imageUrl = message.imgUrl;
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
                                                    .into(viewHolder.ivImage);
                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    } else {
                        Glide.with(viewHolder.ivImage.getContext())
                                .load(message.imgUrl)
                                .into(viewHolder.ivImage);
                    }

                    viewHolder.ivImage.setVisibility(ImageView.VISIBLE);
                } else {
                    viewHolder.ivImage.setVisibility(ImageView.GONE);
                }

                if (!isMyMessage(message)) {
                    viewHolder.tvWriter.setText(message.writer.name);
                    GlideLoader loader = new GlideLoader();
                    loader.loadImage(viewHolder.avatarView, message.writer.photoUrl, message.writer.name);
                }
        }};

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);
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

    private boolean isMyMessage(Message message) {
        if (mUserMySelf == null) {
            return false;
        }

        if (message == null) {
            return false;
        }

        return message.writer.email.equals(mUserMySelf.email);
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Message item);
    }
}