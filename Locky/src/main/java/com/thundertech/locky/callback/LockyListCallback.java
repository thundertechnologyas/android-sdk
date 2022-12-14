package com.thundertech.locky.callback;

import java.util.ArrayList;

public interface LockyListCallback<T> {

    void onSuccess(ArrayList<T> response);

    void onFailure();
}
