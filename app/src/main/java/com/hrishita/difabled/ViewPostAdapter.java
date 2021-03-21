package com.hrishita.difabled;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;

class ViewPostAdapter extends RecyclerView.Adapter<ViewPostHolder> {
    Context context;
    ArrayList<ViewPostModel> arrayList;
    String phoneNumber;
    String uid;
    ViewPostAdapter(Context context, ArrayList<ViewPostModel> arrayList, String phoneNumber, String uid) {
        this.arrayList = arrayList;
        this.context = context;
        this.phoneNumber = phoneNumber;
        this.uid = uid;
    }

    @NonNull
    @Override
    public ViewPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_view_post, parent, false);
        return new ViewPostHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewPostHolder holder, int position) {
        final ViewPostModel model = arrayList.get(position);

        holder.carouselView.setPageCount(model.urls.length);
        holder.carouselView.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                Picasso.get().load((Constants.SERVER_ADDRESS + "/media/posts/"+  model.urls[position])).into(imageView);
            }
        });
        System.out.println(model.creatorId);
        holder.caption.setText(model.caption);
        holder.name.setText(model.creatorName);
        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open profile
                Intent i = new Intent(context, ProfileActivity.class);
                i.putExtra("phone_number", model.creatorId);
                context.startActivity(i);
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Profile
                Intent i = new Intent(context, ProfileActivity.class);
                System.out.println(model.creatorId);
                i.putExtra("phone_number", model.creatorId);
                context.startActivity(i);
            }
        });


        holder.like.setImageResource((model.liked ? R.drawable.ic_thumb_up_black_24dp_liked : R.drawable.ic_thumb_up_black_24dp));
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PostCommentActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("Post", model);
                context.startActivity(i);
            }
        });
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.liked = !model.liked;
                holder.like.setImageResource((model.liked ? R.drawable.ic_thumb_up_black_24dp_liked : R.drawable.ic_thumb_up_black_24dp));
                LikePostTask task = new LikePostTask();
                task.execute(uid, model.postId, (model.liked ? Constants.ACTION_LIKE : Constants.ACTION_UNLIKE));
            }
        });
        Picasso.get().load((Constants.SERVER_ADDRESS + "/media/profile/"+  model.creator_profile_link)).into(holder.profile);
        if(!model.paymentLink.equals("")) {
            holder.payment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(model.paymentLink));
                    context.startActivity(i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
