package com.thundertech.lockyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.thundertech.locky.Locky;
import com.thundertech.locky.bean.LockDevice;
import com.thundertech.locky.callback.LockyDataCallback;
import com.thundertech.locky.callback.LockyListCallback;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText emailEditText;
    private Button startVerifyBtn;
    private EditText codeEditText;
    private Button verifyBtn;
    private Button alLocksBtn;
    private LinearLayout locksLinearLayout;
    private Locky locky = new Locky();
    private ArrayList<LockDevice> lockDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locky.stop();
    }

    private void initView() {
        emailEditText = findViewById(R.id.et_email);
        startVerifyBtn = findViewById(R.id.btn_email);
        codeEditText = findViewById(R.id.et_code);
        verifyBtn = findViewById(R.id.btn_code);
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
        locky.verify(code, new LockyDataCallback() {
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
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int test = tag - 1;
                    String deviceId = lockDevices.get(tag - 1).getId();
                    locky.pulseOpen(deviceId);
                }
            });
        } else {
            button.setVisibility(View.GONE);
        }
        return view;
    }

}