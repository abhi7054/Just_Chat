package com.example.android.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new android.os.Handler().postDelayed(new Runnable() {

            @Override

            public void run() {
                Intent mainActivityIntent = new Intent(SplashActivity.this, LoginActivity.class);


                startActivity(mainActivityIntent);



                finish();  // to close this activity

            }

        }, 6*100);
        }


    }

