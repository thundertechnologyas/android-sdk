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
import com.thundertech.locky.callback.LockyEventCallback;
import com.thundertech.locky.callback.LockyListCallback;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText codeEditText;
    private LinearLayout locksLinearLayout;
    private Locky locky;
    private ArrayList<LockDevice> lockDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        locky = new Locky(this);
//        if want to know the progress, we can should use event callback.
//        locky = new Locky(this, (deviceId, event) -> {
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locky.stop();
    }

    private void initView() {
        emailEditText = findViewById(R.id.et_email);
        Button startVerifyBtn = findViewById(R.id.btn_email);


        codeEditText = findViewById(R.id.et_code);
        Button verifyBtn = findViewById(R.id.btn_code);
        Button alLocksBtn = findViewById(R.id.btn_locks);
        locksLinearLayout = findViewById(R.id.locks_linear_layout);
        startVerifyBtn.setOnClickListener(view -> startVerify());

        verifyBtn.setOnClickListener(view -> verify());
        alLocksBtn.setOnClickListener(view -> getAllLocks());
    }

    private void startVerify() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            return;
        }

        locky.startVerify(email, new LockyDataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

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
        locky.verify(code, new LockyDataCallback<String>() {
            @Override
            public void onSuccess(String token) {

            }

            @Override
            public void onFailure() {

            }
        });
    }

    private void getAllLocks() {
        locky.getAllLocks(new LockyListCallback<LockDevice>() {
            @Override
            public void onSuccess(ArrayList<LockDevice> response) {
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
            button.setOnClickListener(view1 -> {
                String deviceId = lockDevices.get(tag - 1).getId();
                locky.pulseOpen(deviceId);
            });
        } else {
            button.setVisibility(View.GONE);
        }
        return view;
    }
}