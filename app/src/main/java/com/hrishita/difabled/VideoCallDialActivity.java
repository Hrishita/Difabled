package com.hrishita.difabled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONObject;

public class VideoCallDialActivity extends AppCompatActivity {
    private String mPhoneNumber;
    EditText receiver_number;
    Button btnCall;
    TextView textView;
    private LocalBroadcastManager broadcastManager;

    private String mVideoCallId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call_dial);
        receiver_number = findViewById(R.id.receiver_number_et);
        btnCall = findViewById(R.id.btn_call);
        textView = findViewById(R.id.status_videocall);

        getMyPhoneNumber();

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getStringExtra("message").equals("call ended")) {
                    textView.setText("Call Ended");
                }
            }
        }, new IntentFilter("MyData"));

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String receiver = receiver_number.getText().toString();
                if(!receiver.equals(""))
                {
                    //call using Asy ncTask
                    textView.setText("Connecting");
                    final com.hrishita.difabled.VideoCallAsyncTask videoCallAsyncTask = new com.hrishita.difabled.VideoCallAsyncTask(new com.hrishita.difabled.VideoCallAsyncTask.VideoCallResultInterface() {
                        @Override
                        public void success(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                mVideoCallId = response.getString("video_call_id");
                                textView.setText((status + " " + mVideoCallId));
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failure(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                textView.setText(status);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user!=null) {
                        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            @Override
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                String mToken = task.getResult().getToken();
                                videoCallAsyncTask.execute(mToken, mPhoneNumber, receiver);
                            }
                        });
                    }
                }
            }
        });
    }

    private void getMyPhoneNumber() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        mPhoneNumber = preferences.getString("phone_number", "");
        if(mPhoneNumber.equals("")) {
            btnCall.setEnabled(false);
        }
    }
}
