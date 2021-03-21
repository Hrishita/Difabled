package com.hrishita.difabled;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostCommentActivity extends AppCompatActivity {
    TextView caption;
    TextView name;
    CircleImageView circleImageView;
    CarouselView carouselView;
    ImageView imgPayment, imgLike;



    ListView listView;
    CompletePostData post = null;
    String uid;
    String phone;

    TextInputEditText comment;
    ImageView send;

    ArrayList<CommentModel> arrayList = new ArrayList<>();
    CommentAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comment);
        initViews();
        extractBundle();

        final String[] myImages = Utils.convertStringToArray(post.images, ",");
        carouselView.setPageCount(myImages.length);
        carouselView.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setFocusable(true);
                imageView.setClickable(true);
                TypedValue val = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackground,val, true);
                imageView.setBackgroundResource(val.resourceId);
                Picasso.get().load((Constants.SERVER_ADDRESS + "/media/posts/" + myImages[position])).into(imageView);
            }
        });

        imgLike.setImageResource((post.liked ? R.drawable.ic_thumb_up_black_24dp_liked : R.drawable.ic_thumb_up_black_24dp));


        caption.setText(post.caption);
        name.setText(post.name);
        imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikePostTask task = new LikePostTask();
                task.execute(uid, post.id + "", (post.liked ? Constants.ACTION_UNLIKE : Constants.ACTION_LIKE));
                final Likes likes=  new Likes();
                likes.setUid(post.uid);
                likes.setPid(post.id);

                post.liked = !post.liked;
                imgLike.setImageResource((post.liked ? R.drawable.ic_thumb_up_black_24dp_liked : R.drawable.ic_thumb_up_black_24dp));

            }
        });
        imgPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        Picasso.get()
                .load(Constants.SERVER_ADDRESS + "/media/profile/" + post.profile_link)
                .into(circleImageView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        adapter = new CommentAdapter(com.hrishita.difabled.PostCommentActivity.this, arrayList);
        listView.setAdapter(adapter);
        phone = user.getPhoneNumber();

        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()) {
                    uid = task.getResult().getToken();
                    loadComments();

                    send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!comment.getText().toString().equals("")) {
                                System.out.println("Sending Message");
                                CommentInteractionTask task2 = new CommentInteractionTask(com.hrishita.difabled.PostCommentActivity.this, new CommentInteractionTask.CommentTaskCallback() {
                                    @Override
                                    public void finish(String s) {
                                        try{
                                            JSONObject object = new JSONObject(s);
                                            String type = object.getString("type");
                                            if(type.equals("success")) {
                                                CommentModel model = new CommentModel();
                                                model.comment = object.getString("comment");
                                                model.username = object.getString("username");
                                                model.userProfileImageUrl = object.getString("profile_photo");
                                                model.uid = object.getString("uid");
                                                model.postId = post.id + "";
                                                model.id = object.getString("id");
                                                arrayList.add(model);
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(com.hrishita.difabled.PostCommentActivity.this, "Failed To Upload Comment", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } catch (Exception e ){
                                            e.printStackTrace();
                                        }



                                    }
                                });
                                task2.execute(Constants.COMMENT_TASK_TYPE_POST, uid, post.id + "", comment.getText().toString());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        comment.setText("");
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        comment.setError("Type Comment");
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void loadComments() {
                CommentInteractionTask task = new CommentInteractionTask(com.hrishita.difabled.PostCommentActivity.this, new CommentInteractionTask.CommentTaskCallback() {
                    @Override
                    public void finish(String s) {
                        System.out.println("Comments: " + s);
                        try {
                            JSONObject object = new JSONObject(s);
                            String type = object.getString("type");
                            if(type.equals("success")) {
                                JSONArray comments = object.getJSONArray("result");
                                    for(int i = 0;i < comments.length();i++) {
                                        JSONObject mComment = comments.getJSONObject(i);
                                        CommentModel model = new CommentModel();
                                        model.comment = mComment.getString("comment");
                                        model.id = mComment.getString("id");
                                        model.postId = mComment.getString("pid");
                                    model.uid = mComment.getString("uid");
                                    model.username = mComment.getString("username");
                                    model.userProfileImageUrl = mComment.getString("profile_photo");
                                    arrayList.add(model);
                                }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(com.hrishita.difabled.PostCommentActivity.this, "Failed To Load Comment", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                task.execute(Constants.COMMENT_TASK_TYPE_GET, uid, post.id + "");
    }

    private void extractBundle() {
        post = (CompletePostData) getIntent().getSerializableExtra("Post");
    }

    private void initViews() {
        name = findViewById(R.id.name_view_post);
        caption =findViewById(R.id.caption_view_post);
        listView = findViewById(R.id.list_view_comments);
        comment = findViewById(R.id.edit_text_comment);
        circleImageView = findViewById(R.id.profile_view_post);
        send=  findViewById(R.id.image_view_post_comment);
        carouselView = findViewById(R.id.carousel_view_post);
        imgPayment = findViewById(R.id.payment_link_view_post);
        imgLike = findViewById(R.id.view_post_like);

    }
}
