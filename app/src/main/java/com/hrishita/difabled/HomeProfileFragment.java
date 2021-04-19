package com.hrishita.difabled;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeProfileFragment extends Fragment {
    CircleImageView profile;
    Context context;
    HomeActivityInterface mInterface;
    FirebaseUser user;
    FirebaseAuth auth;
    String uid;
    String requestedProfile;
    String name;
    String status;
    String handler;
    String category;
    String profile_photo;
    int totalPosts;
    ArrayList<Post> arrayList = new ArrayList();
    TextView txtName, txtStatus, txtHandler;
    HomeProfileStatView following, followers, posts;

    ImageView settings;
    FlexboxLayout mainFlex;
    Button btnEditProfile;
    SharedPreferences prefs;

    public static int STATE_REQUEST_RECVD = 0;
    public static int STATE_REQUEST_SENT = 1;
    public static int STATE_REQUEST_SELF = 2;
    public static int STATE_REQUEST_DEFAULT = 3;
    public static int STATE_FRIENDS = 4;

    private int currentState = STATE_REQUEST_DEFAULT;


    public HomeProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        if(this.mInterface!=null)
        {
            this.mInterface.hideAppBar();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);
        if(context instanceof HomeActivityInterface)
        {
            this.context= context;
            this.mInterface = (HomeActivityInterface) context;
            this.mInterface.hideAppBar();
          //   this.mInterface.hideBottomNav();

        }
        else
        {
            throw new ClassCastException("Implement Interface");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home_profile, container, false);
        prefs=getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mainFlex = v.findViewById(R.id.hp_flex_posts);
        btnEditProfile = v.findViewById(R.id.btn_edit_profile);
        posts = v.findViewById(R.id.hp_hpsv_posts);
        profile = v.findViewById(R.id.hp_profile);
        txtHandler = v.findViewById(R.id.hp_handler);
        txtName = v.findViewById(R.id.hp_name);
        txtStatus = v.findViewById(R.id.hp_status);
        //System.out.println (((FlexboxLayout.LayoutParams)v.findViewById(R.id.hp_temp_image).getLayoutParams()).getFlexBasisPercent() + "");
        settings=v.findViewById(R.id.profile_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
        auth = FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        extractData();

        if(user!=null) {
            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if(task.isSuccessful())
                    {
                        uid=task.getResult().getToken();
                        fetchProfile();
                        fetchAllPosts();
                        fetchFriendStatus();
                    }
                }
            });
        }
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFragment frag = new ProfileFragment();

                System.out.println("state = " + currentState);
                if(currentState == STATE_REQUEST_SELF) {
                    prefs.edit().putBoolean("editing", true).commit();

                    ((HomeActivity) getContext())
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_activity_base,frag, "edit profile")
                            .commit();

                  /*Intent i = new Intent(getContext(), PhoneAuthActivity.class);
                    startActivity(i);
                    ((HomeActivity) getContext())
                            .finish();*/
                }
                else if(currentState == STATE_FRIENDS || currentState == STATE_REQUEST_SENT)
                {
                    denyRequest();
                }
                else if(currentState == STATE_REQUEST_RECVD)
                {
                    acceptRequest();
                }
                else
                {
                    sendRequest();
                }
            }


               /* AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(R.layout.loading).setCancelable(false).create();
                dialog.show();
                String phone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                FirebaseAuth.getInstance().signOut();

                FirebaseMessaging.getInstance().unsubscribeFromTopic(phone.substring(1))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });*/

        /*        SelectCatFragment selectCatFragment=new SelectCatFragment();


        Intent i = new Intent(getContext(), PreHomeScreenActivity.class);
                startActivity(i);

                ((HomeActivity)getContext()).finish();
                dialog.dismiss();*/

        });

        return v;
    }

    private void fetchFriendStatus() {
        if(!requestedProfile.equals(user.getPhoneNumber())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                    database.child("").child("friend-request").child(user.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot s : snapshot.getChildren()) {
                                //first direct child is uid of another user
                                if (s.getKey().equals(requestedProfile)) {
                                    if (s.hasChild("type") && s.child("type").getValue()!=null)
                                        if (s.child("type").getValue().toString().equals(Constants.FIREBASE_FRIEND_REQUEST_RECVD + "")) {
                                            updateUI(STATE_REQUEST_RECVD);
                                        } else if (s.child("type").getValue().toString().equals(Constants.FIREBASE_FRIEND_REQUEST_SENT + "")) {
                                            updateUI(STATE_REQUEST_SENT);
                                        } else if(s.child("type").getValue().toString().equals(Constants.FIREBASE_FRIEND_REQUEST_ACCEPTED + "")) {
                                            updateUI(STATE_FRIENDS);
                                        }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }).start();
        }
    }

    private void denyRequest() {
        FirebaseDatabase.getInstance().getReference().child("").child("friend-request").child(user.getPhoneNumber()).child(requestedProfile).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference().child("").child("friend-request").child(requestedProfile).child(user.getPhoneNumber()).removeValue() .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                updateUI(STATE_REQUEST_DEFAULT);
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendRequest() {
        FirebaseDatabase.getInstance().getReference().child("").child("friend-request").child(user.getPhoneNumber()).child(requestedProfile).child("type").setValue(Constants.FIREBASE_FRIEND_REQUEST_SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference().child("").child("friend-request").child(requestedProfile).child(user.getPhoneNumber()).child("type").setValue(Constants.FIREBASE_FRIEND_REQUEST_RECVD).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                updateUI(STATE_REQUEST_SENT);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptRequest() {
        final HashMap<String, String> data = new HashMap<>();
        data.put("type", "2");
        data.put("chat_id", user.getPhoneNumber() + "_" + requestedProfile);
        DatabaseReference storage = FirebaseDatabase.getInstance().getReference();
        storage.child("").child("friend-request").child(user.getPhoneNumber()).child(requestedProfile).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference().child("").child("friend-request").child(requestedProfile).child(user.getPhoneNumber()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateUI(STATE_FRIENDS);
                            }
                        }
                    });
                }
            }
        });
    }


    private void updateUI(final int stateRequestSelf) {
        currentState = stateRequestSelf;
        if((HomeActivity)getContext() == null) return;
        System.out.println("Working");
        ((HomeActivity)getContext())
                .runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(stateRequestSelf == STATE_REQUEST_SELF) {
                            btnEditProfile.setText(("Edit Profile"));
                        } else if(stateRequestSelf == STATE_REQUEST_RECVD) {
                            btnEditProfile.setText(("Accept Request"));
                        } else if(stateRequestSelf == STATE_REQUEST_SENT) {
                            btnEditProfile.setText(("Cancel Request"));
                        } else if(stateRequestSelf == STATE_FRIENDS) {
                            btnEditProfile.setText(("Remove Friends"));
                        }
                        else if(stateRequestSelf == STATE_REQUEST_DEFAULT) {
                            if(!user.getPhoneNumber().equals(requestedProfile)) {
                                btnEditProfile.setText(("Send Request"));
                            } else {
                                btnEditProfile.setText(("Edit profile"));
                            }
                        }
                        else {
                            btnEditProfile.setText(("Cancel Request"));
                        }
                    }
                });

    }

    private void fetchAllPosts() {
        UserPostTask task = new UserPostTask(((HomeActivity) getContext()), new UserPostTask.UserPostTaskCallback() {
            @Override
            public void onResult(String s) {
                try {

                    System.out.println(s);
                    JSONObject object = new JSONObject(s);
                    String resultType = object.getString("type");
                    if(resultType.equals("success"))
                    {
                        JSONArray array = object.getJSONArray("result");
                        totalPosts = array.length();
                        for(int i = 0;i < totalPosts;i++)
                        {
                            JSONObject current = array.getJSONObject(i);
                            String pid = current.getString("pid");
                            final String url = current.getString("url");

//                            <ImageView
//                            android:clickable="true"
//                            android:focusable="true"
//                            android:background="?attr/selectableItemBackground"
//                            android:adjustViewBounds="true"
//                            app:layout_flexBasisPercent="30%"
//                            android:layout_marginStart="2dp"
//                            android:layout_marginEnd="2dp"
//                            android:src="@drawable/test_post_1"
//                            android:layout_width="wrap_content"
//                            android:layout_height="wrap_content" />
//
                            Post post = new Post();
                            post.setUid(uid);

                            final ImageView imageView = new ImageView(getContext());
                            imageView.setAdjustViewBounds(true);

                            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setFlexBasisPercent(0.33f);
                            params.topMargin = (int) getResources().getDimension(R.dimen.two_margin);
                            params.bottomMargin = (int) getResources().getDimension(R.dimen.two_margin);
                            params.setHeight(500);
                            params.setWidth(500);
                            imageView.setLayoutParams(params);
                            ((HomeActivity)getContext())
                                     .runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            mainFlex.addView(imageView);
                                            Glide.with(context).load(Constants.SERVER_ADDRESS + "/media/posts/" + url).into(imageView);
                                        }
                                    });
                        }
                        ((HomeActivity)getContext())
                                .runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        posts.setTxtNumber(totalPosts);
                                    }
                                });
                    }
                    else
                    {
                        ((HomeActivity)getContext())
                                .runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Failed to retrive info", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
                catch (Exception e)
                {
                    ((HomeActivity)getContext())
                            .runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "Failed to retrive info", Toast.LENGTH_SHORT).show();
                                }
                            });
                    e.printStackTrace();
                }
            }
        });
        task.execute(uid, requestedProfile);
    }

    private void fetchProfile() {
        final GetProfileTask task = new GetProfileTask(getContext(), new GetProfileTask.ProfileReceivedListener() {
            @Override
            public void takeProfile(String body) {
//                btnSave.setEnabled(true);
                try {
                    JSONObject object = new JSONObject(body);
                    String type = object.getString("type");
                    if(type.equals("success")) {
                        int rowCount = Integer.parseInt(object.getString("no_of_rows"));
                        JSONArray array = object.getJSONArray("rows");
                        for(int i = 0;i < rowCount;i++) {
                            JSONObject object1 = array.getJSONObject(i);
                            String name = object1.getString("u_name");
                            System.out.println("name = " + name);
                            String email = object1.getString("email");
                            String status = object1.getString("status");
                            String image_url = object1.getString("u_profile_link");
                            String category = object1.getString("u_category");
                            com.hrishita.difabled.HomeProfileFragment.this.category = category;
                            com.hrishita.difabled.HomeProfileFragment.this.name = name;
                            com.hrishita.difabled.HomeProfileFragment.this.status = status;
                            com.hrishita.difabled.HomeProfileFragment.this.handler= email;
                            profile_photo = image_url;
//                            et_name.setText(name);
                            System.out.println(image_url);
                            if(!image_url.equals("DEFAULT")) {
//                                final long ONE_MEGABYTE = 4096 * 4096;
//                                mStorageReference = FirebaseStorage.getInstance().getReference().child(image_url);
//                                mStorageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                                    @Override
//                                    public void onSuccess(byte[] bytes) {
//                                        Bitmap bmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                                        profile_photo.setImageBitmap(bmap);
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                });

                                Picasso.get().load(Constants.SERVER_ADDRESS + "/media/profile/" + image_url).into(profile);
                            }
                            txtHandler.setText(handler);
                            txtName.setText(name);
                            txtStatus.setText(status);

                            break;
                        }
                    }
                    else{
                        System.out.println("Failure : " + body);
                    }
                } catch (Exception e) {
                    System.out.println(body);

                    e.printStackTrace();
                }

            }
        });
        task.execute(uid, requestedProfile);
    }

    private void extractData() {
        if(getArguments()!=null)
        {
            requestedProfile = getArguments().getString("id");
        }
        else
        {
            requestedProfile = user.getPhoneNumber();
        }
        if(requestedProfile!=null) {
            if (requestedProfile.equals(user.getPhoneNumber())) {
                updateUI(STATE_REQUEST_SELF);
            } else {
                updateUI(STATE_REQUEST_DEFAULT);
            }
        }
    }

    private void openSettings() {
        ((HomeActivity)getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_activity_base,new SettingsFragment(), "settings")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mInterface.showAppBar();
    }
}
