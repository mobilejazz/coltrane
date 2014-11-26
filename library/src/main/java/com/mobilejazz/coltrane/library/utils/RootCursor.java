package com.mobilejazz.coltrane.library.utils;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;

public class RootCursor extends CursorWrapper {

    private int mDocumentId;
    private int mTitle;

    public RootCursor(Cursor c) {
        super(c);
        mDocumentId = c.getColumnIndex(DocumentsContract.Root.COLUMN_DOCUMENT_ID);
        mTitle = c.getColumnIndex(DocumentsContract.Root.COLUMN_TITLE);
    }

    public String getDocumentId() {
        return getWrappedCursor().getString(mDocumentId);
    }

    public String getTitle() {
        return getWrappedCursor().getString(mTitle);
    }

    // TODO: complete

}
