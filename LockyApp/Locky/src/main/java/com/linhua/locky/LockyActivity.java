package com.linhua.locky;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linhua.locky.api.ApiAuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LockyActivity extends AppCompatActivity {

    private static final String TAG = "LockyActivity";
    private EditText emailEditText;
    private Button startVerifyBtn;
    private EditText codeEditText;
    private Button verifyBtn;
    private TextView tokenTextView;
    private Button mobileKeysBtn;
    private Button alLocksBtn;
    private LinearLayout locksLinearLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locky);
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
    }

    private void startVerify() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            return;
        }
        Call call = ApiAuthManager.getInstance().getHttpApi().startVerify(email, "mobilekey");
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.v(TAG, "success");
                // successful
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.v(TAG, "error");
                // fail
            }
        });
    }


    private View createLockView() {
        View view = LayoutInflater.from(this.getApplicationContext()).inflate(R.layout.layout_lock, null, false);
        locksLinearLayout.addView(view);
        return view;
    }

}