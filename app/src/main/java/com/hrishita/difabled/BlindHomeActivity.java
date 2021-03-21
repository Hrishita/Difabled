package com.hrishita.difabled;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Locale;

public class BlindHomeActivity extends AppCompatActivity {
    private TextToSpeech tts;
    ConstraintLayout tapid;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    SharedPreferences prefs;
    private String uid;
    int currentId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blind_home);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        tapid=findViewById(R.id.tapidblind);
            tapid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    speak(" ");
                    listen();
                }
            });
        tts = new TextToSpeech(BlindHomeActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(new Locale("Eng","IND"));

                    tts.setSpeechRate(1.0f);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    tts.setPitch(1.0f);
                    speak("Tap on the screen and speak object detection to see nearby objects, speak check message to view all latest message, speak read text to read, and speak write post to add post");
                } else {

                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });
    }
    private void recognition(String text){
            if (text.contains("object")||text.contains("detect")){
                Intent it=new Intent(BlindHomeActivity.this,TrycamActivity.class);
                startActivity(it);
            }
            if (text.contains("read")||text.contains("text")){
                Intent it=new Intent(BlindHomeActivity.this,Textrec.class);
                startActivity(it);
            }
            if(text.contains("post")||text.contains("write")){

            }
            if (text.contains("message")){

                Intent it=new Intent(BlindHomeActivity.this,ChatActivity.class);
                it.putExtra("category",getIntent().getStringExtra("category"));

                startActivity(it);
            }




        }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if (resultCode == -1 && null != data) {
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String inSpeech = res.get(0);
                recognition(inSpeech);
            }
        }
    }
    private void listen(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(BlindHomeActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
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
}