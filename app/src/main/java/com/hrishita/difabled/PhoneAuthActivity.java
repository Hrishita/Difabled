package com.hrishita.difabled;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PhoneAuthActivity extends AppCompatActivity {
    RelativeLayout section1, section2;
    Button btnSendOTP, btnConfirmOTP;
    EditText phoneNumber, otpET;
    private String mVerificationID;
    private FirebaseAuth mAuth;
    TextView textView;
    SharedPreferences prefs;

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        createChannel();

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

       /* if(user!=null) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            if(!prefs.getBoolean("saved", false) || prefs.getBoolean("editing", false)) {
                openProfileFragment();
            }
            else {
                if(prefs.getBoolean("interest",false))
                    gotoChatActivity();
                else
                    gotoInterestFragment();
            }
        }
        else {
            //open phone auth fragment
            openPhoneAuthFragment();
        }*/
        openPhoneAuthFragment();

//        btnSendOTP = findViewById(R.id.btn_send_otp);
//        phoneNumber = findViewById(R.id.phone_number_et);
////        caption =findViewById(R.id.text_view_message);
//        otpET = findViewById(R.id.otp_et);
//        btnConfirmOTP = findViewById(R.id.btn_confirm_otp);
//        section1 = findViewById(R.id.phone_auth_section_1);
//        section2 = findViewById(R.id.phone_auth_section_2);
//
//        final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
//
//        btnConfirmOTP.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationID, otpET.getText().toString());
//                signInWithPhoneAuthCredentials(phoneAuthCredential);
//            }
//        });
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
//
//        btnSendOTP.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updateUI(STATE_INITIALIZED);
//                PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber.getText().toString(),60,TimeUnit.SECONDS,PhoneAuthActivity.this, mCallBacks);
//            }
//        });

    }

    private void gotoInterestFragment() {
        ((com.hrishita.difabled.PhoneAuthActivity.this))
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.phone_auth_base, new SelectIntrests(),"interests")
                .commit();
    }

    private void openPhoneAuthFragment() {
        Bundle bundle=new Bundle();
        PhoneAuthFragment phoneAuthFragment=new PhoneAuthFragment();
        phoneAuthFragment.setArguments(bundle);
        bundle.putString("category",getIntent().getExtras().getString("category"));
        getSupportFragmentManager().beginTransaction().replace(R.id.phone_auth_base, phoneAuthFragment, "phone auth").commit();
    }

    private void updateUI(int state) {
        switch (state) {
            case STATE_INITIALIZED:
                textView.setTextColor(Color.GRAY);
                textView.setText("Verifying phone number");
                btnSendOTP.setEnabled(false);
                break;
            case STATE_CODE_SENT :
                textView.setTextColor(Color.GRAY);
                textView.setText("Enter OTP sent to you phone number");
                section1.setVisibility(View.INVISIBLE);
                section2.setVisibility(View.VISIBLE);
                break;
            case STATE_VERIFY_FAILED:
                textView.setTextColor(Color.RED);
                textView.setText("Failed To verify phone number");
                section1.setVisibility(View.VISIBLE);
                section2.setVisibility(View.INVISIBLE);
                btnSendOTP.setEnabled(true);
                break;
            case STATE_VERIFY_SUCCESS:
                textView.setTextColor(Color.GREEN);
                textView.setText("Phone Number Verified");
                section1.setVisibility(View.INVISIBLE);
                section2.setVisibility(View.VISIBLE);
                break;
            case STATE_SIGNIN_FAILED:
                textView.setTextColor(Color.RED);
                textView.setText("Wrong OTP! try again");
                section1.setVisibility(View.INVISIBLE);
                section2.setVisibility(View.VISIBLE);
                break;
            case STATE_SIGNIN_SUCCESS:
                textView.setTextColor(Color.GREEN);
                textView.setText("Signed in:)");
                section1.setVisibility(View.INVISIBLE);
                section2.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            String name = "VOIP XMPP";
            String descriptionText = "Hellooooo uthay e";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel("jid", name, importance);
            mChannel.setDescription(descriptionText);
            mChannel.setShowBadge(true);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    private void openProfileFragment() {
        ((com.hrishita.difabled.PhoneAuthActivity.this))
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.phone_auth_base,new ProfileFragment(), "profile")
                .commit();
    }

    private void signInWithPhoneAuthCredentials(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful() && task.isComplete()) {
                    final String phone_number = task.getResult().getUser().getPhoneNumber();
                    task.getResult().getUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull final Task<GetTokenResult> task) {
                            if(task.isSuccessful()) {
                                String uid = task.getResult().getToken();
                                final LoginTask loginTask = new LoginTask(com.hrishita.difabled.PhoneAuthActivity.this, new ResponseListener() {
                                    @Override
                                    public void sendResponse(String jsonResponse) {
                                        try {
                                            System.out.println(jsonResponse);
                                            JSONObject jsonObject= new JSONObject(jsonResponse);
                                            if(jsonObject.getString("type").equals("success")) {
                                                updateUI(STATE_SIGNIN_SUCCESS);
                                                openProfileFragment();
                                            }
                                            else{
                                                //failure due to database problem
                                                Toast.makeText(com.hrishita.difabled.PhoneAuthActivity.this, "Failed To Connect With Database", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                loginTask.execute(uid, phone_number);
                            }

                        }
                    });
                }  else {
                    updateUI(STATE_SIGNIN_FAILED);
                }
            }
        });
    }

    private void gotoChatActivity() {
        Intent i = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(i);
        finish();
    }

    public class LoginTask extends AsyncTask<String, Void, String> {
        Context context;
        AlertDialog dialog;
        ResponseListener listener;
        LoginTask(Context context, ResponseListener responseListener) {
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
                runOnUiThread(new Runnable() {
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
    }
    public interface ResponseListener{
        void sendResponse(String jsonResponse);
    }
}
