package com.linhua.locky.callback;

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

public interface LockyEventCallback<T> {
    void postEvent(EventType event);
}
