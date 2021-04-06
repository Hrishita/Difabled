package com.hrishita.difabled;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements HomeActivity.postFragmentCallbacks {
    ArrayList<CompletePostData> arrayList = new ArrayList<>();
    private List<CompletePostData> oldData;
    private SwipeRefreshLayout refreshLayout;

    private final int limit = 5;

    int lastViewedPost = 0;
    private boolean arePostsAvailableOnServer = true;
    private boolean isDownloadingData = false;

    private String uid;
    boolean shouldContinue = false;

    private SharedPreferences prefs;


//    ArrayList<CompletePostData> arrayList;
    PostAdapter adapter;
    private RecyclerView recyclerView;

    int current;

    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_post, container, false);


        prefs = ((HomeActivity)getContext()).getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        //big = prefs.getInt("big", 0);

        extract();

        recyclerView = v.findViewById(R.id.fp_rcview);
        refreshLayout = v.findViewById(R.id.swipe_refresh_post);
        adapter = new PostAdapter(getContext(), arrayList, new PostAdapter.PostInteractInterface() {
            @Override
            public void onPostClicked(CompletePostData data) {
                //todo open post
            }

            @Override
            public void onPostLiked(final CompletePostData data) {
                LikePostTask task = new LikePostTask();
                task.execute(uid, data.id + "", (data.liked ? Constants.ACTION_UNLIKE : Constants.ACTION_LIKE));
                final Likes likes=  new Likes();
                likes.setUid(data.uid);
                likes.setPid(data.id);

            }

            @Override
            public void onProfileClicked(CompletePostData data) {
                //take to profile
                HomeProfileFragment fragment = new HomeProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", data.uid);
                fragment.setArguments(bundle);
                ((HomeActivity)getContext())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.home_activity_base,fragment,"home profile")
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onPaymentClicked(CompletePostData data) {

            }

            @Override
            public void onCommentClicked(CompletePostData data) {
                Intent i = new Intent(getContext(), PostCommentActivity.class);
                System.out.println("images = " + data.images);
                i.putExtra("Post", (Serializable) data);
                startActivity(i);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                arrayList.clear();
                //oldData.clear();
                arePostsAvailableOnServer = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downloadData();
                        refreshLayout.setRefreshing(false);
                    }
                }).start();
            }
        });

       new Thread(new Runnable() {
            @Override
            public void run() {
                downloadData();
            }
        }).start();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (!isDownloadingData && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem) && arePostsAvailableOnServer) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            downloadData();
                        }
                    }).start();
                }
            }
        });

        return v;
    }


    private void extract() {
        if(getArguments()!=null) {
            uid = getArguments().getString("uid");
            current = getArguments().getInt("current");
            shouldContinue = getArguments().getBoolean("continue", false);
        }
    }

    private void downloadData() {
        if(arePostsAvailableOnServer) {
            isDownloadingData = true;
                    ((HomeActivity)getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(((HomeActivity)getContext()), "Loading From Server", Toast.LENGTH_SHORT).show();
                        }
                    });
                    OkHttpClient client = new OkHttpClient.Builder().writeTimeout(1, TimeUnit.MINUTES).readTimeout(1, TimeUnit.MINUTES).build();
                    String url = Constants.SERVER_ADDRESS + "/api/getpost/v2/";
                    String jsonBody = "{" +
                            "\"start\":\"" + 0 + "\"," +
                            "\"limit\":\"" + 100 + "\"," +
                            "\"uid\":\"" + uid + "\"" +
                            "}";


                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

                    Request request = new Request.Builder().url(url).post(body).build();
                    try {
                        Response response = client.newCall(request).execute();
                        String responseS = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseS);
                        System.out.println(responseS);
                        String type = jsonObject.getString("type");
                        JSONArray result = jsonObject.getJSONArray("result");

                        if (type.equals("success")) {
                            arePostsAvailableOnServer = (result.length() >= limit);

                            for (int x = 0; x < result.length(); x++) {
                                JSONObject object = result.getJSONObject(x);

                                Post post = new Post();
                                User user = new User();
                                Likes likes = new Likes();
                                CompletePostData completePostData = new CompletePostData();

                                String caption = object.getString("caption");
                                String creation_date = object.getString("timestamp");
                                String creatorName = object.getString("u_name");
                                String creatorId = object.getString("puid");
                                int postId = object.getInt("pid");
                                String creator_profile_link = object.getString("profile_link");
                                String category = object.getString("category");
                                String email = object.getString("email");
                                String status = object.getString("status");

                                String liked = object.getString("liked");

                                post.setId(postId);
                                post.setCaption(caption);
                                post.setTimestamp(creation_date);
                                post.setUid(creatorId);

                                user.setName(creatorName);
                                user.setId(creatorId);
                                user.setProfile_link(creator_profile_link);
                                user.setCategory(category);
                                user.setEmail(email);
                                user.setStatus(status);
                                user.setPhone(creatorId);




                                if (liked.equals("false")) {
                                    likes.setPid(postId);
                                    likes.setUid(uid);

                                }
                                JSONArray images = new JSONArray();
                                if(object.has("images"))
                                if(!object.isNull("images"))
                                {images = object.getJSONArray("images");}
                                PostMedia[] postMedia = new PostMedia[images.length()];
                                completePostData.images = "";
                                StringBuilder sb = new StringBuilder("");

                                for (int y = 0; y < images.length(); y++) {
                                    postMedia[y] = new PostMedia();
                                    postMedia[y].setOrderno(y);
                                    postMedia[y].setPid(postId);
                                    postMedia[y].setUrl(images.getString(y));

                                    sb.append((images.getString(y)));
                                    sb.append(",");

                                }
                                if(sb.length() > 0)
                                    completePostData.images = sb.deleteCharAt(sb.length() - 1).toString();
                                else
                                    completePostData.images=null;
                                completePostData.id = postId;
                                completePostData.uid = uid;
                                completePostData.caption = caption;
                                completePostData.profile_link = creator_profile_link;
                                completePostData.timestamp = creation_date;
                                completePostData.name = creatorName;
                                completePostData.liked = !liked.equals("false");

                                arrayList.add(completePostData);
                            }
                            /*if(!arePostsAvailableOnServer) {
                                System.out.println("POST ARE NOT AVAILABLE");
                                arrayList.addAll(oldData);
                            }*/
                            ((HomeActivity)getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    isDownloadingData = false;
                                }
                            });
                        } else if (type.equals("failure")) {
                            ((HomeActivity)getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(((HomeActivity)getContext()), "Failed to load data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
    }

    @Override
    public int getLastItem() {
        return ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
    }
}
