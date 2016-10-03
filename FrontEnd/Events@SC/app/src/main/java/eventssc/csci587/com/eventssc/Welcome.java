package eventssc.csci587.com.eventssc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        String welcomeMessage = getIntent().getStringExtra("welcome_message");
        TextView welcomeTextView = (TextView) findViewById(R.id.welcome_textview);
        welcomeTextView.setText(welcomeMessage);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

}
