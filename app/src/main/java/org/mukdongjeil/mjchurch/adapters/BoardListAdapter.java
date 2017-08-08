package org.mukdongjeil.mjchurch.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.models.Board;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by gradler on 2016. 9. 29..
 */
public class BoardListAdapter extends RecyclerView.Adapter<BoardListAdapter.ViewHolder> implements RealmChangeListener {
    private RealmResults<Board> mList;

    @Override
    public void onChange(Object element) {
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView writer;
        public TextView date;
        public TextView content;
        public ViewHolder(ViewGroup v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            writer = (TextView) v.findViewById(R.id.writer);
            date = (TextView) v.findViewById(R.id.date);
            content = (TextView) v.findViewById(R.id.content);
        }
    }

    public BoardListAdapter(RealmResults<Board> objects) {
        mList = objects;
        mList.addChangeListener(this);
    }

    @Override
    public BoardListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_board, parent, false);
        return new ViewHolder((ViewGroup)v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Board item = mList.get(position);
        holder.title.setText(item.title);
        holder.writer.setText(item.writer);
        holder.date.setText(item.date);
        holder.content.setText(item.content);
    }

    @Override
    public int getItemCount() {
        return (mList != null) ? mList.size() : 0;
    }
}
