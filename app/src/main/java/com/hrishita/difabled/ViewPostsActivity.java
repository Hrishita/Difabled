package com.hrishita.difabled;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewPostsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<ViewPostModel> arrayList = new ArrayList<>();
    ViewPostAdapter adapter;
    ImageView myProfile;
    ImageView uploadPost;
    ImageView chat;
    ProgressBar progressBar;
    String phoneNumber;
    String uid;

    int start = 0;
    int limit = 5;
    boolean isLoadingDataFromServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);
        initViews();
        phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()) {
                    uid = task.getResult().getToken();
                    adapter = new ViewPostAdapter(com.hrishita.difabled.ViewPostsActivity.this, arrayList, phoneNumber, task.getResult().getToken());
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(com.hrishita.difabled.ViewPostsActivity.this));

                    loadDataFromServer();
                    isLoadingDataFromServer = true;


                    myProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gotoProfileActivity();
                        }
                    });
                    uploadPost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gotoPostActivity();
                        }
                    });

                    chat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(com.hrishita.difabled.ViewPostsActivity.this, ChatActivity.class);
                            startActivity(i);
                        }
                    });

                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            int visibleItemCount = recyclerView.getChildCount();
                            int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                            int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                            if (!isLoadingDataFromServer && (totalItemCount - visibleItemCount)
                                    <= (firstVisibleItem)) {
                                loadDataFromServer();
                                isLoadingDataFromServer = true;
                                progressBar.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });


    }

    private void gotoPostActivity() {
        Intent i = new Intent(com.hrishita.difabled.ViewPostsActivity.this, UploadPostActivity.class);
        startActivity(i);
    }

    private void gotoProfileActivity() {
        Intent i = new Intent(com.hrishita.difabled.ViewPostsActivity.this, ProfileActivity.class);
        startActivity(i);
    }

    private void loadDataFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(com.hrishita.difabled.ViewPostsActivity.this, "Loading From Server", Toast.LENGTH_SHORT).show();
                    }
                });
                OkHttpClient client = new OkHttpClient.Builder().writeTimeout(1, TimeUnit.MINUTES).readTimeout(1, TimeUnit.MINUTES).build();
                String url = Constants.SERVER_ADDRESS + "/api/getpost/";
                String jsonBody = "{" +
                        "\"start\":\"" + start + "\"," +
                        "\"limit\":\"" + limit + "\"," +
                        "\"uid\":\"" + uid + "\"" +
                        "}";

                System.out.println(jsonBody);

                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

                Request request = new Request.Builder().url(url).post(body).build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String type = jsonObject.getString("type");
                    if(type.equals("success")) {
                        System.out.println(jsonObject.toString());
                            start = Integer.parseInt(jsonObject.getString("newstart"));
                        JSONArray result = jsonObject.getJSONArray("result");
                        for(int x = 0;x < result.length();x++) {
                            JSONObject object = result.getJSONObject(x);

                            ViewPostModel viewPostModel = new ViewPostModel();
                            viewPostModel.caption = object.getString("caption");
                            viewPostModel.paymentLink = object.getString("payment");
                            viewPostModel.creation_date = object.getString("creation_date");
                            viewPostModel.creatorName = object.getString("name");
                            viewPostModel.creatorId = object.getString("uid");
                            viewPostModel.postId = object.getString("id");
                            viewPostModel.creator_profile_link = object.getString("profile_photo");
                            viewPostModel.liked = object.getBoolean("liked");

                            JSONArray images = object.getJSONArray("images");
                            viewPostModel.urls = new String[images.length()];
                            for(int y = 0;y < images.length();y++){
                                viewPostModel.urls[y] = images.getString(y);
                            }
                            arrayList.add(viewPostModel);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                isLoadingDataFromServer = false;
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                    } else if (type.equals("end")) {
                        isLoadingDataFromServer = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(com.hrishita.difabled.ViewPostsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_view_posts);
        myProfile = findViewById(R.id.chat_profile);
        uploadPost = findViewById(R.id.chat_post);
        progressBar = findViewById(R.id.progress_bar_view_post);
        chat = findViewById(R.id.view_post_chat);
    }
}
