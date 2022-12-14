package com.hrishita.difabled;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private Button btn_save,btn_edit;
    private TextInputEditText ti_name, ti_handler, ti_status;
    private CircleImageView im_profileImage;
    private Uri imageLocation;
    private String remoteImageUrl;
    private String imageAction = Constants.IMAGE_UPLOAD_EXISTING;
    HomeActivityInterface homeActivityInterface;
    //states
    private static int STATE_REQUEST_RECVD = 0;
    private static int STATE_REQUEST_SENT = 1;
    private static int STATE_REQUEST_SELF = 2;
    private static int STATE_REQUEST_DEFAULT = 3;
    private static int STATE_FRIENDS = 4;

    FirebaseUser user;
    private String requestedUserPhone = null;
    private int RC_PROFILE = 0;
  //  Class<HomeActivity> appCompatActivity;
    private String uid;
    private String name;
    private String handler;
    private String status;
    private String category;

    private ImageView dialogImage;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_profile, container, false);
        btn_save = v.findViewById(R.id.btn_profile_save);
        ti_name = v.findViewById(R.id.profile_name);
        ti_handler = v.findViewById(R.id.profile_handler);
        ti_status = v.findViewById(R.id.profile_status);

        im_profileImage = v.findViewById(R.id.profile_my_image);
        btn_edit = v.findViewById(R.id.btn_profile_edit_number);
        user = FirebaseAuth.getInstance().getCurrentUser();


        if(user == null) {
            gotoLoginFragment();
        }
        final AlertDialog dialog = new AlertDialog.Builder((PreHomeScreenActivity)getContext())
                .setView(R.layout.loading)
                .setCancelable(false)
                .create();
        dialog.show();

        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()) {
                    uid = task.getResult().getToken();
                }
                dialog.dismiss();
                fillUI();
            }
        });

        im_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog imgDialog = new AlertDialog.Builder(((PreHomeScreenActivity)getContext()))
                        .setView(R.layout.profile_image_operations_dialog)
                        .setCancelable(false)
                        .create();
                imgDialog.show();

                ImageView closeDialog = imgDialog.findViewById(R.id.pio_dialog_close);
                dialogImage = imgDialog.findViewById(R.id.pio_dialog);
                Button removeImage = imgDialog.findViewById(R.id.btn_pio_remove);
                Button uploadImage = imgDialog.findViewById(R.id.btn_pio_upload);

                if(remoteImageUrl != null && imageAction.equals(Constants.IMAGE_UPLOAD_EXISTING)) {
                    Picasso.get().load(Constants.SERVER_ADDRESS + "/media/profile/" + remoteImageUrl).into(dialogImage);
                } else if(imageLocation!=null && imageAction.equals(Constants.IMAGE_UPLOAD_NEW)){
                    dialogImage.setImageURI(imageLocation);
                } else {
                    dialogImage.setImageResource(R.drawable.default_profile);
                }

                uploadImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                        i.setType("image/*");
                        startActivityForResult(i, RC_PROFILE);
                    }
                });
                closeDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imgDialog.dismiss();
                    }
                });
                removeImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogImage.setImageResource(R.drawable.default_profile);
                        im_profileImage.setImageResource(R.drawable.default_profile);
                        imageAction = Constants.IMAGE_UPLOAD_REMOVE;
                    }
                });
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(((PreHomeScreenActivity)getContext()))
                        .setMessage("Are You Sure you want to edit number?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                FirebaseAuth.getInstance().signOut();
                                gotoLoginFragment();
                            }
                        })
                        .setNegativeButton("no", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ti_name.getText().toString().equals("")) {
                    ti_name.setError("Enter Username");
                }
                else if(ti_handler.getText().toString().equals("")) {
                    ti_handler.setError("Enter email");
                }
                else if(ti_status.getText().toString().equals("")) {
                    ti_status.setError("Enter Status");
                }
                else {
                    name = ti_name.getText().toString();
                    status = ti_status.getText().toString();
                    handler = ti_handler.getText().toString();

                    uploadData();
                   /* Bundle bundle = new Bundle();
                    bundle.putString("name", ti_name.getText().toString());
                    bundle.putString("handler",ti_handler.getText().toString());
                    bundle.putString("status", ti_status.getText().toString());
                    bundle.putString("category", category);
                    bundle.putString("uid", uid);
                    bundle.putString("command", imageAction);
                    System.out.println("pf" + ti_status.getText().toString());
                    System.out.println("pf" + ti_handler.getText().toString());
                    bundle.putString("image", imageLocation == null ? null : imageLocation.toString());
                    gotoPreferencesFragment();*/
                  //  gotoCategoryFragment(bundle);
                }
            }
        });

