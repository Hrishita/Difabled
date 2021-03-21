package com.hrishita.difabled;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.synnapps.carouselview.CarouselView;

import de.hdodenhof.circleimageview.CircleImageView;

class PostHolder extends RecyclerView.ViewHolder {
    TextView caption, name;
    CarouselView carouselView;
    ImageView payment;
    CircleImageView profile;
    ImageView like, comment;


    public PostHolder(@NonNull View itemView) {
        super(itemView);
        caption = itemView.findViewById(R.id.caption_view_post);
        carouselView = itemView.findViewById(R.id.carousel_view_post);
        payment = itemView.findViewById(R.id.payment_link_view_post);
        name = itemView.findViewById(R.id.name_view_post);
        profile = itemView.findViewById(R.id.profile_view_post);
        like = itemView.findViewById(R.id.view_post_like);
        comment = itemView.findViewById(R.id.comment_view_post);
    }
}
