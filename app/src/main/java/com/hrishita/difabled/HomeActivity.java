package com.hrishita.difabled;

import android.app.ActionBar;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hrishita.difabled.org.tensorflow.lite.examples.detection.DetectorActivity;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements HomeActivityInterface
{
    //CustomBottomNavigationView1 customBottomNavigationView1;
    FloatingActionButton fab;

    ArrayList<CompletePostData> arrayList = new ArrayList<>();
    int limit = 5;
    int big = 0;

    int lastViewedPost = 0;
    boolean arePostsAvailableOnServer = true;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private String uid;
    int currentId;
    private ChipNavigationBar navigationBar;
    private Fragment selectedFragment = null;
    SharedPreferences prefs;
      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

       getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_language_black_24dp);// set drawable icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        //  getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        createChannel();





        FirebaseMessaging.getInstance().subscribeToTopic(mUser.getPhoneNumber().substring(1))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });


        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);


        final AlertDialog dialog = new AlertDialog.Builder(com.hrishita.difabled.HomeActivity.this)
                .setCancelable(false)
                .setView(R.layout.loading)
                .create();

        dialog.show();
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if(task.isSuccessful()) {
                            uid = task.getResult().getToken();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initViews();
                                }
                            });

                        }
                        dialog.dismiss();
                    }
                });



    }

    protected void initViews() {
        //customBottomNavigationView1 = findViewById(R.id.customBottomBar);
       // fab = findViewById(R.id.fab);
       /* fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.home_activity_base, new AddPostFragment(), "add post")
                        .addToBackStack(null)
                        .commit();
            }
        });*/


 //       customBottomNavigationView1.inflateMenu(R.menu.bottom_menu);

        navigationBar = findViewById(R.id.customBottomBar);

        navigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i) {
                    case R.id.home:
                       // selectedFragment = new HomeFragment();
                        openPostFragment(false);
                        break;
                    case R.id.chat:
                       //  selectedFragment = new VideoCallFragment();
                        BottomModalVideoCall modal = new BottomModalVideoCall();
                        modal.show(getSupportFragmentManager(), "video call");
                        break;
                    case R.id.add_post:
                        // selectedFragment = new AddPostFragment();

                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.home_activity_base, new AddPostFragment(), "add post")
                                .addToBackStack(null)
                                .commit();
                        break;
                    case R.id.profile:
                      //  hideBottomNav();
                        //selectedFragment = new NotificationShowFragment();
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.home_activity_base, new HomeProfileFragment(), "home profile")
                                .addToBackStack(null)
                                .commit();
                        break;
                    case R.id.detect:
                        //selectedFragment = new ProfileFragment();
                        openNotificationFragment();

                        break;
                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_base,
                            selectedFragment).commit();
                }
            }
        });
      /*  currentId = R.id.action_post;
        openPostFragment(false);
        customBottomNavigationView1.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.action_post && currentId != R.id.action_post) {
                    currentId = R.id.action_post;
                    openPostFragment(false);
                }
                if(item.getItemId() == R.id.action_video_call && currentId != R.id.action_video_call)
                {
                    BottomModalVideoCall modal = new BottomModalVideoCall();
                    modal.show(getSupportFragmentManager(), "video call");
                }
                if(item.getItemId() == R.id.action_notification && currentId != R.id.action_notification) {
                    currentId = R.id.action_notification;
                    openNotificationFragment();
                }
                if(item.getItemId() == R.id.action_profile && currentId != R.id.action_profile)
                {
                    currentId = R.id.action_profile;
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_activity_base, new HomeProfileFragment(), "home profile")
                            .addToBackStack(null)
                            .commit();
                }
                return true;
            }
        });*/
    }

    private void openNotificationFragment() {
        NotificationFragment fragment = new NotificationFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_activity_base, fragment, "notification")
                .addToBackStack(null)
                .commit();
    }

    private void openPostFragment(boolean shouldContinue) {
         // hideBottomNav();
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        bundle.putInt("current", lastViewedPost);
        bundle.putBoolean("continue", shouldContinue);
//        if(shouldContinue) {
//            bundle.putParcelableArrayList("arraylist", arrayList);
//        }

        PostFragment fragment = new PostFragment();

        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_activity_base, fragment, "post fragment")
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
          if (item.getItemId()==android.R.id.home) {
              Intent i = new Intent(com.hrishita.difabled.HomeActivity.this, DetectorActivity.class);
              startActivity(i);

          }

        if(item.getItemId() == R.id.top_menu_search)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_activity_base, new SearchFragment(), "search")
                    .addToBackStack(null)
                    .commit();
        }
        if(item.getItemId() == R.id.top_menu_chat)
        {
            Intent i = new Intent(com.hrishita.difabled.HomeActivity.this, ChatActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void hideBottomNav() {
        navigationBar.setVisibility(View.GONE);
        //fab.hide();
    }

    @Override
    public void showBottomNav() {
        navigationBar.setVisibility(View.VISIBLE);
        //fab.show();
    }

    @Override
    public void hideAppBar() {
        getSupportActionBar().hide();
    }

    @Override
    public void showAppBar() {
        getSupportActionBar().show();
    }

    @Override
    public void changeCurrentFragment(int id) {
       navigationBar.setItemSelected(id,true);
        /*customBottomNavigationView1.setSelectedItemId(id);*/
    }

    public interface postFragmentCallbacks{
        int getLastItem();
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

}
