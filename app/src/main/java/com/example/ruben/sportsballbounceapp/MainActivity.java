package com.example.ruben.sportsballbounceapp;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout StartTest = findViewById(R.id.buttonStartTest);
        StartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartTestActivity();
            }
        });
    }

    public void StartTestActivity(){
        Intent intent = new Intent(MainActivity.this,Football1Test.class);
        startActivity(intent);
    }
}
