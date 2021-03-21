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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhoneAuthFragment extends Fragment {

    TextInputEditText et_phoneNumber;
    Button btn_verifyNumber;

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    public PhoneAuthFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_phone_auth, container, false);
        et_phoneNumber = v.findViewById(R.id.phone_number_ti);
        btn_verifyNumber = v.findViewById(R.id.btn_verify_phone);

           btn_verifyNumber.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   gotoOtpScreen();
//                updateUI(STATE_INITIALIZED);
//                PhoneAuthProvider.getInstance().verifyPhoneNumber(et_phoneNumber.getText().toString(),60, TimeUnit.SECONDS,getContext(), mCallBacks);
               }
           });


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



        return v;
    }

    private void gotoOtpScreen() {
        OtpFragment otpFragment = new OtpFragment();
        Bundle bundle = new Bundle();
        bundle.putString("category",getArguments().getString("category"));
        bundle.putString("phone",et_phoneNumber.getText().toString());
        otpFragment.setArguments(bundle);
        ((PreHomeScreenActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragcontainer, otpFragment, "otp verify")
                .addToBackStack(null)
                .commit();
    }


}
