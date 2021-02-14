package com.hrishita.difabled;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {//@link OtpFill#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OtpFillFragment extends Fragment {
    private String strPhoneNumber;
    private Button btnVerify, btnResend;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private PinView pinView;
    private String mVerificationID;
    private FirebaseAuth mAuth;
    AlertDialog dialog;
    Timer timer;
    int secondsPassed = 0;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    /*private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
*/
    // TODO: Rename and change types of parameters
    /*private String mParam1;
    private String mParam2;*/

    public OtpFillFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * //@param param1 Parameter 1.
     * //@param param2 Parameter 2.
     * @return A new instance of fragment OtpFill.
     */
    // TODO: Rename and change types and number of parameters
    public static OtpFillFragment newInstance(String param1, String param2) {
        OtpFillFragment fragment = new OtpFillFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_otp_fill,container,false);
        getPhoneNumber();

        mAuth=FirebaseAuth.getInstance();
        timer=new Timer();

        pinView=v.findViewById(R.id.pin_view);
        btnVerify=v.findViewById(R.id.btn_verify_otp);
        btnResend=v.findViewById(R.id.btn_resend_otp);
        btnVerify.setEnabled(false);


        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOtp();
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new AlertDialog.Builder(getActivity()).setCancelable(false).setView(R.layout.loading).create();
                dialog.show();
                PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(mVerificationID, Objects.requireNonNull(pinView.getText().toString()));
                signInWithPhoneAuthCredentials(phoneAuthCredential);
            }
        });

    mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            pinView.setText(phoneAuthCredential.getSmsCode());
            signInWithPhoneAuthCredentials(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            dialog.dismiss();
            Toast.makeText(getContext(), "Verification Failed", Toast.LENGTH_SHORT).show();
            ((PreHomeScreenActivity) getContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragcontainer, new PhoneAuthFragment(), "phone auth")
                    .commit();
            e.printStackTrace();
        }
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            btnVerify.setEnabled(true);
            mVerificationID = s;
        }
    };
        sendOtp();
        return v;
    }
    private void sendOtp() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(strPhoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity((PreHomeScreenActivity)getContext())                 // Activity (for callback binding)
                        .setCallbacks(mCallBacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


//        PhoneAuthProvider.getInstance().verifyPhoneNumber(strPhoneNumber,60, TimeUnit.SECONDS,((PreHomeScreenActivity) getContext()), mCallBacks);
        Toast.makeText(getContext(), "OTP Sent", Toast.LENGTH_LONG).show();
        wait60Seconds();
    }
    private void wait60Seconds() {
        btnResend.setEnabled(false);
        btnResend.setText(("Resend OTP in 60 seconds"));
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsPassed++;
                if(secondsPassed == 60) {
                    timer.cancel();
                    secondsPassed = 0;
//                    btnResend.setEnabled(true);
                }
                else {
                    btnResend.setText(("Resend OTP in " + (60 - secondsPassed) + " seconds"));
                }
            }
        },1000,1000);
    }
    private void signInWithPhoneAuthCredentials(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful() && task.isComplete()) {
                    final String phone_number = Objects.requireNonNull(task.getResult().getUser()).getPhoneNumber();
                    task.getResult().getUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull final Task<GetTokenResult> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()) {
                                System.out.println("success");
                                gotoRegisterProfileFragment();
                                                    }
                        }
                    });
                }  else {
                    Toast.makeText(getContext(), "Failed to verify profile number", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
private void gotoRegisterProfileFragment(){
        RegisterProfileFragment registerProfileFragment=new RegisterProfileFragment();
        registerProfileFragment.setArguments(getArguments());
    ((PreHomeScreenActivity) getContext())
            .getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragcontainer,new RegisterProfileFragment(), "profile")
            .addToBackStack(null)
            .commit();
}
    private void getPhoneNumber() {
        strPhoneNumber = getArguments().getString("phone");
        if(strPhoneNumber == null) {
            Toast.makeText(getContext(), "Failed to verify phone number", Toast.LENGTH_SHORT).show();
            ((PreHomeScreenActivity) getContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .remove(((PreHomeScreenActivity) getContext()).getSupportFragmentManager().findFragmentByTag("otp verify"))
                    .add(new PhoneAuthFragment(), "phone auth")
                    .commit();
        }
    }
}