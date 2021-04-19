package com.hrishita.difabled;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostHolder> {
    Context context;
    ArrayList<CompletePostData> arrayList;
    PostInteractInterface mInterface;
    PostAdapter(Context context, ArrayList<CompletePostData> arrayList, PostInteractInterface mInterface) {
        this.context = context;
        this.arrayList = arrayList;
        this.mInterface = mInterface;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_post_view,parent,false);
        return new PostHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostHolder holder, final int position) {
        final CompletePostData completePostData = arrayList.get(position);
        System.out.println("position " + position);
        holder.caption.setText(completePostData.caption);
        holder.name.setText(completePostData.name);

        Picasso.get().load((Constants.SERVER_ADDRESS + "/media/profile/" + completePostData.profile_link)).into(holder.profile);
        System.out.println(completePostData.images + " ");

        if(completePostData.images == null || completePostData.images.equals(""))
        {
            holder.carouselView.setVisibility(View.GONE);
        }
        else
        {
            final String[] myImages = Utils.convertStringToArray(completePostData.images, ",");
                holder.carouselView.setVisibility(View.VISIBLE);
                holder.carouselView.setPageCount(myImages.length);
                holder.carouselView.setImageListener(new ImageListener() {
                    @Override
                    public void setImageForPosition(int position, ImageView imageView) {
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        imageView.setFocusable(true);
                        imageView.setClickable(true);
                      //  imageView.setRotation(ExifInterface.ORIENTATION_ROTATE_90);
                        TypedValue val = new TypedValue();
                        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,val, true);
                        imageView.setBackgroundResource(val.resourceId);
                        Glide.with(context).load((Constants.SERVER_ADDRESS + "/media/posts/" + myImages[position])).into(imageView);
                       // Picasso.get().load((Constants.SERVER_ADDRESS + "/media/posts/" + myImages[position])).into(imageView);
                    }
                });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.onPostClicked(completePostData);
            }
        });
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.onCommentClicked(completePostData);
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.onProfileClicked(completePostData);
            }
        });

        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.onProfileClicked(completePostData);
            }
        });
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInterface.onPostLiked(completePostData);
                arrayList.get(position).liked = !completePostData.liked;
                if(completePostData.liked) {
                    holder.like.setImageResource(R.drawable.ic_thumb_up_black_24dp_liked);
                }
                else
                {
                    holder.like.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                }
            }
        });
        if(completePostData.liked) {
            holder.like.setImageResource(R.drawable.ic_thumb_up_black_24dp_liked);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface PostInteractInterface
    {
        void onPostClicked(CompletePostData data);
        void onPostLiked(CompletePostData data);
        void onProfileClicked(CompletePostData data);
        void onPaymentClicked(CompletePostData data);
        void onCommentClicked(CompletePostData data);
    }

}
