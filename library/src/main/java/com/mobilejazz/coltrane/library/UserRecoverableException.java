package com.mobilejazz.coltrane.library;

import android.app.PendingIntent;

public class UserRecoverableException extends Exception {

    private PendingIntent pendingAction;

    public UserRecoverableException(String message, Throwable cause, PendingIntent pendingAction) {
        super(message, cause);
    }

    public PendingIntent getPendingAction() {
        return pendingAction;
    }

}
