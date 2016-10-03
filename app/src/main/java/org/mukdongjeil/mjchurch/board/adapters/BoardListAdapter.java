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

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
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

    // Provide a suitable constructor (depends on the kind of dataset)
    public BoardListAdapter(List<BoardItem> objects) {
        itemList = objects;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BoardListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_board, parent, false); //check this out
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder((ViewGroup)v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
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

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return (itemList != null) ? itemList.size() : 0;
    }
}
