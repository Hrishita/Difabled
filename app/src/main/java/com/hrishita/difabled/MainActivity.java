package com.hrishita.difabled;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements MessageEditTextView.MessageEditTextViewInterface, HomeActivityInterface
{

    private boolean isOnline;
    private RelativeLayout main_base;
    private FirebaseStorage storage;
    private static final int REQUEST_IMAGE = 0;
    private RecyclerView recyclerView;
    private Adapter mAdapter;
    private ArrayList<FirebaseMessageModel> mMessageData = new ArrayList<>();
    private Button send;
    private MessageEditTextView sendMessageEt;
    private FirebaseUser user;
    private FriendsModel endUser;
    private ImageView endUserProfile, videoCallButton;
    private TextView endUserName;
    private String chatUid = null;
    private Uri myDownloadUri;
    private AlertDialog dialog;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportActionBar().hide();
        storage = FirebaseStorage.getInstance();

        recyclerView = findViewById(R.id.recycler_view);
        dialog = new AlertDialog.Builder(this)
                .setView(R.layout.loading)
                .setCancelable(false)
                .create();

//        send = findViewById(R.id.btn_send);
        sendMessageEt = findViewById(R.id.send_message_edit_text);
        endUserProfile = findViewById(R.id.main_user_profile_image);
        endUserName = findViewById(R.id.main_user_name);
        videoCallButton = findViewById(R.id.video_call_button);
        main_base = findViewById(R.id.main_base);


        final LinearLayoutManager manager = new LinearLayoutManager(this);


//        mMessageData.add(new FirebaseMessageModel("1", "Devarsh", "Hrishita", "Hello, How Are you?\nSecond line of message", true));
//        mMessageData.add(new FirebaseMessageModel("1", "Devarsh", "Hrishita", "Hello, How Are you", true));
//        mMessageData.add(new FirebaseMessageModel("1", "Devarsh", "Hrishita", "Hello, How Are you", false));
//        mMessageData.add(new FirebaseMessageModel("1", "Devarsh", "Hrishita", "Hello, How Are you", true));
//        mMessageData.add(new FirebaseMessageModel("1", "Devarsh", "Hrishita", "Hello, How Are you", true));
//        mMessageData.add(new FirebaseMessageModel("1", "Devarsh", "Hrishita", "Hello, How Are you", false));
//        mMessageData.add(new FirebaseMessageModel("1", "Devarsh", "Hrishita", "Hello, How Are you", false));

        mAdapter = new Adapter(MainActivity.this, mMessageData);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(mAdapter);

        user = FirebaseAuth.getInstance().getCurrentUser();

        endUser = (FriendsModel) getIntent().getSerializableExtra("Friend");
        chatUid = endUser.getChat_id();

        final UpdatePrecenceTask task = new UpdatePrecenceTask(MainActivity.this);
        task.execute(user.getPhoneNumber(), "true");

        firestore= FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(endUser.getUser().getPhone())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable final DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error==null)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        isOnline = (boolean) value.get("presence");
                                        if (endUser != null) {
                                            String html = "<font color = \"white\">" + endUser.getUser().getName() + "</font> <br> <font color = \"green\">" + (isOnline ? "online" : "offline") + "</font>";
                                            endUserName.setText(Html.fromHtml(html));
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        if (endUser != null) {
                                            String html = "<font color = \"white\">" + endUser.getUser().getName() + "</font> <br> <font color = \"green\">" + (isOnline ? "online" : "offline") + "</font>";
                                            endUserName.setText(Html.fromHtml(html));
                                        }
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });


        videoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this, PreVideoCallActivity.class);
                i.putExtra("User", endUser);
                i.putExtra("chat_id", chatUid);
                startActivity(i);

            }
        });

        endUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                HomeProfileFragment fragment = new HomeProfileFragment();
