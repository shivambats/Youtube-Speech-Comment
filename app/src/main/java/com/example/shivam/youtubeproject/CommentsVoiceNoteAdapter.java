package com.example.shivam.youtubeproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Casino on 7/4/17.
 */

public class CommentsVoiceNoteAdapter extends BaseAdapter {


    public Context context;
    public ArrayList<CommentsVoiceNote> CommentsList;


    public CommentsVoiceNoteAdapter(Context context, ArrayList<CommentsVoiceNote> CommentsList) {
        super();
        this.context = context;
        this.CommentsList = CommentsList;

    }

    public class ProductHolder {

        TextView comment;


    }

    @Override
    public int getCount() {
        return CommentsList.size();
    }

    @Override
    public Object getItem(int position) {
        return CommentsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ProductHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listviewcomments, parent, false);
            holder = new ProductHolder();
            holder.comment = (TextView) convertView.findViewById(R.id.comment);

            convertView.setTag(holder);
        } else {
            holder = (ProductHolder) convertView.getTag();
        }

        holder.comment.setText(CommentsList.get(position).getComment());
        return convertView;
    }
}
