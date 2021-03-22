package com.hrishita.difabled;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InstantVideoCallAdapter extends RecyclerView.Adapter<InstantVideoCallAdapter.MyHolder>
{
    Context context;
    List<FriendsModel> arrayList;
    InstantVideoCallAdapter(Context context, List<FriendsModel> arrayList)
    {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.layout_instant_video_call, parent, false);

        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        holder.name.setText(arrayList.get(position).getUser().getName());
        Picasso
                .get()
                .load(Constants.SERVER_ADDRESS + "/media/profile/"  + arrayList.get(position).getUser().getProfile_link())
                .error(R.drawable.default_profile)
                .into(holder.profile);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PreVideoCallActivity.class);
                i.putExtra("User", arrayList.get(position));
                i.putExtra("chat_id", arrayList.get(position).getChat_id());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView call;
        CircleImageView profile;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.ivc_item_name);
            call = itemView.findViewById(R.id.ivc_item_call);
            profile = itemView.findViewById(R.id.ivc_item_profile);
        }
    }
}
