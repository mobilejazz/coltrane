package com.mobilejazz.coltrane.library.action;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

public class IntentPendingAction implements PendingAction {

    private Intent mIntent;

    public IntentPendingAction(Intent intent) {
        mIntent = intent;
    }

    @Override
    public void performWith(Activity activity, int callbackRequestCode) {
        activity.startActivityForResult(mIntent, callbackRequestCode);
    }

    @Override
    public void performWith(Fragment fragment, int callbackRequestCode) {
        fragment.startActivityForResult(mIntent, callbackRequestCode);
    }
}
