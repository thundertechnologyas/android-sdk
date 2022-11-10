package com.linhua.lockyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.linhua.locky.Locky;
import com.linhua.locky.bean.LockDevice;
import com.linhua.locky.callback.LockyDataCallback;
import com.linhua.locky.callback.LockyListCallback;
import com.linhua.locky.bean.TokenModel;

import java.util.ArrayList;

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
    private ArrayList<LockDevice> lockDevices;

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

        locky.startVerify(email, new LockyDataCallback() {
            @Override
            public void onSuccess(Object response) {

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
        locky.verify(email, code, new LockyDataCallback() {
            @Override
            public void onSuccess(Object response) {

            }

            @Override
            public void onFailure() {

            }
        });
    }

    private void getAllLocks() {
        locky.getAllLocks(new LockyListCallback() {
            @Override
            public void onSuccess(ArrayList response) {
                lockDevices = response;
                createLockView();
            }

            @Override
            public void onFailure() {

            }
        });
    }


    private void createLockView() {
        locksLinearLayout.removeAllViews();
        for (int k = 0; k < lockDevices.size(); k++) {
            View view = createLockItemView(lockDevices.get(k), k + 1);
            locksLinearLayout.addView(view);
        }
        locksLinearLayout.requestLayout();
    }

    private View createLockItemView(LockDevice device, int tag) {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_lock, null);
        view.setTag(tag);
        TextView textView = view.findViewById(R.id.label_name);
        Button button = view.findViewById(R.id.btn_pulse_open);
        textView.setText(device.getName());
        if (device.getHasBLE()) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
        return view;
    }

}