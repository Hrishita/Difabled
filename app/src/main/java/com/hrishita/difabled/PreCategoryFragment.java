    package com.hrishita.difabled;




import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;





/*  ************************************************************
TO - DO
Add speech input to check if the user is blind
re open speech input dialogue on screen tap
insert category to db if blind
add intent accordingly (if blind - phone auth, if not select category fragment)
store category in shared preferences
enter in db after completing registration
category constants are in Constants class
Pre Home screen activity is the activity for all the initial fragments

  ************************************************************ */


/**
 * A simple {//@link Fragment} subclass.
 * Use the {@link PreCategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreCategoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
   /* private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";*/

    // TODO: Rename and change types of parameters
  /*  private String mParam1;
    private String mParam2;*/
    String catConst;
    private TextToSpeech tts;
    GifImageView gifImageView;
    RelativeLayout tapview ;
    public PreCategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * //@param param1 Parameter 1.
     * //@param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PreCategoryFragment newInstance(String param1, String param2) {
        PreCategoryFragment fragment = new PreCategoryFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {


           /* mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);*/


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_precategory, container, false);

        gifImageView = v.findViewById(R.id.gifImageView);
        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(new Locale("hin","IND"));

                    tts.setSpeechRate(0.9f);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    tts.setPitch(0.9f);

                } else {

                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               // speak("Hello Hrishita, welcome to the world of specially-abled people. Do you want to register as visually impaired person.");
                Bundle bundle = new Bundle();
                bundle.putString("category", catConst);
                System.out.println(catConst);
                gotoCategoryFragment(bundle);

            }
        }, 2000);
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_precategory, container, false);

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
    private void recognition(String text){

       /* if (text.contains("yes")|| text.contains("YES" )){
            Bundle bundle = new Bundle();
            bundle.putString("category", catConst);
            System.out.println(catConst);
            gotoCategoryFragment(bundle);

        }*/


        }
    private void gotoCategoryFragment(Bundle bundle) {
        SelectCategoryFragment frag = new SelectCategoryFragment();
        frag.setArguments(bundle);
        System.out.println(catConst);

        ((PreHomeScreenActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragcontainer,frag, "select category")
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
}