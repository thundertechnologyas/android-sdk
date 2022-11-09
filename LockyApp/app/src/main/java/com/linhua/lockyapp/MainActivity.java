package com.linhua.lockyapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linhua.locky.Locky;
import com.linhua.locky.LockyEmailCallback;
import com.linhua.locky.LockyLocksCallback;
import com.linhua.locky.LockyTokenCallback;
import com.linhua.locky.api.ApiAuthManager;
import com.linhua.locky.bean.TokenModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText emailEditText;
    private Button startVerifyBtn;
    private EditText codeEditText;
    private Button verifyBtn;
    private TextView tokenTextView;
    private Button alLocksBtn;
    private LinearLayout locksLinearLayout;
    private Locky locky = new Locky();

    private TokenModel tokenModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        emailEditText = findViewById(R.id.et_email);
        startVerifyBtn = findViewById(R.id.btn_email);
        codeEditText = findViewById(R.id.et_code);
        verifyBtn = findViewById(R.id.btn_code);
        tokenTextView = findViewById(R.id.lb_token);
        alLocksBtn = findViewById(R.id.btn_locks);
        locksLinearLayout = findViewById(R.id.locks_linear_layout);
        startVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVerify();
            }
        });
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verify();
            }
        });

        alLocksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllLocks();
            }
        });

    }

    private void startVerify() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            return;
        }

        locky.startVerify(email, new LockyEmailCallback() {
            @Override
            public void onResponse(Object response) {

            }

            @Override
            public void onFailure() {

            }
        });
    }

    private void verify() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            return;
        }
        String code = codeEditText.getText().toString().trim();
        if (code.isEmpty()) {
            return;
        }
        locky.verify(email, code, new LockyTokenCallback() {
            @Override
            public void onResponse(Object response) {

            }

            @Override
            public void onFailure() {

            }
        });
    }

    private void getAllLocks() {
        locky.getAllLocks(new LockyLocksCallback() {
            @Override
            public void onResponse(ArrayList response) {

            }

            @Override
            public void onFailure() {

            }
        });
    }


    private View createLockView() {
        View view = LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.layout_lock, null, false);
        locksLinearLayout.addView(view);
        return view;
    }

}