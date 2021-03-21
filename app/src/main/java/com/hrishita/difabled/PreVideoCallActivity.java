package com.hrishita.difabled;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class PreVideoCallActivity extends AppCompatActivity {
    FriendsModel remoteUser;
    String phoneNumber;
    FirebaseUser user;
    String chatUid;

    //views
    ImageView profile;
    TextView messages;
    FloatingActionButton fabEnd;
    private LocalBroadcastManager broadcastManager;

    Timer t;
    String vCallId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_video_call);
        initViews();
        getCurrentUser();
        extract();
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                rejectCallForBothEnds();
            }
        },10000, 10000);
        fabEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectCallForBothEnds();
            }
        });
        Glide.with(this)
                .load(Uri.parse(Constants.SERVER_ADDRESS + "/media/profile/" + remoteUser.getUser().getProfile_link()))
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(profile);

//        final VideoCallStartTask task = new VideoCallStartTask(PreVideoCallActivity.this, new VideoCallStartTask.VideoCallStatusCallback() {
//            @Override
//            public void success(JSONObject s) {
//                try {
//                    String mVideoCallId = s.getString("video_call_id");
//                    System.out.println(mVideoCallId);
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void failure(JSONObject s) {
//                System.out.println("Failure");
//            }
//
//            @Override
//            public void onUpdateStatus(String s) {
//                if(s.equals("Failure"))
//                {
//                    Toast.makeText(PreVideoCallActivity.this, "Failed to call", Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//                else {
//                    messages.setText(s);
//                }
//            }
//        });

        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task1) {
                if(task1.isSuccessful()) {
                    startVideoCall();
                    broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                    broadcastManager.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if(intent.getStringExtra("message").equals("call ended")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messages.setText("Call Declined");
                                    }
                                });
                            }
                        }
                    }, new IntentFilter("MyData"));
//                    task.execute(user.getPhoneNumber(), remoteUser.getUser().getPhone(), task1.getResult().getToken());
                }
            }
        });
    }

    private void rejectCallForBothEnds()
    {
        if(vCallId!=null)
        {
            //todo
            //step1 : remove from database
            FirebaseDatabase fb = FirebaseDatabase.getInstance();
            fb.getReference()
                    .child("video-call")
                    .child(chatUid)
                    .child(vCallId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //hope for best that task is successful
                }
            });

            //step2 : send fcm to other end to stop notification and ring service
            VideoCallRejectBothEndTask task = new VideoCallRejectBothEndTask();
            task.execute(remoteUser.getUser().getPhone(), vCallId);

            //step3: close current activity
            finish();
        }

    }

    private void startVideoCall() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference()
                .child("video-call")
                .child(chatUid).push();

        HashMap<String, String> payload = new HashMap<>();
        payload.put("caller", user.getPhoneNumber());
        payload.put("receiver", remoteUser.getUser().getPhone());
        payload.put("status", "1");
        payload.put("videocallid", reference.getKey());

        vCallId = reference.getKey();

        reference.setValue(payload);

        SendVideoCallFcMNotificationTask task = new SendVideoCallFcMNotificationTask(PreVideoCallActivity.this, new SendVideoCallFcMNotificationTask.SendVideoCallTaskCallback() {
            @Override
            public void onVideoCallDialSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messages.setText("Ringing Call");
                    }
                });
            }

            @Override
            public void onVideoCallDialFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        });
        task.execute(user.getPhoneNumber(), remoteUser.getUser().getPhone(), reference.getKey());
    }

    private void initViews() {
        profile = findViewById(R.id.pre_video_call_profile);
        fabEnd = findViewById(R.id.pre_video_call_end);
        messages= findViewById(R.id.pre_video_call_txt);
    }

    private void extract() {
        if(getIntent()!=null)
        {
            if(getIntent().getSerializableExtra("User")!=null)
            {
                remoteUser = (FriendsModel) getIntent().getSerializableExtra("User");
                chatUid = getIntent().getStringExtra("chat_id");
            }
            else
            {
                Toast.makeText(this, "ERROR1", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else
        {
            Toast.makeText(this, "ERROR2", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getCurrentUser() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            finish();
            return;
        }
        phoneNumber = user.getPhoneNumber();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        t.cancel();
    }
}