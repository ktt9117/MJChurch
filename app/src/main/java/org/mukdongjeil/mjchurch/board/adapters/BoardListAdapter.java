package org.mukdongjeil.mjchurch.board.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.dao.BoardItem;

import java.util.List;

/**
 * Created by gradler on 2016. 9. 29..
 */
public class BoardListAdapter extends RecyclerView.Adapter<BoardListAdapter.ViewHolder> {
    private List<BoardItem> itemList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
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

    public BoardListAdapter(List<BoardItem> objects) {
        itemList = objects;
    }

    @Override
    public BoardListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_board, parent, false); //check this out
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder((ViewGroup)v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        BoardItem item = itemList.get(position);
        holder.title.setText(item.title);
        holder.writer.setText(item.writer);
        holder.date.setText(item.date);
        holder.content.setText(item.content);
    }

    @Override
    public int getItemCount() {
        return (itemList != null) ? itemList.size() : 0;
    }
}
