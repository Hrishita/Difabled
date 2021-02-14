package com.hrishita.difabled;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.hrishita.difabled.AddPostFragment;
import com.hrishita.difabled.ChatFragment;
import com.hrishita.difabled.HomeFragment;
import com.hrishita.difabled.ObjectDetectFragment;
import com.hrishita.difabled.ProfileFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class MainActivity extends AppCompatActivity {
    private ChipNavigationBar navigationBar;
    private Fragment selectedFragment = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationBar = findViewById(R.id.bottom_chip_nav_bar);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        navigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i) {
                    case R.id.home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.chat:
                        selectedFragment = new ChatFragment();
                        break;
                    case R.id.add_post:
                        selectedFragment = new AddPostFragment();
                        break;
                    case R.id.profile:
                        selectedFragment = new ProfileFragment();
                        break;
                    case R.id.detect:
                        selectedFragment = new ObjectDetectFragment();
                        break;
                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                }
            }
        });
    }
}