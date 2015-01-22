package com.mobilejazz.coltrane.library;

import com.mobilejazz.coltrane.library.action.PendingAction;
import java.io.IOException;

public class UserRecoverableException extends IOException {

    private PendingAction pendingAction;

    public UserRecoverableException(String message, Throwable cause, PendingAction pendingAction) {
        super(message, cause);
        this.pendingAction = pendingAction;
    }

    public PendingAction getPendingAction() {
        return pendingAction;
    }

}
