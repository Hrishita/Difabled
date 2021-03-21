package com.hrishita.difabled;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import  android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class SelectCategoryFragment extends Fragment {
    ImageView disabled, normal;
    boolean isDisabled = false;
    Button btn_continue, btn_previous;
    TextView textView;
    String catConst=" ";

    private String name;
    private String handler;
    private String status;
    private Uri imageData;
    private String uid;
    private String command;

    FirebaseAuth auth;
    FirebaseUser user;
    boolean isUidInitialized = false;
    SharedPreferences prefs;

    public SelectCategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_select_category, container, false);;
        disabled = v.findViewById(R.id.sc_disabled);
        normal = v.findViewById(R.id.sc_normal);
        btn_continue = v.findViewById(R.id.btn_sc_continue);
        textView = v.findViewById(R.id.text_view_select_category);

        prefs = ((PreHomeScreenActivity)getContext()).getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

       // extractData();
       // textView.setText(("Hi " + name + ",\n Select your category\n"));

        disabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDisabled = true;
                catConst=Constants.CATEGORY_MUTE_DEAF;
                disabled.setImageResource(R.drawable.deaflogo);
                normal.setImageResource(R.drawable.transnormlogo);
            }
        });
        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDisabled = false;
                catConst=Constants.CATEGORY_NORMAL;
                disabled.setImageResource(R.drawable.transdeaflogo);
                normal.setImageResource(R.drawable.normallogo);
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(catConst.contains(" ")){
                   Toast.makeText(getContext(), "Please select category", Toast.LENGTH_SHORT).show();
               }
               else{
                   Bundle bundle = new Bundle();
                   bundle.putString("category", catConst);
                   System.out.println(catConst);
                   gotoPhoneAuthFragment(bundle);
               }

                /*if(name==null) {
                    Toast.makeText(getContext(), "Failed to upload name", Toast.LENGTH_SHORT).show();
                } else if(status == null) {
                    Toast.makeText(getContext(), "Failed to upload status", Toast.LENGTH_SHORT).show();
                } else if(handler == null) {
                    Toast.makeText(getContext(), "Failed to upload handler", Toast.LENGTH_SHORT).show();
                } else {
                    uploadData();
                }
            }*/
            }
        });

     /*   btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfile();
            }
        });
*/
        return v;
    }
    private void gotoPhoneAuthFragment(Bundle bundle) {
        PhoneAuthFragment frag = new PhoneAuthFragment();
        frag.setArguments(bundle);
        System.out.println(catConst);

        ((PreHomeScreenActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragcontainer,frag, "Phone Auth")
                .commit();
    }
/*    private void extractData() {
        if(getArguments()!=null) {
            name = getArguments().getString("name");
            command = getArguments().getString("command");
            handler = getArguments().getString("handler");
            status = getArguments().getString("status");

            imageData = getArguments().getString("image") != null ? Uri.parse(getArguments().getString("image")) : null;
            uid = getArguments().getString("uid");
            if(getArguments().getString("category")!=null) {
                isDisabled = getArguments().getString("category").equals(Constants.CATEGORY_MUTE_DEAF);
                if(isDisabled) {
                    disabled.setImageResource(R.drawable.deaflogo);
                } else {
                    normal.setImageResource(R.drawable.normallogo);
                }
            }
        }
    }*/

    /*private void uploadData() {
        final String cat = isDisabled ? Constants.CATEGORY_MUTE_DEAF : Constants.CATEGORY_NORMAL;
        RegisterTask task = new RegisterTask(((PhoneAuthActivity) getContext()), new RegisterCallbackListener() {
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
                        fbData.put("handler", handler);
                        fbData.put("status", status);
                        fbData.put("category", cat);
                        fbData.put("phone", user.getPhoneNumber());


                        if(command.equals(Constants.IMAGE_UPLOAD_EXISTING))
                        {
                            //update
                            fbData.put("profile", "DEFAULT");
                            firebaseFirestore.collection("users").document(user.getPhoneNumber()).set(fbData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    prefs.edit().putString("name", name).putString("handler", handler).putString("status", status).putString("phone", user.getPhoneNumber()).putString("category", isDisabled ? Constants.CATEGORY_MUTE_DEAF : Constants.CATEGORY_NORMAL).putBoolean("saved", true).putBoolean("editing", false).commit();
                                    gotoPreferencesFragment();
                                }
                            });
                        }
                        else{
                            if(command.equals(Constants.IMAGE_UPLOAD_REMOVE))
                            {
                                //remove
                                serverPath = "DEFAULT";
                            }
                            fbData.put("profile", serverPath);
                            firebaseFirestore.collection("users").document(user.getPhoneNumber()).set(fbData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    prefs.edit().putString("name", name).putString("handler", handler).putString("status", status).putString("phone", user.getPhoneNumber()).putString("category", isDisabled ? Constants.CATEGORY_MUTE_DEAF : Constants.CATEGORY_NORMAL).putBoolean("saved", true).putBoolean("editing", false).commit();
                                    gotoPreferencesFragment();
                                }
                            });
                        }


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
        profileModel.cat = cat;
        profileModel.imageUri = imageData;
        profileModel.command = command;
        profileModel.status = status;
        profileModel.handler = handler;
        profileModel.phoneNumber = user.getPhoneNumber();
//        while(!isUidInitialized) {
////            System.out.println(isUidInitialized + " : ");
////        }
        profileModel.uid = uid;
        System.out.println("ud" + status);
        System.out.println("ud" + handler);
        task.execute(profileModel);
    }*/

 /*   private void gotoPreferencesFragment() {
        ((PhoneAuthActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.phone_auth_base, new SelectIntrests(),"interests")
                .commit();
    }
*/
   /* private void editProfile() {
        Bundle b = getArguments();
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(b);
        ((PhoneAuthActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.phone_auth_base, fragment,"profile")
                .commit();
    }*/

}