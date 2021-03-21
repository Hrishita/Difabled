package com.hrishita.difabled;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UploadPostImagesAdapter extends RecyclerView.Adapter<UploadPostHolder> {
    Context context;
    ArrayList<GalleryModel> arrayList;
    UploadPostImagesAdapter(Context context, ArrayList<GalleryModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public UploadPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_upload_post_images, parent,false);
        return new UploadPostHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadPostHolder holder, int position) {
        GalleryModel model = arrayList.get(position);
        Picasso.get().load(model.imageUri).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
