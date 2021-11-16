package com.vcab.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.vcab.driver.fragments.HomeFragment;
import com.vcab.driver.fragments.ProfileFragment;
import com.vcab.driver.fragments.SupportFragment;
import com.vcab.driver.fragments.TripsFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    FrameLayout frameLayout;
    LinearLayout home_layout,trips_layout,profile_layout,support_layout;
    ImageView drawerIcon;
    DrawerLayout drawerLayout;
    NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = findViewById(R.id.frameLayout);

        drawerIcon = (ImageView) findViewById(R.id.drawerIcon);
        drawerIcon.setOnClickListener(this);


        displayFragment(new HomeFragment());

        onSetNavigationDrawerEvents();

    }

    private void displayFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(frameLayout.getId(), fragment)
                .commit();
    }

    private void onSetNavigationDrawerEvents() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navigationView);

        //drawerLayout.openDrawer(GravityCompat.END);

        home_layout = (LinearLayout) findViewById(R.id.home_layout);
        trips_layout = (LinearLayout) findViewById(R.id.trips_layout);
        profile_layout = (LinearLayout) findViewById(R.id.profile_layout);
        support_layout = (LinearLayout) findViewById(R.id.support_layout);

        home_layout.setOnClickListener(this);
        trips_layout.setOnClickListener(this);
        profile_layout.setOnClickListener(this);
        support_layout.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.drawerIcon:
                MessagesClass.showToastMsg("aaa",this);
                drawerLayout.openDrawer(navigationView, true);
                break;
            case R.id.home_layout:
                drawerLayout.closeDrawer(navigationView, true);
                displayFragment(new HomeFragment());
                break;
            case R.id.trips_layout:
                drawerLayout.closeDrawer(navigationView, true);
                displayFragment(new TripsFragment());
                break;
            case R.id.profile_layout:
                drawerLayout.closeDrawer(navigationView, true);
                displayFragment(new ProfileFragment());
                break;
            case R.id.support_layout:
                drawerLayout.closeDrawer(navigationView, true);
                displayFragment(new SupportFragment());
                break;

        }
    }
}