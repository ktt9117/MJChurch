package org.mukdongjeil.mjchurch.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.models.ChatMessage;
import org.mukdongjeil.mjchurch.utils.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import me.himanshusoni.chatmessageview.ChatMessageView;

/**
 * Created by Gradler on 2017-12-15.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private static final String TAG = ChatAdapter.class.getSimpleName();
    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1;

    private Context mContext;
    private ArrayList<ChatMessage> mList;
    private RequestManager mGlide;
    private String mMyEmailAddress;
    private OnRowItemClickedListener mListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerView;
        CircleImageView avatarView;
        TextView tvMessage, tvTime, tvWriter;
        ImageView ivImage;
        ChatMessageView chatMessageView;

        ViewHolder(View itemView) {
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

    public interface OnRowItemClickedListener {
        void onRowItemClicked(View view);
    }

    public ChatAdapter(Context context, ArrayList<ChatMessage> items, RequestManager glide,
                       String myEmailAddress, OnRowItemClickedListener listener) {
        this.mContext = context;
        this.mList = items;
        this.mGlide = glide;
        this.mMyEmailAddress = myEmailAddress;
        this.mListener = listener;
    }

    @Override
    public int getItemCount() {
        return (mList != null) ? mList.size() : 0;
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MY_MESSAGE) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.row_chat_mine, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.row_chat_other, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage item = mList.get(position);
        if (item == null) {
            return super.getItemViewType(position);
        }

        String email = item.email;
        if (!TextUtils.isEmpty(mMyEmailAddress) && !TextUtils.isEmpty(email) && email.equals(mMyEmailAddress)) {
            return MY_MESSAGE;
        } else {
            return OTHER_MESSAGE;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatMessage chatMessage = mList.get(position);

        holder.containerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) mListener.onRowItemClicked(view);
            }
        });

        if (!TextUtils.isEmpty(chatMessage.body)) {
            holder.tvMessage.setText(chatMessage.body);
            holder.tvMessage.setVisibility(TextView.VISIBLE);
        } else {
            holder.tvMessage.setVisibility(TextView.GONE);
        }

        Date tempDate = new Date(chatMessage.timeStamp);
        long diffOfDays = TimeUnit.DAYS.convert(System.currentTimeMillis()
                - chatMessage.timeStamp, TimeUnit.MILLISECONDS);
        Logger.e(TAG, "diffOfDays : " + diffOfDays);
        String format;
        if (diffOfDays < 1) {
            format = "오늘 aa hh:mm";
        } else if (diffOfDays < 2) {
            format = "어제 aa hh:mm";
        } else {
            format = "MM월dd일 aa hh:mm";
        }

        String date = new SimpleDateFormat(format, Locale.KOREA).format(tempDate);
        holder.tvTime.setText(date);

        if (!TextUtils.isEmpty(chatMessage.imgUrl)) {
            bindingImage(holder.ivImage, chatMessage.imgUrl);
        } else {
            holder.ivImage.setVisibility(ImageView.GONE);
        }

        if (!isMyMessage(chatMessage.email)) {
            holder.tvWriter.setText(chatMessage.name);
            if (!TextUtils.isEmpty(chatMessage.avatarUrl)) {
                bindingImage(holder.avatarView, chatMessage.avatarUrl);
            } else {
                holder.avatarView.setImageResource(R.drawable.ic_account_circle_black_36dp);
            }
        }
    }

    public void setMyEmailAddress(String email) {
        if (!TextUtils.isEmpty(mMyEmailAddress) && !TextUtils.isEmpty(email)
                && mMyEmailAddress.equals(email)) {
            return;
        }

        this.mMyEmailAddress = email;
        notifyDataSetChanged();
    }

    private void bindingImage(final ImageView imageView, String imageUrl) {
        if (imageUrl.startsWith("gs://")) {
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl);
            storageReference.getDownloadUrl().addOnCompleteListener(
                    new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String downloadUrl = task.getResult().toString();
                                Glide.with(imageView.getContext())
                                        .load(downloadUrl)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(imageView);
                            } else {
                                Log.w(TAG, "Getting download url was not successful.",
                                        task.getException());
                            }
                        }
                    });
        } else {
            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .error(R.drawable.ic_account_circle_black_36dp)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }

        imageView.setVisibility(ImageView.VISIBLE);
    }

    private boolean isMyMessage(String email) {
        if (TextUtils.isEmpty(mMyEmailAddress) || TextUtils.isEmpty(email)) {
            return false;
        }

        return email.equals(mMyEmailAddress);
    }
}