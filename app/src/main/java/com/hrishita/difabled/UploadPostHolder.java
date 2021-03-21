package com.hrishita.difabled;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class UploadPostHolder extends RecyclerView.ViewHolder {
    ImageView image;
    ImageView delete;
    public UploadPostHolder(@NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.image_upload_post_images);
        delete = itemView.findViewById(R.id.remove_upload_post_images);
    }
}
