package com.mobilejazz.coltrane.provider.dropbox;

import android.app.Activity;
import android.app.Fragment;

import com.dropbox.sync.android.DbxAccountManager;
import com.mobilejazz.coltrane.library.action.PendingAction;

public class LinkAction implements PendingAction {

    private DbxAccountManager mDbxAcctMgr;

    public LinkAction(DbxAccountManager dbxAcctMgr) {
        mDbxAcctMgr = dbxAcctMgr;
    }

    @Override
    public void performWith(Activity activity, int callbackRequestCode) {
        mDbxAcctMgr.startLink(activity, callbackRequestCode);
    }

    @Override
    public void performWith(Fragment fragment, int callbackRequestCode) {
        mDbxAcctMgr.startLink(fragment, callbackRequestCode);
    }
}