//                Bundle bundle = new Bundle();
//                bundle.putString("id", endUser.getUser().getPhone());
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.main_base, fragment, "home profile")
//                        .addToBackStack(null)
//                        .commit();
//                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
//                i.putExtra("phone_number", endUser.getUser().getPhone());
//                startActivity(i);
            }
        });
        updateUI();
        FirebaseDatabase.getInstance().getReference().child("").child("chat").child(endUser.getChat_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    if(snapshot.hasChildren()) {
                        mMessageData.clear();
                        for (DataSnapshot s : snapshot.getChildren()) {
//                            FirebaseMessageModel model = new FirebaseMessageModel(s.child("datetime").getValue().toString(), s.child("sender").getValue().toString(), s.child("receiver").getValue().toString(), s.child("message").getValue().toString());
                            FirebaseMessageModel model = new FirebaseMessageModel(s.child("datetime").getValue().toString(), s.child("sender").getValue().toString(), s.child("receiver").getValue().toString(), s.child("message").getValue().toString(), (s.child("receiver").getValue().toString().equals(user.getPhoneNumber())), (s.child("type").getValue() == null ? Constants.MESSAGE_TYPE_TEXT : s.child("type").getValue().toString()));
                            mMessageData.add(model);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter = new Adapter(MainActivity.this,mMessageData);
                                recyclerView.setAdapter(mAdapter);
                                recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull final DatabaseError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Error Fetching Data " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

//        send.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String messageSend = sendMessageEt.getText().toString();
//                if(!messageSend.equals("")) {
//                    //send message
//                    sendMessage(messageSend);
//                }
//            }
//        });

    }

    private void updateUI() {
        Picasso.get().load(Constants.SERVER_ADDRESS + "/media/profile/" + endUser.getUser().getProfile_link()).into(endUserProfile);
//        Picasso.get().load(Constants.SERVER_ADDRESS + "/media/profile/1601463438130.png").into(endUserProfile);

//        FirebaseStorage.getInstance().getReference().child(endUser.getProfile_link()).getBytes((4096 * 4096)).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//                Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//                endUserProfile.setImageBitmap(bmp);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                e.printStackTrace();
//            }
//        });
//        endUserName.setText(endUser.getName());

    }

    private void sendMessage(HashMap<String, String> hashMap) {
        DatabaseReference chatDatabase = FirebaseDatabase.getInstance().getReference().child("chat").child(chatUid);
        String key = chatDatabase.push().getKey();
        chatDatabase.child(key).setValue(hashMap);
        SendMessageNotificationTask sendMessageNotificationTask = new SendMessageNotificationTask(MainActivity.this);
        sendMessageNotificationTask.execute(endUser.getUser().getPhone(), user.getPhoneNumber(), hashMap.get("message"));
    }

    private void sendMessage(Uri uri) {
        dialog.show();
        StorageReference ref = storage.getReference();

        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("sender", user.getPhoneNumber());
        hashMap.put("receiver", endUser.getUser().getPhone());
        hashMap.put("datetime", System.currentTimeMillis() + "");
        hashMap.put("type", Constants.MESSAGE_TYPE_IMAGE);

        final DatabaseReference chatDatabase = FirebaseDatabase.getInstance().getReference().child("chat").child(chatUid);
        final String key = chatDatabase.push().getKey();

        final StorageReference filePath = ref.child((key + ".jpg"));

        filePath
                .putFile(uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    myDownloadUri = task.getResult();
                                    hashMap.put("message", myDownloadUri.toString());
                                    chatDatabase.child(key).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    dialog.dismiss();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
    }

    @Override
    public void onMessageSent() {
        if(!sendMessageEt.getText().toString().equals("")) {
            HashMap<String, String> data = new HashMap<>();
            data.put("sender", user.getPhoneNumber());
            data.put("message", sendMessageEt.getText().toString());
            data.put("receiver", endUser.getUser().getPhone());
            data.put("datetime", System.currentTimeMillis() + "");
            data.put("type", Constants.MESSAGE_TYPE_TEXT);
            sendMessage(data);
        }
        sendMessageEt.clearText();
    }

    @Override
    public void onMediaSelected() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK)
        {
            if(data!=null)
            {
                Uri uri = data.getData();
                if(uri!=null)
                {
                    final String name = System.currentTimeMillis() + "_" + user.getPhoneNumber();
                    try {

                        sendMessage(uri);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void hideBottomNav() {

    }

    @Override
    public void showBottomNav() {

    }

    @Override
    public void hideAppBar() {

    }

    @Override
    public void showAppBar() {

    }

    @Override
    public void changeCurrentFragment(int id) {

    }
    public class SendMessageNotificationTask extends AsyncTask<String, Void, String>
    {
        Context context;
        SendMessageNotificationTask(Context context)
        {
            this.context = context;
        }
        @Override
        protected String doInBackground(String... strings) {
            String receiver = strings[0];
            String sender = strings[1];
            String message = strings[2];

            OkHttpClient client = new OkHttpClient();
            String url = "https://fcm.googleapis.com/fcm/send";

            String jsonBody = "{" +
                    "\"to\": \"/topics/"+ receiver.substring(1) +"\",\n" +
                    "\"priority\": \"high\",\n" +
                    "\"data\": {\n" +
                    "\"receiver\": \""+receiver+"\",\n" +
                    "\"sender\": \""+sender+"\",\n" +
                    "\"chat_message\": \""+message+"\",\n" +
                    "\"type\": \"CHAT_MESSAGE_RECVD\"\n" +
                    "}," +
                    "\"android\":{\n" +
                    "       \"ttl\":\"0s\",\n" +
                    "     }," +
                    "\"ttl\":\"0\"" +
                    "}";
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

            System.out.println(jsonBody + " both reject");

            Request request = new Request.Builder()
                    .addHeader("Authorization", "key=AAAA0ZGEB6w:APA91bEDDZnlpiqmnBWogfL_Lg-Q8kfZDd0CdiqpGCaS62h3_xvQ2Tw2P39BzJZVmzsxCniBgpe1-fn1t0q-kNa7o31ffkcYCjBND3W32Q63m1qKyWF-vDCY3dbxeOba0SAVb6BtLIZh")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .url(url)
                    .build();

            try {
                Response res =  client.newCall(request).execute();
                System.out.println(res.body().string() + " both reject 2");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onPause() {
        UpdatePrecenceTask task = new UpdatePrecenceTask(MainActivity.this);
        task.execute(user.getPhoneNumber(), "false");
        super.onPause();
    }
}
