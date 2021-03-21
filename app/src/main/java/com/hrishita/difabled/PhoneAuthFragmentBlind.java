package com.hrishita.difabled;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhoneAuthFragmentBlind extends Fragment {

    TextInputEditText et_phoneNumber;
    Button btn_verifyNumber;
    private TextToSpeech tts;

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;
    RelativeLayout blindtouch;
    public PhoneAuthFragmentBlind() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_phone_auth_blind, container, false);
        et_phoneNumber = v.findViewById(R.id.phone_number_ti);
        btn_verifyNumber = v.findViewById(R.id.btn_verify_phone);
       blindtouch =v.findViewById(R.id.touchid);
    v.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            speak(" ");
            listen();
        }
    });
          /* btn_verifyNumber.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   gotoOtpScreen();
//                updateUI(STATE_INITIALIZED);
//                PhoneAuthProvider.getInstance().verifyPhoneNumber(et_phoneNumber.getText().toString(),60, TimeUnit.SECONDS,getContext(), mCallBacks);
               }
           });
       */
       
//        final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
//
//        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//            @Override
//            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                updateUI(STATE_VERIFY_SUCCESS);
//                otpET.setText(phoneAuthCredential.getSmsCode());
//                signInWithPhoneAuthCredentials(phoneAuthCredential);
//            }
//
//            @Override
//            public void onVerificationFailed(@NonNull FirebaseException e) {
//                updateUI(STATE_VERIFY_FAILED);
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                updateUI(STATE_CODE_SENT);
//                mVerificationID = s;
//            }
//        };

        tts = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(new Locale("Eng","IND"));

                    tts.setSpeechRate(1.0f);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    tts.setPitch(1.0f);
                    speak("Please tap on the screen and speak your mobile number");
                } else {

                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });

        return v;
    }

    private void gotoOtpScreen() {
        OtpFragment otpFragment = new OtpFragment();
        Bundle bundle = new Bundle();
        bundle.putString("category", getArguments().getString("category"));
        bundle.putString("phone","+91"+et_phoneNumber.getText().toString()/*"+16505551234"*/);
        otpFragment.setArguments(bundle);
        ((PreHomeScreenActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragcontainer, otpFragment, "otp verify")
                .addToBackStack(null)
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
       if(text.contains("done")||text.contains("don")||text.contains("dan")||text.contains("dun")||text.contains("none")||text.contains("dan")){

            gotoOtpScreen();

        }
       else{
           et_phoneNumber.setText(text);
           speak("your number is "+et_phoneNumber.getText().toString()+"speak done to continue . Tap and speak number again to edit.");

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
