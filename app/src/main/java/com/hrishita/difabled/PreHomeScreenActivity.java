package com.hrishita.difabled;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

// Container of fragments - precategory, selectcat, phoneauth, otp,register
public class PreHomeScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_home_screen);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragcontainer, new PreCategoryFragment(), "select category").commit();

    }
}