package com.eventssc.csci587.login;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class WelcomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);


        String welcomeMessage = getIntent().getExtras().get("UserId").toString();

        TextView welcomeTextView = (TextView) findViewById(R.id.txt_userId);
        welcomeTextView.setText("User Id = " + welcomeMessage);
    }
}
