package com.hrishita.difabled;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {/@link RegisterProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterProfileFragment extends Fragment {

    CircleImageView setProfileImageView;
    TextInputEditText txtHandler, txtName, txtEmail;
    Button save, editphoneno;
    String strname, strcat, strhand, stremail, uid,strcategory;
    SharedPreferences prefs;
    FirebaseAuth auth;
    FirebaseUser user;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    /*private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";*/

    // TODO: Rename and change types of parameters
   /* private String mParam1;
    private String mParam2;*/

    public RegisterProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p>
     * //@param param1 Parameter 1.
     * //@param param2 Parameter 2.
     *
     * @return A new instance of fragment RegisterProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterProfileFragment newInstance(String param1, String param2) {
        RegisterProfileFragment fragment = new RegisterProfileFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  /*      if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register_profile, container, false);
        setProfileImageView = v.findViewById(R.id.profile_my_image);
        save = v.findViewById(R.id.btn_profile_save);
        editphoneno = v.findViewById(R.id.btn_profile_edit_number);
        txtHandler = v.findViewById(R.id.profile_handler);
        txtEmail = v.findViewById(R.id.profile_status);
        txtName = v.findViewById(R.id.profile_name);
        prefs = ((PreHomeScreenActivity) getContext()).getSharedPreferences("Mypres", Context.MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (getArguments() != null) {
            strcategory = getArguments().getString("category");
        }
        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    uid = task.getResult().getToken();
                }
                fillUI();
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        editphoneno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return inflater.inflate(R.layout.fragment_register_profile, container, false);
    }

    private void fillUI() {
        //load data from server
        final GetProfileTask task = new GetProfileTask(((PreHomeScreenActivity) getContext()), new GetProfileTask.ProfileReceivedListener() {
            @Override
            public void takeProfile(String body) {
                try {
                    JSONObject object = new JSONObject(body);
                    String type = object.getString("type");
                    if (type.equals("success")) {
                        int rowCount = Integer.parseInt(object.getString("no_of_rows"));
                        JSONArray array = object.getJSONArray("rows");
                        for (int i = 0; i < rowCount; i++) {
                            JSONObject object1 = array.getJSONObject(i);
                            strname = object1.getString("u_name");
                            String image_url = object1.getString("u_profile_link");
                            strcat = object1.getString("u_category");
                            //todo init handler and status from server

                            txtName.setText(strname);

                            if (Constants.CATEGORY_MUTE.equals(strcat)) {
                                strcat = Constants.CATEGORY_MUTE;
                            } else if (Constants.CATEGORY_BLIND.equals(strcat)) {
                                strcat = Constants.CATEGORY_BLIND;
                            } else {
                                strcat = Constants.CATEGORY_NORMAL;
                            }

                            if (!image_url.equals("DEFAULT")) {
                                String remoteImageUrl = image_url;
                                Picasso.get().load(Constants.SERVER_ADDRESS + "/media/profile/" + image_url).into(setProfileImageView);
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

    private void uploadData() {
        RegisterTask task = new RegisterTask(((PreHomeScreenActivity) getContext()), new RegisterCallbackListener() {
            @Override
            public void registerCallback(String string) {
                System.out.println(string);
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    if (jsonObject.getString("type").equals("success")) {
                        prefs.edit().putString("name", txtName.getText().toString()).putString("handler", txtHandler.getText().toString()).putString("email", txtEmail.getText().toString()).putString("phone", user.getPhoneNumber()).putString("category", strcategory).commit();
                        Toast.makeText(getContext(), "Successfulshit", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to upload Profile", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}