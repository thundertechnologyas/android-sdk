package com.linhua.lockyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.linhua.locky.LockyActivity;

public class MainActivity extends AppCompatActivity {

    private TextView enterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        enterTextView = findViewById(R.id.tv_enter);
        enterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent lockyIntent = new Intent(MainActivity.this, LockyActivity.class);
                startActivity(lockyIntent);
            }
        });
    }

}