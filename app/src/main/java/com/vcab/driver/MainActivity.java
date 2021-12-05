package com.vcab.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.vcab.driver.fragments.HomeFragmentOld;
import com.vcab.driver.fragments.ProfileFragment;
import com.vcab.driver.fragments.SupportFragment;
import com.vcab.driver.fragments.TripsFragment;
import com.vcab.driver.model.DriverInfoModel;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    FrameLayout frameLayout;
    LinearLayout home_layout,trips_layout,profile_layout,support_layout,logout_layout;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolBar;
    TextView userName,phoneNumber;
    ImageView profile_pic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = findViewById(R.id.frameLayout);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        navigationView = (NavigationView) findViewById(R.id.navigationView);
        //drawerLayout.openDrawer(GravityCompat.END);
        toolBar = findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.menu_icon);
        toolBar.setNavigationIcon(drawable);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(navigationView, true);
            }
        });
        onSetNavigationDrawerEvents();

        displayFragment(new HomeFragmentOld());

        updateFirebaseToken();

        getDriverInformation();


    }

    private void getDriverInformation() {

        String fireStorePath="users/drivers/userData/"+FirebaseAuth.getInstance().getUid();
        DocumentReference docRef = FirebaseFirestore.getInstance().document(fireStorePath);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Messages_Common_Class.driverInfo = document.toObject(DriverInfoModel.class);
                        userName.setText(Messages_Common_Class.driverInfo.getName());
                        phoneNumber.setText(Messages_Common_Class.driverInfo.getPhone());
                        Picasso.get().load(Messages_Common_Class.driverInfo.getProfileImage()).placeholder(R.drawable.add_user_two).into(profile_pic);

                       // System.out.println("aaaaaa "+Messages_Common_Class.driverInfo.getPhone());
                    }
                }
            }
        });
    }

    private void displayFragment(Fragment fragment) {

        drawerLayout.closeDrawer(navigationView, true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack("HomeFragmentOld")
                        .commit();
            }
        }, 300);

    }

    public void updateFirebaseToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "token receive failed", Toast.LENGTH_SHORT).show();
                            return;
                        }



                        String refreshToken = task.getResult();

                        if (!refreshToken.equals(new SessionManagement().getFBToken(getApplicationContext()))) {

                            String fireStorePath="users/drivers/userData/"+FirebaseAuth.getInstance().getUid();

                            DocumentReference nycRef = FirebaseFirestore.getInstance().document(fireStorePath);
                            nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Map<String, Object> note = new HashMap<>();
                                        note.put("firebaseToken", refreshToken);

                                        nycRef.update(note).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                new SessionManagement().setFBToken(getApplicationContext(), refreshToken);
                                            }
                                        });

                                    }

                                }
                            });

                        }
                    }
                });
    }


    private void onSetNavigationDrawerEvents() {

        home_layout = (LinearLayout) findViewById(R.id.home_layout);
        trips_layout = (LinearLayout) findViewById(R.id.trips_layout);
        profile_layout = (LinearLayout) findViewById(R.id.profile_layout);
        support_layout = (LinearLayout) findViewById(R.id.support_layout);
        logout_layout = (LinearLayout) findViewById(R.id.logout_layout);

        userName =   findViewById(R.id.userName);
        phoneNumber =  findViewById(R.id.phoneNumber);
        profile_pic = findViewById(R.id.profile_pic);

        home_layout.setOnClickListener(this);
        trips_layout.setOnClickListener(this);
        profile_layout.setOnClickListener(this);
        support_layout.setOnClickListener(this);
        logout_layout.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){/*
            case R.id.drawerIcon:
                drawerLayout.openDrawer(navigationView, true);
                break;*/
            case R.id.home_layout:
                displayFragment(new HomeFragmentOld());
                break;
            case R.id.trips_layout:
                displayFragment(new TripsFragment());
                break;
            case R.id.profile_layout:
                displayFragment(new ProfileFragment());
                break;
            case R.id.support_layout:
                displayFragment(new SupportFragment());
                break;
            case R.id.logout_layout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this,SplashScreenActivity.class));
                finish();
                break;

        }
    }



}