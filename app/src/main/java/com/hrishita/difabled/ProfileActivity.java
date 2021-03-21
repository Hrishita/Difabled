package com.hrishita.difabled;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    FirebaseUser user;
    Button btnSave, btnSend, btnCancel, btnAccept, btnRemoveFriend, btnDeny;
    TextInputEditText et_name;
    CheckBox rdb_mute, rdb_deaf, rdb_normal;
    CircleImageView profile_photo;
    private StorageReference mStorageReference;
    private String cat = null;
    private int REQUEST_PICK_IMAGE = 0;
    private String fileName = null;
    private String mUser = null;
    private Uri profileUri;

    public static int STATE_REQUEST_RECVD = 0;
    public static int STATE_REQUEST_SENT = 1;
    public static int STATE_REQUEST_SELF = 2;
    public static int STATE_REQUEST_DEFAULT = 3;
    public static int STATE_FRIENDS = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) {
            gotoLoginActivity();
        }

        initViews();
        extractBundle();

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest();
            }
        });

        btnDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                denyRequest();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                denyRequest();
            }
        });

        btnRemoveFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                denyRequest();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        if(!mUser.equals(user.getPhoneNumber())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                    database.child("").child("friend-request").child(user.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot s : snapshot.getChildren()) {
                                //first direct child is uid of another user
                                    if (s.getKey().equals(mUser)) {
                                        if (s.hasChild("type") && s.child("type").getValue()!=null)
                                            if (s.child("type").getValue().toString().equals(Constants.FIREBASE_FRIEND_REQUEST_RECVD + "")) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateUI(STATE_REQUEST_RECVD);
                                                    }
                                                });
                                            } else if (s.child("type").getValue().toString().equals(Constants.FIREBASE_FRIEND_REQUEST_SENT + "")) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateUI(STATE_REQUEST_SENT);
                                                    }
                                                });
                                            } else if(s.child("type").getValue().toString().equals(Constants.FIREBASE_FRIEND_REQUEST_ACCEPTED + "")) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateUI(STATE_FRIENDS);
                                                    }
                                                });
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


        final GetProfileTask task = new GetProfileTask(com.hrishita.difabled.ProfileActivity.this, new GetProfileTask.ProfileReceivedListener() {
            @Override
            public void takeProfile(String body) {
                btnSave.setEnabled(true);
                try {
                    JSONObject object = new JSONObject(body);
                    String type = object.getString("type");
                    if(type.equals("success")) {
                        int rowCount = Integer.parseInt(object.getString("no_of_rows"));
                        JSONArray array = object.getJSONArray("rows");
                        for(int i = 0;i < rowCount;i++) {
                            JSONObject object1 = array.getJSONObject(i);
                            String name = object1.getString("u_name");
                            String image_url = object1.getString("u_profile_link");
                            String category = object1.getString("u_category");
                            if(Constants.CATEGORY_NORMAL.equals(category)) {
                                rdb_normal.setChecked(true);
                            }
                            else if(Constants.CATEGORY_MUTE.equals(category)) {
                                rdb_deaf.setChecked(true);
                            }
                            else if(Constants.CATEGORY_MUTE.equals(category)) {
                                rdb_mute.setChecked(true);
                            }
                            else if(Constants.CATEGORY_MUTE.equals(category)) {
                                rdb_mute.setChecked(true);
                                rdb_deaf.setChecked(true);

                            }

                            et_name.setText(name);
                            if(!image_url.equals("DEFAULT")) {
                                System.out.println(image_url);

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

                                Picasso.get().load(Constants.SERVER_ADDRESS + "/media/profile/" + image_url).into(profile_photo);
                            }

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
        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task2) {
                if(task2.isSuccessful()) {
                    task.execute(task2.getResult().getToken(), mUser);
                }
                else{
                    finish();
                }
            }
        });

        profile_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(i, "Choose Your profile image"), REQUEST_PICK_IMAGE);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RegisterTask registerTask = new RegisterTask(com.hrishita.difabled.ProfileActivity.this, new RegisterCallbackListener() {
                    @Override
                    public void registerCallback(String string) {
                        try {
                            JSONObject jsonObject = new JSONObject(string);
                            if(jsonObject.getString("type").equals("success")) {
                                PreferenceManager.saveProfile(getApplicationContext(), et_name.getText().toString(), cat);
                                gotoChatsActivity();
                            }
                            else {
                                Toast.makeText(com.hrishita.difabled.ProfileActivity.this, "Failed to upload Profile", Toast.LENGTH_SHORT).show();
                                gotoLoginActivity();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//                        Toast.makeText(ProfileActivity.this, string, Toast.LENGTH_SHORT).show();
                    }
                });
                if(et_name.getText().toString().equals("")) {
                    cat = "";
                    et_name.setError("Please Enter Your Name");
                }
                else if(rdb_deaf.isChecked() && rdb_mute.isChecked()) {
                    //both deaf and mute
                    cat = Constants.CATEGORY_MUTE;
                }
                else if(rdb_mute.isChecked()) {
                    //only mute
                    cat = Constants.CATEGORY_MUTE;
                }
                else if(rdb_deaf.isChecked()) {
                    //only deaf
                    cat = Constants.CATEGORY_MUTE;
                }
                else if(rdb_normal.isChecked()) {
                    //normal
                    cat = Constants.CATEGORY_NORMAL;
                }
                else {
                    cat = "";
                    Toast.makeText(com.hrishita.difabled.ProfileActivity.this, "Please Select A Category", Toast.LENGTH_SHORT).show();
                }
                if(!cat.equals("")) {
                    final String finalCat = cat;
                    user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull final Task<GetTokenResult> task) {

                            final String myPath = "profile/" + System.currentTimeMillis() + ".png";
//                            FirebaseStorage.getInstance().getReference().child(myPath).putFile(Uri.parse("file://" + fileName)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task2) {
//                                    String finalPath;
//                                    if(task.isSuccessful()) {
//                                        finalPath = myPath;
//                                    }
//                                    else{
//                                        finalPath = "DEFAULT";
//                                    }
                                    ProfileModel profileModel = new ProfileModel();
                                    profileModel.name = et_name.getText().toString();
                                    profileModel.cat = finalCat;
                                    profileModel.imageUri = profileUri;
                                    profileModel.phoneNumber = user.getPhoneNumber();
                                    profileModel.uid = task.getResult().getToken();
                                    registerTask.execute(profileModel);
//                                }
//                            });

                        }
                    });
                }
            }
        });

    }

    private void sendRequest() {
        FirebaseDatabase.getInstance().getReference().child("").child("friend-request").child(user.getPhoneNumber()).child(mUser).child("type").setValue(Constants.FIREBASE_FRIEND_REQUEST_SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference().child("").child("friend-request").child(mUser).child(user.getPhoneNumber()).child("type").setValue(Constants.FIREBASE_FRIEND_REQUEST_RECVD).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(com.hrishita.difabled.ProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                        updateUI(STATE_REQUEST_SENT);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void denyRequest() {
        FirebaseDatabase.getInstance().getReference().child("").child("friend-request").child(user.getPhoneNumber()).child(mUser).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference().child("").child("friend-request").child(mUser).child(user.getPhoneNumber()).removeValue() .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(com.hrishita.difabled.ProfileActivity.this, "Request Rejected", Toast.LENGTH_SHORT).show();
                                        updateUI(STATE_REQUEST_DEFAULT);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(com.hrishita.difabled.ProfileActivity.this, "Failed to Reject", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(com.hrishita.difabled.ProfileActivity.this, "Failed To Deny Request", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void acceptRequest() {
        final HashMap<String, String> data = new HashMap<>();
        data.put("type", "2");
        data.put("chat_id", user.getPhoneNumber() + "_" + mUser);
        DatabaseReference storage = FirebaseDatabase.getInstance().getReference();
        storage.child("").child("friend-request").child(user.getPhoneNumber()).child(mUser).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference().child("").child("friend-request").child(mUser).child(user.getPhoneNumber()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(com.hrishita.difabled.ProfileActivity.this, "Friend Request Accepted", Toast.LENGTH_SHORT).show();
                                        updateUI(STATE_FRIENDS);                                    }
                                });

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(com.hrishita.difabled.ProfileActivity.this, "Failed To Send Friend Request", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(com.hrishita.difabled.ProfileActivity.this, "Failed To Send Friend Request", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void extractBundle() {
        if(getIntent()!=null) {


            if (getIntent().getSerializableExtra("phone_number")!=null) {
                mUser =  getIntent().getStringExtra("phone_number");
            }
            else{
                mUser = user.getPhoneNumber();
            }
            if(mUser.equals(user.getPhoneNumber())) {
                updateUI(STATE_REQUEST_SELF);
            } else {
                updateUI(STATE_REQUEST_DEFAULT);
            }
        }
    }

    private void updateUI(int stateRequestSelf) {

        if(stateRequestSelf == STATE_REQUEST_SELF) {
            btnSave.setVisibility(View.VISIBLE);
            btnSend.setVisibility(View.INVISIBLE);
            btnRemoveFriend.setVisibility(View.INVISIBLE);
            btnCancel.setVisibility(View.INVISIBLE);
            btnAccept.setVisibility(View.INVISIBLE);
            btnDeny.setVisibility(View.INVISIBLE);
        } else if(stateRequestSelf == STATE_REQUEST_RECVD) {
            btnSave.setVisibility(View.INVISIBLE);
            btnSend.setVisibility(View.INVISIBLE);
            btnRemoveFriend.setVisibility(View.INVISIBLE);
            btnCancel.setVisibility(View.INVISIBLE);
            btnAccept.setVisibility(View.VISIBLE);
            btnDeny.setVisibility(View.VISIBLE);
        } else if(stateRequestSelf == STATE_REQUEST_SENT) {
            btnSave.setVisibility(View.INVISIBLE);
            btnSend.setVisibility(View.INVISIBLE);
            btnRemoveFriend.setVisibility(View.INVISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnAccept.setVisibility(View.INVISIBLE);
            btnDeny.setVisibility(View.INVISIBLE);
        } else if(stateRequestSelf == STATE_FRIENDS) {
            btnSave.setVisibility(View.INVISIBLE);
            btnSend.setVisibility(View.INVISIBLE);
            btnRemoveFriend.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.INVISIBLE);
            btnAccept.setVisibility(View.INVISIBLE);
            btnDeny.setVisibility(View.INVISIBLE);
        }
        else if(stateRequestSelf == STATE_REQUEST_DEFAULT) {
            if(!user.getPhoneNumber().equals(mUser)) {
                btnSave.setVisibility(View.INVISIBLE);
                btnSend.setVisibility(View. VISIBLE);
                btnRemoveFriend.setVisibility(View.INVISIBLE);
                btnCancel.setVisibility(View.INVISIBLE);
                btnAccept.setVisibility(View.INVISIBLE);
                btnDeny.setVisibility(View.INVISIBLE);
            } else {
                btnSave.setVisibility(View.VISIBLE);
                btnSend.setVisibility(View.INVISIBLE);
                btnRemoveFriend.setVisibility(View.INVISIBLE);
                btnCancel.setVisibility(View.INVISIBLE);
                btnAccept.setVisibility(View.INVISIBLE);
                btnDeny.setVisibility(View.INVISIBLE);
            }
        }
        else {
            btnSave.setVisibility(View.INVISIBLE);
            btnSend.setVisibility(View.INVISIBLE);
            btnRemoveFriend.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.INVISIBLE);
            btnAccept.setVisibility(View.INVISIBLE);
            btnDeny.setVisibility(View.INVISIBLE);
        }
    }


    private boolean isProfileSet() {
        return PreferenceManager.isUserProfileCreated(getApplicationContext());
    }

    private void gotoChatsActivity() {
        Intent i = new Intent(getApplicationContext(), ViewPostsActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
//            Uri selectedImage = data.getData();
            profileUri = data.getData();
            Picasso.get().load(profileUri).into(profile_photo);
//            String[] filePathColumn = { MediaStore.Images.Media.DATA };
//            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);
//            try {
//                Bitmap bmap = BitmapFactory.decodeFile(picturePath);
//                profile_photo.setImageBitmap(bmap);
//                fileName = picturePath;
//                StorageReference ref = FirebaseStorage.getInstance().getReference();
//                ref = ref.child("profile-images/" + System.currentTimeMillis() + ".png");
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            cursor.close();
        }
    }

    private void initViews() {
        btnSave = findViewById(R.id.profile_btn_send);
        btnSend = findViewById(R.id.profile_send_request);
        btnCancel = findViewById(R.id.profile_cancel_request);
        btnAccept = findViewById(R.id.profile_accept_request);
        btnRemoveFriend = findViewById(R.id.profile_remove_friend);
        btnDeny = findViewById(R.id.profile_deny_request);
        et_name = findViewById(R.id.profile_name);
        rdb_deaf = findViewById(R.id.profile_deaf);
        rdb_mute = findViewById(R.id.profile_mute);
        rdb_normal = findViewById(R.id.profile_normal);
        profile_photo = findViewById(R.id.profile_my_image);
        btnSave.setEnabled(false);
    }

    private void gotoLoginActivity() {
        Intent i = new Intent(getApplicationContext(), PhoneAuthActivity.class);
        startActivity(i);
        finish();
    }

}
