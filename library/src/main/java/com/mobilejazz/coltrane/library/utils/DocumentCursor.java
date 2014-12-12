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

    private String getStringField(int index) {
        if (index >= 0) {
            return getWrappedCursor().getString(index);
        } else {
            return null;
        }
    }

    private Long getLongField(int index) {
        if (index >= 0) {
            return getWrappedCursor().getLong(index);
        } else {
            return null;
        }
    }

    private Integer getIntField(int index) {
        if (index >= 0) {
            return getWrappedCursor().getInt(index);
        } else {
            return null;
        }
    }

    public String getId() {
        return getStringField(mId);
    }

    public String getMimeType() {
        return getStringField(mMimeType);
    }

    public String getName() {
        return getStringField(mName);
    }

    public String getSummary() {
        return getStringField(mSummary);
    }

    public Long getLastModified() {
        return getLongField(mLastModified);
    }

    public Integer getIcon() {
        return getIntField(mLastModified);
    }

    public Integer getFlags() {
        return getIntField(mLastModified);
    }

    public Long getSize() {
        return getLongField(mSize);
    }

    public boolean isDirectory() {
        return getMimeType().equals(DocumentsContract.Document.MIME_TYPE_DIR);
    }

}
