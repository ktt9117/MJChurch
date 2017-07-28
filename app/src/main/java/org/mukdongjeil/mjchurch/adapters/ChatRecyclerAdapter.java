package org.mukdongjeil.mjchurch.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.fragments.ChatFragment;
import org.mukdongjeil.mjchurch.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import agency.tango.android.avatarview.views.AvatarView;
import agency.tango.android.avatarviewglide.GlideLoader;
import me.himanshusoni.chatmessageview.ChatMessageView;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.MessageHolder> {
    private static final String TAG = "ChatRecyclerViewAdapter";
    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1;

    private List<Message> mMessages;
    private Context mContext;
    private FirebaseUser me;
    private ChatFragment.OnListFragmentInteractionListener mListener;

    public ChatRecyclerAdapter(Context context, List<Message> data, ChatFragment.OnListFragmentInteractionListener listener) {
        mContext = context;
        mMessages = data;
        mListener = listener;
    }

    @Override
    public int getItemCount() {
        return mMessages == null ? 0 : mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessages == null) return 0;

        Message item = mMessages.get(position);
        String email = item.writer.email;

        if (item != null && me != null && !TextUtils.isEmpty(email) && email.equals(me.getEmail())) {
            return MY_MESSAGE;
        } else {
            return OTHER_MESSAGE;
        }
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MY_MESSAGE) {
            return new MessageHolder(LayoutInflater.from(mContext).inflate(R.layout.row_chat_mine, parent, false));
        } else {
            return new MessageHolder(LayoutInflater.from(mContext).inflate(R.layout.row_chat_other, parent, false));
        }
    }

    public void add(Message message) {
        mMessages.add(message);
        notifyItemInserted(mMessages.size() - 1);
    }

    @Override
    public void onBindViewHolder(final MessageHolder holder, final int position) {
        final Message chatMessage = mMessages.get(position);
//        if (chatMessage.isImage) {
//            holder.ivImage.setVisibility(View.VISIBLE);
//            holder.tvMessage.setVisibility(View.GONE);
//
//        } else {
        holder.ivImage.setVisibility(View.GONE);
        holder.tvMessage.setVisibility(View.VISIBLE);
        holder.tvMessage.setText(chatMessage.body);

//        }

        String date = new SimpleDateFormat("aa hh:mm", Locale.KOREA).format(new Date(chatMessage.timeStamp));
        holder.tvTime.setText(date);

        if (getItemViewType(position) == OTHER_MESSAGE) {
            GlideLoader loader = new GlideLoader();
            loader.loadImage(holder.avatarView, chatMessage.writer.photoUrl, chatMessage.writer.name);
            holder.tvWriter.setText(chatMessage.writer.name);
            holder.tvWriter.setVisibility(View.VISIBLE);
        }

        holder.chatMessageView.setClickable(false);
        holder.containerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onListFragmentInteraction(chatMessage);
                }
            }
        });
    }

    public void setUser(FirebaseUser user) {
        this.me = user;
    }

    class MessageHolder extends RecyclerView.ViewHolder {
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
}