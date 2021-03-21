package com.hrishita.difabled;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

class ChatsAdapter extends BaseAdapter {
    private Context context;
    private List<FriendsModel> arrayList;
    ChatsAdapter(Context context, List<FriendsModel> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(this.context).inflate(R.layout.custom_list_view_users,null);

        ImageView profile = convertView.findViewById(R.id.chat_user_profile);
        TextView textView = convertView.findViewById(R.id.chat_user_name);
        textView.setText(arrayList.get(position).getUser().getName());
        Picasso
                .get()
                .load(Constants.SERVER_ADDRESS + "/media/profile/" + arrayList.get(position).getUser().getProfile_link())
                .into(profile);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo open when properly done
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("Friend", arrayList.get(position));
                context.startActivity(i);
            }
        });
        return convertView;
    }
}
