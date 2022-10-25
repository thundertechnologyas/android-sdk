package com.linhua.lockyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.linhua.locky.Locky;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Locky().startEmail();
    }
}