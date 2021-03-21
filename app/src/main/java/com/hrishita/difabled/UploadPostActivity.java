package com.hrishita.difabled;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;

import java.util.ArrayList;

public class UploadPostActivity extends AppCompatActivity {
    FloatingActionButton fabUpload;
    FloatingActionButton fabAdd;
    private int REQUEST_PICK_IMAGES = 100;
    ArrayList<GalleryModel> arrayList = new ArrayList<>();
    com.hrishita.difabled.UploadPostImagesAdapter adapter;
    RecyclerView recyclerView;
    TextInputEditText editText, paymentLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_post);

        initViews();

        adapter = new com.hrishita.difabled.UploadPostImagesAdapter(com.hrishita.difabled.UploadPostActivity.this, arrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(com.hrishita.difabled.UploadPostActivity.this, LinearLayoutManager.HORIZONTAL, false));

        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString().equals("")) {
                    editText.setError("Please Enter Caption");
                } else {
//                    final UploadPostAsyncTask task = new UploadPostAsyncTask(UploadPostActivity.this, new UploadPostAsyncTask.UploadPostCallback() {
//                        @Override
//                        public void uploadpostresult(String s) {
//                            if(s == null) {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(UploadPostActivity.this, "Failed to upload Post", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                            } else {
//                                try {
//                                    JSONObject object = new JSONObject(s);
//                                     String type = object.getString("type");
//                                     if(type.equals("success")) {
//                                         runOnUiThread(new Runnable() {
//                                             @Override
//                                             public void run() {
//                                                 Toast.makeText(UploadPostActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
//                                             }
//                                         });
//                                     } else {
//                                         runOnUiThread(new Runnable() {
//                                             @Override
//                                             public void run() {
//                                                 Toast.makeText(UploadPostActivity.this, "Unable To Upload Post", Toast.LENGTH_SHORT).show();
//                                             }
//                                         });
//                                     }
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    });
                    FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<GetTokenResult> task1) {
                            if(task1.isSuccessful()) {
                                PostModel model = new PostModel();
                                model.caption = editText.getText().toString();
                                model.paymentLink = paymentLink.getText().toString();
                                model.uploading = true;
                                model.phone_number = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                                model.uid = task1.getResult().getToken();
                                model.uri = new Uri[arrayList.size()];
                                for(int x = 0;x < arrayList.size();x++) {
                                    model.uri[x] = arrayList.get(x).getImageUri();
                                }
//                                task.execute(model);
                            }
                        }
                    });

                }
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                i.setType("image/*");
                startActivityForResult(i, REQUEST_PICK_IMAGES);
//                GalleryBottomModal galleryBottomModal = new GalleryBottomModal(UploadPostActivity.this);
//                galleryBottomModal.show(getSupportFragmentManager(), "gallery");

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PICK_IMAGES && resultCode == RESULT_OK) {
            if(data!=null) {
                if(data.getClipData()!=null) {
                    for(int i = 0;i < data.getClipData().getItemCount();i++) {
                        GalleryModel model = new GalleryModel();
                        model.setImageUri(data.getClipData().getItemAt(i).getUri());
                        arrayList.add(model);
                    }
                } else {
                    GalleryModel model = new GalleryModel();
                    model.setImageUri(data.getData());
                    arrayList.add(model);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void initViews() {
        fabUpload = findViewById(R.id.fab_upload_post);
        fabAdd = findViewById(R.id.fab_add_image);
        editText = findViewById(R.id.edit_text_caption);
        paymentLink = findViewById(R.id.edit_text_payment_url);
        recyclerView = findViewById(R.id.recycler_view_upload_posts);
    }
}
