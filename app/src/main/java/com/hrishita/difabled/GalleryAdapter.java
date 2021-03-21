package com.hrishita.difabled;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class GalleryAdapter extends BaseAdapter {
    Context context;
    ArrayList<GalleryModel> arrayList;
    GalleryAdapter(ArrayList<GalleryModel> arrayList, Context context) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.custom_gallery_grid, parent, false);
        ImageView imageView = convertView.findViewById(R.id.image_gallery_grid);
//        CheckBox checkBox = convertView.findViewById(R.id.checkbox_gallery_grid);
//        checkBox.setChecked(arrayList.get(position).isChecked);
        System.out.println(arrayList.get(position).imagePath);
        Picasso.get().load(Uri.parse("file://" + arrayList.get(position).imagePath)).into(imageView);
        return convertView;
    }
}
