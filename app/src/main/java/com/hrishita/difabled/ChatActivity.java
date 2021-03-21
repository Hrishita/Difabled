package com.hrishita.difabled;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    ListView recyclerView;
    List<FriendsModel> arrayList = new ArrayList<>();
    FirebaseFirestore firebaseFirestore;
    ChatsAdapter arrayAdapter;
    FirebaseUser user;
    ImageView myProfile;
    ImageView uploadPost;
    String idToken;
    Map<String, String> phoneNumbers = new HashMap<>();
    String cate;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseFirestore = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Directs");
        cate=getIntent().getStringExtra("category");
        Toast.makeText(this, ""+cate, Toast.LENGTH_SHORT).show();
        user = FirebaseAuth.getInstance().getCurrentUser();
        progressBar= findViewById(R.id.chat_pbar);

        UpdatePrecenceTask task = new UpdatePrecenceTask(com.hrishita.difabled.ChatActivity.this);
        task.execute(user.getPhoneNumber(), "true");

        initViews();
        arrayAdapter = new ChatsAdapter(com.hrishita.difabled.ChatActivity.this, arrayList);
        recyclerView.setAdapter(arrayAdapter);

        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()) {
                    idToken = task.getResult().getToken();
                    refreshPage();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void refreshPage() {
       arrayList.clear();
       loadData();

    }

    private void loadData() {

        arrayList.clear();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("").child("friend-request").child(user.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    //first direct child is uid of another user
                    if (s.hasChild("type") && s.child("type").getValue() != null) {
                        if (s.child("type").getValue().toString().equals(Constants.FIREBASE_FRIEND_REQUEST_ACCEPTED + "")) {
                            phoneNumbers.put(s.getKey(), s.child("chat_id").getValue().toString());
                        }
                    }
                }
                if (!phoneNumbers.isEmpty()) {
                    firebaseFirestore.collection("users")
                            .whereIn("phone", Arrays.asList(phoneNumbers.keySet().toArray()))
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isComplete()) {
                                        System.out.println("Total Result Size = " + task.getResult().size());

                                        for (DocumentSnapshot s : task.getResult().getDocuments()) {
                                            System.out.println("Inside id = " + s.getId());

                                            String name = (String) s.get("name");
                                            String email = (String) s.get("email");
                                            String status = (String) s.get("status");
                                            String category = (String) s.get("category");
                                            String profile = (String) s.get("profile");
                                            String phone = s.getId();
                                            String chatId = phoneNumbers.get(phone);

                                            User u = new User();
                                            u.setPhone(phone);
                                            u.setStatus(status);
                                            u.setProfile_link(profile);
                                            u.setName(name);
                                            u.setEmail(email);
                                            u.setCategory(category);

                                            FriendsModel friends = new FriendsModel(u, chatId);
                                            arrayList.add(friends);
                                        }

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressBar.setVisibility(View.GONE);
                                                arrayAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.chat_recycler_view);
        myProfile = findViewById(R.id.chat_profile);
        uploadPost = findViewById(R.id.chat_post);
    }

    @Override
    protected void onPause() {
        UpdatePrecenceTask task = new UpdatePrecenceTask(com.hrishita.difabled.ChatActivity.this);
        task.execute(user.getPhoneNumber(), "false");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
