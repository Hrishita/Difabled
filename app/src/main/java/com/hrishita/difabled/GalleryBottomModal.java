package com.hrishita.difabled;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;


public class GalleryBottomModal extends BottomSheetDialogFragment {
    private GridView gridView;
    private ArrayList<GalleryModel> arrayList = new ArrayList<>();
    private com.hrishita.difabled.GalleryAdapter adapter;
    Context context;
    GalleryBottomModal(Context context) {
        this.context = context;
        fetchImagePath();
    }

    private void fetchImagePath() {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projections = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projections, null, null, null);

        while (cursor.moveToNext()) {
            String absolutePathOfImage = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            GalleryModel model = new GalleryModel();
            model.setChecked(false);
            model.setImagePath(absolutePathOfImage);
            arrayList.add(model);
        }
        adapter = new com.hrishita.difabled.GalleryAdapter(arrayList, context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery_bottom_modal, container, false);
        gridView = v.findViewById(R.id.gallery_grid_view);
        gridView.setAdapter(adapter);
        return v;
    }
}
