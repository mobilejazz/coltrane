package com.mobilejazz.coltrane.library.action;

import android.app.Activity;
import android.app.Fragment;

public interface PendingAction {

    public void performWith(Activity activity, int callbackRequestCode);

    public void performWith(Fragment fragment, int callbackRequestCode);

}
