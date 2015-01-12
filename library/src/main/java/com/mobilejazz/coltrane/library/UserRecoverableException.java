package com.mobilejazz.coltrane.library;

import android.content.Intent;

import java.io.IOException;

public class UserRecoverableException extends IOException {

    private Intent pendingAction;

    public UserRecoverableException(String message, Throwable cause, Intent pendingAction) {
        super(message, cause);
        this.pendingAction = pendingAction;
    }

    public Intent getPendingAction() {
        return pendingAction;
    }

}
