package com.hrishita.difabled;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BlindPost extends AppCompatActivity {
    HomeActivityInterface mInterface;
    CarouselView carouselView;
    TextView caption;
    TextView share;
    private ArrayList<String> arraylist;
    FirebaseUser user;
    ConstraintLayout tapp;
    String uid;
    TextView txt;
    Boolean shareflag=false;
    private TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blind_post);

        tts = new TextToSpeech(BlindPost.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(new Locale("Eng", "IND"));

                    tts.setSpeechRate(1.0f);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    tts.setPitch(1.0f);
                    speak("Tap on the screen and speak the message you want to post." );
                } else {

                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });
        txt=findViewById(R.id.txtcaption);
        tapp=findViewById(R.id.taphere);
        tapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak(" ");
                listen();
                shareflag=true;
            }
        });

        tapp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listen();
                return true;

            }
        });

        user= FirebaseAuth.getInstance().getCurrentUser();
     //   share=  v.findViewById(R.id.add_caption_share);
        //carouselView = v.findViewById(R.id.add_caption_preview_img);
        caption = findViewById(R.id.txtcaption);
       // paymentUrl = v.findViewById(R.id.add_caption_payment_link);
      //  extract();

    }

    private void recognition(String text) {
        txt.setText(text);
        speak("your post content is "+text+". long press on screen and speak share to post.");
        if(text.equals("share")&& shareflag){

                    if(caption.getText().toString().equals(""))
                    {
                        speak("Enter content for post");
                        listen();
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
                                            model.paymentLink=" ";
                                            model.phone_number= user.getPhoneNumber();
                                            model.uid = task.getResult().getToken();
                                            model.uploading = true;
                                            model.locations = new String[]{};
                                           /* for(int i = 0;i < arraylist.size(); i++)
                                            {
                                                model.locations[i] = arraylist.get(i);
                                            }*/
                                            UploadPostAsyncTask task1 = new UploadPostAsyncTask(BlindPost.this, new UploadPostAsyncTask.UploadPostCallback() {
                                                @Override
                                                public void uploadpostresult(String s) {
                                                    if(s == null) {
                                                        BlindPost.this.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(BlindPost.this, "Failed to upload Post", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    } else {
                                                        try {
                                                            JSONObject object = new JSONObject(s);
                                                            String type = object.getString("type");
                                                            if(type.equals("success")) {
                                                                BlindPost.this.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Toast.makeText(BlindPost.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            } else {
                                                                BlindPost.this.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Toast.makeText(BlindPost.this, "Unable To Upload Post", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        } catch (Exception e) {
                                                            BlindPost.this.runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(BlindPost.this, e+"Unable To Upload Post", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                            e.printStackTrace();
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


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == -1 && null != data) {
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String inSpeech = res.get(0);
                recognition(inSpeech);
            }
        }
    }

    private void listen() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(BlindPost.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
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

    private void speak(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onResume() {
        speak(" ");
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        speak(" ");
        super.onRestart();
    }
    private void extract() {
       /* if(getArguments()!=null)
        {
            if(getArguments().getStringArrayList("selectedimages")!=null)
            {
                arraylist = getArguments().getStringArrayList("selectedimages");
            }
        }*/
    }


        }