//        im_deaf.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                im_deaf.setImageResource(R.drawable.deaf_selected);
//                im_normal.setImageResource(R.drawable.general_unselected);
//                isDisabled = true;
//            }
//        });
//
//        im_normal.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                im_deaf.setImageResource(R.drawable.deaf_unselected);
//                im_normal.setImageResource(R.drawable.general_selected);
//                isDisabled = false;
//            }
//        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PROFILE && resultCode == Activity.RESULT_OK) {
            if(data!=null) {
                imageLocation = data.getData();
                if(dialogImage != null) {
                    dialogImage.setImageURI(imageLocation);
                    imageAction = Constants.IMAGE_UPLOAD_NEW;
                } else {
                    Toast.makeText(getContext(), "NULL", Toast.LENGTH_SHORT).show();
                }
                im_profileImage.setImageURI(imageLocation);
            }
        }
    }
    private void gotoPreferencesFragment() {

            ((PreHomeScreenActivity) getContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragcontainer, new SelectIntrests(),"interests")
                    .commit();





        }




    private void fillUI() {
        //check if data passed from other fragment
        if(getArguments()!=null) {

                requestedUserPhone = user.getPhoneNumber();
                category = (getArguments().getString("category"));
                handler = (getArguments().getString("handler"));
                status = (getArguments().getString("status"));

                ti_name.setText(name);
                ti_handler.setText(handler);
                ti_status.setText(status);
        }
            //load data from server
            final GetProfileTask task = new GetProfileTask(((PreHomeScreenActivity)getContext()), new GetProfileTask.ProfileReceivedListener() {
                @Override
                public void takeProfile(String body) {
                    try {
                        JSONObject object = new JSONObject(body);
                        String type = object.getString("type");
                        if(type.equals("success")) {
                            int rowCount = Integer.parseInt(object.getString("no_of_rows"));
                            JSONArray array = object.getJSONArray("rows");
                            for(int i = 0;i < rowCount;i++) {
                                JSONObject object1 = array.getJSONObject(i);
                                name = object1.getString("u_name");
                                String image_url = object1.getString("u_profile_link");
                                category = object1.getString("u_category");
                                status = object1.getString("status");
                                handler = object1.getString("email");

                                ti_name.setText(name);
                                ti_handler.setText(handler);
                                ti_status.setText(status);

                                if(Constants.CATEGORY_MUTE_DEAF.equals(category))
                                {
                                    category = Constants.CATEGORY_MUTE_DEAF;
                                }
                                else
                                {
                                    category = Constants.CATEGORY_NORMAL;
                                }

                                if(!image_url.equals("DEFAULT")) {
                                    remoteImageUrl = image_url;
                                    Picasso.get().load(Constants.SERVER_ADDRESS + "/media/profile/" + image_url).into(im_profileImage);
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            task.execute(uid, user.getPhoneNumber());
    }
    /*@Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);
        if(context instanceof HomeActivityInterface)
        {
           homeActivityInterface= (HomeActivityInterface) context;
            homeActivityInterface.hideAppBar();

        }
        else
        {
            throw new ClassCastException("Implement Interface");
        }
    }*/

    private void gotoLoginFragment() {
        ((PreHomeScreenActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragcontainer,new PhoneAuthFragment(), "phone auth")
                .commit();
    }
    private void uploadData() {


            RegisterTask task = new RegisterTask(((PreHomeScreenActivity) getContext()), new RegisterCallbackListener() {
                @Override
                public void registerCallback(String string) {
                    try {
                        System.out.println("Profile Path server" + string);

                        JSONObject jsonObject = new JSONObject(string);
                        if (jsonObject.getString("type").equals("success")) {

                            String serverPath = jsonObject.getString("profile");
                            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

                            Map<String, Object> fbData = new HashMap<>();
                            fbData.put("name", name);
                            fbData.put("handler", " ");
                            fbData.put("status", " ");
                            fbData.put("category", category);
                            fbData.put("phone", user.getPhoneNumber());
                            fbData.put("profile", serverPath);

                            firebaseFirestore.collection("users")
                                    .document(user.getPhoneNumber())
                                    .set(fbData)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            e.printStackTrace();
                                        }
                                    })
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            gotoPreferencesFragment();
                                        }
                                    })
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });


                        } else {
                            Toast.makeText(getContext(), "Failed to upload Profile", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            ProfileModel profileModel = new ProfileModel();
            profileModel.name = name;
            profileModel.cat = category;
            profileModel.imageUri = null;
            profileModel.command = imageAction;
            profileModel.status = status;
            profileModel.handler = handler;
            profileModel.phoneNumber = user.getPhoneNumber();
//        while(!isUidInitialized) {
////            System.out.println(isUidInitialized + " : ");
////        }
            profileModel.uid = uid;
       /* System.out.println("ud" + status);
        System.out.println("ud" + handler);*/
            task.execute(profileModel);
        }



    }
