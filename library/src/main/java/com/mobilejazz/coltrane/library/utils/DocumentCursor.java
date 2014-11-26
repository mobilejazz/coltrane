package com.mobilejazz.coltrane.library.utils;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;

public class DocumentCursor extends CursorWrapper {

    private int mId;
    private int mMimeType;
    private int mName;

    public DocumentCursor(Cursor c) {
        super(c);
        mId = c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
        mMimeType = c.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
        mName = c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
    }

    public String getId() {
        return getWrappedCursor().getString(mId);
    }

    public String getMimeType() {
        return getWrappedCursor().getString(mMimeType);
    }

    public String getName() {
        return getWrappedCursor().getString(mName);
    }

    public boolean isDirectory() {
        return getMimeType().equals(DocumentsContract.Document.MIME_TYPE_DIR);
    }

}
