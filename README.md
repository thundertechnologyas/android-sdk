# Locky Android SDK
This SDK contains a demo SDK written in java. It also contains communication over the bluetooth and restful services.

This SDK has been built to demostrate how to build other app's on top of this demostration code. If you need a sdk for ios, have a look at our sdk's named: ios-sdk.

More information about locky:
https://www.locky.tech


### How to
+ Step 1. Add the JitPack repository to your build file

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
+ Step 2. Add the dependency, now the latest version of sdk is 0.9.1

``` bash
dependencies {
	  implementation 'com.github.thundertechnologyas:android-sdk:0.9.1'
	}
```

### Init Locky
The sdk provides two initialize constructors. permissionCallback is the callback to get bluetooth or location permissions. If we want to know the progress of bluetooth, we need use eventCallback.

```
public Locky(Context context, LockyPermissionCallback permissionCallback)
public Locky(Context context, LockyPermissionCallback permissionCallback, LockyEventCallback eventCallback)
```

```
private Locky locky;
locky = new Locky(this, permission -> {
	//'this' is the context of Activity, Fragment, or Application Context.
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
```


### You log on to the sdk using a two token based authentication process, first ask for a verification code sent by email


```
// Ask the locky backend for an authentication code.
locky.startVerify(email, new LockyDataCallback<Boolean>() {
    @Override
    public void onSuccess(Boolean result) {
        if (result) {
            //success
        } else {
            //fail
        }
    }

    @Override
    public void onFailure() {
        showMsg("Fail to start verification");
    }
});
```
### Then use the authentication code to login
```
let sdk = Locky()
locky.verify(code: codeFromEmail) { result in
   // result is Bool to show sucess or failure.
}
```

### Recieve the list of locks
Now you have access and get ask for all locks this user has access to.

The devices object contain all the nessesary data to run operations on the lock, example pulse open.

If the ble status is changed, the callback would trigger again at once.

```
locky.verify(code, new LockyDataCallback<String>() {
    @Override
    public void onSuccess(String token) {
    	//success
       // result is Bool to show sucess or failure.
       // locks is one list contains all locks which contain ble status
    }

    @Override
    public void onFailure() {
    	showMsg("Fail to verify");
    }
});
```

### Run pulse open
If the ble status of lock is true, then we can run perations by the device id.

```
locky.pulseOpen(deviceId);
```

### Receive event
The sdk also gives feedback to the end user about things happening. If we wants to know the current status, then we need init LockyEventCallback in Locky constructor

```
public interface LockyEventCallback {
    enum EventType {
        DiscoveredDevice,       // it has discovered the device
        ConnectingDevice,       // it is connecting the device
        DidConnectDevice,       // it has connected the device
        DisConnectDevice,       // it disconnects the device
        WritingDevice,          // it is writing to the device
        DidWriteDevice,         // it has written to the device
        FailureWriteDevice,     // it fails to write to the device
        DownloadPackage,        // it is downloading package for the device
        DeliveringMessage,      // messge is delivering
        MessageDelivered,       // the message is delivered
    }
    void postEvent(String deviceId, EventType event);
}
```


