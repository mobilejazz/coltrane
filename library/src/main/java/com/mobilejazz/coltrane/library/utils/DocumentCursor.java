package com.mobilejazz.coltrane.library.utils;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.mobilejazz.coltrane.library.compatibility.DocumentsContract;

public class DocumentCursor extends CursorWrapper {

    private int mId;
    private int mMimeType;
    private int mName;
    private int mSummary;
    private int mLastModified;
    private int mIcon;
    private int mFlags;
    private int mSize;

    public DocumentCursor(Cursor c) {
        super(c);
        mId = c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
        mMimeType = c.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
        mName = c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
        mSummary = c.getColumnIndex(DocumentsContract.Document.COLUMN_SUMMARY);
        mLastModified = c.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
        mIcon = c.getColumnIndex(DocumentsContract.Document.COLUMN_ICON);
        mFlags = c.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS);
        mSize = c.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE);
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

    public String getSummary() {
        return getWrappedCursor().getString(mSummary);
    }

    public long getLastModified() {
        return getWrappedCursor().getLong(mLastModified);
    }

    public int getIcon() {
        return getWrappedCursor().getInt(mLastModified);
    }

    public int getFlags() {
        return getWrappedCursor().getInt(mLastModified);
    }

    public long getSize() {
        return getWrappedCursor().getLong(mSize);
    }

    public boolean isDirectory() {
        return getMimeType().equals(DocumentsContract.Document.MIME_TYPE_DIR);
    }

}
