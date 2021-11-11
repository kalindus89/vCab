package com.vcab.driver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Action;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_activity);


        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe(new Action() {
            @Override
            public void run() throws Throwable {

                MessagesClass.showToastMsg("Finished!",SplashScreenActivity.this);

            }
        });
    }
}