package com.hrishita.difabled;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class OtpFragment extends Fragment {
    private String strPhoneNumber;
    private Button btnVerify, btnResend;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private PinView pinView;
    private String mVerificationID;
    private FirebaseAuth mAuth;
    private TextToSpeech tts;
    Timer timer;
    int secondsPassed = 0;
    String catconst=" ";
    Bundle b;
    Bundle bundle2;
    public OtpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_otp, container, false);
        getPhoneNumber();
       b=getArguments();
        catconst= b.getString("category");
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
                } else {

                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });
        mAuth = FirebaseAuth.getInstance();
        timer = new Timer();

        pinView = v.findViewById(R.id.pin_view);
        btnVerify = v.findViewById(R.id.btn_verify_otp);
        btnVerify.setEnabled(false);
        btnResend = v.findViewById(R.id.btn_resend_otp);

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOtp();
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationID, Objects.requireNonNull(pinView.getText()).toString());
                signInWithPhoneAuthCredentials(phoneAuthCredential);
            }
        });

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                updateUI(STATE_VERIFY_SUCCESS);
                pinView.setText(phoneAuthCredential.getSmsCode());
                signInWithPhoneAuthCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getContext(), "Verification Failed", Toast.LENGTH_SHORT).show();
                ((PreHomeScreenActivity) Objects.requireNonNull(getContext()))
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragcontainer, new PhoneAuthFragment(), "phone auth")
                        .commit();
                e.printStackTrace();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                updateUI(STATE_CODE_SENT);
                mVerificationID = s;
                btnVerify.setEnabled(true);
            }
        };
        sendOtp();

        return v;
    }

    private void sendOtp() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(strPhoneNumber,60, TimeUnit.SECONDS,((PreHomeScreenActivity) Objects.requireNonNull(getContext())), mCallBacks);
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
                    ((PreHomeScreenActivity)getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnResend.setEnabled(true);
                        }
                    });
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
                            if(task.isSuccessful()) {
                                System.out.println("jijaji jidabad"+catconst);

                                bundle2=new Bundle();
                                if(catconst.equals("4")){
                                    bundle2.putString("category", catconst);
                                    Toast.makeText(getContext(), "Authentication successful", Toast.LENGTH_SHORT).show();
                                    speak("Authentication Successful");
                                    openProfileFragment(bundle2);

                                }
                                else{
                                    bundle2.putString("category", catconst);

                                    openProfileFragment(bundle2);

                                }


//                                String uid = Objects.requireNonNull(task.getResult()).getToken();
//                                final LoginTask loginTask = new LoginTask(getContext(), new PreHomeScreenActivity.ResponseListener()
//                                {
//                                    @Override
//                                    public void sendResponse(String jsonResponse) {
//                                        try {
//                                            JSONObject jsonObject= new JSONObject(jsonResponse);
//                                            if(jsonObject.getString("type").equals("success")) {
////                                                updateUI(STATE_SIGNIN_SUCCESS);
//                                                openProfileFragment();
//                                            }
//                                            else{
//                                                //failure due to database problem
//                                                Toast.makeText(getContext(), "Failed To Connect With Database", Toast.LENGTH_SHORT).show();
//                                            }
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
//                                loginTask.execute(uid, phone_number);
                            }
                        }
                    });
                }  else {
                    Toast.makeText(getContext(), "Failed to verify profile number", Toast.LENGTH_SHORT).show();
//                    updateUI(STATE_SIGNIN_FAILED);
                }
            }
        });

    }

    private void openProfileFragment(Bundle bundle3) {
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("category", catconst).commit();
        if(bundle3.getString("category").equals("4")){
            bundle3.putString("category",catconst);
            System.out.println(("otp cat = " + catconst));

            System.out.println("category is = " + bundle3.getString("category"));
            ProfileFragmentBlind profileFragmentblind=new ProfileFragmentBlind();
            profileFragmentblind.setArguments(bundle3);
            timer.cancel();
            ((PreHomeScreenActivity) getContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragcontainer,profileFragmentblind, "profile")
                    .commit();
        }
        else{
            ProfileFragment profileFragment=new ProfileFragment();
            bundle3.putString("category",catconst);

            profileFragment.setArguments(bundle3);
            timer.cancel();
            ((PreHomeScreenActivity) getContext())
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragcontainer,profileFragment, "profile")
                    .commit();
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


    }

    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void getPhoneNumber() {
        strPhoneNumber = getArguments().getString("phone");
        if(strPhoneNumber == null) {
            if(catconst.contains("4")){
speak("Failed to verify phone number. tap on the screen and re-enter");
                ((PreHomeScreenActivity) getContext())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .remove(((PreHomeScreenActivity) getContext()).getSupportFragmentManager().findFragmentByTag("otp verify"))
                        .add(new PhoneAuthFragmentBlind(), "phone auth")
                        .commit();
            }else {
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
  /*  public class LoginTask extends AsyncTask<String, Void, String> {
        Context context;
        AlertDialog dialog;
        PreHomeScreenActivity.ResponseListener listener;
        LoginTask(Context context, PreHomeScreenActivity.ResponseListener responseListener) {
            this.context = context;
            this.listener = responseListener;
        }
        @Override
        protected void onPreExecute() {
            dialog = new AlertDialog.Builder(context).setView(R.layout.loading).setCancelable(false).create();
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            String jsonBody = "{" +
                    "\"uid\":\"" + strings[0] + "\"," +
                    "\"phone_number\":\"" + strings[1] + "\"" +
                    "}";
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);
            Request request = new Request.Builder().url(Constants.SERVER_ADDRESS + "/api/login").post(body).build();
            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                return responseBody;
            } catch (Exception e) {
                ((PreHomeScreenActivity)getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Failed To Connect To Server", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            dialog.dismiss();
            listener.sendResponse(string);
            super.onPostExecute(string);
        }
    }*/
    public interface ResponseListener{
        void sendResponse(String jsonResponse);
    }


    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();

        }
    }
}
