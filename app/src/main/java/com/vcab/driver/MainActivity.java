package com.vcab.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.navigation.NavigationView;
import com.vcab.driver.fragments.HomeFragmentOld;
import com.vcab.driver.fragments.ProfileFragment;
import com.vcab.driver.fragments.SupportFragment;
import com.vcab.driver.fragments.TripsFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

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

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        navigationView = (NavigationView) findViewById(R.id.navigationView);
        //drawerLayout.openDrawer(GravityCompat.END);

        onSetNavigationDrawerEvents();

        displayFragment(new HomeFragmentOld());


    }

    private void displayFragment(Fragment fragment) {

        drawerLayout.closeDrawer(navigationView, true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();

      /*  new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(fragment.getClass().getSimpleName())
                        .commit();
            }
        }, 250);*/

    }

    private void onSetNavigationDrawerEvents() {


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
                drawerLayout.openDrawer(navigationView, true);
                break;
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

        }
    }


}