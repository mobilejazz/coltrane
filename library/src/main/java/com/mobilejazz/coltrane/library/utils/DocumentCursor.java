package com.mobilejazz.coltrane.library.utils;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.text.TextUtils;

import com.mobilejazz.coltrane.library.compatibility.DocumentsContract.Document;

public class DocumentCursor extends CursorWrapper {

    private int mUri;
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
        mUri = c.getColumnIndex(Document.COLUMN_DOCUMENT_URI);
        mId = c.getColumnIndex(Document.COLUMN_DOCUMENT_ID);
        mMimeType = c.getColumnIndex(Document.COLUMN_MIME_TYPE);
        mName = c.getColumnIndex(Document.COLUMN_DISPLAY_NAME);
        mSummary = c.getColumnIndex(Document.COLUMN_SUMMARY);
        mLastModified = c.getColumnIndex(Document.COLUMN_LAST_MODIFIED);
        mIcon = c.getColumnIndex(Document.COLUMN_ICON);
        mFlags = c.getColumnIndex(Document.COLUMN_FLAGS);
        mSize = c.getColumnIndex(Document.COLUMN_SIZE);
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

    public Uri getUri() {
        String s = getString(mUri);
        if (!TextUtils.isEmpty(s)) {
            return Uri.parse(s);
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
        return getMimeType().equals(Document.MIME_TYPE_DIR);
    }

}
