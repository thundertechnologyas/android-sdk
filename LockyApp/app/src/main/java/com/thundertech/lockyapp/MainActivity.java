package com.thundertech.lockyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.thundertech.locky.Locky;
import com.thundertech.locky.bean.LockDevice;
import com.thundertech.locky.callback.LockyDataCallback;
import com.thundertech.locky.callback.LockyEventCallback;
import com.thundertech.locky.callback.LockyListCallback;

import java.util.ArrayList;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText codeEditText;
    private LinearLayout locksLinearLayout;
    private Locky locky;
    private ArrayList<LockDevice> lockDevices;

    private static final int REQUEST_ENABLE_BLUETOOTH = 100;

    public static final int REQUEST_PERMISSION_CODE = 9527;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        locky = new Locky(this, permission -> {
            switch (permission) {
                case NeedLocation:
                    requestLocationPermission();
                    break;
                case NeedOpenBlueTooth:
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH);
                    break;
                case PhoneNotSupport:
                    showMsg("Your phone does not support bluetooth");

            }
        });
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    showMsg("Bluetooth is open");
                } else {
                    showMsg("Please open bluetooth");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locky.stop();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_CODE)
    private void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "App需要定位权限", REQUEST_PERMISSION_CODE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 将结果转发给 EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
            showMsg("Please input valid email");
            return;
        }

        locky.startVerify(email, new LockyDataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onFailure() {
                showMsg("Fail to start verification");
            }
        });
    }

    private void verify() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            showMsg("Please input valid email");
            return;
        }
        String code = codeEditText.getText().toString().trim();
        if (code.isEmpty()) {
            showMsg("Please input code");
            return;
        }
        locky.verify(code, new LockyDataCallback<String>() {
            @Override
            public void onSuccess(String token) {

            }

            @Override
            public void onFailure() {
                showMsg("Fail to verify");
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
                showMsg("Fail to get locks");
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