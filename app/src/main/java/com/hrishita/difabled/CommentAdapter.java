package com.hrishita.difabled;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

class CommentAdapter extends BaseAdapter {
    Context context;
    ArrayList<com.hrishita.difabled.CommentModel> arrayList;
    CommentAdapter(Context context, ArrayList<com.hrishita.difabled.CommentModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }
    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        com.hrishita.difabled.CommentModel model = arrayList.get(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.comment_layout, parent, false);
        CircleImageView imageView = convertView.findViewById(R.id.image_view_comments);
        TextView textView = convertView.findViewById(R.id.text_view_comments);

        String text = "<b>" + model.username + "</b>&nbsp;" + model.comment;
        textView.setText((Html.fromHtml(text)));

        Picasso.get().load(Uri.parse(Constants.SERVER_ADDRESS + "/media/profile/" + model.userProfileImageUrl)).into(imageView);

        return convertView;
    }
}
