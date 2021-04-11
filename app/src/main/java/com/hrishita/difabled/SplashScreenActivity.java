package com.hrishita.difabled;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
            Thread thread=new Thread();
        SharedPreferences prefs;
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String cat=prefs.getString("category", "-1");
        if(cat.equals("-1"))
        {
            //first time user
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FirebaseUser user;
                    user= FirebaseAuth.getInstance().getCurrentUser();
                    if(user==null){
                        Intent intent=new Intent(SplashScreenActivity.this, PreHomeScreenActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Intent intent;
                        if (prefs.equals(Constants.CATEGORY_BLIND)){
                            intent = new Intent(SplashScreenActivity.this, BlindHomeActivity.class);
                        }
                        else{
                            intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                        }
                        startActivity(intent);


                    }
                }
            },3000);
        }
        else if(cat.equals(Constants.CATEGORY_BLIND))
        {
            //blind
            FirebaseUser user;
            user= FirebaseAuth.getInstance().getCurrentUser();
            if(user==null){
                Intent intent=new Intent(SplashScreenActivity.this, PreHomeScreenActivity.class);
                startActivity(intent);
            }
            else {
                Intent intent;
                intent = new Intent(SplashScreenActivity.this, BlindHomeActivity.class);
                startActivity(intent);
            }
        }
        else {
            FirebaseUser user;
            user= FirebaseAuth.getInstance().getCurrentUser();
            if(user==null){
                Intent intent=new Intent(SplashScreenActivity.this, PreHomeScreenActivity.class);
                startActivity(intent);
                finish();

            }
            else {
                Intent intent;
                intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();

            }
        }




    }
}