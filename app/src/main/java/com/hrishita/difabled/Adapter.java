package com.hrishita.difabled;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
    private ArrayList<FirebaseMessageModel> mMessageData;
    private Context context;
    FirebaseStorage storage;
    public Adapter(Context context, ArrayList<FirebaseMessageModel> arrayList) {
        this.mMessageData = arrayList;
        this.context = context;
        storage = FirebaseStorage.getInstance();
    }
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_recycler_view_layout,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
//        Glide.with(context).clear(holder.image);
        holder.image.setVisibility(View.GONE);
        holder.messages.setVisibility(View.GONE);

        FirebaseMessageModel data =  mMessageData.get(position);
//        holder.heading.setText(data.getSender());
        if(data.type == null || data.type.equals(Constants.MESSAGE_TYPE_TEXT)) {
            holder.messages.setText(data.getMessage());
            holder.messages.setVisibility(View.VISIBLE);

            if (!mMessageData.get(position).isMessageRecvd) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                holder.messages.setLayoutParams(params);
                holder.messages.setBackgroundResource(R.drawable.chat_bubble_sender);
                holder.messages.setTextColor(context.getResources().getColor(R.color.colorAccent));
            }
            else
            {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_START);
                holder.messages.setLayoutParams(params);
                holder.messages.setBackgroundResource(R.drawable.chat_bubble_sender);
                holder.messages.setTextColor(context.getResources().getColor(R.color.colorAccent));
            }
        }
        else if(data.type.equals(Constants.MESSAGE_TYPE_IMAGE))
        {
            holder.image.setVisibility(View.VISIBLE);
            if (!mMessageData.get(position).isMessageRecvd) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_END);

                holder.image.setLayoutParams(params);
                holder.image.setBackgroundResource(R.drawable.chat_bubble_sender);
            }
            else
            {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_START);
                holder.messages.setLayoutParams(params);
                holder.messages.setBackgroundResource(R.drawable.chat_bubble_sender);
                holder.messages.setTextColor(context.getResources().getColor(R.color.colorAccent));
            }

            Picasso.get().load(data.message).into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageData.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView messages;
        ImageView image;
        public Holder(@NonNull View itemView) {
            super(itemView);
//            heading = itemView.findViewById(R.id.heading);
            messages = itemView.findViewById(R.id.message_body);
            image = itemView.findViewById(R.id.message_image);
        }
    }
}

