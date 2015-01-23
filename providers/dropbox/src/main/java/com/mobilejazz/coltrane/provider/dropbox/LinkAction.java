package com.mobilejazz.coltrane.provider.dropbox;

import android.app.Activity;
import android.app.Fragment;

import com.dropbox.sync.android.DbxAccountManager;
import com.mobilejazz.coltrane.library.action.PendingAction;

public class LinkAction implements PendingAction {

    private DropboxProvider mProvider;

    public LinkAction(DropboxProvider provider) {
        mProvider = provider;
    }

    @Override
    public void performWith(Activity activity, int callbackRequestCode) {
        mProvider.getAccountManager().startLink(activity, callbackRequestCode);
        mProvider.resetRoots();
    }

    @Override
    public void performWith(Fragment fragment, int callbackRequestCode) {
        mProvider.getAccountManager().startLink(fragment, callbackRequestCode);
        mProvider.resetRoots();
    }
}
