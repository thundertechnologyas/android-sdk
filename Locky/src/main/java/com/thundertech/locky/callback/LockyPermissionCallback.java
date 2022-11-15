package com.thundertech.locky.callback;

public interface LockyPermissionCallback {
    enum PermissionType {
        NeedLocation,           // Phone need location permission
        NeedOpenBlueTooth,      // Phone need open bluetooth
        PhoneNotSupport,        // phone not support bluetooth
    }
    void requestPermission(PermissionType permission);
}
