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

import com.linhua.locky.api.ApiAuthManager;
import com.linhua.locky.bean.TokenModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LockyActivity";
    private EditText emailEditText;
    private Button startVerifyBtn;
    private EditText codeEditText;
    private Button verifyBtn;
    private TextView tokenTextView;
    private Button mobileKeysBtn;
    private Button alLocksBtn;
    private LinearLayout locksLinearLayout;

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
        mobileKeysBtn = findViewById(R.id.btn_keys);
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

        mobileKeysBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMobileKeys();
            }
        });

        startVerifyBtn.setOnClickListener(new View.OnClickListener() {
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
        Call<Void> call = ApiAuthManager.getInstance().getHttpApi().startVerify(email, "mobilekey");
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.v(TAG, "success");
                // successful
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.v(TAG, "error");
                // fail
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

        Call<TokenModel> call = ApiAuthManager.getInstance().getHttpApi().verify(email, code, "mobilekey");
        call.enqueue(new Callback<TokenModel>() {
            @Override
            public void onResponse(Call<TokenModel> call, Response<TokenModel> response) {
                tokenModel = response.body();
            }

            @Override
            public void onFailure(Call<TokenModel> call, Throwable t) {

            }
        });
    }

    private void getMobileKeys() {

    }

    private void getAllLocks() {

    }


    private View createLockView() {
        View view = LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.layout_lock, null, false);
        locksLinearLayout.addView(view);
        return view;
    }

}