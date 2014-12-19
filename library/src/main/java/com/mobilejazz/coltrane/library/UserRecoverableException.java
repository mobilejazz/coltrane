package com.mobilejazz.coltrane.library;

import android.content.Intent;

public class UserRecoverableException extends Exception {

    private Intent pendingAction;

    public UserRecoverableException(String message, Throwable cause, Intent pendingAction) {
        super(message, cause);
        this.pendingAction = pendingAction;
    }

    public Intent getPendingAction() {
        return pendingAction;
    }

}
