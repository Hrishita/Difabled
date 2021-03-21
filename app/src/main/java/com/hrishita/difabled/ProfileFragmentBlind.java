package com.hrishita.difabled;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragmentBlind extends Fragment {
    private Button btn_save,btn_edit;
    private TextInputEditText ti_name, ti_handler, ti_status;
   // private CircleImageView im_profileImage;
   // private Uri imageLocation;
    //private String remoteImageUrl;
    //private String imageAction = Constants.IMAGE_UPLOAD_EXISTING;
RelativeLayout touchrelate;
    //states
    FirebaseAuth auth;
    FirebaseUser user;
    private static int STATE_REQUEST_RECVD = 0;
    private static int STATE_REQUEST_SENT = 1;
    private static int STATE_REQUEST_SELF = 2;
    private static int STATE_REQUEST_DEFAULT = 3;
    private static int STATE_FRIENDS = 4;

    private String requestedUserPhone = null;
    private int RC_PROFILE = 0;
    private TextToSpeech tts;
    private String uid;
    private String name;
    /*private String handler;
    private String status;*/
    private String category;

  //  private ImageView dialogImage;

    public ProfileFragmentBlind() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_profile_blind, container, false);
        btn_save = v.findViewById(R.id.btn_profile_save);
        ti_name = v.findViewById(R.id.profile_name);
      /*  ti_handler = v.findViewById(R.id.profile_handler);
        ti_status = v.findViewById(R.id.profile_status);
*/
      /*  im_profileImage = v.findViewById(R.id.profile_my_image);
        btn_edit = v.findViewById(R.id.btn_profile_edit_number);
*/
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        touchrelate=v.findViewById(R.id.touchblind);
        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(new Locale("Eng","IND"));

                    tts.setSpeechRate(0.9f);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    tts.setPitch(0.9f);
                    speak("Tap on the screen and say you name");
                } else {

                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });
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
touchrelate.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        listen();
    }
});
       /* im_profileImage.setOnClickListener(new View.OnClickListener() {
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
        });*/

  /*      btn_edit.setOnClickListener(new View.OnClickListener() {
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
*/


        /*btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ti_name.getText().toString().equals("")) {
                    ti_name.setError("Enter Username");
                }
                *//*else if(ti_handler.getText().toString().equals("")) {
                    ti_handler.setError("Enter email");
                }
                else if(ti_status.getText().toString().equals("")) {
                    ti_status.setError("Enter Status");
                }*//*
                else {
                    Bundle bundle = new Bundle();
                    bundle.putString("name", ti_name.getText().toString());
                    bundle.putString("handler"," ");
                    bundle.putString("status", " ");
                    bundle.putString("category", category);
                    bundle.putString("uid", uid);
                  //  bundle.putString("command", imageAction);
                   *//* System.out.println("pf" + ti_status.getText().toString());
                    System.out.println("pf" + ti_handler.getText().toString());*//*
                 //   bundle.putString("image", imageLocation == null ? null : imageLocation.toString());
                    gotoPreferencesFragment();
                  //  gotoCategoryFragment(bundle);
                }
            }
        });*/

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
        if(requestCode == 100){
            if (resultCode == -1 && null != data) {
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String inSpeech = res.get(0);
                recognition(inSpeech);
            }
        }
        if(requestCode == RC_PROFILE && resultCode == Activity.RESULT_OK) {
            /*if(data!=null) {
                imageLocation = data.getData();
                if(dialogImage != null) {
                    dialogImage.setImageURI(imageLocation);
                    imageAction = Constants.IMAGE_UPLOAD_NEW;
                } else {
                    Toast.makeText(getContext(), "NULL", Toast.LENGTH_SHORT).show();
                }
                im_profileImage.setImageURI(imageLocation);
            }*/
        }
    }

    private void recognition(String text){
        if(text.contains("ok")){
            if(ti_name.getText().toString().equals("")) {
               speak("Please tap on the screen and enter your name");
            }
            else {
                Bundle bundle = new Bundle();
                name = ti_name.getText().toString();
                bundle.putString("name", ti_name.getText().toString());
                bundle.putString("handler"," ");
                bundle.putString("status", " ");
                bundle.putString("category", category);
                bundle.putString("uid", uid);

                uploadData();

                //  bundle.putString("command", imageAction);
             /*   System.out.println("pf" + ti_status.getText().toString());
                System.out.println("pf" + ti_handler.getText().toString());*/
                //   bundle.putString("image", imageLocation == null ? null : imageLocation.toString());
                //gotoPreferencesFragment();
                //  gotoCategoryFragment(bundle);
            }
        }
        else{
            ti_name.setText(text);
            speak("your name is "+ti_name.getText()+". say ok to continue");
        }


    }

    private void gotoPreferencesFragment() {
        ((PreHomeScreenActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragcontainer, new SelectIntrests(),"interests")
                .commit();}
    private void gotoCategoryFragment(Bundle bundle) {
        SelectCategoryFragment frag = new SelectCategoryFragment();
        frag.setArguments(bundle);
        ((PreHomeScreenActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragcontainer,frag, "select category")
                .commit();
    }

    private void fillUI() {
        //check if data passed from other fragment
        if(getArguments()!=null) {
                requestedUserPhone = user.getPhoneNumber();
                category= (getArguments().getString("category"));
                System.out.println("blind cat = " + category);
              /*  handler = (getArguments().getString("handler"));
                status = (getArguments().getString("status"));
*/
                ti_name.setText(name);
               /* ti_handler.setText(handler);
                ti_status.setText(status);*/

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
                                //String image_url = object1.getString("u_profile_link");
                                category = object1.getString("u_category");
                               /* status = object1.getString("status");
                                handler = object1.getString("email");*/

                                ti_name.setText(name);
                              /*  ti_handler.setText(handler);
                                ti_status.setText(status);*/

                                if(Constants.CATEGORY_MUTE_DEAF.equals(category))
                                {
                                    category = Constants.CATEGORY_MUTE_DEAF;
                                }
                                else if(Constants.CATEGORY_BLIND.equals(category)){
                                    category=Constants.CATEGORY_BLIND;
                                }
                                else
                                {
                                    category = Constants.CATEGORY_NORMAL;
                                }

                               /* if(!image_url.equals("DEFAULT")) {
                                    remoteImageUrl = image_url;
                                    Picasso.get().load(Constants.SERVER_ADDRESS + "/media/profile/" + image_url).into(im_profileImage);
                                }*/
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

    private void gotoLoginFragment() {
        ((PreHomeScreenActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragcontainer,new PhoneAuthFragment(), "phone auth")
                .commit();
    }




    private void listen(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getContext(), "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();

        }
        super.onDestroy();
    }
    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void gotoHome() {

        Intent i = new Intent(getContext(), BlindHomeActivity.class);
        i.putExtra("category",getArguments().getString("category"));
        startActivity(i);
        ((PreHomeScreenActivity)getContext()).finish();
    }
    private void uploadData() {

        RegisterTask task = new RegisterTask(((PreHomeScreenActivity) getContext()), new RegisterCallbackListener() {
            @Override
            public void registerCallback(String string) {
                try {
                    System.out.println("Profile Path server" + string);

                    JSONObject jsonObject = new JSONObject(string);
                    if(jsonObject.getString("type").equals("success")) {

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
                                        gotoHome();
                                    }
                                })
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });


                    }
                    else {
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
        profileModel.imageUri =null;
        profileModel.command = Constants.IMAGE_UPLOAD_REMOVE;
        profileModel.status = " ";
        profileModel.handler = " ";
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
