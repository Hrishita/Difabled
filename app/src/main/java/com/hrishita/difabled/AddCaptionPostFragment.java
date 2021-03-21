package com.hrishita.difabled;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddCaptionPostFragment extends Fragment {
    HomeActivityInterface mInterface;
    CarouselView carouselView;
    TextInputEditText caption, paymentUrl;
    TextView share;
    private ArrayList<String> arraylist;
    FirebaseUser user;
    String uid;

    public AddCaptionPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof HomeActivityInterface)
        {
            mInterface = (HomeActivityInterface) context;
            mInterface.showBottomNav();
            mInterface.showAppBar();
            mInterface.hideBottomNav();
            mInterface.hideAppBar();
        }
        else
        {
            throw new ClassCastException("Implement interface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_caption_post, container, false);
        user= FirebaseAuth.getInstance().getCurrentUser();

        share=  v.findViewById(R.id.add_caption_share);
        carouselView = v.findViewById(R.id.add_caption_preview_img);
        caption = v.findViewById(R.id.add_caption_et);
        paymentUrl = v.findViewById(R.id.add_caption_payment_link);
        extract();
        carouselView.setPageCount(arraylist.size());
        carouselView.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                Picasso.get()
                        .load(Uri.parse("file://" + arraylist.get(position)))
                        .error(R.drawable.default_profile)
                        .into(imageView);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(caption.getText().toString().equals(""))
                {
                    caption.setError("Enter a caption");
                }
                else
                {
                    user.getIdToken(true)
                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            @Override
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if(task.isSuccessful())
                                {
                                    UploadPostModel model = new UploadPostModel();
                                    model.caption=caption.getText().toString();
                                    model.paymentLink=paymentUrl.getText().toString();
                                    model.phone_number= user.getPhoneNumber();
                                    model.uid = task.getResult().getToken();
                                    model.uploading = true;
                                    model.locations = new String[arraylist.size()];
                                    for(int i = 0;i < arraylist.size(); i++)
                                    {
                                        model.locations[i] = arraylist.get(i);
                                    }
                                    UploadPostAsyncTask task1 = new UploadPostAsyncTask(getContext(), new UploadPostAsyncTask.UploadPostCallback() {
                                        @Override
                                        public void uploadpostresult(String s) {
                                            if(s == null) {
                                                ((HomeActivity)getContext()).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getContext(), "Failed to upload Post", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                try {
                                                    JSONObject object = new JSONObject(s);
                                                    String type = object.getString("type");
                                                    if(type.equals("success")) {
                                                        ((HomeActivity)getContext()).runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    } else {
                                                        ((HomeActivity)getContext()).runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getContext(), "Unable To Upload Post", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                } catch (Exception e) {
                                                    ((HomeActivity)getContext()).runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getContext(), "Unable To Upload Post", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    e.printStackTrace();
                                                }
                                                finally {
                                                    ((HomeActivity)getContext()).runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            ((HomeActivity)getContext()).getSupportFragmentManager().popBackStack();
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                                    task1.execute(model);
                                }
                            }
                        });
                }
            }
        });


        return v;
    }


    private void extract() {
        if(getArguments()!=null)
        {
            if(getArguments().getStringArrayList("selectedimages")!=null)
            {
                arraylist = getArguments().getStringArrayList("selectedimages");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mInterface.showAppBar();
        mInterface.showBottomNav();
    }
}
